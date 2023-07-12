package dev.effortless.building;

public enum TracingResult {
    SUCCESS_FULFILLED,
    SUCCESS_PARTIAL,
    NOT_BUILDING,
    MISSING_HIT;

    public boolean isSuccess() {
        return this == SUCCESS_FULFILLED || this == SUCCESS_PARTIAL;
    }
}