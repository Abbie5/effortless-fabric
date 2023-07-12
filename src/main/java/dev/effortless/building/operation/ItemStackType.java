package dev.effortless.building.operation;

import net.minecraft.ChatFormatting;

public enum ItemStackType {
    SUCCESS(ChatFormatting.WHITE.getColor()),
    FAILURE(ChatFormatting.RED.getColor()),
    UNCERTAIN(ChatFormatting.GRAY.getColor());

    private final int color;

    ItemStackType(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
