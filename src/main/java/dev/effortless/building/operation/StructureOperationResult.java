package dev.effortless.building.operation;

import com.mojang.datafixers.util.Pair;
import dev.effortless.building.TracingResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record StructureOperationResult(
        StructureOperation operation,
        TracingResult result,
        List<SingleBlockOperationResult> children
) implements OperationResult<StructureOperationResult> {

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

    public ItemStackSummary summary() {
        return new ItemStackSummary(operation().context(),
                Map.of(
                        ItemStackType.SUCCESS, reduceItemStacks(children.stream().flatMap(r -> r.result().consumesAction() ? r.inventoryConsumed().stream() : Stream.empty()).toList()),
                        ItemStackType.FAILURE, reduceItemStacks(children.stream().flatMap(r -> !r.result().consumesAction() ? r.inventoryConsumed().stream() : Stream.empty()).toList())),
                Map.of(
                        ItemStackType.SUCCESS, reduceItemStacks(children.stream().flatMap(r -> r.result().consumesAction() ? r.inventoryPicked().stream() : Stream.empty()).toList()),
                        ItemStackType.FAILURE, reduceItemStacks(children.stream().flatMap(r -> !r.result().consumesAction() ? r.inventoryPicked().stream() : Stream.empty()).toList())),
                Map.of(
                        ItemStackType.SUCCESS, reduceItemStacks(children.stream().flatMap(r -> r.result().consumesAction() ? r.levelConsumed().stream() : Stream.empty()).toList()),
                        ItemStackType.FAILURE, reduceItemStacks(children.stream().flatMap(r -> !r.result().consumesAction() ? r.levelConsumed().stream() : Stream.empty()).toList())),
                Map.of(
                        ItemStackType.FAILURE, reduceItemStacks(children.stream().flatMap(r -> r.result().consumesAction() ? r.levelDropped().stream() : Stream.empty()).toList())));
//                        ItemStackType.FAILURE, reduceItemStacks(result.stream().flatMap(r -> !r.result().consumesAction() ? r.levelDropped().stream() : Stream.empty()).toList())));
    }

    public Vec3i size() {
        if (operation().context().blockHitResults().isEmpty()) {
            return Vec3i.ZERO;
        }

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (var hitResult : operation().context().blockHitResults()) {
            var blockPos = hitResult.getBlockPos();
            if (blockPos.getX() < minX) minX = blockPos.getX();
            if (blockPos.getX() > maxX) maxX = blockPos.getX();
            if (blockPos.getY() < minY) minY = blockPos.getY();
            if (blockPos.getY() > maxY) maxY = blockPos.getY();
            if (blockPos.getZ() < minZ) minZ = blockPos.getZ();
            if (blockPos.getZ() > maxZ) maxZ = blockPos.getZ();
        }

        return new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
    }

    public boolean isEmpty() {
        return size() == Vec3i.ZERO;
    }

    public Iterable<BlockPos> blockPoses() {
        return children().stream().map((p) -> p.operation().getPosition()).toList();
    }

}
