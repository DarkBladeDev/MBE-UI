package com.mbe.addons.ui.ux.engine.runtime;

import com.darkbladedev.engine.api.capability.Capability;
import com.darkbladedev.engine.model.MultiblockInstance;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

public interface MenuContext {
    Player player();

    String menuId();

    int page();

    Map<String, Object> variables();

    Optional<MultiblockInstance> multiblock();

    <T extends Capability> Optional<T> capability(Class<T> type);
}

