package dev.effortless.building;

import dev.effortless.building.operation.StructureBuildOperation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EffortlessServerBuilder {

    private static final EffortlessServerBuilder INSTANCE = new EffortlessServerBuilder();

    public static EffortlessServerBuilder getInstance() {
        return INSTANCE;
    }

    private static StructureBuildOperation generateStructureFromContext(Level level, Player player, Context context) {
        return new StructureBuildOperation(level, player, context, null);
    }

    public void perform(Player player, Context context) {
        generateStructureFromContext(player.getCommandSenderWorld(), player, context).perform();
    }

}
