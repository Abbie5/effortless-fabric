package dev.effortless.core.event.client;

import com.mojang.brigadier.CommandDispatcher;
import dev.effortless.core.event.api.Event;
import dev.effortless.core.event.api.EventFactory;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public class ClientCommandEvent {

    public static final Event<Register> REGISTER = EventFactory.createArrayBacked(Register.class, (listeners) -> (dispatcher, commandBuildContext) -> {
        for (Register listener : listeners) {
            listener.onRegister(dispatcher, commandBuildContext);
        }
    });

    @FunctionalInterface
    public interface Register {
        void onRegister(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext);
    }
}
