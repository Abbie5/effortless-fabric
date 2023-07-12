package dev.effortless.config;

import com.google.gson.annotations.Expose;
import dev.effortless.screen.ContainerOverlay;

public class PreviewConfig extends Config {

    public static final int MIN_SHADER_DISSOLVE_TIME_MULTIPLIER = 1;
    public static final int MAX_SHADER_DISSOLVE_TIME_MULTIPLIER = 40;

    @Expose
    private int itemUsagePosition = ContainerOverlay.Position.RIGHT.ordinal();
    @Expose
    private int buildInfoPosition = ContainerOverlay.Position.RIGHT.ordinal();
    @Expose
    private boolean alwaysShowBlockPreview = false;
    @Expose
    private int shaderDissolveTimeMultiplier = 10;

    public static double shaderDissolveTimeMultiplier() {
        return ConfigManager.getGlobalPreviewConfig().getShaderDissolveTimeMultiplier() * 0.1;
    }

    public boolean isShowingBuildInfo() {
        return buildInfoPosition != ContainerOverlay.Position.DISABLED.ordinal();
    }

    public ContainerOverlay.Position getBuildInfoPosition() {
        return ContainerOverlay.Position.values()[buildInfoPosition];
    }

    public void setBuildInfoPosition(ContainerOverlay.Position position) {
        this.buildInfoPosition = position.ordinal();
    }

    public boolean isShowItemUsage() {
        return itemUsagePosition != ContainerOverlay.Position.DISABLED.ordinal();
    }

    public ContainerOverlay.Position getItemUsagePosition() {
        return ContainerOverlay.Position.values()[itemUsagePosition];
    }

    public void setItemUsagePosition(ContainerOverlay.Position position) {
        this.itemUsagePosition = position.ordinal();
    }

    public boolean isAlwaysShowBlockPreview() {
        return alwaysShowBlockPreview;
    }

    public void setAlwaysShowBlockPreview(boolean alwaysShowBlockPreview) {
        this.alwaysShowBlockPreview = alwaysShowBlockPreview;
    }

    public int getShaderDissolveTimeMultiplier() {
        return shaderDissolveTimeMultiplier;
    }

    public void setShaderDissolveTimeMultiplier(int shaderDissolveTimeMultiplier) {
        this.shaderDissolveTimeMultiplier = shaderDissolveTimeMultiplier;
    }

    @Override
    public boolean isValid() {
        return shaderDissolveTimeMultiplier >= MIN_SHADER_DISSOLVE_TIME_MULTIPLIER &&
                shaderDissolveTimeMultiplier <= MAX_SHADER_DISSOLVE_TIME_MULTIPLIER;
    }

    @Override
    public void validate() {
        shaderDissolveTimeMultiplier = Math.max(MIN_SHADER_DISSOLVE_TIME_MULTIPLIER, Math.min(shaderDissolveTimeMultiplier, MAX_SHADER_DISSOLVE_TIME_MULTIPLIER));
    }
}
