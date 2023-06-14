package dev.huskcasaca.effortless.mixin;

import dev.huskcasaca.effortless.core.event.client.ClientPlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class PlayerActionMixin {

    @Shadow @Nullable public LocalPlayer player;

    // TODO: 15/9/22 extract to EffortlessClient class
    // startAttack
    @Inject(method = "startAttack", at = @At(value = "HEAD"), cancellable = true)
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        var result = ClientPlayerEvent.START_ATTACH.invoker().onStartAttach(player);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void onContinueAttack(boolean bl, CallbackInfo ci) {
        if (ClientPlayerEvent.CONTINUE_ATTACK.invoker().onContinueAttack(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At(value = "HEAD"), cancellable = true)
    private void onStartUseItem(CallbackInfo ci) {
        if (ClientPlayerEvent.START_USE_ITEM.invoker().onStartUseItem(player)) {
            ci.cancel();
        }
    }

}
