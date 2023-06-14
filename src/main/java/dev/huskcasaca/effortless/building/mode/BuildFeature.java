package dev.huskcasaca.effortless.building.mode;

public enum BuildFeature {
    CIRCLE_START("circle_start", CircleStart.values()),
    CUBE_FILLING("cube_filling", CubeFilling.values()),
    PLANE_FACING("plane_facing", PlaneFacing.values()),
    PLANE_FILLING("plane_filling", PlaneFilling.values()),
    RAISED_EDGE("raised_edge", RaisedEdge.values()),
    ;

    private final String name;
    private final Entry[] entries;

    BuildFeature(String name, Entry... defaultEntries) {
        this.name = name;
        this.entries = defaultEntries;
    }

    public String getName() {
        return name;
    }

    public Entry[] getEntries() {
        return entries;
    }

    public enum CircleStart implements Entry {
        CIRCLE_START_CORNER("circle_start_corner"),
        CIRCLE_START_CENTER("circle_start_center"),
        ;

        private final String name;

        CircleStart(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getCategory() {
            return CIRCLE_START.getName();
        }
    }

    public enum CubeFilling implements Entry {
        CUBE_FULL("cube_full"),
        CUBE_HOLLOW("cube_hollow"),
        CUBE_SKELETON("cube_skeleton");

        private final String name;

        CubeFilling(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getCategory() {
            return CUBE_FILLING.getName();
        }
    }

    public enum PlaneFilling implements Entry {

        PLANE_FULL("plane_full"),
        PLANE_HOLLOW("plane_hollow");

        private final String name;

        PlaneFilling(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getCategory() {
            return PLANE_FILLING.getName();
        }
    }

    public enum PlaneFacing implements Entry {
        HORIZONTAL("face_horizontal"),
        VERTICAL("face_vertical");

        private final String name;

        PlaneFacing(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getCategory() {
            return PLANE_FACING.getName();
        }
    }

    public enum RaisedEdge implements Entry {

        RAISE_SHORT_EDGE("raise_short_edge"),
        RAISE_LONG_EDGE("raise_long_edge");

        private final String name;

        RaisedEdge(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getCategory() {
            return RAISED_EDGE.getName();
        }
    }

    public interface Entry extends BuildOption {
    }
}
