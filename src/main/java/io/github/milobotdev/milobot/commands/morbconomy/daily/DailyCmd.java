package io.github.milobotdev.milobot.commands.morbconomy.daily;

import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultChannelTypes;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultFlags;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultSlashParentCommand;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultTextParentCommand;
import io.github.milobotdev.milobot.commands.morbconomy.MorbconomyCmd;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ExecutorService;

public class DailyCmd extends ParentCommand implements DefaultTextParentCommand, DefaultSlashParentCommand,
        DefaultFlags, DefaultChannelTypes, MorbconomyCmd {

    private final ExecutorService executorService;

    public DailyCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new CommandData("daily", "Collect your daily reward.");
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }
}
