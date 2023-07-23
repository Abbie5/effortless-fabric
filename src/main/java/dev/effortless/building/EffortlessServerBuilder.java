package dev.effortless.building;

import dev.effortless.building.operation.Operations;
import dev.effortless.network.Packets;
import dev.effortless.network.protocol.building.ClientboundPlayerBuildPacket;
import net.minecraft.world.entity.player.Player;

public class EffortlessServerBuilder {

    private static final EffortlessServerBuilder INSTANCE = new EffortlessServerBuilder();

    public static EffortlessServerBuilder getInstance() {
        return INSTANCE;
    }

    public void onContextReceived(Player player, Context context) {

        if (context.isPreview()) {
            if (player.getServer() != null) {
                for (var serverPlayer : player.getServer().getPlayerList().getPlayers()) {
                    if (serverPlayer.getUUID() == player.getUUID()) {
                        continue;
                    }
                    Packets.channel().sendToClient(new ClientboundPlayerBuildPacket(player.getUUID(), context), serverPlayer);
                }
            }
        } else {
            Operations.createStructure(player.getCommandSenderWorld(), player, context).perform();
        }
    }

}
