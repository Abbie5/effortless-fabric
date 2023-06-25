package dev.huskcasaca.effortless.building;

import dev.huskcasaca.effortless.Effortless;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuildingContextProvider {

    private final Map<UUID, Context> contexts = new HashMap<>();

    public static Context defaultContext() {
        return Context.defaultSet();
    }

    public Context get(Player player) {
        return contexts.computeIfAbsent(player.getUUID(), (uuid) -> defaultContext());
    }

    public void set(Player player, Context context) {
        contexts.put(player.getUUID(), context);
        Effortless.log("setContext: " + player.getUUID() + " to " + context);
    }

    public void remove(Player player) {
        contexts.remove(player.getUUID());
    }

    public void tick() {

    }

}
