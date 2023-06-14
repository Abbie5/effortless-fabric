package dev.huskcasaca.effortless.building.operation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public interface Operation<P, E, R> {

    R perform(P performer);

    R preview(P performer, E extras);

    ItemStack getRequiredItemStack();

    ItemStack getResultItemStack();

    BlockPos getPosition();

    Type getType();

    enum Type {
        WORLD_PLACE_OP,
        WORLD_BREAK_OP,
        WORLD_PERFORM_OP,
    }

    interface Preview<O extends Operation<P, ?, R>, P, R> {

        void render(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, O operation, R result);
    }


}
