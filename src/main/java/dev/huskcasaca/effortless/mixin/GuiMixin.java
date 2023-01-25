package dev.huskcasaca.effortless.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.gui.BuildInfoOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    private BuildInfoOverlay buildInfoOverlay;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/SubtitleOverlay;<init>(Lnet/minecraft/client/Minecraft;)V", shift = At.Shift.AFTER))
    private void renderGui(Minecraft minecraft, ItemRenderer itemRenderer, CallbackInfo ci) {
        buildInfoOverlay = new BuildInfoOverlay(minecraft);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSavingIndicator(Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void renderGui(PoseStack poseStack, float f, CallbackInfo ci) {
        buildInfoOverlay.render(poseStack);
    }

}
