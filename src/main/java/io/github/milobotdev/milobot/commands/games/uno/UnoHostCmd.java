package io.github.milobotdev.milobot.commands.games.uno;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.instance.model.*;
import io.github.milobotdev.milobot.games.hungergames.model.LobbyEntry;
import io.github.milobotdev.milobot.games.uno.UnoGame;
import io.github.milobotdev.milobot.utility.datatypes.CircularLinkedList;
import io.github.milobotdev.milobot.utility.lobby.BotLobby;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UnoHostCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, Aliases, Instance {

    private final ExecutorService executorService;

    public UnoHostCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new SubcommandData("host", "Host a game of Uno!")
                .addOptions(new OptionData(OptionType.INTEGER, "max-players", "Maximum number of players", false)
                        .setRequiredRange(2, 8));
    }

    @Override
    public List<String> getCommandArgs() {
        return List.of("*maxPlayers");
    }

    @Override
    public boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        if (args.size() > 0) {
            try {
                int i = Integer.parseInt(args.get(0));
                if (i < 2 || i > 4) {
                    event.getChannel().sendMessage("maxPlayers must be a number between 2 and 4.").queue();
                    return false;
                }
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("maxPlayers must be a number between 2 and 4.").queue();
                return false;
            }
        }
        return true;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        try {
            int maxPlayers = 4;
            MessageChannel channel = event.getChannel();
            BotLobby unoLobby = new BotLobby("Uno Lobby", event.getAuthor(),
                    (entries, message) -> {
                        ArrayList<LobbyEntry> participants = new ArrayList<>();
                        entries.forEach((players, npcs) -> {
                            npcs.forEach(npc -> participants.add(new LobbyEntry(npc.getName())));
                            players.forEach(user -> participants.add(new LobbyEntry(user)));
                        });
                        UnoGame unoGame = new UnoGame(participants);
                        unoGame.start(channel, message);
                    }, 2, 4);
            if (args.size() > 0) {
                int i = Integer.parseInt(args.get(0));
                unoLobby.setMaxPlayers(i);
                unoLobby.initialize(channel);
            } else {
                unoLobby.setMaxPlayers(maxPlayers);
                unoLobby.initialize(channel);
            }
        } catch (CantCreateLobbyException e) {
            event.getMessage().reply("You can't create a new lobby when you are already in one.").queue();
        }

    }

    @Override
    public void executeCommand(SlashCommandEvent event) {
        try {
            event.deferReply().queue();
            int maxPlayers = 4;
            MessageChannel channel = event.getChannel();
            BotLobby unoLobby = new BotLobby("Uno Lobby", event.getUser(),
                    (entries, message) -> {
                        ArrayList<LobbyEntry> participants = new ArrayList<>();
                        entries.forEach((players, npcs) -> {
                            npcs.forEach(npc -> participants.add(new LobbyEntry(npc.getName())));
                            players.forEach(user -> participants.add(new LobbyEntry(user)));
                        });
                        UnoGame unoGame = new UnoGame(participants);
                        unoGame.start(channel, message);
                    }, 2, 4);
            if (event.getOption("max-players") != null) {
                int i = Integer.parseInt(String.valueOf(event.getOption("max-players").getAsLong()));
                unoLobby.setMaxPlayers(i);
                unoLobby.initialize(event);
            } else {
                unoLobby.setMaxPlayers(maxPlayers);
                unoLobby.initialize(event);
            }
        } catch (CantCreateLobbyException e) {
            event.reply("You can't create a new lobby when you are already in one.").queue();
        }

    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("start", "play", "h");
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public InstanceData isInstanced() {
        return new InstanceData(true, 1800, new GameType("Uno",
                userId -> UnoGame.unoGames.forEach(unoGame -> unoGame.removePlayerMidGame(userId)),
                true,
                userId -> {
                    for(UnoGame unoGame : UnoGame.unoGames) {
                        for(LobbyEntry lobbyEntry : unoGame.getPlayerList().toList()) {
                            if(lobbyEntry.getUserId() == userId) {
                                return true;
                            }
                        }
                    }
                    return false;
                }));
    }
}
