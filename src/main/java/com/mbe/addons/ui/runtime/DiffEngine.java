package com.mbe.addons.ui.runtime;

import com.mbe.ui.api.menu.MenuItem;
import com.mbe.ui.api.menu.MenuView;
import com.mbe.ui.api.menu.PlayerContext;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.darkbladedev.engine.api.logging.EngineLogger;

import java.util.Map;

final class DiffEngine {
    private final EngineLogger logger;

    DiffEngine(EngineLogger logger) {
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

        MenuItem newFiller = newView.filler().orElse(null);
        MenuItem oldFiller = oldView != null ? oldView.filler().orElse(null) : null;

        for (int slot = 0; slot < size; slot++) {
            MenuItem newItem = newItems.get(slot);
            if (newItem == null) {
                newItem = newFiller;
            }

            MenuItem oldItem = oldItems.get(slot);
            if (oldItem == null) {
                oldItem = oldFiller;
            }

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
                logger.error("Error rendering slot " + slot + " for menu " + session.menu().id(), t);
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
