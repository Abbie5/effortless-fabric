package dev.effortless.building;

public enum BuildingSource {
    PERFORM,
    PREVIEW,
    PREVIEW_ONCE,
    ;

    public boolean isPreview() {
        return this == PREVIEW || this == PREVIEW_ONCE;
    }

}
