package com.mbe.addons.ui.services.impl;

import com.mbe.addons.ui.legacy.services.UiSessionService;
import com.mbe.addons.ui.runtime.SessionManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class DefaultUiSessionService implements UiSessionService {
    private final Function<Player, Map<String, Object>> sessionData;

    public DefaultUiSessionService(SessionManager sessions) {
        Objects.requireNonNull(sessions, "sessions");
        this.sessionData = sessions::sessionData;
    }

    public DefaultUiSessionService(Function<Player, Map<String, Object>> sessionData) {
        this.sessionData = Objects.requireNonNull(sessionData, "sessionData");
    }

    @Override
    public Map<String, Object> sessionData(Player player) {
        return sessionData.apply(player);
    }
}
