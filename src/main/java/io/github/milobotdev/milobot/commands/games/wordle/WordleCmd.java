package io.github.milobotdev.milobot.commands.games.wordle;

import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.games.GamesCmd;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class WordleCmd extends ParentCommand implements DefaultTextParentCommand, DefaultSlashParentCommand, Aliases,
        DefaultFlags, DefaultChannelTypes, GamesCmd {

    private final ExecutorService executorService;

    public WordleCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new CommandData("wordle", "Wordle brought to discord.");
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return this.executorService;
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("morble");
    }
}