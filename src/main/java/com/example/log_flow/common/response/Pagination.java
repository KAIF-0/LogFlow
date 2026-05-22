package com.example.log_flow.common.response;

public class Pagination {

    private final int page;
    private final int limit;
    private final long total;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrev;

    public Pagination(int page, int limit, long total, int totalPages, boolean hasNext, boolean hasPrev) {
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
    }

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

    public long getTotal() {
        return total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public boolean isHasPrev() {
        return hasPrev;
    }
}
