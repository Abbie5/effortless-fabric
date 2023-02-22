package dev.huskcasaca.effortless.screen.buildmode;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmode.BuildModeOptions;
import dev.huskcasaca.effortless.buildmode.NewBuildMode;
import dev.huskcasaca.effortless.control.Keys;
import dev.huskcasaca.effortless.screen.radial.AbstractRadialScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EffortlessModeRadialScreen extends AbstractRadialScreen {

    private static final EffortlessModeRadialScreen INSTANCE = new EffortlessModeRadialScreen();

    public EffortlessModeRadialScreen() {
        super(Component.translatable(String.join(".", Effortless.MOD_ID, "screen", "build_mode_radial")));
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
        radial.setFixedOptions(
                List.of(BuildModeOptions.UNDO_REDO, BuildModeOptions.SETTINGS)
        );
        radial.setRadialSelectResponder((slot) -> {
            if (slot instanceof NewBuildMode) {
                radial.setLocalOptions(((NewBuildMode) slot).getOptionSets());
            }
        });
        radial.setRadialSlots(
                Arrays.asList(NewBuildMode.values())
        );
    }

    @Override
    public void tick() {
        if (!Keys.BUILD_MODE_RADIAL.isKeyDown()) {
            onClose();
        }
    }

}

