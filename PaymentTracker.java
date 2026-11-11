package com.paytracker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class PaymentTracker {
    private static final Gson GSON = new Gson();
    private static final int MAX_HISTORY = 1000;

    public static final List<Transaction> history = new ArrayList<>();
    public static double totalReceived = 0;
    public static double totalSent = 0;

    // Timer state
    public static boolean timerRunning = false;
    public static long timerStart = 0;
    public static long timerEnd = 0;
    public static Transaction topReceived = null;
    public static Transaction topSent = null;
    public static boolean hasResult = false;

    public static double netWorth() {
        return Config.INSTANCE.startingBalance + totalReceived - totalSent;
    }

    // ---------- chat parsing ----------

    public static void onMessage(Text message) {
        String s = message.getString().trim();

        Matcher m = Config.INSTANCE.received.matcher(s);
        if (m.matches()) { record(true, m.group(1), parse(m)); return; }

        m = Config.INSTANCE.sent.matcher(s);
        if (m.matches()) { record(false, m.group(1), parse(m)); return; }

        if (Config.INSTANCE.debug && s.toLowerCase().contains("paid")) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("[PayTracker] Could not read: " + s).formatted(Formatting.GRAY), false);
            }
        }
    }

    private static double parse(Matcher m) {
        try {
            double v = Double.parseDouble(m.group(2).replace(",", ""));
            String suf = m.groupCount() >= 3 ? m.group(3) : null;
            if (suf != null && !suf.isEmpty()) {
                switch (Character.toLowerCase(suf.charAt(0))) {
                    case 'k' -> v *= 1_000d;
                    case 'm' -> v *= 1_000_000d;
                    case 'b' -> v *= 1_000_000_000d;
                    case 't' -> v *= 1_000_000_000_000d;
                }
            }
            return v;
        } catch (Exception e) {
            return -1;
        }
    }

    private static void record(boolean received, String player, double amount) {
        if (amount < 0) return;
        Transaction t = new Transaction(System.currentTimeMillis(), received, player, amount);
        history.add(t);
        if (history.size() > MAX_HISTORY) history.remove(0);

        if (received) totalReceived += amount; else totalSent += amount;

        if (timerRunning) {
            if (received && (topReceived == null || amount > topReceived.amount)) topReceived = t;
            if (!received && (topSent == null || amount > topSent.amount)) topSent = t;
        }
        saveData();
    }

    // ---------- timer ----------

    public static void startTimer() {
        timerRunning = true;
        hasResult = false;
        timerStart = System.currentTimeMillis();
        topReceived = null;
        topSent = null;
    }

    public static void stopTimer() {
        if (!timerRunning) return;
        timerRunning = false;
        timerEnd = System.currentTimeMillis();
        hasResult = true;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("[PayTracker] Timer stopped. " + resultLine()).formatted(Formatting.GOLD), false);
        }
    }

    public static long elapsedMillis() {
        if (timerRunning) return System.currentTimeMillis() - timerStart;
        return hasResult ? timerEnd - timerStart : 0;
    }

    public static String resultLine() {
        if (topReceived == null) return "No payments received during the timer.";
        return "Highest payment: " + topReceived.player + " paid you " + Format.money(topReceived.amount);
    }

    // ---------- persistence ----------

    private static Path dataFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("paytracker_data.json");
    }

    public static void loadData() {
        try {
            if (!Files.exists(dataFile())) return;
            List<Transaction> list = GSON.fromJson(Files.readString(dataFile()), new TypeToken<List<Transaction>>() {}.getType());
            if (list == null) return;
            history.clear();
            history.addAll(list);
            totalReceived = 0; totalSent = 0;
            for (Transaction t : history) {
                if (t.received) totalReceived += t.amount; else totalSent += t.amount;
            }
        } catch (Exception e) {
            System.err.println("[PayTracker] Could not load data: " + e);
        }
    }

    public static void saveData() {
        try { Files.writeString(dataFile(), GSON.toJson(history)); }
        catch (Exception e) { System.err.println("[PayTracker] Could not save data: " + e); }
    }

    public static void resetData() {
        history.clear();
        totalReceived = 0;
        totalSent = 0;
        saveData();
    }
}
