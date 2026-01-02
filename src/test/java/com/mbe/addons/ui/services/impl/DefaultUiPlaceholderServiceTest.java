package com.mbe.addons.ui.services.impl;

import org.junit.jupiter.api.Test;

import com.darkbladedev.engine.api.logging.EngineLogger;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DefaultUiPlaceholderServiceTest {

    @Test
    void process_emptyInput_returnsEmpty() {
        EngineLogger logger = (level, message, throwable, kvs) -> {
        };

        DefaultUiPlaceholderService service = new DefaultUiPlaceholderService(logger);

        assertEquals("", service.process(null, ""));
        assertEquals("", service.process(null, null));
    }
}
