package dev.huskcasaca.effortless.building;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public record StructurePreview(
        List<OperationResult> operations
) {

    public static final StructurePreview EMPTY = new StructurePreview(Collections.emptyList());

    // TODO: 30/3/23 replcae HashMap with tag
    public ItemUsage usages() {
        var sufficientItems = new HashMap<Item, Integer>();
        var insufficientItems = new HashMap<Item, Integer>();

        for (var operation : operations) {

            var required = operation.operation().getRequiredItemStack();
            switch (operation.operation().getType()) {
                case WORLD_PLACE_OP -> {
                    (operation.result() == InteractionResult.SUCCESS ? sufficientItems : insufficientItems).put(required.getItem(), required.getCount());
                }
                case WORLD_BREAK_OP -> {
                    insufficientItems.put(required.getItem(), required.getCount());
                }
            }
        }

        var sufficientItemStacks = new ArrayList<ItemStack>();
        var insufficientItemStacks = new ArrayList<ItemStack>();

        sufficientItems.forEach((item, count) -> {
            if (item == Items.AIR) return;

            while (count > 0) {
                var itemStack = new ItemStack(item);
                var maxStackSize = itemStack.getMaxStackSize();
                var used = count > maxStackSize ? maxStackSize : count;
                itemStack.setCount(used);
                count -= used;
                sufficientItemStacks.add(itemStack);
            }
        });

        insufficientItems.forEach((item, count) -> {
            if (item == Items.AIR) return;
            while (count > 0) {
                var itemStack = new ItemStack(item);
                var maxStackSize = itemStack.getMaxStackSize();
                var used = count > maxStackSize ? maxStackSize : count;
                itemStack.setCount(used);
                count -= used;
                insufficientItemStacks.add(itemStack);
            }
        });

        return new ItemUsage(sufficientItemStacks, insufficientItemStacks);
    }

    @Nullable
    public BlockPos firstPos() {
        return isEmpty() ? null : operations.get(0).operation().getPosition();
    }

    @Nullable
    public BlockPos secondPos() {
        return isEmpty() ? null : operations.get(operations.size() - 1).operation().getPosition();
    }

    public double dissolveSize() {
        return (isEmpty()) ? 0 : Mth.clampedLerp(30, 60, firstPos().distSqr(secondPos()) / 100.0);
    }

    public boolean isEmpty() {
        return operations.isEmpty();
    }

    public Vec3i size() {
        if (operations.isEmpty()) {
            return Vec3i.ZERO;
        }

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (var operation : operations()) {
            var blockPos = operation.operation().getPosition();
            if (blockPos.getX() < minX) minX = blockPos.getX();
            if (blockPos.getX() > maxX) maxX = blockPos.getX();
            if (blockPos.getY() < minY) minY = blockPos.getY();
            if (blockPos.getY() > maxY) maxY = blockPos.getY();
            if (blockPos.getZ() < minZ) minZ = blockPos.getZ();
            if (blockPos.getZ() > maxZ) maxZ = blockPos.getZ();
        }
        return new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
    }

    public Iterable<BlockPos> blockPoses() {
        return operations().stream().map((p) -> p.operation().getPosition()).toList();
    }

    public record ItemUsage(
            List<ItemStack> sufficientItems,
            List<ItemStack> insufficientItems
    ) {

        public static ItemUsage EMPTY = new ItemUsage(Collections.emptyList(), Collections.emptyList());

        public int sufficientCount() {
            return sufficientItems.stream().mapToInt(ItemStack::getCount).sum();
        }

        public int insufficientCount() {
            return insufficientItems.stream().mapToInt(ItemStack::getCount).sum();
        }

        public boolean isFilled() {
            return insufficientItems.isEmpty();
        }

        public int totalCount() {
            return sufficientCount() + insufficientCount();
        }
    }

}
