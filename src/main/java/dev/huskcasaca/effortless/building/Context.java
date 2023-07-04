package dev.huskcasaca.effortless.building;

import dev.huskcasaca.effortless.building.mode.BuildFeature;
import dev.huskcasaca.effortless.building.mode.BuildFeature.*;
import dev.huskcasaca.effortless.building.mode.BuildMode;
import dev.huskcasaca.effortless.building.mode.BuildOption;
import dev.huskcasaca.effortless.building.operation.StructureBuildOperation;
import dev.huskcasaca.effortless.building.pattern.randomizer.Randomizer;
import dev.huskcasaca.effortless.building.replace.ReplaceMode;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
                        friendlyByteBuf.readEnum(CircleStart.class),
                        friendlyByteBuf.readEnum(CubeFilling.class),
                        friendlyByteBuf.readEnum(PlaneFilling.class),
                        friendlyByteBuf.readEnum(PlaneFacing.class),
                        friendlyByteBuf.readEnum(RaisedEdge.class),
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
                        CircleStart.CIRCLE_START_CORNER,
                        CubeFilling.CUBE_FULL,
                        PlaneFilling.PLANE_FULL,
                        PlaneFacing.HORIZONTAL,
                        RaisedEdge.RAISE_LONG_EDGE,
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
                BuildingState.PLACING,
                Collections.emptyList(),
                structureParams, patternParams, randomizerParams, reachParams, skipRaytrace
        );
    }

    // new context for breaking
    public Context breaking() {
        return new Context(
                UUID.randomUUID(),
                BuildingState.BREAKING,
                Collections.emptyList(),
                structureParams, patternParams, randomizerParams, reachParams, skipRaytrace
        );
    }

    public Context withNextBreak(BlockHitResult blockHitResult) {
        if (isBreaking()) {
            return this.withNextHit(blockHitResult);
        } else {
            return this.withBreakingState().withNextHit(blockHitResult);
        }
    }

    public Context withNextPlace(BlockHitResult blockHitResult) {
        if (isBreaking()) {
            return this.withNextHit(blockHitResult);
        } else {
            return this.withPlacingState().withNextHit(blockHitResult);
        }
    }

    public Context withPlacingState() {
        return this.withState(BuildingState.PLACING);
    }

    public Context withBreakingState() {
        return this.withState(BuildingState.BREAKING);
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
            case PLACING -> new Context(
                    uuid,
                    BuildingState.PLACING,
                    blockHitResults,
                    structureParams, patternParams, randomizerParams, reachParams, skipRaytrace
            );
            case BREAKING -> new Context(
                    uuid,
                    BuildingState.BREAKING,
                    blockHitResults,
                    structureParams, patternParams, randomizerParams, reachParams, skipRaytrace
            );
        };
    }

    public boolean isPlacing() {
        return state == BuildingState.PLACING;
    }

    public boolean isBreaking() {
        return state == BuildingState.BREAKING;
    }

    public boolean isIdle() {
        return state.isIdle();
    }

    public boolean isBuilding() {
        return buildMode() != BuildMode.DISABLED && state != BuildingState.IDLE;
    }

    public boolean isSkipTracing() {
        return state().isBreaking() || structureParams().replaceMode() == ReplaceMode.QUICK;
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

    public Context withUUID() {
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

    public CircleStart circleStart() {
        return structureParams.circleStart();
    }

    public CubeFilling cubeFilling() {
        return structureParams.cubeFilling();
    }

    public PlaneFilling planeFilling() {
        return structureParams.planeFilling();
    }

    public PlaneFacing planeFacing() {
        return structureParams.planeFacing();
    }

    public RaisedEdge raisedEdge() {
        return structureParams.raisedEdge();
    }

    public ReplaceMode replaceMode() {
        return structureParams.replaceMode();
    }

    // reach
    public int maxBlockPlacePerAxis() {
        return 64; // reachParams.maxBlockPlacePerAxis();
    }

    public int maxReachDistance() {
        return 128; // reachParams.maxReachDistance();
    }

    public boolean isFulfilled() {
        return isBuilding() && structureParams().buildMode().getInstance().totalClicks(this) == clicks();
    }

    public boolean isMissingHit() {
        return isBuilding() && blockHitResults.stream().anyMatch((blockHitResult) -> blockHitResult == null || blockHitResult.getType() != BlockHitResult.Type.BLOCK);
    }

    // for build mode only
    public BlockHitResult trace(Player player, boolean preview) {
        var result = buildMode().getInstance().trace(player, this);
//        if (!preview) {
//            Effortless.log("traceBuildMode: " + result);
//        } else {
//            if (result != null) Effortless.log("traceIt", result.getType(), result.getBlockPos());
//        }
        return result;
    }

    // for build mode only
    public TracingResult collect() {
        if (isIdle()) {
            return TracingResult.pass();
        }
        if (isMissingHit()) {
            return TracingResult.fail();
        }

        var hitResults = buildMode().getInstance().collect(this).map((blockPos) -> firstBlockHitResult().withPosition(blockPos)).toList();

        if (isFulfilled()) {
            return TracingResult.success(hitResults);
        } else {
            return TracingResult.partial(hitResults);
        }
    }

    public StructureBuildOperation getStructure(Level level, Player player) {
        return new StructureBuildOperation(level, player, this, null);
    }

    public StructureBuildOperation getStructure(Level level, Player player, Storage storage) {
        return new StructureBuildOperation(level, player, this, storage);
    }

    public String getTranslatedModeOptionName() {

        var mode = buildMode();
        var modeName = new StringBuilder();
        for (var option : buildMode().getSupportedFeatures()) {
            // TODO: 20/3/23
//            modeName.append(I18n.get(getOptionSetting(option).getNameKey()));
            modeName.append(" ");
        }
        return modeName + I18n.get(mode.getNameKey());
    }

    public record GeneralParams(
            List<BlockHitResult> blockHitResults,
            Boolean skipRaytrace
    ) {
    }

    public record StructureParams(
            BuildMode buildMode,

            CircleStart circleStart,
            CubeFilling cubeFilling,
            PlaneFilling planeFilling,
            PlaneFacing planeFacing,
            RaisedEdge raisedEdge,

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
            if (feature instanceof CircleStart) return withCircleStart((CircleStart) feature);
            if (feature instanceof CubeFilling) return withCubeFilling((CubeFilling) feature);
            if (feature instanceof PlaneFilling) return withPlaneFilling((PlaneFilling) feature);
            if (feature instanceof PlaneFacing) return withPlaneFacing((PlaneFacing) feature);
            if (feature instanceof RaisedEdge) return withRaisedEdge((RaisedEdge) feature);
            return this;
        }

        public StructureParams withCircleStart(CircleStart circleStart) {
            return new StructureParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public StructureParams withCubeFilling(CubeFilling cubeFilling) {
            return new StructureParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public StructureParams withPlaneFilling(PlaneFilling planeFilling) {
            return new StructureParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public StructureParams withPlaneFacing(PlaneFacing planeFacing) {
            return new StructureParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public StructureParams withRaisedEdge(RaisedEdge raisedEdge) {
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
