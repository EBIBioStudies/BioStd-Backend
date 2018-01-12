package uk.ac.ebi.biostd.exporter.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConcurrentUtil {

    @SneakyThrows
    public <T> List<T> resolveFutures(List<Future<T>> futureList) {
        List<T> futures = new ArrayList<>(futureList.size());
        for (Future<T> report : futureList) {
            futures.add(report.get());
        }

        return futures;
    }

    @SneakyThrows
    public <T> T resolveFuture(Future<T> future) {
        return future.get();
    }
}
