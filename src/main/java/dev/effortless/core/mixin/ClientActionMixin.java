package dev.effortless.core.mixin;

import dev.effortless.core.event.client.ClientPlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class ClientActionMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "startAttack", at = @At(value = "HEAD"), cancellable = true)
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        var result = ClientPlayerEvent.START_ATTACH.invoker().onStartAttack(player);
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(result.consumesAction());
        }
    }

    @Inject(method = "startUseItem", at = @At(value = "HEAD"), cancellable = true)
    private void onStartUseItem(CallbackInfo ci) {
        var result = ClientPlayerEvent.START_USE.invoker().onStartUse(player);
        if (result != InteractionResult.PASS) {
            ci.cancel();
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void onContinueAttack(boolean bl, CallbackInfo ci) {
        if (ClientPlayerEvent.CONTINUE_ATTACK.invoker().onContinueAttack(player)) {
            ci.cancel();
        }
    }

}
