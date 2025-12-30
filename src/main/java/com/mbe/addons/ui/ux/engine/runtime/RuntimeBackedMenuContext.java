package com.mbe.addons.ui.ux.engine.runtime;

import com.darkbladedev.engine.model.MultiblockInstance;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class RuntimeBackedMenuContext extends DefaultMenuContext {
    private final Map<String, Object> session;

    RuntimeBackedMenuContext(Player player, String menuId, int page, Map<String, Object> variables, Optional<MultiblockInstance> multiblock, Map<String, Object> session) {
        super(player, menuId, page, variables, multiblock);
        this.session = Objects.requireNonNull(session, "session");
    }

    Map<String, Object> session() {
        return session;
    }
}

