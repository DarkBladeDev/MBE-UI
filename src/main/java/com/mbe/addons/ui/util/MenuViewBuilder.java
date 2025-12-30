package com.mbe.addons.ui.util;

import com.mbe.addons.ui.api.MenuClickContext;
import com.mbe.addons.ui.api.MenuItem;
import com.mbe.addons.ui.api.MenuView;
import com.mbe.addons.ui.api.PlayerContext;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class MenuViewBuilder {
    private final Map<Integer, MenuItem> items = new HashMap<>();

    private MenuViewBuilder() {
    }

    public static MenuViewBuilder create() {
        return new MenuViewBuilder();
    }

    public MenuViewBuilder slot(int slot, MenuItem item) {
        if (slot < 0) {
            throw new IllegalArgumentException("slot must be >= 0");
        }
        if (item == null) {
            items.remove(slot);
        } else {
            items.put(slot, item);
        }
        return this;
    }

    public MenuViewBuilder fillRange(int fromInclusive, int toInclusive, BiFunction<Integer, Integer, MenuItem> provider) {
        Objects.requireNonNull(provider, "provider");
        if (toInclusive < fromInclusive) {
            return this;
        }
        int index = 0;
        for (int slot = fromInclusive; slot <= toInclusive; slot++) {
            MenuItem item = provider.apply(index, slot);
            if (item != null) {
                items.put(slot, item);
            }
            index++;
        }
        return this;
    }

    public MenuView build() {
        Map<Integer, MenuItem> snapshot = Collections.unmodifiableMap(new HashMap<>(items));
        return () -> snapshot;
    }

    public static MenuItem item(Function<PlayerContext, ItemStack> renderer, Consumer<MenuClickContext> onClick) {
        Objects.requireNonNull(renderer, "renderer");
        Objects.requireNonNull(onClick, "onClick");
        return new MenuItem() {
            @Override
            public ItemStack render(PlayerContext ctx) {
                return renderer.apply(ctx);
            }

            @Override
            public void onClick(MenuClickContext ctx) {
                onClick.accept(ctx);
            }
        };
    }
}

