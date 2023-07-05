package dev.effortless.building.reach;

public record ReachConfig(
        int maxReachDistance,
        int maxBlockPlacePerAxis,
        int maxBlockPlaceAtOnce,
        boolean canBreakFar,
        boolean enableUndoRedo,
        int undoStackSize
) {
    public ReachConfig() {
        this(
                512,
                128,
                10_000,
                true,
                true,
                200
        );
    }

}
