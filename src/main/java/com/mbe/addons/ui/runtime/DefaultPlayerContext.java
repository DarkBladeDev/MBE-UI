package com.mbe.addons.ui.runtime;

import com.mbe.ui.api.menu.PlayerContext;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

final class DefaultPlayerContext implements PlayerContext {
    private final Player player;
    private final Map<String, Object> sessionData;

    DefaultPlayerContext(Player player, Map<String, Object> sessionData) {
        this.player = player;
        this.sessionData = sessionData;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public UUID uuid() {
        return player.getUniqueId();
    }

    @Override
    public Locale locale() {
        return player.locale();
    }

    @Override
    public Map<String, Object> sessionData() {
        return sessionData;
    }
}
