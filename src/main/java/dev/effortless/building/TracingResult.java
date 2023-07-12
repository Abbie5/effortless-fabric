package dev.effortless.building;

import net.minecraft.world.phys.BlockHitResult;

import java.util.stream.Stream;

public record TracingResult(
        Stream<BlockHitResult> result,
        Type type
) {

    public static TracingResult success(Stream<BlockHitResult> result) {
        return new TracingResult(result, Type.SUCCESS);
    }

    public static TracingResult partial(Stream<BlockHitResult> result) {
        return new TracingResult(result, Type.SUCCESS_PARTIAL);
    }

    public static TracingResult pass() { // placing
        return new TracingResult(Stream.empty(), Type.PASS);
    }

    public static TracingResult fail() {
        return new TracingResult(Stream.empty(), Type.FAIL);
    }

    public enum Type {
        SUCCESS,
        SUCCESS_PARTIAL,
        PASS,
        FAIL;

        public boolean isSuccess() {
            return this == SUCCESS || this == SUCCESS_PARTIAL;
        }
    }

}
