package com.mbe.addons.ui.services.impl.ux;

import com.mbe.addons.ui.api.MenuView;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertSame;

final class DefaultUiUxRuntimeServiceTest {

    @Test
    void render_delegatesToRuntime() {
        AtomicReference<Object> seen = new AtomicReference<>();
        MenuView expected = () -> Map.of();

        DefaultUiUxRuntimeService service = new DefaultUiUxRuntimeService(ctx -> {
            seen.set(ctx);
            return expected;
        });

        assertSame(expected, service.render(null));
        assertSame(null, seen.get());
    }
}
