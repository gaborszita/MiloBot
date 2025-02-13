package io.github.milobotdev.milobot.commands.command.extensions;

import io.github.milobotdev.milobot.commands.GuildPrefixManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import io.github.milobotdev.milobot.utility.EmbedUtils;

import java.util.Objects;

public interface Permissions {
    boolean hasPermission(@NotNull IPermissionHolder permissionHolder);
    @NotNull String getPermissionsText();

    default void sendMissingPermissions(@NotNull MessageReceivedEvent event) {
        String prefix = GuildPrefixManager.getInstance().getPrefix(event.getGuild().getIdLong());
        User user = event.getAuthor();
        EmbedBuilder embed = new EmbedBuilder();
        String commandName = ((TextCommand) this).getCommandName();
        embed.setTitle(String.format("Missing required permissions for: %s%s", prefix, commandName));
        String missingPermissionsText = "You are missing the following permission(s): " +
                getPermissionsText();
        embed.setDescription(missingPermissionsText);
        EmbedUtils.styleEmbed(embed, user);
        event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    default void sendMissingPermissions(@NotNull SlashCommandEvent event) {
        String prefix = GuildPrefixManager.getInstance().getPrefix(Objects.requireNonNull(event.getGuild()).getIdLong());
        User user = event.getUser();
        EmbedBuilder embed = new EmbedBuilder();
        String commandName = ((TextCommand) this).getCommandName();
        embed.setTitle(String.format("Missing required permissions for: %s%s", prefix, commandName));
        String missingPermissionsText = "You are missing the following permission(s): " +
                getPermissionsText();
        embed.setDescription(missingPermissionsText);
        EmbedUtils.styleEmbed(embed, user);
        event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
    }
}
