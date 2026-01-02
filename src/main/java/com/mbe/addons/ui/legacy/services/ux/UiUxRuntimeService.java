package com.mbe.addons.ui.legacy.services.ux;

import com.mbe.addons.ui.api.MenuView;
import com.mbe.addons.ui.api.PlayerContext;

public interface UiUxRuntimeService {
    MenuView render(PlayerContext ctx);
}
