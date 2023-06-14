package dev.huskcasaca.effortless.building;

public enum BuildingState {
    IDLE,
    PLACING,
    BREAKING;

    public boolean isIdle() {
        return this == IDLE;
    }

    public boolean isBuilding() {
        return this == PLACING;
    }

    public boolean isBreaking() {
        return this == BREAKING;
    }

}
