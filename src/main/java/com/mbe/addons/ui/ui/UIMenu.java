package com.mbe.addons.ui.ui;

/**
 * Contrato de un menú virtual.
 *
 * <p>Stateless: el runtime guarda el estado temporal en {@link PlayerContext#sessionData()}.</p>
 */
public interface UIMenu {
    MenuId id();

    default String title(PlayerContext ctx) {
        return id().name();
    }

    /**
     * Tamaño del inventario renderizado (múltiplo de 9).
     */
    int size();

    /**
     * Render por jugador.
     *
     * <p>Debe ser puro y rápido. Prohibido IO.</p>
     */
    MenuView render(PlayerContext ctx);
}
