package com.paybackplus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigScreen extends Screen {
    private static final Identifier BG_ID = Identifier.of("paybackplus", "custom_bg");
    private static NativeImageBackedTexture bgTex;
    private static int bgW, bgH;
    private static String loadedName = null;

    private final Screen parent;
    private TextFieldWidget multField, panelField, textField, accentField, imageField;
    private ButtonWidget toggleBtn;

    public ConfigScreen(Screen parent) {
        super(Text.literal("PayBack Plus"));
        this.parent = parent;
    }

    private static String hex(int c) { return String.format("%06X", c & 0xFFFFFF); }

    private static int parseHex(String s, int fallback) {
        try { return Integer.parseInt(s.replace("#", "").trim(), 16) & 0xFFFFFF; }
        catch (Exception e) { return fallback; }
    }

    @Override
    protected void init() {
        Config c = Config.I;
        int cx = width / 2, y = height / 2 - 100, w = 200;

        toggleBtn = addDrawableChild(ButtonWidget.builder(Text.literal(label()), b -> {
            c.enabled = !c.enabled; b.setMessage(Text.literal(label()));
        }).dimensions(cx - w / 2, y, w, 20).build());

        multField = field(cx - w / 2, y += 28, w, "Chat multiplier (x)", String.valueOf(c.multiplier));

        addDrawableChild(new SliderWidget(cx - w / 2, y += 28, w, 20,
                Text.literal("Pay-back chance: " + c.winChance + "%"), c.winChance / 100.0) {
            @Override protected void updateMessage() {
                setMessage(Text.literal("Pay-back chance: " + (int) Math.round(value * 100) + "%"));
            }
            @Override protected void applyValue() { Config.I.winChance = (int) Math.round(value * 100); }
        });

        panelField = field(cx - w / 2, y += 28, w, "Panel colour (hex)", hex(c.panelColor));
        textField = field(cx - w / 2, y += 28, w, "Text colour (hex)", hex(c.textColor));
        accentField = field(cx - w / 2, y += 28, w, "Accent colour (hex)", hex(c.accentColor));
        imageField = field(cx - w / 2, y += 28, w, "Background image (file in config/paybackplus)", c.backgroundImage);

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), b -> { apply(); close(); })
                .dimensions(cx - w / 2, y += 32, w, 20).build());
    }

    private TextFieldWidget field(int x, int y, int w, String hint, String val) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, w, 20, Text.literal(hint));
        f.setMaxLength(128);
        f.setText(val);
        f.setPlaceholder(Text.literal(hint));
        return addDrawableChild(f);
    }

    private String label() { return "Auto pay-back: " + (Config.I.enabled ? "ON" : "OFF"); }

    private void apply() {
        Config c = Config.I;
        try { c.multiplier = Math.max(0, Double.parseDouble(multField.getText())); } catch (Exception ignored) {}
        c.panelColor = parseHex(panelField.getText(), c.panelColor);
        c.textColor = parseHex(textField.getText(), c.textColor);
        c.accentColor = parseHex(accentField.getText(), c.accentColor);
        c.backgroundImage = imageField.getText().trim();
        Config.save();
        loadedName = null; // force reload
    }

    private void ensureBackground() {
        String name = Config.I.backgroundImage;
        if (name == null || name.isEmpty()) { bgTex = null; loadedName = ""; return; }
        if (name.equals(loadedName)) return;
        loadedName = name;
        try {
            Path p = Config.dir().resolve(name).normalize();
            if (!p.startsWith(Config.dir()) || !Files.exists(p)) { bgTex = null; return; }
            try (InputStream in = Files.newInputStream(p)) {
                NativeImage img = NativeImage.read(in);
                bgW = img.getWidth(); bgH = img.getHeight();
                bgTex = new NativeImageBackedTexture(() -> "paybackplus_bg", img);
                MinecraftClient.getInstance().getTextureManager().registerTexture(BG_ID, bgTex);
            }
        } catch (Exception e) { bgTex = null; }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ensureBackground();
        Config c = Config.I;
        if (bgTex != null) {
            ctx.drawTexture(RenderLayer::getGuiTextured, BG_ID, 0, 0, 0f, 0f, width, height, bgW, bgH, bgW, bgH);
        } else {
            ctx.fill(0, 0, width, height, 0xFF000000 | c.panelColor);
        }
        int pw = 240, ph = 270, px = width / 2 - pw / 2, py = height / 2 - 112;
        int bg = (c.panelOpacity << 24) | c.panelColor;
        ctx.fill(px, py, px + pw, py + ph, bg);
        ctx.fill(px, py, px + pw, py + 2, 0xFF000000 | c.accentColor);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, py - 14, 0xFF000000 | c.textColor);
        super.render(ctx, mx, my, delta);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) { /* custom background drawn in render() */ }

    @Override
    public void close() { MinecraftClient.getInstance().setScreen(parent); }
}
