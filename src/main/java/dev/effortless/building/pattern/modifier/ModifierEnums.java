package dev.effortless.building.pattern.modifier;

import dev.effortless.Effortless;
import dev.effortless.building.pattern.modifier.array.Array;
import dev.effortless.building.pattern.modifier.mirror.Mirror;
import dev.effortless.building.pattern.modifier.mirror.RadialMirror;

public enum ModifierEnums {
    ARRAY("array", new Array()),
    MIRROR("mirror", new Mirror()),
    RADIAL_MIRROR("radial_mirror", new RadialMirror());

    private final Modifier instance;
    private final String name;

    ModifierEnums(String name, Modifier instance) {
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
        return Effortless.asKey("modifier", getName());
    }

    public Modifier getInstance() {
        return instance;
    }

    public String getName() {
        return name;
    }

}
