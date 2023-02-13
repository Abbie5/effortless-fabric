package dev.huskcasaca.effortless;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.huskcasaca.effortless.building.ReachHelper;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.buildmodifier.UndoRedo;
import dev.huskcasaca.effortless.control.Keys;
import dev.huskcasaca.effortless.event.ClientReloadShadersEvent;
import dev.huskcasaca.effortless.event.ClientScreenEvent;
import dev.huskcasaca.effortless.event.ClientScreenInputEvent;
import dev.huskcasaca.effortless.network.Packets;
import dev.huskcasaca.effortless.network.protocol.player.ServerboundPlayerSetBuildModePacket;
import dev.huskcasaca.effortless.randomizer.RandomizerSettings;
import dev.huskcasaca.effortless.render.BuildRenderTypes;
import dev.huskcasaca.effortless.render.SuperRenderTypeBuffer;
import dev.huskcasaca.effortless.render.modifier.ModifierRenderer;
import dev.huskcasaca.effortless.render.outliner.OutlineRenderer;
import dev.huskcasaca.effortless.render.preview.BlockPreviewRenderer;
import dev.huskcasaca.effortless.screen.buildmode.BuildModeMenuScreen;
import dev.huskcasaca.effortless.screen.buildmode.PlayerSettingsScreen;
import dev.huskcasaca.effortless.screen.buildmodifier.ModifierSettingsScreen;
import dev.huskcasaca.effortless.screen.config.EffortlessConfigScreen;
import dev.huskcasaca.effortless.screen.randomizer.RandomizerConfigScreen;
import dev.huskcasaca.effortless.utils.AnimationTicker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public class EffortlessClient implements ClientModInitializer {

    // TODO: 28/1/23 move
    public static HitResult previousLookAt;
    public static HitResult currentLookAt;
    private static int ticksInGame = 0;

    public static void onStartClientTick(Minecraft client) {
        //Update previousLookAt
        HitResult objectMouseOver = Minecraft.getInstance().hitResult;
        //Checking for null is necessary! Even in vanilla when looking down ladders it is occasionally null (instead of Type MISS)
        if (objectMouseOver == null) return;

        if (currentLookAt == null) {
            currentLookAt = objectMouseOver;
            previousLookAt = objectMouseOver;
            return;
        }

        if (objectMouseOver.getType() == HitResult.Type.BLOCK) {
            if (currentLookAt.getType() != HitResult.Type.BLOCK) {
                currentLookAt = objectMouseOver;
                previousLookAt = objectMouseOver;
            } else {
                if (((BlockHitResult) currentLookAt).getBlockPos() != ((BlockHitResult) objectMouseOver).getBlockPos()) {
                    previousLookAt = currentLookAt;
                    currentLookAt = objectMouseOver;
                }
            }
        }

        OutlineRenderer.getInstance().tick();
        AnimationTicker.tick();

    }

    public static void onEndClientTick(Minecraft client) {
        Screen gui = client.screen;
        if (gui == null || !gui.isPauseScreen()) {
            ticksInGame++;
        }
    }

    public static void onKeyPress(int key, int scanCode, int action, int modifiers) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
        if (Keys.BUILD_MODE_MENU.isDown()) {
            showBuildModelMenu();
        }
        if (Keys.BUILD_MODIFIER_MENU.isDown()) {
        }
        if (Keys.ITEM_RANDOMIZER_MENU.isDown()) {
        }
//        // remember to send packet to server if necessary
        if (Keys.BUILD_MODE_SETTINGS.getKeyMapping().consumeClick()) {
//            openModeSettings();
        }
        if (Keys.BUILD_MODIFIER_SETTINGS.getKeyMapping().consumeClick()) {
            openBuildModifierSettings();
        }
        if (Keys.ITEM_RANDOMIZER_SETTINGS.getKeyMapping().consumeClick()) {
            openItemRandomizerSettings();
        }
        if (Keys.UNDO.getKeyMapping().consumeClick()) {
            UndoRedo.undo(player);
        }
        if (Keys.REDO.getKeyMapping().consumeClick()) {
            UndoRedo.undo(player);
        }
        if (Keys.SETTINGS.getKeyMapping().consumeClick()) {
            openSettings();
        }
//        if (Keys.CYCLE_REPLACE_MODE.getKeyMapping().consumeClick()) {
//            BuildModifierHelper.cycleReplaceMode(player);
//        }
        if (Keys.TOGGLE_REPLACE.getKeyMapping().consumeClick()) {
            BuildModifierHelper.cycleReplaceMode(player);
        }
//        if (Keys.TOGGLE_QUICK_REPLACE.getKeyMapping().consumeClick()) {
//            BuildModifierHelper.toggleQuickReplaceMode(player);
//        }
    }

    public static void showBuildModelMenu() {
        if (!BuildModeMenuScreen.getInstance().isVisible()) {
            Minecraft.getInstance().setScreen(BuildModeMenuScreen.getInstance());
        }
    }

    public static void openBuildModifierSettings() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;

        //Disabled if max reach is 0, might be set in the config that way.
        if (ReachHelper.getMaxReachDistance(player) == 0) {
            Effortless.log(player, "Build modifiers are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
        } else {
            mc.setScreen(null);
            mc.setScreen(new ModifierSettingsScreen());
        }
    }

    public static void openItemRandomizerSettings() {
        Minecraft.getInstance().setScreen(new RandomizerConfigScreen(
                Minecraft.getInstance().screen,
                (settings) -> {},
                RandomizerSettings.getSamples()
        ));
    }

    public static void openPlayerSettings() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new PlayerSettingsScreen());

    }

    public static void openSettings() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(EffortlessConfigScreen.createConfigScreen(mc.screen));

    }

    public static void onScreenEvent(Screen screen) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            var modeSettings = BuildModeHelper.getModeSettings(player);
            BuildModeHelper.setModeSettings(player, modeSettings);
            BuildModeHandler.reset(player);
            Packets.sendToServer(new ServerboundPlayerSetBuildModePacket(modeSettings));
        }
    }

    protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluid) {
        float f = player.getXRot();
        float g = player.getYRot();
        var vec3 = player.getEyePosition();
        float h = Mth.cos(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float i = Mth.sin(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float j = -Mth.cos(-f * ((float) Math.PI / 180));
        float k = Mth.sin(-f * ((float) Math.PI / 180));
        float l = i * j;
        float m = k;
        float n = h * j;
        double d = 5.0;
        var vec32 = vec3.add((double) l * 5.0, (double) m * 5.0, (double) n * 5.0);
        return level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, fluid, player));
    }

    public static HitResult getLookingAt(Player player) {
        var level = player.level;

        //base distance off of player ability (config)
        float raytraceRange = ReachHelper.getPlacementReach(player) * 4;

        var look = player.getLookAngle();
        var start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        var end = new Vec3(player.getX() + look.x * raytraceRange, player.getY() + player.getEyeHeight() + look.y * raytraceRange, player.getZ() + look.z * raytraceRange);
//        return player.rayTrace(raytraceRange, 1f, RayTraceFluidMode.NEVER);
        //TODO 1.14 check if correct
        return level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    public static void registerShaders(ResourceProvider resourceProvider, ClientReloadShadersEvent.ShaderRegister.ShadersSink sink) throws IOException {
        sink.registerShader(
                // TODO: 10/9/22 use custom namespace
                new ShaderInstance(resourceProvider, "dissolve", DefaultVertexFormat.BLOCK),
                (shaderInstance) -> BuildRenderTypes.setDissolveShaderInstance(shaderInstance)
        );
    }

    public static int getTicksInGame() {
        return ticksInGame;
    }

    public static void renderBlockPreview(PoseStack poseStack, Camera camera) {
        var bufferBuilder = Tesselator.getInstance().getBuilder();
        var bufferSource = MultiBufferSource.immediate(bufferBuilder);

        BlockPreviewRenderer.getInstance().render(poseStack, bufferSource);
    }

    public static void renderModifierSettings(PoseStack poseStack, Camera camera) {
        var bufferBuilder = Tesselator.getInstance().getBuilder();
        var bufferSource = MultiBufferSource.immediate(bufferBuilder);

        ModifierRenderer.getInstance().render(poseStack, bufferSource, camera);
    }

    public static void renderBlockOutlines(PoseStack poseStack, Camera camera) {
        var cameraPos = camera.getPosition();
        var partialTicks = AnimationTicker.getPartialTicks();
        poseStack.pushPose();
//        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        var buffer = SuperRenderTypeBuffer.getInstance();
        OutlineRenderer.getInstance().renderOutlines(poseStack, buffer, partialTicks);
        buffer.draw();
        poseStack.popPose();
    }

    @Override
    public void onInitializeClient() {
        // register key bindings
        Keys.register();

        ClientScreenEvent.SCREEN_OPENING_EVENT.register(EffortlessClient::onScreenEvent);

        ClientScreenInputEvent.KEY_PRESS_EVENT.register(EffortlessClient::onKeyPress);

        ClientTickEvents.START_CLIENT_TICK.register(EffortlessClient::onStartClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(EffortlessClient::onEndClientTick);

        ClientReloadShadersEvent.REGISTER_SHADER.register(EffortlessClient::registerShaders);

        WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
            renderBlockPreview(context.matrixStack(), context.camera());
        });
        WorldRenderEvents.END.register((context) -> {
            renderModifierSettings(context.matrixStack(), context.camera());
            renderBlockOutlines(context.matrixStack(), context.camera());
        });


    }



}
