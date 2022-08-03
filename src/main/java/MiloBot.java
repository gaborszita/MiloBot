import commands.CommandHandler;
import commands.CommandLoader;
import commands.games.blackjack.BlackjackPlayCmd;
import database.DatabaseManager;
import database.queries.PrefixTableQueries;
import database.queries.UsersTableQueries;
import events.OnButtonInteractionEvent;
import events.OnReadyEvent;
import events.OnUserUpdateNameEvent;
import events.guild.OnGuildJoinEvent;
import events.guild.OnGuildLeaveEvent;
import games.Blackjack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Config;
import utility.Paginator;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The Main class from where the bot is started.
 */
public class MiloBot {

	private final static Logger logger = LoggerFactory.getLogger(MiloBot.class);

	public static void main(String[] args) throws LoginException, InterruptedException {
		DatabaseManager manager = DatabaseManager.getInstance();
		Config config = Config.getInstance();

		JDA bot = JDABuilder.createDefault(config.getBotToken(),
						GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_VOICE_STATES,
						GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_TYPING,
						GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_REACTIONS)
				.setActivity(Activity.watching("Morbius"))
				.addEventListeners(new CommandHandler(), new OnGuildJoinEvent(), new OnGuildLeaveEvent(),
						new OnReadyEvent(), new OnUserUpdateNameEvent(), new OnButtonInteractionEvent())
				.build().awaitReady();

		CommandLoader.loadAllCommands(bot);

		loadPrefixes(manager, config, bot);
		updateUserNames(manager, bot);

		Timer timer = new Timer();
		TimerTask clearBlackjackInstances = getClearBlackjackInstancesTask(bot);
		timer.schedule(clearBlackjackInstances, 1000 * 60 * 60, 1000 * 60 * 60);
	}

	/**
	 * Clears blackjack instances that haven't been used for 15 minutes every hour.
	 */
	@NotNull
	private static TimerTask getClearBlackjackInstancesTask(JDA bot) {
		return new TimerTask() {
			@Override
			public void run() {
				logger.info("Attempting to clear idle instances.");

				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
				TextChannel logs = Objects.requireNonNull(bot.getGuildById(Config.getInstance().getTestGuildId()))
						.getTextChannelsByName(Config.getInstance().getLoggingChannelName(), true).get(0);
				long currentNanoTime = System.nanoTime();

				Map<String, Blackjack> blackjackGames = BlackjackPlayCmd.blackjackGames;
				List<String> blackjackInstancesToRemove = new ArrayList<>();
				blackjackGames.forEach(
						(s, blackjack) -> {
							long startTime = blackjack.getStartTime();
							long elapsedTime = currentNanoTime - startTime;
							long elapsedTimeSeconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
							if (elapsedTimeSeconds > 900) {
								logger.info(String.format("Blackjack instance by: %s timed out. Time elapsed %d seconds.",
										s, elapsedTimeSeconds));
								blackjackInstancesToRemove.add(s);
							}
						}
				);

				Map<String, Paginator> paginatorInstances = Paginator.paginatorInstances;
				List<String> paginatorInstancesToRemove = new ArrayList<>();
				paginatorInstances.forEach(
						(s, paginator) -> {
							long startTime = paginator.getStartTime();
							long elapsedTime = currentNanoTime - startTime;
							long elapsedTimeSeconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
							if (elapsedTimeSeconds > 900) {
								logger.info(String.format("Paginator instance by: %s timed out. Time elapsed %d seconds.",
										s, elapsedTimeSeconds));
								paginatorInstancesToRemove.add(s);
							}
						}
				);

				if(blackjackInstancesToRemove.size() == 0) {
					logger.info("No blackjack instances timed out.");
				} else {
					for(String s : blackjackInstancesToRemove) {
						blackjackGames.remove(s);
					}
					logger.info(String.format("Removed %d blackjack instances.", blackjackInstancesToRemove.size()));
				}

				if(paginatorInstancesToRemove.size() == 0) {
					logger.info("No paginator instances timed out.");
				} else {
					for(String s : paginatorInstancesToRemove) {
						paginatorInstances.remove(s);
					}
					logger.info(String.format("Removed %d paginator instances.", paginatorInstancesToRemove.size()));
				}

				EmbedBuilder logEmbed = new EmbedBuilder();
				logEmbed.setTitle("Idle Instance Cleanup");
				logEmbed.setColor(Color.green);
				logEmbed.setFooter(dtf.format(LocalDateTime.now()));
				logEmbed.setDescription(String.format("Cleared `%d` blackjack instances.\nCleared `%d` paginator instances.",
						blackjackInstancesToRemove.size(), paginatorInstancesToRemove.size()));
				logs.sendMessageEmbeds(logEmbed.build()).queue();
			}
		};
	}

	/**
	 * Loads prefixes for guilds that have the bot but are not in the database yet.
	 */
	private static void loadPrefixes(@NotNull DatabaseManager manager, Config config, @NotNull JDA bot) {
		List<Guild> guilds = bot.getGuilds();
		ArrayList<String> result = manager.query(PrefixTableQueries.getAllPrefixes, DatabaseManager.QueryTypes.RETURN);
		for (Guild guild : guilds) {
			String id = guild.getId();
			if (!result.contains(id)) {
				logger.info(String.format("Guild: %s does not have a configured prefix.", id));
				manager.query(PrefixTableQueries.addServerPrefix, DatabaseManager.QueryTypes.UPDATE, id, config.getDefaultPrefix());
				CommandHandler.prefixes.put(id, config.getDefaultPrefix());
			}
		}
	}

	/**
	 * Checks if any users updated their name. If so update it in the database.
	 */
	private static void updateUserNames(@NotNull DatabaseManager manager, @NotNull JDA bot) {
		List<Guild> guilds = bot.getGuilds();
		ArrayList<String> result = manager.query(UsersTableQueries.getAllUserIdsAndNames, DatabaseManager.QueryTypes.RETURN);
		HashMap<String, String> users = new HashMap<>();
		for (int i = 0; i < result.size(); i += 2) {
			if (!(i + 2 == result.size() && users.containsKey(result.get(i)))) {
				users.put(result.get(i), result.get(i + 1));
			}
		}
		for (Guild guild : guilds) {
			List<Member> members = new ArrayList<>();
			guild.loadMembers().onSuccess(loadedMembers -> {
				members.addAll(loadedMembers);
				for (Member member : members) {
					User user = member.getUser();
					if (!(user.isBot())) {
						String userId = user.getId();
						if (users.containsKey(userId)) {
							String nameInDatabase = users.get(userId);
							String currentName = user.getName();
							if (!(nameInDatabase.equals(currentName))) {
								logger.info(String.format("%s changed their name to: %s.", nameInDatabase, currentName));
								manager.query(UsersTableQueries.updateUserName, DatabaseManager.QueryTypes.UPDATE, currentName, userId);
							}
						}
					}
				}
			});

		}
	}
}
