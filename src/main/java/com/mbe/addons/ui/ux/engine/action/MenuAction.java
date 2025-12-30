package com.mbe.addons.ui.ux.engine.action;

import com.mbe.addons.ui.ux.engine.runtime.MenuContext;

import java.util.Map;

public interface MenuAction {
    void execute(MenuContext ctx, Map<String, Object> args) throws Exception;
}

