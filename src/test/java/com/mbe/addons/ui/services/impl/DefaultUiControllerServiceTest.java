package com.mbe.addons.ui.services.impl;

import com.mbe.ui.api.menu.MenuController;
import com.mbe.ui.api.menu.UIMenu;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

final class DefaultUiControllerServiceTest {

    @Test
    void controller_returnsSameInstance() {
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

        DefaultUiControllerService service = new DefaultUiControllerService(controller);

        assertSame(controller, service.controller());
    }
}
