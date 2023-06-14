package dev.huskcasaca.effortless.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.brigadier.CommandDispatcher;
import dev.huskcasaca.effortless.building.EffortlessBuilder;
import dev.huskcasaca.effortless.command.BuildCommand;
import dev.huskcasaca.effortless.command.SettingsCommand;
import dev.huskcasaca.effortless.core.event.client.*;
import dev.huskcasaca.effortless.core.event.lifecycle.ClientTickEvents;
import dev.huskcasaca.effortless.render.SuperRenderTypeBuffer;
import dev.huskcasaca.effortless.render.modifier.ModifierRenderer;
import dev.huskcasaca.effortless.render.modifier.Shaders;
import dev.huskcasaca.effortless.render.outliner.OutlineRenderer;
import dev.huskcasaca.effortless.render.preview.StructurePreviewRenderer;
import dev.huskcasaca.effortless.screen.BuildInfoOverlay;
import dev.huskcasaca.effortless.utils.AnimationTicker;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ClientEvents {

    public static void onStartTick(Minecraft minecraft) {
        EffortlessBuilder.getInstance().tick();
        OutlineRenderer.getInstance().tick();
        AnimationTicker.tick();
    }

    public static void onEndTick(Minecraft minecraft) {
    }

    public static void onScreenOpening(@Nullable Screen screen) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            EffortlessBuilder.getInstance().setIdle(player);
        }
    }

    public static void onRenderGui(PoseStack poseStack) {
        BuildInfoOverlay.getInstance().render(poseStack);
    }


    public static void onRenderAfterEntities(WorldRenderContext context) {
        renderBlockPreview(context.poseStack(), context.camera());
    }

    public static void onRenderEnd(WorldRenderContext context) {
        renderModifierSettings(context.poseStack(), context.camera());
        renderBlockOutlines(context.poseStack(), context.camera());
    }


    public static void renderBlockPreview(PoseStack poseStack, Camera camera) {
        var bufferBuilder = Tesselator.getInstance().getBuilder();
        var bufferSource = MultiBufferSource.immediate(bufferBuilder);

        StructurePreviewRenderer.getInstance().render(poseStack, bufferSource);
    }

    public static void renderModifierSettings(PoseStack poseStack, Camera camera) {
        var bufferBuilder = Tesselator.getInstance().getBuilder();
        var bufferSource = MultiBufferSource.immediate(bufferBuilder);

        ModifierRenderer.getInstance().render(poseStack, bufferSource, camera);
    }

    public static void renderBlockOutlines(PoseStack poseStack, Camera camera) {
        var partialTicks = AnimationTicker.getPartialTicks();
        poseStack.pushPose();
        var buffer = SuperRenderTypeBuffer.getInstance();
        OutlineRenderer.getInstance().renderOutlines(poseStack, buffer, partialTicks);
        buffer.draw();
        poseStack.popPose();
    }

    public static void onRegisterShader(ResourceProvider provider, ClientShaderEvent.ShaderRegister.ShadersSink sink) throws IOException {
        Shaders.registerShaders(provider, sink);
    }

    public static void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        SettingsCommand.register(dispatcher);
        BuildCommand.register(dispatcher, commandBuildContext);
    }

    public static Boolean onPlayerStartAttack(LocalPlayer player) {
        var context = EffortlessBuilder.getInstance().getContext(player);
        if (context.isDisabled()) return null; // pass

        var minecraft = Minecraft.getInstance();
        var hitResult = minecraft.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) return null; // pass

        if (minecraft.missTime > 0) return false; // consumed
        if (player.isHandsBusy()) return false; // consumed

        EffortlessBuilder.getInstance().handlePlayerBreak(player); // consumed
        return true;
    }

    public static boolean onPlayerContinueAttack(Player player) {
        var context = EffortlessBuilder.getInstance().getContext(player);
        if (context.isDisabled()) return false; // pass
        return true; // consumed
    }

    public static boolean onPlayerStartUseItem(Player player) {
        var context = EffortlessBuilder.getInstance().getContext(player);
        if (context.isDisabled()) return false; // pass

        var minecraft = Minecraft.getInstance();

        var hitResult = minecraft.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) return false; // pass

        EffortlessBuilder.getInstance().handlePlayerPlace(player); // consumed else pass
        return true;
    }

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(ClientEvents::onStartTick);
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onEndTick);

        ClientScreenEvent.OPENING.register(ClientEvents::onScreenOpening);

        ClientGuiEvent.RENDER_GUI.register(ClientEvents::onRenderGui);

        ClientShaderEvent.REGISTER_SHADER.register(ClientEvents::onRegisterShader);

        WorldRenderEvents.AFTER_ENTITIES.register(ClientEvents::onRenderAfterEntities);
        WorldRenderEvents.END.register(ClientEvents::onRenderEnd);

        ClientCommandEvent.REGISTER.register(ClientEvents::onRegisterCommands);

        ClientPlayerEvent.START_ATTACH.register(ClientEvents::onPlayerStartAttack);
        ClientPlayerEvent.CONTINUE_ATTACK.register(ClientEvents::onPlayerContinueAttack);
        ClientPlayerEvent.START_USE_ITEM.register(ClientEvents::onPlayerStartUseItem);
    }

}
