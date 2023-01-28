package dev.huskcasaca.effortless.render.preview;

import dev.huskcasaca.effortless.buildmodifier.BlockPosState;
import dev.huskcasaca.effortless.utils.AnimationTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public record BlocksPreview(
        List<BlockPosState> blockPosStates,
        List<ItemStack> itemStacks,
        float time,
        boolean breaking,
        boolean skip
) {

    public static final BlocksPreview EMPTY = new BlocksPreview(Collections.emptyList(), Collections.emptyList(), 0, false, false);

    public static boolean arePreviewSizeEqual(BlocksPreview p0, BlocksPreview p1) {
        return p0.blockPosStates.size() == p1.blockPosStates.size();
    }

    public static BlocksPreview snapshot(Player player, List<BlockPosState> blockPosStates, boolean breaking) {
        return new BlocksPreview(
                blockPosStates,
                breaking ? Collections.emptyList() : player.getInventory().items.stream().map(ItemStack::copy).toList(),
                AnimationTicker.getTicks(),
                breaking,
                player.isCreative()
        );
    }

    public PreviewInventory createPreviewInventory() {
        return new PreviewInventory(itemStacks);
    }

    public ItemUsage usages() {
        var sufficientItems = new HashMap<Item, Integer>();
        var insufficientItems = new HashMap<Item, Integer>();
        var inventory = createPreviewInventory();

        for (var blockPosState : blockPosStates) {
            var item = blockPosState.blockState().getBlock().asItem();
            var itemStack = inventory.findItemStackByItem(item);

            var insufficient = !breaking && !skip && itemStack.isEmpty();

            var items = insufficient ? insufficientItems : sufficientItems;
            items.put(item, items.getOrDefault(item, 0) + 1);

            if (skip || breaking) {
                continue;
            }
            itemStack.shrink(1);
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

    public BlockPos firstPos() {
        return blockPosStates.get(0).blockPos();
    }

    public BlockPos secondPos() {
        return blockPosStates.get(blockPosStates.size() - 1).blockPos();
    }

    public double dissolveSize() {
        return Mth.clampedLerp(30, 60, firstPos().distSqr(secondPos()) / 100.0);
    }

    public boolean isEmpty() {
        return blockPosStates.isEmpty();
    }

    public Vec3i size() {
        if (blockPosStates.isEmpty()) {
            return Vec3i.ZERO;
        }

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (var blockPosState : blockPosStates()) {
            var blockPos = blockPosState.blockPos();
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
        return blockPosStates().stream().map(BlockPosState::blockPos).toList();
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
