package dev.effortless.building;

public enum BuildingState {
    IDLE,
    PLACE_BLOCK,
    BREAK_BLOCK;

    public boolean isIdle() {
        return this == IDLE;
    }

}
