package dev.effortless.building.operation;

public interface OperationResult<O extends OperationResult<O>> {

    Operation<O> operation();

}
