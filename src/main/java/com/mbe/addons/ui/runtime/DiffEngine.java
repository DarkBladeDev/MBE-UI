package com.mbe.addons.ui.runtime;

import com.mbe.addons.ui.api.MenuItem;
import com.mbe.addons.ui.api.MenuView;
import com.mbe.addons.ui.api.PlayerContext;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DiffEngine {
    private final Logger logger;

    DiffEngine(Logger logger) {
        this.logger = logger;
    }

    void apply(MenuSession session, PlayerContext playerContext, MenuView newView) {
        Inventory inventory = session.inventory();
        MenuView oldView = session.currentView();

        if (inventory == null) {
            throw new IllegalStateException("Inventory not initialized for session");
        }

        int size = inventory.getSize();
        Map<Integer, MenuItem> newItems = newView.items();
        Map<Integer, MenuItem> oldItems = oldView != null ? oldView.items() : Map.of();

        for (int slot = 0; slot < size; slot++) {
            MenuItem newItem = newItems.get(slot);
            MenuItem oldItem = oldItems.get(slot);

            boolean sameRef = newItem != null && newItem == oldItem;
            if (sameRef) {
                continue;
            }

            ItemStack rendered;
            try {
                if (newItem == null) {
                    rendered = null;
                } else {
                    rendered = newItem.render(playerContext);
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Error rendering slot " + slot + " for menu " + session.menu().id(), t);
                rendered = ErrorItemFactory.errorItem();
            }

            ItemStack currentStack = inventory.getItem(slot);
            if (!equalsItem(currentStack, rendered)) {
                inventory.setItem(slot, rendered);
            }
        }
        session.currentView(newView);
    }

    private boolean equalsItem(ItemStack a, ItemStack b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.isSimilar(b) && a.getAmount() == b.getAmount();
    }
}
