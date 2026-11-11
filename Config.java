package com.paytracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Settings stored in .minecraft/config/paytracker.json */
public class Config {
    // Group 1 = player name, group 2 = number, group 3 = optional K/M/B/T suffix
    private static final String AMT = "\\$?([\\d,]+(?:\\.\\d+)?)\\s*([kKmMbBtT])?(?!\\w).*";
    public static final String DEFAULT_RECEIVED = "^(?:\\[[^\\]]*\\]\\s*)?([\\w.]{1,17}) (?:has )?(?:paid|sent|gave) you " + AMT;
    public static final String DEFAULT_SENT = "^You (?:have )?(?:paid|sent|gave) ([\\w.]{1,17}) " + AMT;

    public String receivedPattern = DEFAULT_RECEIVED;
    public String sentPattern = DEFAULT_SENT;
    /** Add your current balance here once so net worth starts from the right number. */
    public double startingBalance = 0;
    public boolean hudVisible = true;
    /** When true, payment-looking messages that did not match are shown in chat so you can fix the patterns. */
    public boolean debug = false;

    public transient Pattern received;
    public transient Pattern sent;

    public static Config INSTANCE = new Config();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("paytracker.json");
    }

    public static void load() {
        try {
            if (Files.exists(file())) {
                Config c = GSON.fromJson(Files.readString(file()), Config.class);
                if (c != null) INSTANCE = c;
            }
        } catch (Exception e) {
            System.err.println("[PayTracker] Could not read config: " + e);
        }
        INSTANCE.compile();
        save();
    }

    public void compile() {
        try { received = Pattern.compile(receivedPattern); }
        catch (PatternSyntaxException e) { received = Pattern.compile(DEFAULT_RECEIVED); }
        try { sent = Pattern.compile(sentPattern); }
        catch (PatternSyntaxException e) { sent = Pattern.compile(DEFAULT_SENT); }
    }

    public static void save() {
        try { Files.writeString(file(), GSON.toJson(INSTANCE)); }
        catch (Exception e) { System.err.println("[PayTracker] Could not save config: " + e); }
    }
}
