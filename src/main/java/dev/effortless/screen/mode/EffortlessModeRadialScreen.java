package dev.effortless.screen.mode;

import dev.effortless.Effortless;
import dev.effortless.building.EffortlessBuilder;
import dev.effortless.building.base.MultiSelectFeature;
import dev.effortless.building.base.SingleSelectFeature;
import dev.effortless.building.history.UndoRedo;
import dev.effortless.building.mode.BuildMode;
import dev.effortless.building.replace.ReplaceMode;
import dev.effortless.building.settings.Settings;
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

@Environment(EnvType.CLIENT)
public class EffortlessModeRadialScreen extends AbstractRadialScreen {

    private static final EffortlessModeRadialScreen INSTANCE = new EffortlessModeRadialScreen();
    private static final RadialButton<UndoRedo> UNDO_OPTION = RadialButton.option(UndoRedo.UNDO);
    private static final RadialButton<UndoRedo> REDO_OPTION = RadialButton.option(UndoRedo.REDO);
    private static final RadialButton<Settings> SETTING_OPTION = RadialButton.option(Settings.MODE_SETTINGS);
    private static final RadialButton<ReplaceMode> REPLACE_OPTION = RadialButton.option(ReplaceMode.DISABLED);

    public EffortlessModeRadialScreen() {
        super(Component.translatable(Effortless.asKey("screen", "build_mode_radial")));
    }

    public static EffortlessModeRadialScreen getInstance() {
        return INSTANCE;
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
                Arrays.stream(BuildMode.values()).map(mode -> RadialSlot.mode(mode)).toList()
        );
        radial.setRadialSelectResponder(slot -> {
            selectBuildMode((BuildMode) slot.getSlot());
            updateRadialState();
        });
        radial.setRadialOptionSelectResponder(entry -> {
            if (entry.getOption() instanceof SingleSelectFeature) {
                selectBuildFeature((SingleSelectFeature) entry.getOption());
                updateRadialState();
                return;
            }
            if (entry.getOption() instanceof MultiSelectFeature) {
                selectBuildFeature((MultiSelectFeature) entry.getOption());
                updateRadialState();
            }

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
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        var context = EffortlessBuilder.getInstance().getContext(minecraft.player);

        radial.setSelectedSlots(RadialSlot.mode(context.buildMode()));
        radial.setRightButtons(
                Arrays.stream(context.buildMode().getSupportedFeatures()).map((feature) -> RadialButtonSet.of(Arrays.stream(feature.getEntries()).map(RadialButton::option).toList())).toList()
        );
        radial.setSelectedButtons(
                context.buildFeatures().stream().map(RadialButton::option).toList()
        );
    }

    private void selectBuildMode(BuildMode mode) {
        EffortlessBuilder.getInstance().setBuildMode(minecraft.player, mode);
    }

    private void selectBuildFeature(SingleSelectFeature feature) {
        EffortlessBuilder.getInstance().setBuildFeature(minecraft.player, feature);
    }

    private void selectBuildFeature(MultiSelectFeature feature) {
        EffortlessBuilder.getInstance().setBuildFeature(minecraft.player, feature);
    }

}

