package dev.huskcasaca.effortless.buildmodifier;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmodifier.array.Array;
import dev.huskcasaca.effortless.buildmodifier.mirror.Mirror;
import dev.huskcasaca.effortless.buildmodifier.mirror.RadialMirror;

public enum BuildModifier {
    ARRAY("array", new Array()),
    MIRROR("mirror", new Mirror()),
    RADIAL_MIRROR("radial_mirror", new RadialMirror());

    private final Modifier instance;
    private final String name;

    BuildModifier(String name, Modifier instance) {
        this.name = name;
        this.instance = instance;
    }

    public static Array getArray() {
        return (Array) ARRAY.getInstance();
    }

    public static Mirror getMirror() {
        return (Mirror) MIRROR.getInstance();
    }

    public static RadialMirror getRadialMirror() {
        return (RadialMirror) RADIAL_MIRROR.getInstance();
    }

    public String getNameKey() {
        return Effortless.MOD_ID + ".modifier." + getName();
    }

    public Modifier getInstance() {
        return instance;
    }

    public String getName() {
        return name;
    }

}
