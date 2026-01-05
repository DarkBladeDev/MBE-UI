package com.mbe.addons.ui.services;

import com.darkbladedev.engine.api.addon.AddonContext;
import com.mbe.addons.ui.legacy.services.UiControllerService;
import com.mbe.addons.ui.legacy.services.UiPlaceholderService;
import com.mbe.addons.ui.legacy.services.UiSessionService;
import com.mbe.addons.ui.legacy.services.ux.UiUxActionsService;
import com.mbe.addons.ui.legacy.services.ux.UiUxMenusService;
import com.mbe.addons.ui.legacy.services.ux.UiUxRuntimeService;
import com.mbe.ui.api.menu.MenuController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class UiServiceInstaller {
    private UiServiceInstaller() {
    }

    public static void registerAll(AddonContext ctx, UiServiceSet services, MenuController controller) {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(controller, "controller");

        Method registerService = resolveRegisterService(ctx);
        if (registerService == null) {
            ctx.getLogger().warn("Core service registry not available; skipping service registration");
            return;
        }

        ServiceRegistrar registrar = new ServiceRegistrar() {
            @Override
            public void register(Class<?> type, Object service) {
                try {
                    registerService.invoke(ctx, type, service);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot access AddonContext.registerService", e);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    throw new IllegalStateException("AddonContext.registerService failed: " + cause.getClass().getSimpleName(), cause);
                }
            }
        };
        registerAll(registrar, services, controller);
    }

    private static Method resolveRegisterService(AddonContext ctx) {
        for (Method m : ctx.getClass().getMethods()) {
            if (!m.getName().equals("registerService")) {
                continue;
            }
            if (m.getParameterCount() != 2) {
                continue;
            }
            Class<?>[] p = m.getParameterTypes();
            if (p[0] == Class.class) {
                return m;
            }
        }
        return null;
    }

    public static void registerAll(ServiceRegistrar registrar, UiServiceSet services, MenuController controller) {
        Objects.requireNonNull(registrar, "registrar");
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(controller, "controller");

        registrar.register(MenuController.class, controller);

        registrar.register(UiControllerService.class, services.controller());
        registrar.register(UiSessionService.class, services.sessions());
        registrar.register(UiPlaceholderService.class, services.placeholders());
        registrar.register(UiUxMenusService.class, services.uxMenus());
        registrar.register(UiUxActionsService.class, services.uxActions());
        registrar.register(UiUxRuntimeService.class, services.uxRuntime());
    }
}
