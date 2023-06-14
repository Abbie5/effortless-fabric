package dev.huskcasaca.effortless.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Player.class)
public abstract class PlayerPickupMixin {

    @ModifyArgs(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
    public void inflate(Args args) {
//        final var player = ((Player) (Object) this);
//        var enable = EffortlessBuilder.getInstance().getModeSettings(player).enableMagnet();
//        var maxReach = ReachHelper.getReachSettings(player).maxReachDistance();
//
//        if (enable) {
//            args.set(0, maxReach);
//            args.set(1, maxReach);
//            args.set(2, maxReach);
//        }
    }

}
