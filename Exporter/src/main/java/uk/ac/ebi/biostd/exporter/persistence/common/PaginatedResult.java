package uk.ac.ebi.biostd.exporter.persistence.common;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class PaginatedResult<T> implements Iterable<List<T>> {

    private final Function<Page, List<T>> nextPageSupplier;
    private final int pageSize;
    private int currentPage;

    private boolean next = true;

    public PaginatedResult(int pageSize, Function<Page, List<T>> nextPageSupplier) {
        this.nextPageSupplier = nextPageSupplier;
        this.pageSize = pageSize;
        this.currentPage = 0;
    }

    public List<T> getNextPage() {
        List<T> listData = nextPageSupplier.apply(new Page(pageSize * currentPage++, pageSize));
        this.next = listData.size() > 0;
        return listData;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new PageIterator();
    }

    @AllArgsConstructor
    @Getter
    public class Page {

        private final long start;
        private final long results;
    }

    public class PageIterator implements Iterator<List<T>> {

        @Override
        public boolean hasNext() {
            return next;
        }

        @Override
        public List<T> next() {
            return getNextPage();
        }
    }
}
