package com.mbe.addons.ui.legacy.services.ux;

import com.mbe.addons.ui.ux.engine.action.MenuAction;

public interface UiUxActionsService {
    void register(String id, MenuAction action);
}
