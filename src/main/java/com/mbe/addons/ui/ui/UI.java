package com.mbe.addons.ui.ui;

import com.mbe.ui.api.menu.MenuController;

public final class UI {
    private static volatile MenuController controller;

    private UI() {
    }

    public static MenuController controller() {
        MenuController current = controller;
        if (current == null) {
            throw new IllegalStateException("MBE-UI controller not initialized");
        }
        return current;
    }

    public static boolean isAvailable() {
        return controller != null;
    }

    public static void register(MenuController menuController) {
        controller = menuController;
    }

    public static void unregister() {
        controller = null;
    }
}
