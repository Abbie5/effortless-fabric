package dev.effortless.utils;

import com.mojang.datafixers.util.Pair;
import dev.effortless.Effortless;
import dev.effortless.building.BuildingState;
import dev.effortless.building.Context;
import dev.effortless.building.TracingResult;
import dev.effortless.building.operation.BlockBreakOperation;
import dev.effortless.building.operation.BlockPlaceOperation;
import dev.effortless.building.operation.BlockResult;
import dev.effortless.building.operation.StructureResult;
import dev.effortless.renderer.preview.OperationRenderer;
import dev.effortless.renderer.preview.result.BlockResultRenderer;
import dev.effortless.screen.ContainerOverlay;
import dev.effortless.screen.radial.RadialButton;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class OverlayHelper {

    private static List<ItemStack> reduceItemStacks(List<ItemStack> stacks) {
        var map = new HashMap<Pair<Item, CompoundTag>, ItemStack>();
        for (var stack : stacks) {
            var item = stack.getItem();
            var key = new Pair<>(item, stack.getTag());
            if (map.containsKey(key)) {
                map.get(key).grow(stack.getCount());
            } else {
                map.put(key, stack.copy());
            }
        }
        var result = new ArrayList<ItemStack>();
        for (var stack : map.values()) {
            var count = stack.getCount();
            var maxStackSize = stack.getMaxStackSize();
            while (count > 0) {
                var newStack = stack.copy();
                newStack.setCount(Math.min(count, maxStackSize));
                result.add(newStack);
                count -= maxStackSize;
            }
        }
        return result;
    }

    public static ItemStack putColorTag(ItemStack itemStack, Integer color) {
        itemStack.addTagElement("RenderColor", IntTag.valueOf(color));
        return itemStack;
    }

    public static Integer getColorTag(ItemStack itemStack) {
        return itemStack.getTag().getInt("RenderColor");
    }

    public static ItemStackSummary createSummary(StructureResult result) {
        var map = new HashMap<Group, List<ItemStack>>();
        for (var value : Group.values()) {
            map.put(value, new ArrayList<>());
        }
        for (var child : result.children()) {
            for (var value : Group.values()) {
                map.get(value).addAll(getItemStackGroups(value, child));
            }
        }

        map.replaceAll((key, value) -> reduceItemStacks(value));
        return new ItemStackSummary(result.operation().context(), map);
    }

    private static List<ItemStack> getItemStackGroups(Group group, BlockResult result) {
        switch (group) {
            case PLAYER_USED -> {
                if (result.operation() instanceof BlockPlaceOperation) {
                    var renderer = (BlockResultRenderer) OperationRenderer.getInstance().getRenderer(result.operation());
                    var color = renderer.getColor(result.result());
                    if (color != null) {
                        return result.inputs().stream().map((stack) -> putColorTag(stack, color.getRGB())).toList();
                    }
                    return Collections.emptyList();
                }
            }
            case LEVEL_DROPPED -> {
                if (result.operation() instanceof BlockBreakOperation && result.result().success()) {
                    var renderer = (BlockResultRenderer) OperationRenderer.getInstance().getRenderer(result.operation());
                    var color = renderer.getColor(result.result());
                    if (color != null) {
                        return result.outputs().stream().map((stack) -> putColorTag(stack, color.getRGB())).toList();
                    }
                    return Collections.emptyList();
                }
            }
        }
        return Collections.emptyList();
    }

    private static Component getStateComponent(BuildingState state) {
        return Component.translatable(Effortless.asKey("state", switch (state) {
                    case IDLE -> "idle";
                    case PLACE_BLOCK -> "place_block";
                    case BREAK_BLOCK -> "break_block";
                })
        );
    }

    private static Component getTracingComponent(TracingResult result) {
        return Component.translatable(Effortless.asKey("tracing", switch (result) {
                    case SUCCESS_FULFILLED -> "success_fulfilled";
                    case SUCCESS_PARTIAL -> "success_partial";
                    case PASS -> "pass";
                    case FAILED -> "failed";
                })
        );
    }

    public static void showOperationResult(UUID uuid, StructureResult result) {
        OperationRenderer.getInstance().showResult(uuid, result);
    }

    public static void showItemStackSummary(UUID uuid, StructureResult result, int priority) {
        var summary = createSummary(result);
        ContainerOverlay.getInstance().showTitledItems("placed" + uuid, Component.literal(ChatFormatting.WHITE + "Placed Blocks"), summary.group().getOrDefault(Group.PLAYER_USED, Collections.emptyList()), priority);
        ContainerOverlay.getInstance().showTitledItems("destroyed" + uuid, Component.literal(ChatFormatting.RED + "Destroyed Blocks"), summary.group().getOrDefault(Group.LEVEL_DROPPED, Collections.emptyList()), priority);
    }

    public static void showContainerContext(UUID uuid, Context context, int priority) {
        var texts = new ArrayList<Component>();
        texts.add(Component.literal(ChatFormatting.WHITE + "Structure " + ChatFormatting.GOLD + context.buildMode().getNameComponent().getString() + ChatFormatting.RESET));
        var replace = RadialButton.option(context.structureParams().replaceMode());
        texts.add(Component.literal(ChatFormatting.WHITE + replace.getCategoryComponent().getString() + " " + ChatFormatting.GOLD + replace.getNameComponent().getString() + ChatFormatting.RESET));

        for (var supportedFeature : context.buildMode().getSupportedFeatures()) {
            var option = context.buildFeatures().stream().filter((feature) -> Objects.equals(feature.getCategory(), supportedFeature.getName())).findFirst();
            if (option.isEmpty()) continue;
            var button = RadialButton.option(option.get());
            texts.add(Component.literal(ChatFormatting.WHITE + button.getCategoryComponent().getString() + " " + ChatFormatting.GOLD + button.getNameComponent().getString() + ChatFormatting.RESET));
        }

        texts.add(Component.literal(ChatFormatting.WHITE + "State" + " " + ChatFormatting.GOLD + getStateComponent(context.state()).getString()));
        texts.add(Component.literal(ChatFormatting.WHITE + "Tracing" + " " + ChatFormatting.GOLD + getTracingComponent(context.tracingResult()).getString()));

        ContainerOverlay.getInstance().showMessages("info" + uuid, texts, priority);
    }

    public enum Group {
        PLAYER_USED,
        LEVEL_DROPPED;
    }

    private record ItemStackSummary(
            Context context,
            Map<Group, List<ItemStack>> group
    ) { }
}
