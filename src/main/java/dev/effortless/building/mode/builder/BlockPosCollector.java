package dev.effortless.building.mode.builder;

import dev.effortless.building.Context;
import net.minecraft.core.BlockPos;

import java.util.stream.Stream;

public interface BlockPosCollector {

    Stream<BlockPos> collect(Context context);

}
