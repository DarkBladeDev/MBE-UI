package com.mbe.addons.ui.runtime;

import com.mbe.ui.api.menu.MenuItem;
import com.mbe.ui.api.menu.MenuView;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.darkbladedev.engine.api.logging.EngineLogger;

import java.security.CodeSource;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public final class ClickDispatcher implements Listener {
    private static final String BUILD_SIG = "mbe-ui:1.1.0";
    private static final long DEBUG_INTERVAL_MS = 10_000;
    private static final AtomicLong LAST_DEBUG_LOG_AT = new AtomicLong(0);
    private static final AtomicLong SELF_SCAN_DONE = new AtomicLong(0);
    private static volatile String selfBytecodeHint;

    private final SessionManager sessionManager;
    private final EngineLogger logger;

    public ClickDispatcher(SessionManager sessionManager, EngineLogger logger) {
        this.sessionManager = sessionManager;
        this.logger = logger;
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            maybeLogRuntimeDebug();

            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            MenuSession session = sessionManager.session(player.getUniqueId());
            if (session == null) {
                return;
            }

            var invView = event.getView();
            var top = invView == null ? null : invView.getTopInventory();
            if (top == null || !top.equals(session.inventory())) {
                return;
            }

            var clickedInventory = event.getClickedInventory();
            if (clickedInventory == null) {
                return;
            }

            if (!clickedInventory.equals(session.inventory())) {
                InventoryAction action = event.getAction();
                if (event.isShiftClick() || action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(true);
                }
                return;
            }

            int slot = event.getSlot();
            if (slot < 0 || slot >= session.inventory().getSize()) {
                return;
            }

            event.setCancelled(true);

            MenuView view = session.currentView();
            if (view == null) {
                return;
            }

            MenuItem item = view.items().get(slot);
            if (item == null) {
                return;
            }

            DefaultPlayerContext playerContext = new DefaultPlayerContext(player, session.sessionData());
            DefaultMenuClickContext clickContext = new DefaultMenuClickContext(
                    playerContext,
                    event.getClick(),
                    slot,
                    event::getCursor,
                    event::setCursor,
                    event::getCurrentItem,
                    event::setCurrentItem,
                    () -> sessionManager.refresh(player),
                    () -> sessionManager.close(player)
            );

            item.onClick(clickContext);
        } catch (Throwable t) {
            logger.error("Error in ClickDispatcher.onInventoryClick build=" + BUILD_SIG, t);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        MenuSession session = sessionManager.session(player.getUniqueId());
        if (session == null) {
            return;
        }

        var view = event.getView();
        var top = view == null ? null : view.getTopInventory();
        if (top == null || !top.equals(session.inventory())) {
            return;
        }

        int topSize = top.getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= 0 && rawSlot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void maybeLogRuntimeDebug() {
        long now = System.currentTimeMillis();
        long prev = LAST_DEBUG_LOG_AT.get();
        if (now - prev < DEBUG_INTERVAL_MS) {
            return;
        }
        if (!LAST_DEBUG_LOG_AT.compareAndSet(prev, now)) {
            return;
        }

        maybeScanSelfBytecode();

        ClassLoader addonCl = ClickDispatcher.class.getClassLoader();
        ClassLoader ctxCl = Thread.currentThread().getContextClassLoader();
        logger.debug("Runtime ClickDispatcher=" + codeSource(ClickDispatcher.class)
                + " addonCL=" + classLoaderId(addonCl)
                + " ctxCL=" + classLoaderId(ctxCl)
                + " build=" + BUILD_SIG
                + " self=" + selfBytecodeHint
                + " InventoryView(addonCL)=" + inventoryViewInfo(addonCl)
                + " InventoryView(ctxCL)=" + inventoryViewInfo(ctxCl));
    }

    private static void maybeScanSelfBytecode() {
        if (SELF_SCAN_DONE.get() != 0) {
            return;
        }
        if (!SELF_SCAN_DONE.compareAndSet(0, 1)) {
            return;
        }

        String hint = "unavailable";
        try (var in = ClickDispatcher.class.getResourceAsStream("ClickDispatcher.class")) {
            if (in == null) {
                hint = "no_resource";
            } else {
                byte[] bytes = in.readAllBytes();
                String hay = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1).toLowerCase(Locale.ROOT);
                boolean mentionsInventoryView = hay.contains("inventoryview");
                hint = "mentionsInventoryView=" + mentionsInventoryView + " bytes=" + bytes.length;
            }
        } catch (Throwable t) {
            hint = "error(" + t.getClass().getSimpleName() + ")";
        }
        selfBytecodeHint = hint;
    }

    private static String inventoryViewInfo(ClassLoader cl) {
        try {
            Class<?> type = Class.forName("org.bukkit.inventory.InventoryView", false, cl);
            return (type.isInterface() ? "interface" : "class") + "@" + codeSource(type) + " loader=" + classLoaderId(type.getClassLoader());
        } catch (Throwable t) {
            return "unavailable(" + t.getClass().getSimpleName() + ")";
        }
    }

    private static String classLoaderId(ClassLoader cl) {
        if (cl == null) {
            return "bootstrap";
        }
        return cl.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(cl));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        MenuSession session = sessionManager.session(player.getUniqueId());
        if (session == null) {
            return;
        }

        sessionManager.clear(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        sessionManager.clear(event.getPlayer().getUniqueId());
    }
}
