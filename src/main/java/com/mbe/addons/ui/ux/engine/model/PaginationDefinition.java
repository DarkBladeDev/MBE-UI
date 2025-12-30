package com.mbe.addons.ui.ux.engine.model;

import java.util.Objects;
import java.util.Optional;

public record PaginationDefinition(
        String source,
        int pageSize,
        int fromSlot,
        int toSlot,
        Optional<SlotDefinition> template
) {
    public PaginationDefinition {
        Objects.requireNonNull(source, "source");
        template = template == null ? Optional.empty() : template;
    }
}

