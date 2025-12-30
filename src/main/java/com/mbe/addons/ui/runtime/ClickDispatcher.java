package com.mbe.addons.ui.runtime;

import com.mbe.addons.ui.api.MenuItem;
import com.mbe.addons.ui.api.MenuView;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class ClickDispatcher implements Listener {
    private final SessionManager sessionManager;
    private final Logger logger;

    public ClickDispatcher(SessionManager sessionManager, Logger logger) {
        this.sessionManager = sessionManager;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        MenuSession session = sessionManager.session(player.getUniqueId());
        if (session == null) {
            return;
        }

        if (!event.getView().getTopInventory().equals(session.inventory())) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= session.inventory().getSize()) {
            return;
        }

        event.setCancelled(true);

        MenuView view = session.currentView();
        if (view == null) {
            return;
        }

        MenuItem item = view.items().get(rawSlot);
        if (item == null) {
            return;
        }

        DefaultPlayerContext playerContext = new DefaultPlayerContext(player, session.sessionData());
        DefaultMenuClickContext clickContext = new DefaultMenuClickContext(
                playerContext,
                event.getClick(),
                rawSlot,
                () -> sessionManager.refresh(player),
                () -> sessionManager.close(player)
        );

        try {
            item.onClick(clickContext);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error handling click for menu " + session.menu().id() + " slot " + rawSlot, t);
            session.inventory().setItem(rawSlot, ErrorItemFactory.errorItem());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        MenuSession session = sessionManager.session(player.getUniqueId());
        if (session == null) {
            return;
        }

        if (!event.getInventory().equals(session.inventory())) {
            return;
        }

        sessionManager.clear(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        sessionManager.clear(event.getPlayer().getUniqueId());
    }
}
