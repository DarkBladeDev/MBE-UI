package com.mbe.addons.ui.ux.engine.view;

import com.mbe.addons.ui.api.MenuView;
import com.mbe.addons.ui.util.MenuViewBuilder;
import com.mbe.addons.ui.ux.engine.model.MenuDefinition;
import com.mbe.addons.ui.ux.engine.model.PaginationDefinition;
import com.mbe.addons.ui.ux.engine.model.SlotDefinition;
import com.mbe.addons.ui.ux.engine.runtime.DeclarativeMenuItem;
import com.mbe.addons.ui.ux.engine.runtime.MenuContext;
import com.mbe.addons.ui.ux.engine.runtime.MenuRuntime;
import com.mbe.addons.ui.ux.engine.runtime.RenderVariableCache;

import com.darkbladedev.engine.api.logging.EngineLogger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class MenuRenderer {
    private final EngineLogger logger;

    public MenuRenderer(EngineLogger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public MenuView render(MenuRuntime runtime, MenuDefinition def, List<Map<String, Object>> pagedEntries, MenuContext ctx, RenderVariableCache cache) {
        Objects.requireNonNull(runtime, "runtime");
        Objects.requireNonNull(def, "def");
        Objects.requireNonNull(pagedEntries, "pagedEntries");
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(cache, "cache");

        MenuViewBuilder builder = MenuViewBuilder.create();
        String menuId = def.id();

        for (var e : def.slots().entrySet()) {
            int slot = e.getKey();
            SlotDefinition slotDef = e.getValue();
            builder.slot(slot, new DeclarativeMenuItem(runtime, menuId, slotDef, Map.of(), ctx, cache));
        }

        Optional<PaginationDefinition> pagination = def.pagination();
        if (pagination.isPresent()) {
            PaginationDefinition p = pagination.get();
            int from = Math.max(0, p.fromSlot());
            int to = Math.max(from, p.toSlot());
            int max = def.size() - 1;
            if (from > max) {
                return builder.build();
            }
            to = Math.min(to, max);

            SlotDefinition template = p.template().orElseGet(() -> new SlotDefinition(
                    "minecraft:paper",
                    "<variable:entry_name>",
                    List.of("<variable:entry_lore>"),
                    List.of(),
                    Map.of()
            ));

            int slot = from;
            for (int i = 0; i < pagedEntries.size() && slot <= to; i++, slot++) {
                Map<String, Object> entryVars = pagedEntries.get(i);
                builder.slot(slot, new DeclarativeMenuItem(runtime, menuId, template, entryVars, ctx, cache));
            }
        }

        return builder.build();
    }
}
