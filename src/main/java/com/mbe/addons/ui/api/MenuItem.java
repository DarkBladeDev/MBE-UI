package com.mbe.addons.ui.api;

import org.bukkit.inventory.ItemStack;

public interface MenuItem {
    ItemStack render(PlayerContext ctx);

    void onClick(MenuClickContext ctx);
}

