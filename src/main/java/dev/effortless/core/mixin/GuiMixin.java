package dev.effortless.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.core.event.client.ClientGuiEvent;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSavingIndicator(Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void renderGui(PoseStack poseStack, float f, CallbackInfo ci) {
        ClientGuiEvent.RENDER_GUI.invoker().onRenderGui(poseStack);
    }

}
