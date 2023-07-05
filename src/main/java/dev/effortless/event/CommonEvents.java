package dev.effortless.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class CommonEvents {

    public static void onPlayerLogin(ServerPlayer player) {
//        EffortlessBuilder.getInstance().handleNewPlayer(player);
//        EffortlessBuilder.getInstance().handleNewPlayer(player);
//        ReachHelper.handleNewPlayer(player);
    }

    public static void onPlayerLogout(ServerPlayer player) {
//        UndoRedoProvider.clear(player);
//        // FIXME: 18/11/22
////        Packets.sendToClient(new ClearUndoMessage(), player);
    }

    public static void onPlayerRespawn(ServerPlayer player) {
//        EffortlessBuilder.getInstance().handleNewPlayer(player);
//        EffortlessBuilder.getInstance().handleNewPlayer(player);
//        ReachHelper.handleNewPlayer(player);
    }

    public static void onPlayerChangedDimension(ServerLevel level, ServerPlayer player) {
////        //Set build mode to normal
//        var modeSettings = EffortlessBuilder.getInstance().getModeSettings(player);
//        modeSettings = new ModeConfig(
//                BuildMode.DISABLED,
//                modeSettings.enableMagnet()
//        );
//        EffortlessBuilder.getInstance().setModeSettings(player, modeSettings);
//
//        var modifierSettings = EffortlessBuilder.getInstance().getModifierSettings(player);
//        modifierSettings = new ModifierConfig();
//
//        EffortlessBuilder.getInstance().setModifierSettings(player, modifierSettings);
//
//        EffortlessBuilder.getInstance().handleNewPlayer(player);
//        EffortlessBuilder.getInstance().handleNewPlayer(player);
//        ReachHelper.handleNewPlayer(player);
//
//        UndoRedoProvider.clear(player);
//        // FIXME: 18/11/22
////        Packets.sendToClient(new ClearUndoMessage(), player);
    }

    //
    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
//        EffortlessBuilder.getInstance().setModifierSettings(newPlayer, EffortlessBuilder.getInstance().getModifierSettings(oldPlayer));
//        EffortlessBuilder.getInstance().setModeSettings(newPlayer, EffortlessBuilder.getInstance().getModeSettings(oldPlayer));
//        ReachHelper.setReachSettings(newPlayer, ReachHelper.getReachSettings(oldPlayer));
    }

    public static void register() {
//        ServerPlayerListEvent.LOGIN.register(CommonEvents::onPlayerLogin);
//        ServerPlayerListEvent.LOGOUT.register(CommonEvents::onPlayerLogout);
//        ServerPlayerListEvent.RESPAWN.register(CommonEvents::onPlayerRespawn);
//
//        ServerPlayerEvent.CHANGE_DIMENSION.register(CommonEvents::onPlayerChangedDimension);
//        ServerPlayerEvent.CLONE.register(CommonEvents::onPlayerClone);
    }


}
