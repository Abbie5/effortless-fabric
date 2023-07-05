package dev.effortless.core.mixin;

import com.mojang.brigadier.CommandDispatcher;
import dev.effortless.core.event.client.ClientCommandEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class CommandMixin {

    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At(value = "TAIL", target = "Lnet/minecraft/server/commands/WorldBorderCommand;register(Lcom/mojang/brigadier/CommandDispatcher;)V"))
    private void addCommands(Commands.CommandSelection commandSelection, CommandBuildContext commandBuildContext, CallbackInfo ci) {
        ClientCommandEvent.REGISTER.invoker().onRegister(dispatcher, commandBuildContext);
    }

}
