package dev.effortless.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.screen.ContainerOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubtitleOverlay.class)
public abstract class SubtitleOverlayMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", shift = At.Shift.AFTER))
    private void onScreenOpening(PoseStack poseStack, CallbackInfo ci) {
        var overlapped = ContainerOverlay.rightTipsHeight() - 2 * 10;
        if (overlapped > 0) {
            poseStack.translate(0, -overlapped, 0);
        }
    }

}
