package com.mbe.addons.ui.legacy.services.ux;

import com.mbe.ui.api.menu.MenuView;
import com.mbe.ui.api.menu.PlayerContext;

public interface UiUxRuntimeService {
    MenuView render(PlayerContext ctx);
}
