package com.mbe.addons.ui.services.impl.ux;

import com.mbe.addons.ui.legacy.services.ux.UiUxActionsService;
import com.mbe.addons.ui.ux.engine.action.MenuAction;
import com.mbe.addons.ui.ux.engine.action.MenuActionRegistry;

import java.util.Objects;
import java.util.function.BiConsumer;

public final class DefaultUiUxActionsService implements UiUxActionsService {
    private final BiConsumer<String, MenuAction> registrar;

    public DefaultUiUxActionsService(MenuActionRegistry actions) {
        Objects.requireNonNull(actions, "actions");
        this.registrar = actions::register;
    }

    public DefaultUiUxActionsService(BiConsumer<String, MenuAction> registrar) {
        this.registrar = Objects.requireNonNull(registrar, "registrar");
    }

    @Override
    public void register(String id, MenuAction action) {
        registrar.accept(id, action);
    }
}
