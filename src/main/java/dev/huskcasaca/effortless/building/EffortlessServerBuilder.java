package dev.huskcasaca.effortless.building;

import net.minecraft.world.entity.player.Player;

public class EffortlessServerBuilder {

    private static final EffortlessServerBuilder INSTANCE = new EffortlessServerBuilder();

    public static EffortlessServerBuilder getInstance() {
        return INSTANCE;
    }

    public void perform(Player player, Context context) {
        context.getStructure(player.getLevel(), player).perform();
    }

}
