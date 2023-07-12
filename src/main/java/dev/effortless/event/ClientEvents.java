package dev.effortless.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.brigadier.CommandDispatcher;
import dev.effortless.building.EffortlessBuilder;
import dev.effortless.command.BuildCommand;
import dev.effortless.command.SettingsCommand;
import dev.effortless.core.event.client.*;
import dev.effortless.core.event.lifecycle.ClientTickEvents;
import dev.effortless.render.SuperRenderTypeBuffer;
import dev.effortless.render.modifier.ModifierRenderer;
import dev.effortless.render.modifier.Shaders;
import dev.effortless.render.outliner.OutlineRenderer;
import dev.effortless.render.preview.OperationRenderer;
import dev.effortless.screen.ContainerOverlay;
import dev.effortless.utils.AnimationTicker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ClientEvents {

    public static void onStartTick(Minecraft minecraft) {
        EffortlessBuilder.getInstance().tick();
        OperationRenderer.getInstance().tick();
        OutlineRenderer.getInstance().tick();
        ContainerOverlay.getInstance().tick();

        AnimationTicker.getInstance().tick();
    }

    public static void onEndTick(Minecraft minecraft) {
    }

    public static void onScreenOpening(@Nullable Screen screen) {
    }

    public static void onRenderGui(PoseStack poseStack) {
        ContainerOverlay.getInstance().renderGuiOverlay(poseStack);
    }

    public static void onRenderAfterEntities(WorldRenderContext context) {
    }

    public static void onRenderEnd(WorldRenderContext context) {

        var poseStack = context.poseStack();
        var partialTicks = AnimationTicker.getInstance().getPartialTicks();
        var buffer = SuperRenderTypeBuffer.getInstance();
        var camera = context.camera();

        var bufferBuilder = Tesselator.getInstance().getBuilder();
        var bufferSource = MultiBufferSource.immediate(bufferBuilder);
        ModifierRenderer.getInstance().render(poseStack, bufferSource, camera);

        OperationRenderer.getInstance().renderOperationResults(poseStack, buffer, partialTicks);
        OutlineRenderer.getInstance().renderOutlines(poseStack, buffer, partialTicks);
        buffer.draw();
    }

    public static void onRegisterShader(ResourceProvider provider, ClientShaderEvent.ShaderRegister.ShadersSink sink) throws IOException {
        Shaders.registerShaders(provider, sink);
    }

    public static void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        SettingsCommand.register(dispatcher);
        BuildCommand.register(dispatcher, commandBuildContext);
    }

    public static InteractionResult onPlayerStartAttack(LocalPlayer player) {
        var context = EffortlessBuilder.getInstance().getContext(player);
        if (context.isDisabled()) {
            return InteractionResult.PASS;
        }

        var minecraft = Minecraft.getInstance();
        var hitResult = minecraft.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            return InteractionResult.PASS;
        }

        if (player.isHandsBusy()) {
            return InteractionResult.FAIL;
        }

        EffortlessBuilder.getInstance().handlePlayerBreak(player); // consumed
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult onPlayerStartUseItem(Player player) {
        var context = EffortlessBuilder.getInstance().getContext(player);
        if (context.isDisabled()) {
            return InteractionResult.PASS;
        }

        var minecraft = Minecraft.getInstance();

        var hitResult = minecraft.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            return InteractionResult.PASS;
        }

        EffortlessBuilder.getInstance().handlePlayerPlace(player); // consumed else pass
        return InteractionResult.SUCCESS;
    }

    public static boolean onPlayerContinueAttack(Player player) {
        var context = EffortlessBuilder.getInstance().getContext(player);
        return !context.isDisabled(); // pass
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
        ClientPlayerEvent.START_USE.register(ClientEvents::onPlayerStartUseItem);
        ClientPlayerEvent.CONTINUE_ATTACK.register(ClientEvents::onPlayerContinueAttack);
    }

}