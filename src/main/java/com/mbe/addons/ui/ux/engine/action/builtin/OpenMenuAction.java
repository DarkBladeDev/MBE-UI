package com.mbe.addons.ui.ux.engine.action.builtin;

import com.mbe.addons.ui.ux.engine.action.MenuAction;
import com.mbe.addons.ui.ux.engine.runtime.MenuContext;
import com.mbe.addons.ui.ux.engine.runtime.MenuRuntimeOperations;

import java.util.Map;
import java.util.Objects;

public final class OpenMenuAction implements MenuAction {
    private final MenuRuntimeOperations ops;

    public OpenMenuAction(MenuRuntimeOperations ops) {
        this.ops = Objects.requireNonNull(ops, "ops");
    }

    @Override
    public void execute(MenuContext ctx, Map<String, Object> args) {
        Object idObj = args.get("id");
        if (!(idObj instanceof String menuId) || menuId.isBlank()) {
            return;
        }
        ops.openMenu(menuId);
    }
}

