package dev.effortless.screen.pattern.buildmodifier;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.Effortless;
import dev.effortless.keybinding.Keys;
import dev.effortless.screen.widget.ScrollPane;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class EffortlessModifierSettingsScreen extends Screen {

    private ScrollPane scrollPane;
    private Button buttonDone;

    private MirrorSettingsPane mirrorSettingsPane;
    private ArraySettingsPane arraySettingsPane;
    private RadialMirrorSettingsPane radialMirrorSettingsPane;

    public EffortlessModifierSettingsScreen() {
        super(Component.translatable(Effortless.asKey("screen", "modifier_settings")));
    }

    @Override
    //Create buttons and labels and add them to buttonList/labelList
    protected void init() {

        scrollPane = new ScrollPane(this, font, 8, height - 30);

        arraySettingsPane = new ArraySettingsPane(scrollPane);
        scrollPane.addListEntry(arraySettingsPane);

        mirrorSettingsPane = new MirrorSettingsPane(scrollPane);
        scrollPane.addListEntry(mirrorSettingsPane);

        radialMirrorSettingsPane = new RadialMirrorSettingsPane(scrollPane);
        scrollPane.addListEntry(radialMirrorSettingsPane);

        scrollPane.init(this.renderables);

        //Close button
        int y = height - 26;
        buttonDone = new Button(width / 2 - 100, y, 200, 20, Component.literal("Done"), (button) -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.closeContainer();
            }
        }, Button.DEFAULT_NARRATION);
        addRenderableOnly(buttonDone);
    }

    @Override
    //Process general logic, i.e. hide buttons
    public void tick() {
        scrollPane.updateScreen();

        handleMouseInput();
    }

    @Override
    //Set colors using GL11, use the fontObj field to display text
    //Use drawTexturedModalRect() to transfers areas of a texture resource to the screen
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);

        scrollPane.render(poseStack, mouseX, mouseY, partialTicks);

        buttonDone.render(poseStack, mouseX, mouseY, partialTicks);

        scrollPane.drawTooltip(poseStack, this, mouseX, mouseY);
    }


    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        scrollPane.charTyped(typedChar, keyCode);
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int p_96553_, int p_96554_) {
        if (keyCode == Keys.BUILD_MODIFIER_SETTINGS.getKeyMapping().key.getValue()) {
            return true;
        }
        return super.keyPressed(keyCode, p_96553_, p_96554_);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        renderables.forEach(renderable -> {
            if (renderable instanceof Button btn) {
                btn.mouseClicked(mouseX, mouseY, button);
            }
        });
        return scrollPane.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        if (state != 0 || !scrollPane.mouseReleased(mouseX, mouseY, state)) {
            return super.mouseReleased(mouseX, mouseY, state);
        }
        return false;
    }

    public void handleMouseInput() {
        //super.handleMouseInput();
        scrollPane.handleMouseInput();

        //Scrolling numbers
//        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
//        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
//        numberFieldList.forEach(numberField -> numberField.handleMouseInput(mouseX, mouseY));
    }

//    @Override
//    public void removed() {
//        scrollPane.onGuiClosed();
//
//        var arraySettings = arraySettingsPane.getArraySettings();
//        var mirrorSettings = mirrorSettingsPane.getMirrorSettings();
//        var radialMirrorSettings = radialMirrorSettingsPane.getRadialMirrorSettings();
//
//        var modifierSettings = EffortlessBuilder.getInstance().getModifierSettings(minecraft.player);
//
//        modifierSettings = new ModifierConfig(arraySettings, mirrorSettings, radialMirrorSettings, modifierSettings.replaceMode());
//
//        //Sanitize
//        String error = EffortlessBuilder.getInstance().getSanitizeMessage(modifierSettings, minecraft.player);
//        if (!error.isEmpty()) Effortless.log(minecraft.player, error);
//
//        modifierSettings = EffortlessBuilder.getInstance().sanitize(modifierSettings, minecraft.player);
//        EffortlessBuilder.getInstance().setModifierSettings(minecraft.player, modifierSettings);
//
//        //Send to server
//        Packets.sendToServer(new ServerboundPlayerSetBuildModifierPacket(modifierSettings));
//
//        // TODO: 17/9/22 grabMouse
////        Minecraft.getInstance().mouseHandler.grabMouse();
//    }

}
