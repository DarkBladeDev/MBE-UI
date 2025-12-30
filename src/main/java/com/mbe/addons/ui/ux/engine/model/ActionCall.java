package com.mbe.addons.ui.ux.engine.model;

import java.util.Map;
import java.util.Objects;

public record ActionCall(String action, Map<String, Object> args) {
    public ActionCall {
        Objects.requireNonNull(action, "action");
        args = args == null ? Map.of() : Map.copyOf(args);
    }
}

