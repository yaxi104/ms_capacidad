package com.reactivo.capacidad.domain.model;

import java.util.List;

public class PagedCapacityResponse {
    private List<CapacityWithTechnologies> items;
    private int page;
    private int size;
    private long totalItems;

    public PagedCapacityResponse(List<CapacityWithTechnologies> items, int page, int size, long totalItems) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
    }

    public List<CapacityWithTechnologies> getItems() {
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
