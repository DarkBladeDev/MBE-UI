package com.mbe.addons.ui.ui;

import org.bukkit.entity.Player;

public interface MenuController {
    void open(UIMenu menu, Player player);

    void refresh(Player player);

    void close(Player player);
}
