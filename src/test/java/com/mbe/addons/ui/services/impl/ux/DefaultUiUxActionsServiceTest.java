package com.mbe.addons.ui.services.impl.ux;

import com.mbe.addons.ui.ux.engine.action.MenuAction;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class DefaultUiUxActionsServiceTest {

    @Test
    void register_delegatesToRegistry() {
        AtomicReference<String> seenId = new AtomicReference<>();
        AtomicReference<MenuAction> seenAction = new AtomicReference<>();

        DefaultUiUxActionsService service = new DefaultUiUxActionsService((id, action) -> {
            seenId.set(id);
            seenAction.set(action);
        });

        MenuAction action = (ctx, args) -> {
        };
        service.register("test:action", action);

        assertEquals("test:action", seenId.get());
        assertSame(action, seenAction.get());
    }
}
