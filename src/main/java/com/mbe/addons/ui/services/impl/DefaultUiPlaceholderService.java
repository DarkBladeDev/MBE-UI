package com.mbe.addons.ui.services.impl;

import com.mbe.addons.ui.legacy.services.UiPlaceholderService;
import com.mbe.addons.ui.ux.placeholders.PlaceholderProcessor;
import org.bukkit.entity.Player;

import com.darkbladedev.engine.api.logging.EngineLogger;

import java.util.Objects;

public final class DefaultUiPlaceholderService implements UiPlaceholderService {
    private final EngineLogger logger;

    public DefaultUiPlaceholderService(EngineLogger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public String process(Player player, String input) {
        return PlaceholderProcessor.process(logger, player, input);
    }
}
