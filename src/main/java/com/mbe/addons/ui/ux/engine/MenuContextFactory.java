package com.mbe.addons.ui.ux.engine;

import com.darkbladedev.engine.model.MultiblockInstance;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

public interface MenuContextFactory {
    Player player();

    Map<String, Object> variables();

    Optional<MultiblockInstance> multiblock();
}

