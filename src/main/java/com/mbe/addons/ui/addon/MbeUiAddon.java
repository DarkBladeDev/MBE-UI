package com.mbe.addons.ui.addon;

import com.darkbladedev.engine.api.addon.AddonContext;
import com.darkbladedev.engine.api.addon.AddonException;
import com.darkbladedev.engine.api.addon.MultiblockAddon;
import com.darkbladedev.engine.api.logging.EngineLogger;
import com.darkbladedev.engine.api.logging.LogLevel;
import com.mbe.addons.ui.api.MenuController;
import com.mbe.addons.ui.ui.UI;
import com.mbe.addons.ui.runtime.ClickDispatcher;
import com.mbe.addons.ui.runtime.SessionManager;
import com.mbe.addons.ui.ux.engine.MenuEngine;
import com.mbe.addons.ui.ux.examples.ExampleJavaMenuProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Objects;
import java.util.Optional;

public final class MbeUiAddon implements MultiblockAddon {
    private static volatile MbeUiAddon instance;

    private AddonContext context;
    private SessionManager sessionManager;
    private MenuEngine menuEngine;
    private ClickDispatcher clickDispatcher;

    public static Optional<MbeUiAddon> instance() {
        return Optional.ofNullable(instance);
    }

    public static MbeUiAddon require() {
        MbeUiAddon current = instance;
        if (current == null) {
            throw new IllegalStateException("MBE-UI not initialized");
        }
        return current;
    }

    @Override
    public String getId() {
        return "mbe_ui";
    }

    @Override
    public String getVersion() {
        return "1.1.0";
    }

    @Override
    public void onLoad(AddonContext ctx) throws AddonException {
        Objects.requireNonNull(ctx, "ctx");

        EngineLogger logger = ctx.getLogger();

        if (ctx.getApiVersion() != 1) {
            throw new AddonException(getId(), "Incompatible API", true, AddonException.Phase.LOAD, "apiVersion");
        }

        instance = this;
        this.context = ctx;

        logger.info("Loading " + getId() + " v" + getVersion());

        unregisterStaleClickDispatchers(logger);

        Path menusDir = MenuEngine.resolveMenusDir(ctx);
        try {
            Files.createDirectories(menusDir);
        } catch (IOException | SecurityException e) {
            throw new AddonException(getId(), "Cannot create required folder: menus", e, true, AddonException.Phase.LOAD, "fs");
        }

        this.sessionManager = new SessionManager(ctx);
        UI.register(sessionManager);

        try {
            ctx.registerService(MenuController.class, sessionManager);
            logger.info("MenuController service registered");
        } catch (RuntimeException e) {
            throw new AddonException(getId(), "Failed to register MenuController service", e, true, AddonException.Phase.LOAD, "services");
        }

        this.clickDispatcher = new ClickDispatcher(sessionManager, logger);
        registerBukkitListeners(this.clickDispatcher);

        this.menuEngine = new MenuEngine(ctx);
        this.menuEngine.loadMenus();
        this.menuEngine.registerMenuProvider("example:java", new ExampleJavaMenuProvider());
    }

    private void registerBukkitListeners(ClickDispatcher dispatcher) throws AddonException {
        Plugin owner = Bukkit.getPluginManager().getPlugin("MultiBlockEngine");
        if (owner == null) {
            throw new AddonException(getId(), "Cannot resolve MultiBlockEngine plugin", true, AddonException.Phase.LOAD, "bukkit");
        }

        var pm = Bukkit.getPluginManager();
        pm.registerEvent(InventoryClickEvent.class, dispatcher, org.bukkit.event.EventPriority.NORMAL, castExec(InventoryClickEvent.class, dispatcher::onInventoryClick), owner, true);
        pm.registerEvent(InventoryCloseEvent.class, dispatcher, org.bukkit.event.EventPriority.MONITOR, castExec(InventoryCloseEvent.class, dispatcher::onInventoryClose), owner, false);
        pm.registerEvent(PlayerQuitEvent.class, dispatcher, org.bukkit.event.EventPriority.MONITOR, castExec(PlayerQuitEvent.class, dispatcher::onQuit), owner, false);
    }

    private static <E extends Event> EventExecutor castExec(Class<E> type, java.util.function.Consumer<E> consumer) {
        return (ignored, event) -> {
            if (type.isInstance(event)) {
                consumer.accept(type.cast(event));
            }
        };
    }

    private static String codeSource(Class<?> type) {
        try {
            CodeSource cs = type.getProtectionDomain().getCodeSource();
            if (cs == null || cs.getLocation() == null) {
                return "unknown";
            }
            return cs.getLocation().toString();
        } catch (Throwable ignored) {
            return "unknown";
        }
    }

    private static void unregisterStaleClickDispatchers(EngineLogger logger) {
        Objects.requireNonNull(logger, "logger");
        try {
            int removed = 0;
            for (HandlerList list : HandlerList.getHandlerLists()) {
                for (var registered : list.getRegisteredListeners()) {
                    Listener listener = registered.getListener();
                    if (listener == null) {
                        continue;
                    }
                    if (!listener.getClass().getName().equals(ClickDispatcher.class.getName())) {
                        continue;
                    }
                    HandlerList.unregisterAll(listener);
                    removed++;
                }
            }
            if (removed > 0) {
                logger.info("Unregistered stale ClickDispatcher listeners: " + removed);
            }
        } catch (Throwable t) {
            logger.log(LogLevel.WARN, "Failed to unregister stale ClickDispatcher listeners", t);
        }
    }

    @Override
    public void onEnable() throws AddonException {
    }

    @Override
    public void onDisable() {
        UI.unregister();
        if (clickDispatcher != null) {
            HandlerList.unregisterAll(clickDispatcher);
        }
        if (instance == this) {
            instance = null;
        }
        this.context = null;
        this.sessionManager = null;
        this.menuEngine = null;
        this.clickDispatcher = null;
    }

    public AddonContext context() {
        AddonContext current = context;
        if (current == null) {
            throw new IllegalStateException("MBE-UI not initialized");
        }
        return current;
    }

    public MenuEngine menuEngine() {
        MenuEngine current = menuEngine;
        if (current == null) {
            throw new IllegalStateException("MBE-UI not initialized");
        }
        return current;
    }
}
