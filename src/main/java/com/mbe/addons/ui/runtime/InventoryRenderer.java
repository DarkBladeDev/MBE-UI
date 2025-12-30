package com.mbe.addons.ui.runtime;

import com.mbe.addons.ui.api.MenuView;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.logging.Level;
import java.util.logging.Logger;

final class InventoryRenderer {
    private final DiffEngine diffEngine;
    private final Logger logger;

    InventoryRenderer(DiffEngine diffEngine, Logger logger) {
        this.diffEngine = diffEngine;
        this.logger = logger;
    }

    void open(MenuSession session, Player player) {
        Inventory inventory = session.inventory();
        if (inventory == null) {
            DefaultPlayerContext playerContext = new DefaultPlayerContext(player, session.sessionData());
            String title = session.menu().title(playerContext);
            inventory = Bukkit.createInventory(player, session.menu().size(), title);
            session.inventory(inventory);
            session.title(title);
        }

        render(session, player);
        player.openInventory(inventory);
    }

    void render(MenuSession session, Player player) {
        DefaultPlayerContext playerContext = new DefaultPlayerContext(player, session.sessionData());

        String title = session.menu().title(playerContext);
        if (session.inventory() != null && title != null && !title.equals(session.title())) {
            Inventory inventory = Bukkit.createInventory(player, session.menu().size(), title);
            session.inventory(inventory);
            session.title(title);
            player.openInventory(inventory);
        }

        MenuView newView;
        try {
            newView = session.menu().render(playerContext);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error rendering menu " + session.menu().id(), t);
            return;
        }

        diffEngine.apply(session, playerContext, newView);
    }
}
