package dev.huskcasaca.effortless.buildmodifier;

import dev.huskcasaca.effortless.buildmodifier.array.Array;
import dev.huskcasaca.effortless.buildmodifier.mirror.Mirror;
import dev.huskcasaca.effortless.buildmodifier.mirror.RadialMirror;
import dev.huskcasaca.effortless.entity.player.EffortlessDataProvider;
import dev.huskcasaca.effortless.entity.player.ModifierSettings;
import dev.huskcasaca.effortless.network.Packets;
import dev.huskcasaca.effortless.network.protocol.player.ClientboundPlayerBuildModifierPacket;
import dev.huskcasaca.effortless.render.preview.BlockPreviewRenderer;
import dev.huskcasaca.effortless.utils.InventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;

public class BuildModifierHandler {

    public static void placeBlocks(Player player, List<BlockHitResult> hitResults) {
        if (player.getLevel().isClientSide()) {
            BlockPreviewRenderer.getInstance().saveCurrentPreview();
        }
        var blockPosStates = BuildModifierHandler.getBlockPosStateForPlacing(player, hitResults);

        for (var blockPosState : blockPosStates) {
            if (!blockPosState.place()) continue;

            var slot = InventoryHelper.findItemSlot(player.getInventory(), blockPosState.blockState().getBlock().asItem());
            var swap = InventoryHelper.swapSlot(player.getInventory(), slot);
            if (!swap) continue;

            if (BuildModifierHelper.isReplace(player)) {
                blockPosState.destroyBy(player);
            }

            blockPosState.placeBy(player, InteractionHand.MAIN_HAND);
            InventoryHelper.swapSlot(player.getInventory(), slot);
        }
    }

    public static void destroyBlocks(Player player, List<BlockHitResult> hitResults) {
        if (player.getLevel().isClientSide()) {
            BlockPreviewRenderer.getInstance().saveCurrentPreview();
        }
        var blockPosStates = BuildModifierHandler.getBlockPosStateForBreaking(player, hitResults);

        for (var blockPosState : blockPosStates) {
//            if (!blockPosState.place()) continue;
            blockPosState.destroyBy(player);
        }
    }

    public static List<BlockPosState> getBlockPosStateForPlacing(Player player, List<BlockHitResult> blockHitResults) {

        var level = player.getLevel();
        var blockStates = findBlockStates(player, blockHitResults);
        var result = new ArrayList<BlockPosState>(blockStates.size());

        blockStates.forEach((blockPos, blockState) -> {
            var blockPosState = new BlockPosState(level, blockPos, blockState, true);
            if (blockPosState.canPlaceBy(player)) {
                result.add(blockPosState);
            } else {
//                result.add(new BlockPosState(level, blockPos, blockState, false));
            }
        });

        return result;
    }

    public static List<BlockPosState> getBlockPosStateForBreaking(Player player, List<BlockHitResult> blockHitResults) {

        var level = player.getLevel();
        var blockPoses = findCoordinatesByHitResult(player, blockHitResults);
        var result = new ArrayList<BlockPosState>(blockPoses.size());

        blockPoses.forEach(blockPos -> {
            var blockPosState = new BlockPosState(level, blockPos, level.getBlockState(blockPos), true);
            if (blockPosState.canBreakBy(player)) {
                result.add(blockPosState);
            } else {
//                result.add(new BlockPosState(level, blockPos, level.getBlockState(blockPos), false));
            }
        });

        return result;
    }

    public static Set<BlockPos> findCoordinatesByHitResult(Player player, BlockHitResult blockHitResult) {
        return findCoordinatesByHitResult(player, Collections.singletonList(blockHitResult));
    }

    public static Set<BlockPos> findCoordinatesByHitResult(Player player, List<BlockHitResult> blockHitResults) {
        var coordinates = new LinkedHashSet<BlockPos>();
        for (var hitResult : blockHitResults) {
            coordinates.add(hitResult.getBlockPos());
        }

        for (var hitResult : blockHitResults) {
            var arrayCoordinates = BuildModifier.getArray().findCoordinates(player, hitResult.getBlockPos());
            coordinates.addAll(arrayCoordinates);
            coordinates.addAll(BuildModifier.getMirror().findCoordinates(player, hitResult.getBlockPos()));
            coordinates.addAll(BuildModifier.getRadialMirror().findCoordinates(player, hitResult.getBlockPos()));
            //get mirror for each array coordinate
            for (var coordinate : arrayCoordinates) {
                coordinates.addAll(BuildModifier.getMirror().findCoordinates(player, coordinate));
                coordinates.addAll(BuildModifier.getRadialMirror().findCoordinates(player, coordinate));
            }
        }

        return coordinates;
    }

    public static Set<BlockPos> findCoordinates(Player player, BlockPos blockPos) {
        return findCoordinates(player, Collections.singletonList(blockPos));
    }

    public static Set<BlockPos> findCoordinates(Player player, List<BlockPos> blockPosList) {
        //Add current blocks being placed too
        var coordinates = new LinkedHashSet<>(blockPosList);

        //Find mirror/array/radial mirror coordinates for each blockpos
        for (var blockPos : blockPosList) {
            var arrayCoordinates = BuildModifier.getArray().findCoordinates(player, blockPos);
            coordinates.addAll(arrayCoordinates);
            coordinates.addAll(BuildModifier.getMirror().findCoordinates(player, blockPos));
            coordinates.addAll(BuildModifier.getRadialMirror().findCoordinates(player, blockPos));
            //get mirror for each array coordinate
            for (var coordinate : arrayCoordinates) {
                coordinates.addAll(BuildModifier.getMirror().findCoordinates(player, coordinate));
                coordinates.addAll(BuildModifier.getRadialMirror().findCoordinates(player, coordinate));
            }
        }

        return coordinates;
    }

    public static Map<BlockPos, BlockState> findBlockStates(Player player, List<BlockHitResult> hitResults) {
        var blockStates = new LinkedHashMap<BlockPos, BlockState>();

        for (var hitResult : hitResults) {
            var itemStack = player.getMainHandItem();
            var blockPos = hitResult.getBlockPos();
            var blockState = getBlockStateFromItem(player, InteractionHand.MAIN_HAND, itemStack, hitResult);
//            if (blockState == null) continue;
            blockStates.put(blockPos, blockState);
        }

        // TODO: 11/1/23 use states over coordinates
        for (var hitResult : hitResults) {
            var itemStack = player.getMainHandItem();
            var blockPos = hitResult.getBlockPos();
            var blockState = getBlockStateFromItem(player, InteractionHand.MAIN_HAND, itemStack, hitResult);
//            if (blockState == null) continue;

            var arrayBlockStates = BuildModifier.getArray().findBlockStates(player, blockPos, blockState);
            blockStates.putAll(arrayBlockStates);

            blockStates.putAll(BuildModifier.getMirror().findBlockStates(player, blockPos, blockState));
            blockStates.putAll(BuildModifier.getRadialMirror().findBlockStates(player, blockPos, blockState));
            //add mirror for each array coordinate
            for (BlockPos coordinate : BuildModifier.getArray().findCoordinates(player, blockPos)) {
                var blockState1 = arrayBlockStates.get(coordinate);
//                if (blockState1 == null) continue;

                blockStates.putAll(BuildModifier.getMirror().findBlockStates(player, coordinate, blockState1));
                blockStates.putAll(BuildModifier.getRadialMirror().findBlockStates(player, coordinate, blockState1));
            }
        }

        return blockStates;
    }

    public static boolean isEnabled(ModifierSettings modifierSettings, BlockPos startPos) {
        return Array.isEnabled(modifierSettings.arraySettings()) ||
                Mirror.isEnabled(modifierSettings.mirrorSettings(), startPos) ||
                RadialMirror.isEnabled(modifierSettings.radialMirrorSettings(), startPos) ||
                modifierSettings.enableQuickReplace();
    }

    public static BlockState getBlockStateFromItem(Player player, InteractionHand hand, ItemStack itemStack, BlockHitResult hitResult) {

        var blockPlaceContext = new BlockPlaceContext(player, hand, itemStack, hitResult);
        var item = itemStack.getItem();

        if (item instanceof BlockItem blockItem) {
            var state = blockItem.getPlacementState(blockPlaceContext);
            return state != null ? state : Blocks.AIR.defaultBlockState();
        } else {
            return Block.byItem(item).getStateForPlacement(blockPlaceContext);
        }
    }

    //Returns true if equal (or both null)
    public static boolean compareCoordinates(List<BlockPos> coordinates1, List<BlockPos> coordinates2) {
        if (coordinates1 == null && coordinates2 == null) return true;
        if (coordinates1 == null || coordinates2 == null) return false;

        //Check count, not actual values
        if (coordinates1.size() == coordinates2.size()) {
            if (coordinates1.size() == 1) {
                return coordinates1.get(0).equals(coordinates2.get(0));
            }
            return true;
        } else {
            return false;
        }

//        return coordinates1.equals(coordinates2);
    }

    public static void handleNewPlayer(ServerPlayer player) {
        //Only on server
        Packets.sendToClient(new ClientboundPlayerBuildModifierPacket(((EffortlessDataProvider) player).getModifierSettings()), player);
    }
}
