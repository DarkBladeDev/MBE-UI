package com.mbe.addons.ui.ux.engine.runtime;

public interface MenuRuntimeOperations {
    void enter(MenuContext ctx);

    void exit();

    void openMenu(String menuId);

    void close();

    void refresh();

    void nextPage();

    void previousPage();
}

