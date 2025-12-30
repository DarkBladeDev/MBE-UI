package com.mbe.addons.ui;

import com.darkbladedev.engine.api.addon.AddonContext;
import com.darkbladedev.engine.api.addon.AddonException;
import com.darkbladedev.engine.api.addon.MultiblockAddon;
import com.mbe.addons.ui.api.MenuController;
import com.mbe.addons.ui.api.UI;
import com.mbe.addons.ui.runtime.ClickDispatcher;
import com.mbe.addons.ui.runtime.SessionManager;
import com.mbe.addons.ui.ux.UXAddon;
import com.mbe.addons.ui.ux.examples.ExampleJavaMenuProvider;

import java.util.Objects;

public final class MbeUiAddon implements MultiblockAddon {
    private SessionManager sessionManager;
    private UXAddon uxAddon;

    @Override
    public String getId() {
        return "mbe_ui";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void onLoad(AddonContext ctx) throws AddonException {
        Objects.requireNonNull(ctx, "ctx");

        if (ctx.getApiVersion() != 1) {
            throw new AddonException(getId(), "Incompatible API", true, AddonException.Phase.LOAD, "apiVersion");
        }

        this.sessionManager = new SessionManager(ctx);
        MenuController controller = sessionManager;
        UI.register(controller);

        ctx.registerListener(new ClickDispatcher(sessionManager, ctx.getLogger()));

        this.uxAddon = new UXAddon(ctx);
        UXAddon.setInstance(this.uxAddon);
        this.uxAddon.menuEngine().loadMenus();
        this.uxAddon.menuEngine().registerMenuProvider("example:java", new ExampleJavaMenuProvider());
    }

    @Override
    public void onEnable() throws AddonException {
    }

    @Override
    public void onDisable() {
        UI.unregister();
        this.sessionManager = null;
        this.uxAddon = null;
        UXAddon.clearInstance();
    }
}

