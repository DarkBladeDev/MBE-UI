package com.mbe.addons.ui.runtime;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

final class ErrorItemFactory {
    private static final ItemStack TEMPLATE;

    static {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Error");
            item.setItemMeta(meta);
        }
        TEMPLATE = item;
    }

    private ErrorItemFactory() {
    }

    static ItemStack errorItem() {
        return TEMPLATE.clone();
    }
}

