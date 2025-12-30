package com.mbe.addons.ui.api;

/**
 * Identificador único de menú.
 *
 * <p>El namespace evita colisiones entre addons.</p>
 */
public record MenuId(String namespace, String name) {
    public MenuId {
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("namespace is blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is blank");
        }
    }

    @Override
    public String toString() {
        return namespace + ":" + name;
    }
}
