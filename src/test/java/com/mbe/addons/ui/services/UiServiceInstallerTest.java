package com.mbe.addons.ui.services;

import com.mbe.addons.ui.legacy.services.UiControllerService;
import com.mbe.addons.ui.legacy.services.UiPlaceholderService;
import com.mbe.addons.ui.legacy.services.UiSessionService;
import com.mbe.addons.ui.legacy.services.ux.UiUxActionsService;
import com.mbe.addons.ui.legacy.services.ux.UiUxMenusService;
import com.mbe.addons.ui.legacy.services.ux.UiUxRuntimeService;
import com.mbe.addons.ui.api.MenuController;
import com.mbe.addons.ui.api.UIMenu;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;

final class UiServiceInstallerTest {

    @Test
    void registerAll_registersAllExpectedTypes() {
        Map<Class<?>, Object> registered = new HashMap<>();
        ServiceRegistrar registrar = (type, service) -> registered.put(type, service);

        MenuController controller = new MenuController() {
            @Override
            public void open(UIMenu menu, Player player) {
            }

            @Override
            public void refresh(Player player) {
            }

            @Override
            public void close(Player player) {
            }
        };

        UiControllerService controllerService = () -> controller;
        UiSessionService sessionService = player -> Map.of();
        UiPlaceholderService placeholderService = (player, input) -> input;
        UiUxMenusService uxMenus = new UiUxMenusService() {
            @Override
            public void loadMenus() {
            }

            @Override
            public java.nio.file.Path menusDir() {
                return java.nio.file.Path.of(".");
            }

            @Override
            public java.util.Optional<com.mbe.addons.ui.ux.engine.model.MenuDefinition> getMenuDefinition(String menuId) {
                return java.util.Optional.empty();
            }

            @Override
            public void registerMenuProvider(String menuId, com.mbe.addons.ui.ux.engine.MenuProvider provider) {
            }

            @Override
            public void open(String menuId, com.mbe.addons.ui.ux.engine.MenuContextFactory contextFactory) {
            }
        };
        UiUxActionsService uxActions = (id, action) -> {
        };
        UiUxRuntimeService uxRuntime = ctx -> () -> Map.of();

        UiServiceSet set = new UiServiceSet(controllerService, sessionService, placeholderService, uxMenus, uxActions, uxRuntime);

        UiServiceInstaller.registerAll(registrar, set, controller);

        assertSame(controller, registered.get(MenuController.class));
        assertSame(controllerService, registered.get(UiControllerService.class));
        assertSame(sessionService, registered.get(UiSessionService.class));
        assertSame(placeholderService, registered.get(UiPlaceholderService.class));
        assertSame(uxMenus, registered.get(UiUxMenusService.class));
        assertSame(uxActions, registered.get(UiUxActionsService.class));
        assertSame(uxRuntime, registered.get(UiUxRuntimeService.class));
    }
}
