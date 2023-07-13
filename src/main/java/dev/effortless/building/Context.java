package dev.effortless.building;

import dev.effortless.Effortless;
import dev.effortless.building.mode.BuildFeature;
import dev.effortless.building.mode.BuildMode;
import dev.effortless.building.mode.BuildOption;
import dev.effortless.building.pattern.randomizer.Randomizer;
import dev.effortless.building.replace.ReplaceMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Context(
        UUID uuid,
        BuildingState state,
        List<BlockHitResult> blockHitResults,

        StructureParams structureParams,
        PatternParams patternParams,
        RandomizerParams randomizerParams,
        ReachParams reachParams,
        @Deprecated() boolean skipRaytrace
) {

    public static void write(FriendlyByteBuf friendlyByteBuf, Context context) {
        friendlyByteBuf.writeUUID(context.uuid());
        friendlyByteBuf.writeEnum(context.state());
        friendlyByteBuf.writeVarInt(context.blockHitResults().size());
        context.blockHitResults().forEach(friendlyByteBuf::writeBlockHitResult);

        friendlyByteBuf.writeEnum(context.buildMode());
        friendlyByteBuf.writeEnum(context.circleStart());
        friendlyByteBuf.writeEnum(context.cubeFilling());
        friendlyByteBuf.writeEnum(context.planeFilling());
        friendlyByteBuf.writeEnum(context.planeFacing());
        friendlyByteBuf.writeEnum(context.raisedEdge());
        friendlyByteBuf.writeEnum(context.replaceMode());

        friendlyByteBuf.writeBoolean(context.skipRaytrace());
        Effortless.log("Wrote context with size: " + friendlyByteBuf.readableBytes());
    }

    public static Context decodeBuf(FriendlyByteBuf friendlyByteBuf) {
        return new Context(
                friendlyByteBuf.readUUID(),
                friendlyByteBuf.readEnum(BuildingState.class),
                IntStream.range(0, friendlyByteBuf.readVarInt())
                        .mapToObj(i -> friendlyByteBuf.readBlockHitResult())
                        .toList(),
                new StructureParams(
                        friendlyByteBuf.readEnum(BuildMode.class),
                        friendlyByteBuf.readEnum(BuildFeature.CircleStart.class),
                        friendlyByteBuf.readEnum(BuildFeature.CubeFilling.class),
                        friendlyByteBuf.readEnum(BuildFeature.PlaneFilling.class),
                        friendlyByteBuf.readEnum(BuildFeature.PlaneFacing.class),
                        friendlyByteBuf.readEnum(BuildFeature.RaisedEdge.class),
                        friendlyByteBuf.readEnum(ReplaceMode.class)),
                new PatternParams(),
                new RandomizerParams(Randomizer.EMPTY),
                new ReachParams(0, 0),
                friendlyByteBuf.readBoolean()
        );
    }

    public static Context defaultSet() {
        return new Context(
                UUID.randomUUID(),
                BuildingState.IDLE,
                Collections.emptyList(),
                new StructureParams(
                        BuildMode.DISABLED,
                        BuildFeature.CircleStart.CIRCLE_START_CORNER,
                        BuildFeature.CubeFilling.CUBE_FULL,
                        BuildFeature.PlaneFilling.PLANE_FULL,
                        BuildFeature.PlaneFacing.HORIZONTAL,
                        BuildFeature.RaisedEdge.RAISE_LONG_EDGE,
                        ReplaceMode.DISABLED),
                new PatternParams(),
                new RandomizerParams(Randomizer.EMPTY),
                new ReachParams(0, 0),
                false
        );
    }

    // new context for idle
    public Context reset() {
        return new Context(
                UUID.randomUUID(),
                BuildingState.IDLE,
                Collections.emptyList(),
                structureParams, patternParams, randomizerParams, reachParams, skipRaytrace
        );
    }

    // new context for placing
    public Context placing() {
        return new Context(
                UUID.randomUUID(),
                BuildingState.PLACE_BLOCK,
                Collections.emptyList(),
                structureParams, patternParams, randomizerParams, reachParams, skipRaytrace
        );
    }

    // new context for breaking
    public Context breaking() {
        return new Context(
                UUID.randomUUID(),
                BuildingState.BREAK_BLOCK,
                Collections.emptyList(),
                structureParams, patternParams, randomizerParams, reachParams, skipRaytrace
        );
    }

    public Context withNextBreak(BlockHitResult blockHitResult) {
        if (isBreakBlock()) {
            return this.withNextHit(blockHitResult);
        } else {
            return this.withBreakingState().withNextHit(blockHitResult);
        }
    }

    public Context withNextPlace(BlockHitResult blockHitResult) {
        if (isBreakBlock()) {
            return this.withNextHit(blockHitResult);
        } else {
            return this.withPlacingState().withNextHit(blockHitResult);
        }
    }

    public Context withPlacingState() {
        return this.withState(BuildingState.PLACE_BLOCK);
    }

    public Context withBreakingState() {
        return this.withState(BuildingState.BREAK_BLOCK);
    }

    public Context withState(BuildingState state) {
        if (this.state == state) {
            return this;
        }
        return switch (state) {
            case IDLE -> new Context(
                    uuid,
                    BuildingState.IDLE,
                    blockHitResults,
                    structureParams, patternParams, randomizerParams, reachParams, skipRaytrace
            );
            case PLACE_BLOCK -> new Context(
                    uuid,
                    BuildingState.PLACE_BLOCK,
                    blockHitResults,
                    structureParams, patternParams, randomizerParams, reachParams, skipRaytrace
            );
            case BREAK_BLOCK -> new Context(
                    uuid,
                    BuildingState.BREAK_BLOCK,
                    blockHitResults,
                    structureParams, patternParams, randomizerParams, reachParams, skipRaytrace
            );
        };
    }

    public boolean isPlaceBlock() {
        return state == BuildingState.PLACE_BLOCK;
    }

    public boolean isBreakBlock() {
        return state == BuildingState.BREAK_BLOCK;
    }

    public boolean isIdle() {
        return state.isIdle();
    }

    public boolean isBuilding() {
        return buildMode() != BuildMode.DISABLED && state != BuildingState.IDLE;
    }

    public boolean isSkipTracing() {
        return state() == BuildingState.BREAK_BLOCK || structureParams().replaceMode() == ReplaceMode.QUICK;
    }

    public BlockHitResult firstBlockHitResult() {
        return blockHitResults.get(0);
    }

    public BlockHitResult secondBlockHitResult() {
        return blockHitResults.get(1);
    }

    public BlockHitResult thirdBlockHitResult() {
        return blockHitResults.get(2);
    }

    public int clicks() {
        return blockHitResults.size();
    }

    public boolean noClicks() {
        return blockHitResults.isEmpty();
    }

    public BlockPos firstPos() {
        return firstBlockHitResult().getBlockPos();
    }

    public BlockPos secondPos() {
        return secondBlockHitResult().getBlockPos();
    }

    public BlockPos thirdPos() {
        return thirdBlockHitResult().getBlockPos();
    }

    public Context withFirstPos(int x, int y, int z) {
        return withPos(0, new BlockPos(x, y, z));
    }

    public Context withFirstPos(BlockPos pos) {
        return withPos(0, pos);
    }

    public Context withSecondPos(int x, int y, int z) {
        return withPos(1, new BlockPos(x, y, z));
    }

    public Context withSecondPos(BlockPos pos) {
        return withPos(1, pos);
    }

    public Context withThirdPos(int x, int y, int z) {
        return withPos(2, new BlockPos(x, y, z));
    }


    // builder

    public Context withThirdPos(BlockPos pos) {
        return withPos(2, pos);
    }

    public Context withPos(int position, BlockPos pos) {
        var result = IntStream.range(0, blockHitResults.size()).mapToObj((i) -> i == position ? blockHitResults.get(i).withPosition(pos) : blockHitResults.get(i)).toArray(BlockHitResult[]::new);
        return withHits(result);
    }

    public Context withNextHit(BlockHitResult blockHitResult) {
        return new Context(uuid, state, Stream.concat(blockHitResults.stream(), Stream.of(blockHitResult)).toList(), structureParams, patternParams, randomizerParams, reachParams, skipRaytrace);
    }

    public Context withHits(BlockHitResult... hitResults) {
        return new Context(uuid, state, Arrays.asList(hitResults), structureParams, patternParams, randomizerParams, reachParams, skipRaytrace);
    }

    public Context withEmptyHits() {
        return new Context(uuid, state, new ArrayList<>(), structureParams, patternParams, randomizerParams, reachParams, skipRaytrace);
    }

    public Context withNextHit(Player player, boolean preview) {
        return withNextHit(trace(player, preview));
    }

    public Context withBuildMode(BuildMode buildMode) {
        return new Context(uuid, state, blockHitResults, structureParams.withBuildMode(buildMode), patternParams, randomizerParams, reachParams, skipRaytrace);
    }

    public Context withBuildFeature(BuildFeature.Entry feature) {
        return new Context(uuid, state, blockHitResults, structureParams.withBuildFeature(feature), patternParams, randomizerParams, reachParams, skipRaytrace);
    }

    public Context withRandomUUID() {
        return withUUID(UUID.randomUUID());
    }

    public Context withUUID(UUID uuid) {
        return new Context(uuid, state, blockHitResults, structureParams, patternParams, randomizerParams, reachParams, skipRaytrace);
    }

    // mode
    public BuildMode buildMode() {
        return structureParams.buildMode();
    }

    public BuildOption[] buildFeatures() {
        return structureParams.buildFeatures();
    }

    public boolean isDisabled() {
        return structureParams.buildMode() == BuildMode.DISABLED;
    }

    public BuildFeature.CircleStart circleStart() {
        return structureParams.circleStart();
    }

    public BuildFeature.CubeFilling cubeFilling() {
        return structureParams.cubeFilling();
    }

    public BuildFeature.PlaneFilling planeFilling() {
        return structureParams.planeFilling();
    }

    public BuildFeature.PlaneFacing planeFacing() {
        return structureParams.planeFacing();
    }

    public BuildFeature.RaisedEdge raisedEdge() {
        return structureParams.raisedEdge();
    }

    public ReplaceMode replaceMode() {
        return structureParams.replaceMode();
    }

    // reach
    public int maxBlockPlacePerAxis() {
        return 128; // reachParams.maxBlockPlacePerAxis();
    }

    public int maxReachDistance() {
        return 256; // reachParams.maxReachDistance();
    }

    public boolean isFulfilled() {
        return isBuilding() && structureParams().buildMode().getInstance().totalClicks(this) == clicks();
    }

    public boolean isBlockHitMissing() {
        return isBuilding() && blockHitResults.stream().anyMatch((hitResult) -> hitResult == null || hitResult.getType() != BlockHitResult.Type.BLOCK);
    }

    // for build mode only
    public BlockHitResult trace(Player player, boolean preview) { // TODO: 11/7/23 preview
        return buildMode().getInstance().trace(player, this);
    }

    // for build mode only
    public Stream<BlockHitResult> collect() {
        if (tracingResult().isSuccess()) {
            return buildMode().getInstance().collect(this).map((blockPos) -> firstBlockHitResult().withPosition(blockPos));
        } else {
            return Stream.empty();
        }

    }
    public TracingResult tracingResult() {
        if (isIdle()) {
            return TracingResult.PASS;
        }
        if (isBlockHitMissing()) {
            return TracingResult.FAILED;
        }

        if (isFulfilled()) {
            return TracingResult.SUCCESS_FULFILLED;
        } else {
            return TracingResult.SUCCESS_PARTIAL;
        }
    }

    public record StructureParams(
            BuildMode buildMode,

            BuildFeature.CircleStart circleStart,
            BuildFeature.CubeFilling cubeFilling,
            BuildFeature.PlaneFilling planeFilling,
            BuildFeature.PlaneFacing planeFacing,
            BuildFeature.RaisedEdge raisedEdge,

            ReplaceMode replaceMode
    ) {

        public BuildOption[] buildFeatures() {
            return new BuildOption[]{
                    circleStart,
                    cubeFilling,
                    planeFilling,
                    planeFacing,
                    raisedEdge
            };
        }

        public StructureParams withBuildMode(BuildMode buildMode) {
            return new StructureParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public StructureParams withBuildFeature(BuildFeature.Entry feature) {
            if (feature instanceof BuildFeature.CircleStart) return withCircleStart((BuildFeature.CircleStart) feature);
            if (feature instanceof BuildFeature.CubeFilling) return withCubeFilling((BuildFeature.CubeFilling) feature);
            if (feature instanceof BuildFeature.PlaneFilling) return withPlaneFilling((BuildFeature.PlaneFilling) feature);
            if (feature instanceof BuildFeature.PlaneFacing) return withPlaneFacing((BuildFeature.PlaneFacing) feature);
            if (feature instanceof BuildFeature.RaisedEdge) return withRaisedEdge((BuildFeature.RaisedEdge) feature);
            return this;
        }

        public StructureParams withCircleStart(BuildFeature.CircleStart circleStart) {
            return new StructureParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public StructureParams withCubeFilling(BuildFeature.CubeFilling cubeFilling) {
            return new StructureParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public StructureParams withPlaneFilling(BuildFeature.PlaneFilling planeFilling) {
            return new StructureParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public StructureParams withPlaneFacing(BuildFeature.PlaneFacing planeFacing) {
            return new StructureParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public StructureParams withRaisedEdge(BuildFeature.RaisedEdge raisedEdge) {
            return new StructureParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public StructureParams withReplaceMode(ReplaceMode replaceMode) {
            return new StructureParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

    }

    public record PatternParams(
    ) {
    }

    public record RandomizerParams(
            Randomizer randomizer
    ) {
    }

    public record ReachParams(
            int maxBlockPlacePerAxis,
            int maxReachDistance
    ) {

    }

    public static class BlockHitResultUnion extends UnionEither<BlockHitResult> {
        public BlockHitResultUnion(Stream<UnionEither<BlockHitResult>> children, BlockHitResult child) {
            super(children, child);
        }

        public BlockHitResult hitResult() {
            return super.child();
        }

        public BlockHitResult hitResults() {
            return super.child();
        }

    }

    public static class UnionEither<T> {
        private final Stream<UnionEither<T>> children;
        private final T child;

        public UnionEither(
                Stream<UnionEither<T>> children,
                T child
        ) {
            this.children = children;
            this.child = child;
        }

        public static <T> UnionEither<T> of(T child) {
            return new UnionEither<>(Stream.empty(), child);
        }

        public static <T> UnionEither<T> of(Stream<T> children) {
            return new UnionEither<>(children.map(UnionEither::of), null);
        }

        public Stream<UnionEither<T>> children() {
            return children;
        }

        public T child() {
            return child;
        }

        public boolean isUnion() {
            return child == null;
        }

        public boolean isSingle() {
            return child != null;
        }

        public Stream<T> flatten() {
            if (isSingle()) {
                return Stream.of(child);
            } else {
                return children.flatMap(UnionEither::flatten);
            }
        }
    }
}
