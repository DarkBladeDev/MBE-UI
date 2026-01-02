package com.mbe.addons.ui.services.impl.ux;

import com.mbe.addons.ui.legacy.services.ux.UiUxRuntimeService;
import com.mbe.addons.ui.api.MenuView;
import com.mbe.addons.ui.api.PlayerContext;
import com.mbe.addons.ui.ux.engine.runtime.MenuRuntime;

import java.util.Objects;
import java.util.function.Function;

public final class DefaultUiUxRuntimeService implements UiUxRuntimeService {
    private final Function<PlayerContext, MenuView> render;

    public DefaultUiUxRuntimeService(MenuRuntime runtime) {
        Objects.requireNonNull(runtime, "runtime");
        this.render = runtime::render;
    }

    public DefaultUiUxRuntimeService(Function<PlayerContext, MenuView> render) {
        this.render = Objects.requireNonNull(render, "render");
    }

    @Override
    public MenuView render(PlayerContext ctx) {
        return render.apply(ctx);
    }
}
