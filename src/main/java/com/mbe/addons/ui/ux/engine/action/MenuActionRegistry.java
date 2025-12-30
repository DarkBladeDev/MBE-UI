package com.mbe.addons.ui.ux.engine.action;

import com.mbe.addons.ui.ux.engine.model.ActionCall;
import com.mbe.addons.ui.ux.engine.runtime.MenuContext;
import com.mbe.addons.ui.ux.engine.runtime.MenuRuntimeOperations;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MenuActionRegistry {
    private final Logger logger;
    private final MenuRuntimeOperations ops;
    private final Map<String, MenuAction> actions = new ConcurrentHashMap<>();

    public MenuActionRegistry(Logger logger, MenuRuntimeOperations ops) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.ops = Objects.requireNonNull(ops, "ops");
    }

    public void register(String id, MenuAction action) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(action, "action");
        actions.put(id, action);
    }

    public void executeAll(MenuContext ctx, List<ActionCall> calls) {
        Objects.requireNonNull(ctx, "ctx");
        if (calls == null || calls.isEmpty()) {
            return;
        }

        ops.enter(ctx);
        try {
            for (ActionCall call : calls) {
                if (call == null) {
                    continue;
                }
                String actionId = call.action();
                MenuAction action = actions.get(actionId);
                if (action == null) {
                    logger.warning("[UXAddon][Menu:" + ctx.menuId() + "][Action:" + actionId + "] Cause: ActionNotFound");
                    continue;
                }

                try {
                    action.execute(ctx, call.args());
                } catch (Throwable t) {
                    logger.log(Level.WARNING, "[UXAddon][Menu:" + ctx.menuId() + "][Action:" + actionId + "] Cause: " + t.getClass().getSimpleName(), t);
                }
            }
        } finally {
            ops.exit();
        }
    }
}

