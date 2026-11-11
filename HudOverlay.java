package com.paytracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.List;

public class HudOverlay {
    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Config.INSTANCE.hudVisible || mc.options.hudHidden || mc.player == null) return;

        TextRenderer tr = mc.textRenderer;
        int x = 4, y = 4, line = 10;

        ctx.drawTextWithShadow(tr, "Received: +" + Format.money(PaymentTracker.totalReceived), x, y, 0xFF55FF55);
        y += line;
        ctx.drawTextWithShadow(tr, "Sent: -" + Format.money(PaymentTracker.totalSent), x, y, 0xFFFF5555);
        y += line;
        ctx.drawTextWithShadow(tr, "Net worth: " + Format.money(PaymentTracker.netWorth()), x, y, 0xFFFFFF55);
        y += line;

        if (PaymentTracker.timerRunning) {
            ctx.drawTextWithShadow(tr, "Timer: " + Format.time(PaymentTracker.elapsedMillis()), x, y, 0xFF55FFFF);
            y += line;
        }

        y += 2;
        List<Transaction> h = PaymentTracker.history;
        for (int i = h.size() - 1, shown = 0; i >= 0 && shown < 5; i--, shown++) {
            Transaction t = h.get(i);
            String s = t.received
                    ? "+" + Format.money(t.amount) + " from " + t.player
                    : "-" + Format.money(t.amount) + " to " + t.player;
            ctx.drawTextWithShadow(tr, s, x, y, t.received ? 0xFF55FF55 : 0xFFFF5555);
            y += line;
        }
    }
}
