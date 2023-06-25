package dev.huskcasaca.effortless.building.operation;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.ItemStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SingleBlockOperation implements Operation<SingleBlockOperation.Result> {

    public abstract Level level();
    public abstract Player player();
    public abstract ItemStorage storage();
    public abstract BuildContext context();
    // for preview
    public abstract BlockPos blockPos();
    public abstract BlockState blockState();

    public abstract ItemStack requiredItemStack();

    public final static class DefaultRenderer implements Renderer<Result> {

        private final static DefaultRenderer INSTANCE = new DefaultRenderer();

        public static DefaultRenderer getInstance() {
            return INSTANCE;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Result result) {

            var dispatcher = Minecraft.getInstance().getBlockRenderer();
            var operation = result.operation();

            operation.getType();
            var level = operation.level();
            var blockPos = operation.blockPos();
            var blockState = operation.blockState();
            var item = blockState.getBlock().asItem();

//            if (item instanceof BlockItem blockItem && itemStack.is(item)) {
//                blockState = blockItem.updateBlockStateFromTag(blockPos, level, itemStack, blockState);
//            }
//            var red = breaking || (!skip && itemStack.isEmpty());

            // TODO: 26/5/23
//            renderBlockDissolveShader(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, red);

        }
    }

    protected static boolean canInteract(Level level, Player player, BlockPos blockPos) {
        var gameMode = level.isClientSide() ? Minecraft.getInstance().gameMode.getPlayerMode() : ((ServerPlayer) player).gameMode.getGameModeForPlayer();
        return !player.blockActionRestricted(level, blockPos, gameMode);
    }

    public record Result(
            SingleBlockOperation operation,
            InteractionResult result,
            ItemStack consumedItem,
            ItemStack consumedTool
    ) implements Operation.Result<Result> {

    }
}
