package dev.huskcasaca.effortless.building.base;

import dev.huskcasaca.effortless.building.pattern.modifier.array.Array;
import dev.huskcasaca.effortless.building.pattern.modifier.mirror.Mirror;
import dev.huskcasaca.effortless.building.pattern.modifier.mirror.RadialMirror;
import dev.huskcasaca.effortless.building.replace.ReplaceMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

// removed
public record ModifierConfig(
        Array.ArraySettings arraySettings,
        Mirror.MirrorSettings mirrorSettings,
        RadialMirror.RadialMirrorSettings radialMirrorSettings,
        ReplaceMode replaceMode
) {

    public ModifierConfig() {
        this(new Array.ArraySettings(), new Mirror.MirrorSettings(), new RadialMirror.RadialMirrorSettings(), ReplaceMode.DISABLED);
    }

    public ModifierConfig(Mirror.MirrorSettings mirrorSettings, Array.ArraySettings arraySettings,
                          RadialMirror.RadialMirrorSettings radialMirrorSettings) {
        this(arraySettings, mirrorSettings, radialMirrorSettings, ReplaceMode.DISABLED);
    }

    public static ModifierConfig decodeBuf(FriendlyByteBuf friendlyByteBuf) {

        //ARRAY
        var arraySettings = new Array.ArraySettings();
        if (friendlyByteBuf.readBoolean()) {
            boolean arrayEnabled = friendlyByteBuf.readBoolean();
            var arrayOffset = new BlockPos(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
            int arrayCount = friendlyByteBuf.readInt();
            arraySettings = new Array.ArraySettings(arrayEnabled, arrayOffset, arrayCount);
        }

        //MIRROR
        var mirrorSettings = new Mirror.MirrorSettings();
        if (friendlyByteBuf.readBoolean()) {
            boolean mirrorEnabled = friendlyByteBuf.readBoolean();
            var mirrorPosition = new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble());
            boolean mirrorX = friendlyByteBuf.readBoolean();
            boolean mirrorY = friendlyByteBuf.readBoolean();
            boolean mirrorZ = friendlyByteBuf.readBoolean();
            int mirrorRadius = friendlyByteBuf.readInt();
            boolean mirrorDrawLines = friendlyByteBuf.readBoolean();
            boolean mirrorDrawPlanes = friendlyByteBuf.readBoolean();
            mirrorSettings = new Mirror.MirrorSettings(mirrorEnabled, mirrorPosition, mirrorX, mirrorY, mirrorZ, mirrorRadius,
                    mirrorDrawLines, mirrorDrawPlanes);
        }

        //RADIAL MIRROR
        var radialMirrorSettings = new RadialMirror.RadialMirrorSettings();
        if (friendlyByteBuf.readBoolean()) {
            boolean radialMirrorEnabled = friendlyByteBuf.readBoolean();
            var radialMirrorPosition = new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble());
            int radialMirrorSlices = friendlyByteBuf.readInt();
            boolean radialMirrorAlternate = friendlyByteBuf.readBoolean();
            int radialMirrorRadius = friendlyByteBuf.readInt();
            boolean radialMirrorDrawLines = friendlyByteBuf.readBoolean();
            boolean radialMirrorDrawPlanes = friendlyByteBuf.readBoolean();
            radialMirrorSettings = new RadialMirror.RadialMirrorSettings(radialMirrorEnabled, radialMirrorPosition, radialMirrorSlices, radialMirrorAlternate, radialMirrorRadius, radialMirrorDrawLines, radialMirrorDrawPlanes);
        }

        int replaceMode = friendlyByteBuf.readInt();

        return new ModifierConfig(arraySettings, mirrorSettings, radialMirrorSettings, ReplaceMode.values()[replaceMode]);
    }

    public static void write(FriendlyByteBuf friendlyByteBuf, ModifierConfig modifierSettings) {

        //ARRAY
        var arraySettings = modifierSettings.arraySettings();
        friendlyByteBuf.writeBoolean(arraySettings != null);
        if (arraySettings != null) {
            friendlyByteBuf.writeBoolean(arraySettings.enabled());
            friendlyByteBuf.writeInt(arraySettings.offset().getX());
            friendlyByteBuf.writeInt(arraySettings.offset().getY());
            friendlyByteBuf.writeInt(arraySettings.offset().getZ());
            friendlyByteBuf.writeInt(arraySettings.count());
        }

        //MIRROR
        var mirrorSettings = modifierSettings.mirrorSettings();
        friendlyByteBuf.writeBoolean(mirrorSettings != null);
        if (mirrorSettings != null) {
            friendlyByteBuf.writeBoolean(mirrorSettings.enabled());
            friendlyByteBuf.writeDouble(mirrorSettings.position().x);
            friendlyByteBuf.writeDouble(mirrorSettings.position().y);
            friendlyByteBuf.writeDouble(mirrorSettings.position().z);
            friendlyByteBuf.writeBoolean(mirrorSettings.mirrorX());
            friendlyByteBuf.writeBoolean(mirrorSettings.mirrorY());
            friendlyByteBuf.writeBoolean(mirrorSettings.mirrorZ());
            friendlyByteBuf.writeInt(mirrorSettings.radius());
            friendlyByteBuf.writeBoolean(mirrorSettings.drawLines());
            friendlyByteBuf.writeBoolean(mirrorSettings.drawPlanes());
        }

        //RADIAL MIRROR
        var radialMirrorSettings = modifierSettings.radialMirrorSettings();
        friendlyByteBuf.writeBoolean(radialMirrorSettings != null);
        if (radialMirrorSettings != null) {
            friendlyByteBuf.writeBoolean(radialMirrorSettings.enabled());
            friendlyByteBuf.writeDouble(radialMirrorSettings.position().x);
            friendlyByteBuf.writeDouble(radialMirrorSettings.position().y);
            friendlyByteBuf.writeDouble(radialMirrorSettings.position().z);
            friendlyByteBuf.writeInt(radialMirrorSettings.slices());
            friendlyByteBuf.writeBoolean(radialMirrorSettings.alternate());
            friendlyByteBuf.writeInt(radialMirrorSettings.radius());
            friendlyByteBuf.writeBoolean(radialMirrorSettings.drawLines());
            friendlyByteBuf.writeBoolean(radialMirrorSettings.drawPlanes());
        }
        friendlyByteBuf.writeInt(modifierSettings.replaceMode().ordinal());
    }

    public boolean enableReplace() {
        return replaceMode != ReplaceMode.DISABLED;
    }

    public boolean enableNormalReplace() {
        return replaceMode == ReplaceMode.NORMAL;
    }

    public boolean enableQuickReplace() {
        return replaceMode == ReplaceMode.QUICK;
    }

}
