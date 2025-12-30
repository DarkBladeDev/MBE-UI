package com.mbe.addons.ui.api;

import org.bukkit.event.inventory.ClickType;

public interface MenuClickContext {
    PlayerContext player();

    ClickType clickType();

    int slot();

    void refresh();

    void close();
}

