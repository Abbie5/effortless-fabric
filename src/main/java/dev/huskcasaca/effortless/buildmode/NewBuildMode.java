package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmode.oneclick.Disable;
import dev.huskcasaca.effortless.buildmode.oneclick.Single;
import dev.huskcasaca.effortless.buildmode.threeclick.*;
import dev.huskcasaca.effortless.buildmode.twoclick.Circle;
import dev.huskcasaca.effortless.buildmode.twoclick.Floor;
import dev.huskcasaca.effortless.buildmode.twoclick.Line;
import dev.huskcasaca.effortless.buildmode.twoclick.Wall;
import dev.huskcasaca.effortless.screen.radial.OptionSet;
import dev.huskcasaca.effortless.screen.radial.RadialSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public enum NewBuildMode implements RadialSlot {
    DISABLE("disable", new Disable(), Category.BASIC),
    SINGLE("single", new Single(), Category.BASIC /*, BuildOption.BUILD_SPEED*/),

    LINE("line", new Line(), Category.SQUARE /*, OptionEnum.THICKNESS*/),
    WALL("wall", new Wall(), Category.SQUARE, BuildModeOptions.PLANE_FILLING),
    FLOOR("floor", new Floor(), Category.SQUARE, BuildModeOptions.PLANE_FILLING),
    CUBE("cube", new Cube(), Category.SQUARE, BuildModeOptions.CUBE_FILLING),

    DIAGONAL_LINE("diagonal_line", new DiagonalLine(), Category.DIAGONAL),
    DIAGONAL_WALL("diagonal_wall", new DiagonalWall(), Category.DIAGONAL),
    SLOPE_FLOOR("slope_floor", new SlopeFloor(), Category.DIAGONAL, BuildModeOptions.RAISED_EDGE),

    CIRCLE("circle", new Circle(), Category.CIRCULAR, BuildModeOptions.CIRCLE_START, BuildModeOptions.PLANE_FILLING, BuildModeOptions.ORIENTATION),
    CYLINDER("cylinder", new Cylinder(), Category.CIRCULAR, BuildModeOptions.CIRCLE_START, BuildModeOptions.PLANE_FILLING, BuildModeOptions.ORIENTATION),
    SPHERE("sphere", new Sphere(), Category.CIRCULAR, BuildModeOptions.CIRCLE_START, BuildModeOptions.PLANE_FILLING, BuildModeOptions.ORIENTATION),
    ;

//    PYRAMID("pyramid", new Pyramid(), Category.ROOF),
//    CONE("cone", new Cone(), Category.ROOF),
//    DOME("dome", new Dome(), Category.ROOF);

    private final Buildable provider;
    private final Category category;
    private final BuildModeOptions[] options;
    private final String name;

    NewBuildMode(String name, Buildable instance, Category category, BuildModeOptions... options) {
        this.name = name;
        this.provider = instance;
        this.category = category;
        this.options = options;
    }

    public String getNameKey() {
        return Effortless.MOD_ID + ".mode." + name;
    }

    public Buildable getInstance() {
        return provider;
    }

    public BuildModeOptions[] getOptions() {
        return options;
    }

    @Override
    public Component getComponentName() {
        return Component.translatable(getNameKey());
    }

    @Override
    public ResourceLocation getIcon() {
//        return new ResourceLocation(Effortless.MOD_ID, "textures/gui/radial/" + name + ".png");
        return new ResourceLocation(Effortless.MOD_ID, "textures/mode/" + name + ".png");
    }

    @Override
    public Color getTintColor() {
        return category.getColor();
    }

    public List<OptionSet> getOptionSets() {
        return Arrays.asList(options);
    }

    public enum Category {
        BASIC(new Color(0f, .5f, 1f, .5f)),
        SQUARE(new Color(1f, .54f, .24f, .5f)),
        DIAGONAL(new Color(0.56f, 0.28f, 0.87f, .5f)),
        CIRCULAR(new Color(0.29f, 0.76f, 0.3f, .5f)),
        ROOF(new Color(0.83f, 0.87f, 0.23f, .5f));

        private final Color color;

        Category(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

}
