package com.example.util;

import java.util.List;

/**
 * Encapsule les résultats d'une requête paginée avec ses métadonnées.
 *
 * @param <T> Le type des éléments de la page
 */
public class PaginationResult<T> {

    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final int totalPages;
    private final long totalItems;

    public PaginationResult(List<T> items, int currentPage, int pageSize, long totalItems) {
        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<T> getItems()       { return items; }
    public int getCurrentPage()     { return currentPage; }
    public int getPageSize()        { return pageSize; }
    public int getTotalPages()      { return totalPages; }
    public long getTotalItems()     { return totalItems; }

    /** Indique si une page suivante est disponible. */
    public boolean hasNext()        { return currentPage < totalPages; }

    /** Indique si une page précédente est disponible. */
    public boolean hasPrevious()    { return currentPage > 1; }

    @Override
    public String toString() {
        return String.format(
            "PaginationResult{page=%d/%d, taille=%d, total=%d, suivant=%b, précédent=%b}",
            currentPage, totalPages, pageSize, totalItems, hasNext(), hasPrevious()
        );
    }
}
