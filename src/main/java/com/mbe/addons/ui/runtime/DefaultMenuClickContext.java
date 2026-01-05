package com.mbe.addons.ui.runtime;

import com.mbe.ui.api.menu.MenuClickContext;
import com.mbe.ui.api.menu.PlayerContext;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class DefaultMenuClickContext implements MenuClickContext {
    private final PlayerContext player;
    private final ClickType clickType;
    private final int slot;
    private final Supplier<ItemStack> cursor;
    private final Consumer<ItemStack> setCursor;
    private final Supplier<ItemStack> slotItem;
    private final Consumer<ItemStack> setSlotItem;
    private final Runnable refresh;
    private final Runnable close;

    DefaultMenuClickContext(
            PlayerContext player,
            ClickType clickType,
            int slot,
            Supplier<ItemStack> cursor,
            Consumer<ItemStack> setCursor,
            Supplier<ItemStack> slotItem,
            Consumer<ItemStack> setSlotItem,
            Runnable refresh,
            Runnable close
    ) {
        this.player = Objects.requireNonNull(player, "player");
        this.clickType = Objects.requireNonNull(clickType, "clickType");
        this.slot = slot;
        this.cursor = Objects.requireNonNull(cursor, "cursor");
        this.setCursor = Objects.requireNonNull(setCursor, "setCursor");
        this.slotItem = Objects.requireNonNull(slotItem, "slotItem");
        this.setSlotItem = Objects.requireNonNull(setSlotItem, "setSlotItem");
        this.refresh = Objects.requireNonNull(refresh, "refresh");
        this.close = Objects.requireNonNull(close, "close");
    }

    @Override
    public PlayerContext player() {
        return player;
    }

    @Override
    public ClickType clickType() {
        return clickType;
    }

    @Override
    public int slot() {
        return slot;
    }

    @Override
    public ItemStack cursorItem() {
        return cursor.get();
    }

    @Override
    public void cursorItem(ItemStack stack) {
        setCursor.accept(stack);
    }

    @Override
    public ItemStack slotItem() {
        return slotItem.get();
    }

    @Override
    public void slotItem(ItemStack stack) {
        setSlotItem.accept(stack);
    }

    @Override
    public void refresh() {
        refresh.run();
    }

    @Override
    public void close() {
        close.run();
    }
}
