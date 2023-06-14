package dev.huskcasaca.effortless.building;

import dev.huskcasaca.effortless.Effortless;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuildingContextProvider {

    private final Map<UUID, BuildContext> contexts = new HashMap<>();

    public static BuildContext defaultContext() {
        return BuildContext.defaultSet();
    }

    public BuildContext get(Player player) {
        return contexts.computeIfAbsent(player.getUUID(), (uuid) -> defaultContext());
    }

    public void set(Player player, BuildContext context) {
        contexts.put(player.getUUID(), context);
        Effortless.log("setBuildContext: " + player.getUUID() + " to " + context);
    }

    public void remove(Player player) {
        contexts.remove(player.getUUID());
    }

    public void tick() {

    }

}
