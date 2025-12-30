package com.mbe.addons.ui.ux.engine.action.builtin;

import com.mbe.addons.ui.ux.engine.action.MenuAction;
import com.mbe.addons.ui.ux.engine.runtime.MenuContext;
import com.mbe.addons.ui.ux.engine.runtime.MenuRuntimeOperations;

import java.util.Map;
import java.util.Objects;

public final class PreviousPageAction implements MenuAction {
    private final MenuRuntimeOperations ops;

    public PreviousPageAction(MenuRuntimeOperations ops) {
        this.ops = Objects.requireNonNull(ops, "ops");
    }

    @Override
    public void execute(MenuContext ctx, Map<String, Object> args) {
        ops.previousPage();
    }
}

