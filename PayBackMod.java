package com.paybackplus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayBackMod implements ClientModInitializer {
    private static KeyBinding openMenu;
    private static final Random RNG = new Random();

    private record Pending(String name, long amount, long sendAtTick) {}
    private static final ArrayDeque<Pending> QUEUE = new ArrayDeque<>();
    private static long tick = 0;

    @Override
    public void onInitializeClient() {
        Config.load();

        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.paybackplus.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K,
                "category.paybackplus"));

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay || !Config.I.enabled) return;
            try {
                Matcher m = Pattern.compile(Config.I.regex).matcher(message.getString().trim());
                if (!m.matches()) return;
                String name = m.group(1);
                long received = parseAmount(m.group(2));
                if (received <= 0) return;

                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null && name.equalsIgnoreCase(mc.player.getName().getString())) return;

                // Optional gamble: winChance% to pay out
                if (RNG.nextInt(100) >= Config.I.winChance) {
                    info("No payout to " + name + " (lost roll)");
                    return;
                }
                long payout = Math.min((long) (received * Config.I.multiplier), Config.I.maxPayout);
                if (payout <= 0) return;
                QUEUE.add(new Pending(name, payout, tick + Math.max(1, Config.I.delayTicks)));
            } catch (Exception ignored) {}
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            tick++;
            while (openMenu.wasPressed()) mc.setScreen(new ConfigScreen(mc.currentScreen));
            Pending p = QUEUE.peek();
            if (p != null && tick >= p.sendAtTick() && mc.player != null) {
                QUEUE.poll();
                mc.player.networkHandler.sendChatCommand("pay " + p.name() + " " + p.amount());
                info("Paid " + p.name() + " $" + p.amount());
            }
        });
    }

    /** Parses 1500, 1,500, 1.5K, 2M, 1B, 1T */
    public static long parseAmount(String s) {
        s = s.replace(",", "").trim().toUpperCase();
        double mult = 1;
        char last = s.charAt(s.length() - 1);
        switch (last) {
            case 'K' -> mult = 1e3;
            case 'M' -> mult = 1e6;
            case 'B' -> mult = 1e9;
            case 'T' -> mult = 1e12;
            default -> {}
        }
        if (mult != 1) s = s.substring(0, s.length() - 1);
        return (long) (Double.parseDouble(s) * mult);
    }

    static void info(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(Text.literal("§d[PayBack+] §f" + msg), false);
    }
}
