package dev.huskcasaca.effortless.utils;

import dev.huskcasaca.effortless.randomizer.ItemProbability;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class RandomizerUtils {

    public static List<Component> getRandomizerEntryTooltip(ItemProbability holder, int totalCount) {

        var components = holder.singleItemStack().getTooltipLines(Minecraft.getInstance().player, TooltipFlag.ADVANCED.asCreative());
        var percentage = String.format("%.2f%%", 100.0 * holder.count() / totalCount);
        components.add(
                Component.empty()
        );
        components.add(
                Component.literal(ChatFormatting.GRAY + "Total Probability: " +  ChatFormatting.GOLD + percentage + ChatFormatting.DARK_GRAY + " (" + holder.count() + "/" + totalCount + ")" + ChatFormatting.RESET)
        );
        return components;
    }
}
