package com.mbe.addons.ui.ux.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

public final class PlaceholderApiHook {
    private static volatile Method setPlaceholders;
    private static volatile boolean resolved;

    private PlaceholderApiHook() {
    }

    public static boolean isAvailable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return false;
        }
        return resolveMethod().isPresent();
    }

    public static String apply(Player player, String text) {
        Objects.requireNonNull(player, "player");
        if (text == null || text.isEmpty()) {
            return "";
        }

        Optional<Method> method = resolveMethod();
        if (method.isEmpty()) {
            return text;
        }

        try {
            Object out = method.get().invoke(null, player, text);
            return out instanceof String s ? s : text;
        } catch (Throwable ignored) {
            return text;
        }
    }

    private static Optional<Method> resolveMethod() {
        if (resolved) {
            return Optional.ofNullable(setPlaceholders);
        }
        synchronized (PlaceholderApiHook.class) {
            if (resolved) {
                return Optional.ofNullable(setPlaceholders);
            }
            resolved = true;
            try {
                Class<?> api = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                setPlaceholders = api.getMethod("setPlaceholders", Player.class, String.class);
            } catch (Throwable ignored) {
                setPlaceholders = null;
            }
            return Optional.ofNullable(setPlaceholders);
        }
    }
}

