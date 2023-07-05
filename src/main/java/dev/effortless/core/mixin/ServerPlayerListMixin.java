package dev.effortless.core.mixin;

import dev.effortless.core.event.common.ServerPlayerListEvent;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class ServerPlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void onPlayerLogin(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
        ServerPlayerListEvent.LOGIN.invoker().onLogin(serverPlayer);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerLogout(ServerPlayer serverPlayer, CallbackInfo ci) {
        ServerPlayerListEvent.LOGOUT.invoker().onLogout(serverPlayer);
    }

    @Inject(method = "respawn", at = @At("RETURN"))
    private void onPlayerRespawn(ServerPlayer serverPlayer, boolean bl, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayerListEvent.RESPAWN.invoker().onRespawn(cir.getReturnValue());
    }

}
