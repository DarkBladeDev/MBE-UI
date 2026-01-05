package com.mbe.addons.ui.services.impl;

import com.mbe.addons.ui.legacy.services.UiControllerService;
import com.mbe.ui.api.menu.MenuController;

import java.util.Objects;

public final class DefaultUiControllerService implements UiControllerService {
    private final MenuController controller;

    public DefaultUiControllerService(MenuController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    @Override
    public MenuController controller() {
        return controller;
    }
}
