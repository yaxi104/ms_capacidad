package com.reactivo.capacidad.domain.model;

import java.util.List;

public class PagedCapacityResult {
    private final List<Capacity> items;
    private final int page;
    private final int size;
    private final long totalItems;

    public PagedCapacityResult(List<Capacity> items, int page, int size, long totalItems) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
    }

    public List<Capacity> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalItems() {
        return totalItems;
    }
}
