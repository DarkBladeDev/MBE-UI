package com.mbe.addons.ui.legacy.services.ux;

import com.mbe.addons.ui.ux.engine.MenuContextFactory;
import com.mbe.addons.ui.ux.engine.MenuProvider;
import com.mbe.addons.ui.ux.engine.model.MenuDefinition;

import java.nio.file.Path;
import java.util.Optional;

public interface UiUxMenusService {
    void loadMenus();

    Path menusDir();

    Optional<MenuDefinition> getMenuDefinition(String menuId);

    void registerMenuProvider(String menuId, MenuProvider provider);

    void open(String menuId, MenuContextFactory contextFactory);
}
