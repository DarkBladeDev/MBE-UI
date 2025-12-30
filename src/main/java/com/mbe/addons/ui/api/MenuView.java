package com.mbe.addons.ui.api;

import java.util.Map;

/**
 * Estado renderizado inmutable.
 */
public interface MenuView {
    Map<Integer, MenuItem> items();
}
