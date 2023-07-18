package dev.effortless.building.operation;

import dev.effortless.building.Context;
import dev.effortless.building.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockOperation implements Operation<BlockResult> {

    public abstract Level level();

    public abstract Player player();

    public abstract Storage storage();

    public abstract Context context();

    public abstract BlockPos blockPos();

    public abstract BlockState blockState();

    public abstract Direction direction();

    public abstract ItemStack inputItemStack();

    public abstract ItemStack outputItemStack();

}
