package com.mbe.addons.ui.ux.engine.model;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record MenuDefinition(
        String id,
        String title,
        int rows,
        Map<Integer, SlotDefinition> slots,
        Optional<PaginationDefinition> pagination
) {
    public MenuDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(slots, "slots");
        pagination = pagination == null ? Optional.empty() : pagination;
    }

    public int size() {
        return rows * 9;
    }
}

