package dev.effortless.building.operation;

public enum BlockInteractionResult {
    SUCCESS,
    SUCCESS_PREVIEW,
    CONSUME,

    FAIL_PLAYER_NULL,
    FAIL_PLAYER_SPECTATOR,
    FAIL_PLAYER_EMPTY_INV,
    FAIL_PLAYER_ABILITY_CANNOT_BUILD,
    FAIL_PLAYER_CANNOT_USE_ITEM_TO_ATTACK,
    FAIL_PLAYER_CANNOT_USE_GAME_MASTER_BLOCKS,

    FAIL_LEVEL_FEATURE_LIMIT,
    FAIL_LEVEL_INTERACT_RESTRICTED,
    FAIL_LEVEL_ADVENTURE_LIMIT,

    FAIL_PLAYER_ITEM_NOT_BLOCK,
    FAIL_PLAYER_ITEM_CANNOT_ATTACK_BLOCK,

    FAIL_BLOCK_STATE_AIR,
    FAIL_BLOCK_STATE_NULL,
    FAIL_BLOCK_STATE_FLAG_CANNOT_REPLACE,

    FAIL_INTERNAL_SET_BLOCK,

    FAIL_UNKNOWN;

    public static BlockInteractionResult sidedSuccess(boolean bl) {
        return bl ? SUCCESS : CONSUME;
    }

    public boolean consumesAction() {
        return this == SUCCESS || this == SUCCESS_PREVIEW || this == CONSUME;
    }

    public boolean success() {
        return this == SUCCESS || this == SUCCESS_PREVIEW || this == CONSUME;
    }

    public boolean fail() {
        return this != SUCCESS && this != SUCCESS_PREVIEW && this != CONSUME;
    }

    public boolean shouldSwing() {
        return this == SUCCESS;
    }

    public boolean shouldAwardStats() {
        return this == SUCCESS || this == CONSUME;
    }
}