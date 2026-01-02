package com.mbe.addons.ui.services.impl.ux;

import com.mbe.addons.ui.legacy.services.ux.UiUxMenusService;
import com.mbe.addons.ui.ux.engine.MenuContextFactory;
import com.mbe.addons.ui.ux.engine.MenuEngine;
import com.mbe.addons.ui.ux.engine.MenuProvider;
import com.mbe.addons.ui.ux.engine.model.MenuDefinition;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DefaultUiUxMenusService implements UiUxMenusService {
    private final Runnable loadMenus;
    private final Supplier<Path> menusDir;
    private final Function<String, Optional<MenuDefinition>> getMenuDefinition;
    private final BiConsumer<String, MenuProvider> registerMenuProvider;
    private final BiConsumer<String, MenuContextFactory> open;

    public DefaultUiUxMenusService(MenuEngine engine) {
        Objects.requireNonNull(engine, "engine");
        this.loadMenus = engine::loadMenus;
        this.menusDir = engine::menusDir;
        this.getMenuDefinition = engine::getMenuDefinition;
        this.registerMenuProvider = engine::registerMenuProvider;
        this.open = engine::open;
    }

    public DefaultUiUxMenusService(
            Runnable loadMenus,
            Supplier<Path> menusDir,
            Function<String, Optional<MenuDefinition>> getMenuDefinition,
            BiConsumer<String, MenuProvider> registerMenuProvider,
            BiConsumer<String, MenuContextFactory> open
    ) {
        this.loadMenus = Objects.requireNonNull(loadMenus, "loadMenus");
        this.menusDir = Objects.requireNonNull(menusDir, "menusDir");
        this.getMenuDefinition = Objects.requireNonNull(getMenuDefinition, "getMenuDefinition");
        this.registerMenuProvider = Objects.requireNonNull(registerMenuProvider, "registerMenuProvider");
        this.open = Objects.requireNonNull(open, "open");
    }

    @Override
    public void loadMenus() {
        loadMenus.run();
    }

    @Override
    public Path menusDir() {
        return menusDir.get();
    }

    @Override
    public Optional<MenuDefinition> getMenuDefinition(String menuId) {
        return getMenuDefinition.apply(menuId);
    }

    @Override
    public void registerMenuProvider(String menuId, MenuProvider provider) {
        registerMenuProvider.accept(menuId, provider);
    }

    @Override
    public void open(String menuId, MenuContextFactory contextFactory) {
        open.accept(menuId, contextFactory);
    }
}
