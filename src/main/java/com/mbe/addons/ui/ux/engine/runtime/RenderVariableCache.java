package com.mbe.addons.ui.ux.engine.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class RenderVariableCache {
    private final Map<String, String> cache = new HashMap<>();

    String get(String key, Supplier<String> supplier) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(supplier, "supplier");
        return cache.computeIfAbsent(key, k -> {
            String v = supplier.get();
            return v == null ? "" : v;
        });
    }
}
