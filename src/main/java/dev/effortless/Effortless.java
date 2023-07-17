package dev.effortless;

import dev.effortless.event.CommonEvents;
import dev.effortless.network.Packets;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Effortless implements ModInitializer {

    private static final String MOD_ID = "effortless";
    private static final Logger logger = LogManager.getLogger();

    public static void log(String msg) {
        logger.info(msg);
    }

    public static void log(Player player, String msg) {
        log(player, msg, false);
    }

    public static void log(Player player, String msg, boolean actionBar) {
        player.displayClientMessage(Component.literal(msg), actionBar);
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static String asKey(String... path) {
        return String.join(".", MOD_ID, String.join(".", path));
    }

    @Override
    public void onInitialize() {
        CommonEvents.register();
        Packets.registerServer();
    }

}
