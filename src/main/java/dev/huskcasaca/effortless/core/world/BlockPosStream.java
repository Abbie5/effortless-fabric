package dev.huskcasaca.effortless.core.world;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface BlockPosStream extends BaseStream<BlockPos, BlockPosStream> {

    private static BlockPos transform(BlockPos original, Direction.Axis first, Direction.Axis second) {
        assert first != second;
        var third = Direction.Axis.values()[3 - first.ordinal() - second.ordinal()];
        return new BlockPos(original.get(first), original.get(second), original.get(third));
    }

    private static BlockPos recover(BlockPos original, Direction.Axis first, Direction.Axis second) {
        var third = Direction.Axis.values()[3 - first.ordinal() - second.ordinal()];
        var arr = new int[3];
        arr[first.ordinal()] = original.getX();
        arr[second.ordinal()] = original.getY();
        arr[third.ordinal()] = original.getZ();
        return new BlockPos(arr[0], arr[1], arr[2]);
    }

    static Stream<BlockPos> of(BlockPos blockPos) {
        return Stream.of(blockPos);
    }

    static Stream<BlockPos> between(BlockPos blockPos, BlockPos blockPos2) {
        return between(blockPos, blockPos2, Direction.Axis.X, Direction.Axis.Y);
    }

    static Stream<BlockPos> between(BlockPos blockPos, BlockPos blockPos2, Direction.Axis first, Direction.Axis second) {
        return StreamSupport.stream(betweenClosed(transform(blockPos, first, second), transform(blockPos2, first, second)).spliterator(), false).map(pos -> recover(pos, first, second));
    }

    static Stream<BlockPos> between(int x1, int y1, int z1, int x2, int y2, int z2) {
        return between(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2));
    }

    static Stream<BlockPos> between(int x1, int y1, int z1, int x2, int y2, int z2, Direction.Axis first, Direction.Axis second) {
        return between(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), first, second);
    }

    private static Iterable<BlockPos> betweenClosed(BlockPos blockPos, BlockPos blockPos2) {
        return betweenClosed(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
    }

    private static Iterable<BlockPos> betweenClosed(int x1, int y1, int z1, int x2, int y2, int z2) {
        return () -> new AbstractIterator<>() {


            private final int x = Math.abs(x2 - x1) + 1;
            private final int y = Math.abs(y2 - y1) + 1;
            private final int z = Math.abs(z2 - z1) + 1;
            private final int size = x * y * z;

            private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            private int index = 0;

            protected BlockPos computeNext() {
                if (this.index == size) {
                    return this.endOfData();
                } else {
//                    int ix = (index) % x;
//                    int jx = (index - ix) / x % y;
//                    int kx = (index - ix - jx * x) / x / y % z;
                    int ix = index % x, jx = (index / x) % y, kx = (index / (x * y)) % z;
                    ++index;
                    return this.cursor.set(
                            x1 + ix * (x2 > x1 ? 1 : -1),
                            y1 + jx * (y2 > y1 ? 1 : -1),
                            z1 + kx * (z2 > z1 ? 1 : -1)
                    );

//                    index = x + y * xstep + z * xstep * ystep;
                }
            }
        };
    }
}