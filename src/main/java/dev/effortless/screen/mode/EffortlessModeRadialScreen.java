package dev.effortless.screen.mode;

import dev.effortless.Effortless;
import dev.effortless.building.EffortlessBuilder;
import dev.effortless.building.history.UndoRedo;
import dev.effortless.building.mode.BuildFeature;
import dev.effortless.building.mode.BuildMode;
import dev.effortless.building.mode.BuildOption;
import dev.effortless.building.replace.ReplaceMode;
import dev.effortless.building.settings.SettingType;
import dev.effortless.keybinding.Keys;
import dev.effortless.screen.radial.AbstractRadialScreen;
import dev.effortless.screen.radial.RadialButton;
import dev.effortless.screen.radial.RadialButtonSet;
import dev.effortless.screen.radial.RadialSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.IntFunction;

@Environment(EnvType.CLIENT)
public class EffortlessModeRadialScreen extends AbstractRadialScreen {

    private static final EffortlessModeRadialScreen INSTANCE = new EffortlessModeRadialScreen();
    private static final RadialButton<UndoRedo> UNDO_OPTION = RadialButton.option(UndoRedo.UNDO);
    private static final RadialButton<UndoRedo> REDO_OPTION = RadialButton.option(UndoRedo.REDO);
    private static final RadialButton<SettingType> SETTING_OPTION = RadialButton.option(SettingType.MODE_SETTINGS);
    private static final RadialButton<ReplaceMode> REPLACE_OPTION = RadialButton.option(ReplaceMode.DISABLED);

    public EffortlessModeRadialScreen() {
        super(Component.translatable(String.join(".", Effortless.MOD_ID, "screen", "build_mode_radial")));
    }

    public static EffortlessModeRadialScreen getInstance() {
        return INSTANCE;
    }

    private static RadialSlot<BuildMode> createRadialSlot(BuildMode mode) {
        return RadialSlot.mode(mode);
    }

    private static RadialButtonSet[] createRadialButtonSetArray(BuildFeature[] features) {
        return Arrays.stream(features).map((feature) -> RadialButtonSet.of(createRadialButtons(feature.getEntries()))).toArray(RadialButtonSet[]::new);
    }

    private static RadialButton<BuildOption>[] createRadialButtons(BuildOption[] entries) {
        return Arrays.stream(entries).map(RadialButton::option).toArray((IntFunction<RadialButton<BuildOption>[]>) RadialButton[]::new);
    }

    public boolean isVisible() {
        return Minecraft.getInstance().screen instanceof EffortlessModeRadialScreen;
    }

    @Override
    protected void init() {
        super.init();

        radial.setLeftButtons(
                RadialButtonSet.of(REDO_OPTION, UNDO_OPTION),
                RadialButtonSet.of(SETTING_OPTION, REPLACE_OPTION)
        );
        radial.setRadialSlots(
                Arrays.stream(BuildMode.values()).map(EffortlessModeRadialScreen::createRadialSlot).toArray(RadialSlot[]::new)
        );
        radial.setRadialSelectResponder(slot -> {
            selectBuildMode((BuildMode) slot.getSlot());
            updateRadialState();
        });
        radial.setRadialOptionSelectResponder(entry -> {
            if (entry instanceof BuildFeature.Entry) {
                selectBuildFeature((BuildFeature.Entry) entry.getOption());
            } else {

            }
            updateRadialState();
        });
        updateRadialState();
    }

    @Override
    public void tick() {
        if (!Keys.BUILD_MODE_RADIAL.isKeyDown()) {
            onClose();
        }
    }

    private void updateRadialState() {
        var context = EffortlessBuilder.getInstance().getContext(minecraft.player);
        radial.setSelectedSlots(createRadialSlot(context.buildMode()));
        radial.setRightButtons(createRadialButtonSetArray(context.buildMode().getSupportedFeatures()));
        radial.setSelectedButtons(createRadialButtons(context.buildFeatures()));
    }

    private void selectBuildMode(BuildMode mode) {
        EffortlessBuilder.getInstance().setBuildMode(minecraft.player, mode);
    }

    private void selectBuildFeature(BuildFeature.Entry feature) {
        EffortlessBuilder.getInstance().setBuildFeature(minecraft.player, feature);
    }

}

