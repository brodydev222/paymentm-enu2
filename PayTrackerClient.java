package com.paytracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class PayTrackerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();
        PaymentTracker.loadData();

        // Only server/system messages are parsed (not signed player chat), so players can't fake payments.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) PaymentTracker.onMessage(message);
        });

        HudRenderCallback.EVENT.register(HudOverlay::render);

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("paytracker", "main"));
        KeyBinding openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.paytracker.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                client.setScreen(new TrackerScreen());
            }
        });
    }
}
