package com.mbe.addons.ui.runtime;

import com.darkbladedev.engine.api.addon.AddonContext;
import com.darkbladedev.engine.api.logging.EngineLogger;
import com.mbe.addons.ui.api.MenuController;
import com.mbe.addons.ui.api.UIMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager implements MenuController {
    private final AddonContext context;
    private final InventoryRenderer inventoryRenderer;
    private final Map<UUID, MenuSession> sessions = new ConcurrentHashMap<>();

    public SessionManager(AddonContext context) {
        this.context = context;
        EngineLogger logger = context.getLogger();
        DiffEngine diffEngine = new DiffEngine(logger);
        this.inventoryRenderer = new InventoryRenderer(diffEngine, logger);
    }

    @Override
    public void open(UIMenu menu, Player player) {
        open(menu, player, null);
    }

    public void open(UIMenu menu, Player player, Map<String, Object> initialSessionData) {
        if (!Bukkit.isPrimaryThread()) {
            context.runTask(() -> open(menu, player, initialSessionData));
            return;
        }

        MenuSession session = new MenuSession(menu, player);
        if (initialSessionData != null && !initialSessionData.isEmpty()) {
            session.sessionData().putAll(initialSessionData);
        }
        sessions.put(player.getUniqueId(), session);
        inventoryRenderer.open(session, player);
    }

    @Override
    public void refresh(Player player) {
        if (!Bukkit.isPrimaryThread()) {
            context.runTask(() -> refresh(player));
            return;
        }

        MenuSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        inventoryRenderer.render(session, player);
    }

    @Override
    public void close(Player player) {
        if (!Bukkit.isPrimaryThread()) {
            context.runTask(() -> close(player));
            return;
        }

        sessions.remove(player.getUniqueId());
        player.closeInventory();
    }

    MenuSession session(UUID playerId) {
        return sessions.get(playerId);
    }

    public Map<String, Object> sessionData(Player player) {
        if (player == null) {
            return Map.of();
        }
        MenuSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return Map.of();
        }
        return session.sessionData();
    }

    void clear(UUID playerId) {
        sessions.remove(playerId);
    }
}
