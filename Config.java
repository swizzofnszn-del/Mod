package com.paybackplus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.*;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("paybackplus");
    private static final Path FILE = DIR.resolve("config.json");

    public static Config I = new Config();

    public boolean enabled = false;          // auto pay-back on/off
    public double multiplier = 1.0;          // payout = received * multiplier
    public int winChance = 100;              // % chance to pay back (100 = always)
    public long maxPayout = 1_000_000_000L;  // safety cap per payout
    public int delayTicks = 20;              // delay before sending /pay
    // DonutSMP-style message: "Name paid you $1.5K."  (edit if the format differs)
    public String regex = "^(\\w{3,16}) paid you \\$([\\d.,]+[KMBTkmbt]?)\\.?$";

    // Menu look (RGB hex without alpha)
    public int panelColor = 0x1E1E2E;
    public int textColor = 0xFFFFFF;
    public int accentColor = 0x7C5CFF;
    public int panelOpacity = 200;           // 0-255
    public String backgroundImage = "";      // file name inside config/paybackplus/

    public static Path dir() { return DIR; }

    public static void load() {
        try {
            Files.createDirectories(DIR);
            if (Files.exists(FILE)) I = GSON.fromJson(Files.readString(FILE), Config.class);
            if (I == null) I = new Config();
        } catch (Exception e) { I = new Config(); }
    }

    public static void save() {
        try {
            Files.createDirectories(DIR);
            Files.writeString(FILE, GSON.toJson(I));
        } catch (Exception ignored) {}
    }
}
