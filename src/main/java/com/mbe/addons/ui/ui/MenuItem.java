package com.mbe.addons.ui.ui;

import org.bukkit.inventory.ItemStack;

public interface MenuItem {
    ItemStack render(PlayerContext ctx);

    void onClick(MenuClickContext ctx);
}
