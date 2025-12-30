package com.mbe.addons.ui.ux.engine;

import com.darkbladedev.engine.api.addon.AddonContext;
import com.mbe.addons.ui.api.UI;
import com.mbe.addons.ui.ux.engine.action.MenuActionRegistry;
import com.mbe.addons.ui.ux.engine.action.builtin.CloseAction;
import com.mbe.addons.ui.ux.engine.action.builtin.NextPageAction;
import com.mbe.addons.ui.ux.engine.action.builtin.OpenMenuAction;
import com.mbe.addons.ui.ux.engine.action.builtin.PreviousPageAction;
import com.mbe.addons.ui.ux.engine.action.builtin.RunCommandAction;
import com.mbe.addons.ui.ux.engine.model.MenuDefinition;
import com.mbe.addons.ui.ux.engine.parse.MenuParser;
import com.mbe.addons.ui.ux.engine.runtime.MenuRuntime;
import com.mbe.addons.ui.ux.engine.runtime.MenuRuntimeOperations;
import com.mbe.addons.ui.ux.engine.view.MenuRenderer;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MenuEngine {
    private final AddonContext context;
    private final Logger logger;

    private final MenuParser parser;
    private final MenuRenderer renderer;
    private final MenuRuntime runtime;
    private final MenuActionRegistry actionRegistry;

    private final Map<String, MenuDefinition> menus = new ConcurrentHashMap<>();
    private final Map<String, MenuProvider> providers = new ConcurrentHashMap<>();

    public MenuEngine(AddonContext context) {
        this.context = Objects.requireNonNull(context, "context");
        this.logger = context.getLogger();

        this.parser = new MenuParser(context.getAddonId(), logger);
        this.renderer = new MenuRenderer(logger);
        this.runtime = new MenuRuntime(context, this, renderer, logger);

        MenuRuntimeOperations ops = runtime.operations();
        this.actionRegistry = new MenuActionRegistry(logger, ops);
        registerBaseActions();
    }

    public void loadMenus() {
        Collection<MenuDefinition> parsed = parser.loadAll();
        Map<String, MenuDefinition> next = new ConcurrentHashMap<>();

        for (MenuDefinition def : parsed) {
            String id = def.id();
            if (id == null || id.isBlank()) {
                continue;
            }
            next.put(id, def);
        }

        menus.clear();
        menus.putAll(next);
    }

    public void registerMenuProvider(String menuId, MenuProvider provider) {
        Objects.requireNonNull(menuId, "menuId");
        Objects.requireNonNull(provider, "provider");
        providers.put(menuId, provider);
    }

    public Optional<MenuDefinition> getMenuDefinition(String menuId) {
        return Optional.ofNullable(menus.get(menuId));
    }

    public Optional<MenuProvider> getMenuProvider(String menuId) {
        return Optional.ofNullable(providers.get(menuId));
    }

    public MenuActionRegistry actions() {
        return actionRegistry;
    }

    public AddonContext context() {
        return context;
    }

    public void open(String menuId, MenuContextFactory contextFactory) {
        try {
            runtime.open(menuId, contextFactory);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[UXAddon][Menu:" + menuId + "][Action:open_menu] Cause: " + t.getClass().getSimpleName(), t);
        }
    }

    public void close(org.bukkit.entity.Player player) {
        UI.controller().close(player);
    }

    private void registerBaseActions() {
        MenuRuntimeOperations ops = runtime.operations();
        actionRegistry.register("open_menu", new OpenMenuAction(ops));
        actionRegistry.register("close", new CloseAction(ops));
        actionRegistry.register("next_page", new NextPageAction(ops));
        actionRegistry.register("previous_page", new PreviousPageAction(ops));
        actionRegistry.register("run_command", new RunCommandAction(logger));
    }
}
