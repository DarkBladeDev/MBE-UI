package com.mbe.addons.ui.runtime;

import com.mbe.addons.ui.api.MenuClickContext;
import com.mbe.addons.ui.api.PlayerContext;
import org.bukkit.event.inventory.ClickType;

import java.util.Objects;

final class DefaultMenuClickContext implements MenuClickContext {
    private final PlayerContext player;
    private final ClickType clickType;
    private final int slot;
    private final Runnable refresh;
    private final Runnable close;

    DefaultMenuClickContext(PlayerContext player, ClickType clickType, int slot, Runnable refresh, Runnable close) {
        this.player = Objects.requireNonNull(player, "player");
        this.clickType = Objects.requireNonNull(clickType, "clickType");
        this.slot = slot;
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
    public void refresh() {
        refresh.run();
    }

    @Override
    public void close() {
        close.run();
    }
}

