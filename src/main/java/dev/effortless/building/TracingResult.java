package dev.effortless.building;

import net.minecraft.world.phys.BlockHitResult;

import java.util.Collections;
import java.util.List;

public record TracingResult(
        List<BlockHitResult> result,
        Type type
) {

    public static TracingResult success(List<BlockHitResult> result) {
        return new TracingResult(result, Type.SUCCESS);
    }

    public static TracingResult partial(List<BlockHitResult> result) {
        return new TracingResult(result, Type.SUCCESS_PARTIAL);
    }

    public static TracingResult pass() { // placing
        return new TracingResult(Collections.emptyList(), Type.PASS);
    }

    public static TracingResult fail() {
        return new TracingResult(Collections.emptyList(), Type.FAIL);
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
