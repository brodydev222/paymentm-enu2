package com.paytracker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TrackerScreen extends Screen {
    private ButtonWidget timerButton;
    private ButtonWidget hudButton;

    public TrackerScreen() {
        super(Text.literal("Pay Tracker"));
    }

    private Text timerLabel() {
        return Text.literal(PaymentTracker.timerRunning ? "Stop Timer" : "Start Timer");
    }

    private Text hudLabel() {
        return Text.literal("HUD: " + (Config.INSTANCE.hudVisible ? "ON" : "OFF"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        timerButton = addDrawableChild(ButtonWidget.builder(timerLabel(), b -> {
            if (PaymentTracker.timerRunning) PaymentTracker.stopTimer(); else PaymentTracker.startTimer();
            b.setMessage(timerLabel());
        }).dimensions(cx - 100, 50, 200, 20).build());

        hudButton = addDrawableChild(ButtonWidget.builder(hudLabel(), b -> {
            Config.INSTANCE.hudVisible = !Config.INSTANCE.hudVisible;
            Config.save();
            b.setMessage(hudLabel());
        }).dimensions(cx - 100, 75, 98, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset History"), b -> PaymentTracker.resetData())
                .dimensions(cx + 2, 75, 98, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Debug: " + (Config.INSTANCE.debug ? "ON" : "OFF")), b -> {
            Config.INSTANCE.debug = !Config.INSTANCE.debug;
            Config.save();
            b.setMessage(Text.literal("Debug: " + (Config.INSTANCE.debug ? "ON" : "OFF")));
        }).dimensions(cx - 100, 100, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(cx - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta); // draws background + buttons

        int cx = this.width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer, "Pay Tracker", cx, 20, 0xFFFFFFFF);

        String status = PaymentTracker.timerRunning ? "Running: " : "Time: ";
        ctx.drawCenteredTextWithShadow(textRenderer, status + Format.time(PaymentTracker.elapsedMillis()), cx, 36, 0xFF55FFFF);

        int y = 132;
        if (PaymentTracker.timerRunning) {
            ctx.drawCenteredTextWithShadow(textRenderer, "Timer running... stop it to see the biggest payment.", cx, y, 0xFFAAAAAA);
        } else if (PaymentTracker.hasResult) {
            Transaction r = PaymentTracker.topReceived;
            if (r == null) {
                ctx.drawCenteredTextWithShadow(textRenderer, "No payments received during that timer.", cx, y, 0xFFFFAA00);
            } else {
                ctx.drawCenteredTextWithShadow(textRenderer, "Highest payment received:", cx, y, 0xFFFFFFFF);
                ctx.drawCenteredTextWithShadow(textRenderer, r.player + "  -  " + Format.money(r.amount), cx, y + 12, 0xFF55FF55);
            }
            Transaction s = PaymentTracker.topSent;
            if (s != null) {
                ctx.drawCenteredTextWithShadow(textRenderer, "Highest payment sent: " + s.player + " - " + Format.money(s.amount), cx, y + 30, 0xFFFF5555);
            }
        }

        int ty = 185;
        ctx.drawCenteredTextWithShadow(textRenderer, "Total received: +" + Format.money(PaymentTracker.totalReceived), cx, ty, 0xFF55FF55);
        ctx.drawCenteredTextWithShadow(textRenderer, "Total sent: -" + Format.money(PaymentTracker.totalSent), cx, ty + 12, 0xFFFF5555);
        ctx.drawCenteredTextWithShadow(textRenderer, "Net worth: " + Format.money(PaymentTracker.netWorth()), cx, ty + 24, 0xFFFFFF55);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
