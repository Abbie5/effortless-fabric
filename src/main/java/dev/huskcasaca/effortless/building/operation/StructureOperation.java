package dev.huskcasaca.effortless.building.operation;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.TracingResult;
import dev.huskcasaca.effortless.config.PreviewConfig;
import dev.huskcasaca.effortless.render.RenderTypes;
import dev.huskcasaca.effortless.render.outliner.OutlineRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class StructureOperation implements Operation<StructureOperation.Result> {

    public abstract Level level();
    public abstract Player player();
    public abstract BuildContext context();

    private static void sortOnDistanceToPlayer(List<BlockStateOperation> blockPosStates, Player player) {
        blockPosStates.sort((lpl, rpl) -> {
            // -1 - less than, 1 - greater than, 0 - equal
            double lhsDistanceToPlayer = Vec3.atLowerCornerOf(lpl.blockPos()).subtract(player.getEyePosition(1f)).lengthSqr();
            double rhsDistanceToPlayer = Vec3.atLowerCornerOf(rpl.blockPos()).subtract(player.getEyePosition(1f)).lengthSqr();
            return (int) Math.signum(lhsDistanceToPlayer - rhsDistanceToPlayer);
        });

    }

    public DefaultRenderer getRenderer() {
        return DefaultRenderer.getInstance();
    }

    // for preview

    private static void renderStructureShader(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, StructureOperation.Result preview) {
        if (preview.isEmpty()) return;
        var dispatcher = Minecraft.getInstance().getBlockRenderer();

        double totalTime = preview.dissolveSize() * PreviewConfig.shaderDissolveTimeMultiplier();
//        float dissolve = (getGameTime() - preview.time()) / (float) totalTime;

        float dissolve = 1;

        var firstPos = preview.firstPos();
        var secondPos = preview.secondPos();

//        var states =  preview.operations().stream().map((op) -> {
//            return op.getFirst().ge
//        }).collect(Collectors.toList());
//
//        for (var blockPosState :) {
//            var level = blockPosState.level();
//            var blockPos = blockPosState.blockPos();
//            var blockState = blockPosState.blockState();
//            var item = blockState.getBlock().asItem();
//            var itemStack = inventory.findItemStackByItem(item);
//
//            if (item instanceof BlockItem blockItem && itemStack.is(item)) {
//                blockState = blockItem.updateBlockStateFromTag(blockPos, level, itemStack, blockState);
//            }
//            var red = breaking || (!skip && itemStack.isEmpty());
//
//            renderBlockDissolveShader(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, red);
//            if (skip || breaking) {
//                continue;
//            }
//            itemStack.shrink(1);
//        }
    }

    public final static class DefaultRenderer implements Renderer<Result> {

        private static final Color PLACING_COLOR = new Color(0.92f, 0.92f, 0.92f, 1f);
        private static final Color BREAKING_COLOR = new Color(0.95f, 0f, 0f, 1f);

        private final static DefaultRenderer INSTANCE = new DefaultRenderer();

        public static DefaultRenderer getInstance() {
            return INSTANCE;
        }
        public void render(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, StructureOperation.Result result) {
            if (!result.type().isSuccess()) return;

            var context = result.operation().context();
//            if (!preview.isEmpty() && soundTime < getGameTime() && !BlocksPreview.arePreviewSizeEqual(preview, currentPreview)) {
//                soundTime = getGameTime();
//                var soundType = preview.blockPosStates().get(0).blockState().getSoundType();
//                player.getLevel().playSound(player, player.blockPosition(), context.isBreaking() ? soundType.getBreakSound() : soundType.getPlaceSound(), SoundSource.BLOCKS, 0.3f, 0.8f);
//            }
//                switch (ConfigManager.getGlobalPreviewConfig().getBlockPreviewMode()) {
//                    case OUTLINES -> renderBlockOutlines(poseStack, multiBufferSource, preview, 0);
//                    case DISSOLVE_SHADER -> renderStructureShader(poseStack, multiBufferSource, preview, 0);
//                }
            renderStructureShader(poseStack, multiBufferSource, result);
            OutlineRenderer.getInstance().showCluster(context.uuid(), result.blockPoses())
                    .texture(RenderTypes.CHECKERED_THIN_TEXTURE_LOCATION)
                    .stroke(1 / 64f)
                    .colored(context.isBreaking() ? BREAKING_COLOR : PLACING_COLOR)
                    .disableNormals();
        };
    }

    public record Result(
            StructureOperation operation,
            TracingResult.Type type,
            List<BlockStateOperation.Result> result
    ) implements Operation.Result<Result> {

        // TODO: 30/3/23 replcae HashMap with tag
        public ItemUsage usages() {
            var sufficientItems = new HashMap<Item, Integer>();
            var insufficientItems = new HashMap<Item, Integer>();

            for (var operation : result) {

                var required = operation.operation().requiredItemStack();
                switch (operation.operation().getType()) {
                    case WORLD_PLACE_OP -> {
                        var items = (operation.result() == InteractionResult.SUCCESS ? sufficientItems : insufficientItems);
                        items.put(required.getItem(), items.getOrDefault(required.getItem(), 0) + required.getCount());
                    }
                    case WORLD_BREAK_OP -> {
                        var items = insufficientItems;
                        items.put(required.getItem(), items.getOrDefault(required.getItem(), 0) + required.getCount());
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
            return isEmpty() ? null : result.get(0).operation().getPosition();
        }

        @Nullable
        public BlockPos secondPos() {
            return isEmpty() ? null : result.get(result.size() - 1).operation().getPosition();
        }

        public double dissolveSize() {
            return (isEmpty()) ? 0 : Mth.clampedLerp(30, 60, firstPos().distSqr(secondPos()) / 100.0);
        }

        public boolean isEmpty() {
            return result.isEmpty();
        }

        public Vec3i size() {
            if (result.isEmpty()) {
                return Vec3i.ZERO;
            }

            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

            for (var operation : result()) {
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
            return result().stream().map((p) -> p.operation().getPosition()).toList();
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
}
