package com.mbe.addons.ui.runtime;

import com.mbe.addons.ui.api.MenuView;
import com.mbe.addons.ui.api.UIMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class MenuSession {
    private final UIMenu menu;
    private final UUID playerId;
    private final Map<String, Object> sessionData = new HashMap<>();
    private Inventory inventory;
    private String title;
    private MenuView currentView;

    MenuSession(UIMenu menu, Player player) {
        this.menu = menu;
        this.playerId = player.getUniqueId();
    }

    UIMenu menu() {
        return menu;
    }

    UUID playerId() {
        return playerId;
    }

    Map<String, Object> sessionData() {
        return sessionData;
    }

    Inventory inventory() {
        return inventory;
    }

    void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    String title() {
        return title;
    }

    void title(String title) {
        this.title = title;
    }

    MenuView currentView() {
        return currentView;
    }

    void currentView(MenuView view) {
        this.currentView = view;
    }
}
