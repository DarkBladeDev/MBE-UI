package com.mbe.addons.ui.legacy.services;

import org.bukkit.entity.Player;

import java.util.Map;

public interface UiSessionService {
    Map<String, Object> sessionData(Player player);
}
