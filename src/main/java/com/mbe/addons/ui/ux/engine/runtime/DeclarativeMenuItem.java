package com.mbe.addons.ui.ux.engine.runtime;

import com.mbe.addons.ui.api.MenuClickContext;
import com.mbe.addons.ui.api.MenuItem;
import com.mbe.addons.ui.api.PlayerContext;
import com.mbe.addons.ui.ux.engine.model.ActionCall;
import com.mbe.addons.ui.ux.engine.model.SlotDefinition;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DeclarativeMenuItem implements MenuItem {
    private final MenuRuntime runtime;
    private final String menuId;
    private final SlotDefinition slot;
    private final Map<String, Object> entryVars;
    private final MenuContext renderCtx;
    private final RenderVariableCache cache;

    public DeclarativeMenuItem(MenuRuntime runtime, String menuId, SlotDefinition slot, Map<String, Object> entryVars, MenuContext renderCtx, RenderVariableCache cache) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.menuId = Objects.requireNonNull(menuId, "menuId");
        this.slot = Objects.requireNonNull(slot, "slot");
        this.entryVars = entryVars == null ? Map.of() : Map.copyOf(entryVars);
        this.renderCtx = Objects.requireNonNull(renderCtx, "renderCtx");
        this.cache = Objects.requireNonNull(cache, "cache");
    }

    @Override
    public ItemStack render(PlayerContext ctx) {
        Material material = resolveMaterial(slot.item());
        if (material == null) {
            return errorItem();
        }

        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            MenuContext merged = mergedContext(renderCtx);

            if (slot.name() != null && !slot.name().isBlank()) {
                meta.setDisplayName(runtime.resolveText(slot.name(), merged, cache));
            }

            if (slot.lore() != null && !slot.lore().isEmpty()) {
                List<String> lore = new ArrayList<>(slot.lore().size());
                for (String line : slot.lore()) {
                    lore.add(runtime.resolveText(line, merged, cache));
                }
                meta.setLore(lore);
            }

            stack.setItemMeta(meta);
        }

        return stack;
    }

    @Override
    public void onClick(MenuClickContext ctx) {
        Objects.requireNonNull(ctx, "ctx");
        MenuContext clickCtx = runtime.buildContext(ctx.player());
        MenuContext merged = mergedContext(clickCtx);
        List<ActionCall> calls = slot.actions();
        runtime.engine().actions().executeAll(merged, calls);
    }

    private MenuContext mergedContext(MenuContext base) {
        if (entryVars.isEmpty()) {
            return base;
        }
        Map<String, Object> mergedVars = new HashMap<>(base.variables());
        mergedVars.putAll(entryVars);
        if (base instanceof RuntimeBackedMenuContext internal) {
            return new RuntimeBackedMenuContext(
                    internal.player(),
                    internal.menuId(),
                    internal.page(),
                    Map.copyOf(mergedVars),
                    internal.multiblock(),
                    internal.session()
            );
        }
        return new DefaultMenuContext(base.player(), base.menuId(), base.page(), Map.copyOf(mergedVars), base.multiblock());
    }

    private Material resolveMaterial(String item) {
        if (item == null || item.isBlank()) {
            return null;
        }
        String key = item;
        if (key.contains(":")) {
            key = key.substring(key.indexOf(':') + 1);
        }
        Material mat = Material.matchMaterial(key);
        if (mat != null) {
            return mat;
        }
        return Material.matchMaterial(key.toUpperCase());
    }

    private ItemStack errorItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Error");
            item.setItemMeta(meta);
        }
        return item;
    }
}
