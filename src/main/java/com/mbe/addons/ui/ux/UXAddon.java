package com.mbe.addons.ui.ux;

import com.darkbladedev.engine.api.addon.AddonContext;
import com.mbe.addons.ui.ux.engine.MenuEngine;

import java.util.Objects;
import java.util.Optional;

public final class UXAddon {
    private static volatile UXAddon instance;

    private final AddonContext context;
    private final MenuEngine menuEngine;

    public UXAddon(AddonContext context) {
        this.context = Objects.requireNonNull(context, "context");
        this.menuEngine = new MenuEngine(context);
    }

    public static Optional<UXAddon> instance() {
        return Optional.ofNullable(instance);
    }

    public static UXAddon require() {
        UXAddon current = instance;
        if (current == null) {
            throw new IllegalStateException("UXAddon not initialized");
        }
        return current;
    }

    public static void setInstance(UXAddon addon) {
        instance = addon;
    }

    public static void clearInstance() {
        instance = null;
    }

    public AddonContext context() {
        return context;
    }

    public MenuEngine menuEngine() {
        return menuEngine;
    }
}
