package com.mbe.addons.ui.ux.engine.runtime;

import com.darkbladedev.engine.api.capability.Capability;
import com.darkbladedev.engine.model.MultiblockInstance;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DefaultMenuContext implements MenuContext {
    private final Player player;
    private final String menuId;
    private final int page;
    private final Map<String, Object> variables;
    private final Optional<MultiblockInstance> multiblock;

    DefaultMenuContext(Player player, String menuId, int page, Map<String, Object> variables, Optional<MultiblockInstance> multiblock) {
        this.player = Objects.requireNonNull(player, "player");
        this.menuId = Objects.requireNonNull(menuId, "menuId");
        this.page = page;
        this.variables = Objects.requireNonNull(variables, "variables");
        this.multiblock = multiblock == null ? Optional.empty() : multiblock;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public String menuId() {
        return menuId;
    }

    @Override
    public int page() {
        return page;
    }

    @Override
    public Map<String, Object> variables() {
        return variables;
    }

    @Override
    public Optional<MultiblockInstance> multiblock() {
        return multiblock;
    }

    @Override
    public <T extends Capability> Optional<T> capability(Class<T> type) {
        Objects.requireNonNull(type, "type");
        return multiblock.flatMap(mb -> mb.getCapability(type));
    }
}

