package io.github.milobotdev.milobot.commands.utility;

import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class InviteCmd extends ParentCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, DefaultCommandArgs, UtilityCmd  {

    private final ExecutorService executorService;
    public InviteCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }


    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new CommandData("invite", "Sends an invite link to add the bot to another server.");
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        event.getChannel().sendMessage(generateInviteUrl(event.getJDA())).queue();
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event) {
        event.reply(generateInviteUrl(event.getJDA())).setEphemeral(true).queue();
    }

    private @NotNull String generateInviteUrl(@NotNull JDA jda) {
         return jda.getInviteUrl(Permission.ADMINISTRATOR);
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }
}

