package com.mbe.addons.ui.services.impl;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertSame;

final class DefaultUiSessionServiceTest {

    @Test
    void sessionData_delegatesToSessionManager() {
        AtomicReference<Object> seenPlayer = new AtomicReference<>();
        Map<String, Object> session = Map.of("key", "value");

        DefaultUiSessionService service = new DefaultUiSessionService(player -> {
            seenPlayer.set(player);
            return session;
        });

        assertSame(session, service.sessionData((org.bukkit.entity.Player) null));
        assertSame(null, seenPlayer.get());
    }
}
