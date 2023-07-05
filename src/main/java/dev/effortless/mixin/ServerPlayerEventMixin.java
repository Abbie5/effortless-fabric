package dev.effortless.mixin;

import dev.effortless.core.event.common.ServerPlayerEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEventMixin {

    @Inject(method = "changeDimension", at = @At("RETURN"))
    private void onPlayerChangeDimension(ServerLevel serverLevel, CallbackInfoReturnable<Entity> cir) {
        ServerPlayerEvent.CHANGE_DIMENSION.invoker().onChangeDimension(serverLevel, (ServerPlayer) cir.getReturnValue());
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void onPlayerRestoreFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEvent.CLONE.invoker().onClone((ServerPlayer) (Object) this, oldPlayer, alive);
    }

}
