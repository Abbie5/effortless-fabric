package dev.huskcasaca.effortless.building;

import dev.huskcasaca.effortless.building.operation.Operation;

public record OperationResult(
        Operation<?, ?, ?> operation,
        Object result
) {

}