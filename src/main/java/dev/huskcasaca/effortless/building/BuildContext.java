package dev.huskcasaca.effortless.building;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.building.mode.BuildFeature;
import dev.huskcasaca.effortless.building.mode.BuildFeature.*;
import dev.huskcasaca.effortless.building.mode.BuildMode;
import dev.huskcasaca.effortless.building.mode.BuildOption;
import dev.huskcasaca.effortless.building.pattern.randomizer.Randomizer;
import dev.huskcasaca.effortless.building.replace.ReplaceMode;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record BuildContext(
        UUID uuid,
        BuildingState state,
        List<BlockHitResult> blockHitResults,
        boolean skipRaytrace,

        ModeParams modeParams,
        PatternParams patternParams,
        RandomizerParams randomizerParams,
        ReachParams reachParams
) {

    public static void write(FriendlyByteBuf friendlyByteBuf, BuildContext context) {
        friendlyByteBuf.writeUUID(context.uuid());
        friendlyByteBuf.writeEnum(context.state());
        friendlyByteBuf.writeVarInt(context.blockHitResults().size());
        context.blockHitResults().forEach(friendlyByteBuf::writeBlockHitResult);
        friendlyByteBuf.writeBoolean(context.skipRaytrace());

        friendlyByteBuf.writeEnum(context.buildMode());
    }

    public static BuildContext decodeBuf(FriendlyByteBuf friendlyByteBuf) {
        return new BuildContext(
                friendlyByteBuf.readUUID(),
                friendlyByteBuf.readEnum(BuildingState.class),
                IntStream.range(0, friendlyByteBuf.readVarInt())
                        .mapToObj(i -> friendlyByteBuf.readBlockHitResult())
                        .toList(),
                friendlyByteBuf.readBoolean(),
                new ModeParams(
                        friendlyByteBuf.readEnum(BuildMode.class),
                        CircleStart.CIRCLE_START_CORNER,
                        CubeFilling.CUBE_FULL,
                        PlaneFilling.PLANE_FULL,
                        PlaneFacing.HORIZONTAL,
                        RaisedEdge.RAISE_LONG_EDGE,
                        ReplaceMode.DISABLED),
                new PatternParams(),
                new RandomizerParams(Randomizer.EMPTY),
                new ReachParams(0, 0)
        );
    }

    public static BuildContext defaultSet() {
        return new BuildContext(
                UUID.randomUUID(),
                BuildingState.IDLE,
                Collections.emptyList(),
                false,
                new ModeParams(
                        BuildMode.DISABLED,
                        CircleStart.CIRCLE_START_CORNER,
                        CubeFilling.CUBE_FULL,
                        PlaneFilling.PLANE_FULL,
                        PlaneFacing.HORIZONTAL,
                        RaisedEdge.RAISE_LONG_EDGE,
                        ReplaceMode.DISABLED),
                new PatternParams(),
                new RandomizerParams(Randomizer.EMPTY),
                new ReachParams(0, 0)
        );
    }

    // new context for idle
    public BuildContext idle() {
        return new BuildContext(
                UUID.randomUUID(),
                BuildingState.IDLE,
                Collections.emptyList(),
                skipRaytrace,
                modeParams,
                patternParams,
                randomizerParams,
                reachParams
        );
    }

    // new context for placing
    public BuildContext placing() {
        return new BuildContext(
                UUID.randomUUID(),
                BuildingState.PLACING,
                Collections.emptyList(),
                skipRaytrace,
                modeParams,
                patternParams,
                randomizerParams,
                reachParams
        );
    }

    // new context for breaking
    public BuildContext breaking() {
        return new BuildContext(
                UUID.randomUUID(),
                BuildingState.BREAKING,
                Collections.emptyList(),
                skipRaytrace,
                modeParams,
                patternParams,
                randomizerParams,
                reachParams
        );
    }

    public BuildContext withNextBreak(BlockHitResult blockHitResult) {
        if (isBreaking()) {
            return this.withNextHit(blockHitResult);
        } else {
            return this.breaking().withNextHit(blockHitResult);
        }
    }

    public BuildContext withNextPlace(BlockHitResult blockHitResult) {
        if (isBreaking()) {
            return this.withNextHit(blockHitResult);
        } else {
            return this.placing().withNextHit(blockHitResult);
        }
    }

    public BuildContext withState(BuildingState state) {
        if (this.state == state) {
            return this;
        }
        return switch (state) {
            case IDLE -> idle();
            case PLACING -> placing();
            case BREAKING -> breaking();
        };
    }

    public BuildContext withNextStateHit(BuildingState state, @Nullable BlockHitResult blockHitResult) {
        return withState(state).withNextHit(blockHitResult);
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
        return state != BuildingState.IDLE;
    }

    public boolean isSkipTracing() {
        return skipRaytrace || modeParams().replaceMode() == ReplaceMode.QUICK;
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

    public BuildContext withFirstPos(int x, int y, int z) {
        return withPos(0, new BlockPos(x, y, z));
    }

    public BuildContext withFirstPos(BlockPos pos) {
        return withPos(0, pos);
    }

    public BuildContext withSecondPos(int x, int y, int z) {
        return withPos(1, new BlockPos(x, y, z));
    }

    public BuildContext withSecondPos(BlockPos pos) {
        return withPos(1, pos);
    }

    public BuildContext withThirdPos(int x, int y, int z) {
        return withPos(2, new BlockPos(x, y, z));
    }


    // builder

    public BuildContext withThirdPos(BlockPos pos) {
        return withPos(2, pos);
    }

    public BuildContext withPos(int position, BlockPos pos) {
        var result = IntStream.range(0, blockHitResults.size()).mapToObj((i) -> i == position ? blockHitResults.get(i).withPosition(pos) : blockHitResults.get(i)).toArray(BlockHitResult[]::new);
        return withHits(result);
    }

    public BuildContext withNextHit(BlockHitResult blockHitResult) {
        return new BuildContext(uuid, state, Stream.concat(blockHitResults.stream(), Stream.of(blockHitResult)).toList(), skipRaytrace, modeParams, patternParams, randomizerParams, reachParams);
    }

    public BuildContext withHits(BlockHitResult... hitResults) {
        return new BuildContext(uuid, state, Arrays.asList(hitResults), skipRaytrace, modeParams, patternParams, randomizerParams, reachParams);
    }

    public BuildContext withEmptyHits() {
        return new BuildContext(uuid, state, new ArrayList<>(), skipRaytrace, modeParams, patternParams, randomizerParams, reachParams);
    }

    public BuildContext withNextHit(Player player, boolean preview) {
        return withNextHit(trace(player, preview));
    }

    public BuildContext withBuildMode(BuildMode buildMode) {
        return new BuildContext(uuid, state, blockHitResults, skipRaytrace, modeParams.withBuildMode(buildMode), patternParams, randomizerParams, reachParams);
    }

    public BuildContext withBuildFeature(BuildFeature.Entry feature) {
        return new BuildContext(uuid, state, blockHitResults, skipRaytrace, modeParams.withBuildFeature(feature), patternParams, randomizerParams, reachParams);
    }

    public BuildContext withUUID() {
        return withUUID(UUID.randomUUID());
    }

    public BuildContext withUUID(UUID uuid) {
        return new BuildContext(uuid, state, blockHitResults, skipRaytrace, modeParams, patternParams, randomizerParams, reachParams);
    }

    // mode
    public BuildMode buildMode() {
        return modeParams.buildMode();
    }

    public BuildOption[] buildFeatures() {
        return modeParams.buildFeatures();
    }

    public boolean isDisabled() {
        return modeParams.buildMode() == BuildMode.DISABLED;
    }

    public CircleStart circleStart() {
        return modeParams.circleStart();
    }

    public CubeFilling cubeFilling() {
        return modeParams.cubeFilling();
    }

    public PlaneFilling planeFilling() {
        return modeParams.planeFilling();
    }

    public PlaneFacing planeOrientation() {
        return modeParams.planeFacing();
    }

    public RaisedEdge raisedEdge() {
        return modeParams.raisedEdge();
    }

    // reach
    public int maxBlockPlacePerAxis() {
        return reachParams.maxBlockPlacePerAxis();
    }

    public int maxReachDistance() {
        return reachParams.maxReachDistance();
    }

    public boolean isFulfilled() {
        return isBuilding() && modeParams().buildMode().getInstance().totalClicks(this) == clicks();
    }

    public boolean isMissingHit() {
        return isBuilding() && blockHitResults.stream().anyMatch((blockHitResult) -> blockHitResult.getType() != BlockHitResult.Type.BLOCK);
    }

    // for build mode only
    public BlockHitResult trace(Player player, boolean preview) {
        var result = buildMode().getInstance().trace(player, this);
        if (!preview) {
            Effortless.log("traceBuildMode: " + result);
        }
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

        var hitResults = buildMode().getInstance().collect(this).map((blockPos) -> firstBlockHitResult().withPosition(blockPos));

        if (isFulfilled()) {
            return TracingResult.success(hitResults);
        } else {
            return TracingResult.partial(hitResults);
        }
    }

    public StructurePlaceOperation getStructure(Player player) {
        return new StructurePlaceOperation(player.getLevel(), player, this);
    }

    public StructurePlaceOperation getStructure(Level level, Player player) {
        return new StructurePlaceOperation(level, player, this);
    }

    public BlockFilter getBlockFilter(Level level, Player player) {
        return new BlockFilter(level, player, state, modeParams.replaceMode());
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

    public record ModeParams(
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

        public ModeParams withBuildMode(BuildMode buildMode) {
            return new ModeParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public ModeParams withBuildFeature(BuildFeature.Entry feature) {
            if (feature instanceof CircleStart) return withCircleStart((CircleStart) feature);
            if (feature instanceof CubeFilling) return withCubeFilling((CubeFilling) feature);
            if (feature instanceof PlaneFilling) return withPlaneFilling((PlaneFilling) feature);
            if (feature instanceof PlaneFacing) return withPlaneFacing((PlaneFacing) feature);
            if (feature instanceof RaisedEdge) return withRaisedEdge((RaisedEdge) feature);
            return this;
        }

        public ModeParams withCircleStart(CircleStart circleStart) {
            return new ModeParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public ModeParams withCubeFilling(CubeFilling cubeFilling) {
            return new ModeParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public ModeParams withPlaneFilling(PlaneFilling planeFilling) {
            return new ModeParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public ModeParams withPlaneFacing(PlaneFacing planeFacing) {
            return new ModeParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public ModeParams withRaisedEdge(RaisedEdge raisedEdge) {
            return new ModeParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
        }

        public ModeParams withReplaceMode(ReplaceMode replaceMode) {
            return new ModeParams(buildMode, circleStart, cubeFilling, planeFilling, planeFacing, raisedEdge, replaceMode);
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
