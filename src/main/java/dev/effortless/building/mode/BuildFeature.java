package dev.effortless.building.mode;

import dev.effortless.building.base.Feature;
import dev.effortless.building.base.SingleSelectFeature;

public enum BuildFeature {
    CIRCLE_START("circle_start", CircleStart.values()),
    CUBE_FILLING("cube_filling", CubeFilling.values()),
    PLANE_FACING("plane_facing", PlaneFacing.values()),
    PLANE_FILLING("plane_filling", PlaneFilling.values()),
    RAISED_EDGE("raised_edge", RaisedEdge.values()),
    ;

    private final String name;
    private final Feature[] entries;

    BuildFeature(String name, Feature... defaultEntries) {
        this.name = name;
        this.entries = defaultEntries;
    }

    public String getName() {
        return name;
    }

    public Feature[] getEntries() {
        return entries;
    }

    public enum CircleStart implements SingleSelectFeature {
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

    public enum CubeFilling implements SingleSelectFeature {
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

    public enum PlaneFilling implements SingleSelectFeature {

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

    public enum PlaneFacing implements SingleSelectFeature {
        HORIZONTAL("face_horizontal"),
        VERTICAL("face_vertical"),
        BOTH("face_both");

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

    public enum RaisedEdge implements SingleSelectFeature {

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

}
