package com.paybackplus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Tracks money received (people paying you) and money paid out (you paying back). */
public class Stats {
    public static class Entry {
        public boolean in;     // true = received, false = paid out
        public String name;
        public long amount;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Config.dir().resolve("stats.json");

    public static Stats I = new Stats();

    public long totalIn, totalOut;
    public int countIn, countOut;
    public transient long sessionIn, sessionOut;
    public List<Entry> history = new ArrayList<>();

    public static void load() {
        try {
            if (Files.exists(FILE)) I = GSON.fromJson(Files.readString(FILE), Stats.class);
            if (I == null) I = new Stats();
            if (I.history == null) I.history = new ArrayList<>();
        } catch (Exception e) { I = new Stats(); }
    }

    public static void save() {
        try {
            Files.createDirectories(Config.dir());
            Files.writeString(FILE, GSON.toJson(I));
        } catch (Exception ignored) {}
    }

    public static void in(String name, long amount) {
        I.totalIn += amount; I.sessionIn += amount; I.countIn++;
        add(true, name, amount);
    }

    public static void out(String name, long amount) {
        I.totalOut += amount; I.sessionOut += amount; I.countOut++;
        add(false, name, amount);
    }

    private static void add(boolean in, String name, long amount) {
        Entry e = new Entry();
        e.in = in; e.name = name; e.amount = amount;
        I.history.add(e);
        while (I.history.size() > 100) I.history.remove(0);
        save();
    }

    public static void reset() {
        I.totalIn = 0; I.totalOut = 0; I.countIn = 0; I.countOut = 0;
        I.sessionIn = 0; I.sessionOut = 0; I.history.clear();
        save();
    }

    public static long net() { return I.totalIn - I.totalOut; }
    public static long sessionNet() { return I.sessionIn - I.sessionOut; }

    /** 1500 -> $1.5K */
    public static String money(long v) {
        String sign = v < 0 ? "-" : "";
        double a = Math.abs((double) v);
        String[] suf = {"", "K", "M", "B", "T"};
        int i = 0;
        while (a >= 1000 && i < 4) { a /= 1000; i++; }
        String num = (a == Math.floor(a)) ? String.valueOf((long) a)
                : String.format(Locale.ROOT, "%.2f", a).replaceAll("0+$", "").replaceAll("\\.$", "");
        return sign + "$" + num + suf[i];
    }

    public static String signed(long v) {
        return (v >= 0 ? "+" : "-") + money(Math.abs(v));
    }

    /** 2.0 -> 2, 1.5 -> 1.5 */
    public static String mult(double m) {
        return m == Math.floor(m) ? String.valueOf((long) m) : String.valueOf(m);
    }
}
