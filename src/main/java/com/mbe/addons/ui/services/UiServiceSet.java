package com.mbe.addons.ui.services;

import com.mbe.addons.ui.legacy.services.UiControllerService;
import com.mbe.addons.ui.legacy.services.UiPlaceholderService;
import com.mbe.addons.ui.legacy.services.UiSessionService;
import com.mbe.addons.ui.legacy.services.ux.UiUxActionsService;
import com.mbe.addons.ui.legacy.services.ux.UiUxMenusService;
import com.mbe.addons.ui.legacy.services.ux.UiUxRuntimeService;

import java.util.Objects;

public record UiServiceSet(
        UiControllerService controller,
        UiSessionService sessions,
        UiPlaceholderService placeholders,
        UiUxMenusService uxMenus,
        UiUxActionsService uxActions,
        UiUxRuntimeService uxRuntime
) {
    public UiServiceSet {
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(sessions, "sessions");
        Objects.requireNonNull(placeholders, "placeholders");
        Objects.requireNonNull(uxMenus, "uxMenus");
        Objects.requireNonNull(uxActions, "uxActions");
        Objects.requireNonNull(uxRuntime, "uxRuntime");
    }
}
