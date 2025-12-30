package com.mbe.addons.ui.ux.engine;

import com.mbe.addons.ui.ux.engine.model.MenuDefinition;
import com.mbe.addons.ui.ux.engine.runtime.MenuContext;

public interface MenuProvider {
    MenuDefinition create(MenuContext ctx);
}

