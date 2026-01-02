package com.mbe.addons.ui.ui;

public interface PageContext {
    int page();

    int pageSize();

    int totalItems();

    int totalPages();

    default boolean hasNext() {
        return page() + 1 < totalPages();
    }

    default boolean hasPrevious() {
        return page() > 0;
    }
}
