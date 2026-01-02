package com.mbe.addons.ui.ui;

import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Contexto por jugador para render y runtime del menú.
 */
public interface PlayerContext {
    Player player();

    UUID uuid();

    Locale locale();

    /**
     * Estado temporal por sesión de menú.
     *
     * <p>No persistente. Útil para páginas, filtros y selecciones.</p>
     */
    Map<String, Object> sessionData();

    default Map<String, Object> session() {
        return sessionData();
    }
}
