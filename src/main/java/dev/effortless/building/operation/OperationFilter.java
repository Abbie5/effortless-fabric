package dev.effortless.building.operation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public interface OperationFilter<T extends Operation> extends Predicate<T> {

    static OperationFilter<Operation> distinctByPosition() {
        return distinctByKey(Operation::getPosition);
    }

    private static <T extends Operation> OperationFilter<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        var seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

}
