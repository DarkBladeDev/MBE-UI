package com.mbe.addons.ui.ux.engine.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SlotDefinition(
        String item,
        String name,
        List<String> lore,
        List<ActionCall> actions,
        Map<String, Object> extra
) {
    public SlotDefinition {
        Objects.requireNonNull(item, "item");
        lore = lore == null ? List.of() : List.copyOf(lore);
        actions = actions == null ? List.of() : List.copyOf(actions);
        extra = extra == null ? Map.of() : Map.copyOf(extra);
    }
}

