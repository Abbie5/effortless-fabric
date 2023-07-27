package dev.effortless.building.operation;

import dev.effortless.building.Context;
import dev.effortless.building.Storage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class Operations {

    public static StructureBuildOperation createStructure(Level level, Player player, Context context) {
        return new StructureBuildOperation(level, player, context);
    }

    public static StructureBuildOperation createStructurePreview(Player player, Context context) {
        var storage = Storage.createTemp(player.getInventory().items);
        return new StructureBuildOperation(player.level(), player, context.withPreviewSource(), storage);
    }

    public static StructureBuildOperation createStructurePreviewOnce(Player player, Context context) {
        var storage = Storage.createTemp(player.getInventory().items);
        return new StructureBuildOperation(player.level(), player, context.withPreviewOnceSource(), storage);
    }


}
