package com.mbe.addons.ui.runtime;

import com.mbe.addons.ui.api.MenuView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.darkbladedev.engine.api.logging.EngineLogger;

final class InventoryRenderer {
    private final DiffEngine diffEngine;
    private final EngineLogger logger;
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer LEGACY_AMP = LegacyComponentSerializer.legacyAmpersand();

    InventoryRenderer(DiffEngine diffEngine, EngineLogger logger) {
        this.diffEngine = diffEngine;
        this.logger = logger;
    }

    void open(MenuSession session, Player player) {
        Inventory inventory = session.inventory();
        if (inventory == null) {
            DefaultPlayerContext playerContext = new DefaultPlayerContext(player, session.sessionData());
            String title = session.menu().title(playerContext);
            inventory = Bukkit.createInventory(player, session.menu().size(), legacyText(title));
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
            Inventory inventory = Bukkit.createInventory(player, session.menu().size(), legacyText(title));
            session.inventory(inventory);
            session.title(title);
            player.openInventory(inventory);
        }

        MenuView newView;
        try {
            newView = session.menu().render(playerContext);
        } catch (Throwable t) {
            logger.error("Error rendering menu " + session.menu().id(), t);
            return;
        }

        diffEngine.apply(session, playerContext, newView);
    }

    private static Component legacyText(String text) {
        if (text == null || text.isBlank()) {
            return Component.empty();
        }
        if (text.indexOf('§') >= 0) {
            return LEGACY_SECTION.deserialize(text);
        }
        return LEGACY_AMP.deserialize(text);
    }
}
