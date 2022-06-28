package utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Helpful functions for embeds.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class EmbedUtils {

	/**
	 * Adds ❌ as an emoji under the message that when added by the user who issued the command removes the message.
	 * If the emoji isnt clicked in under 60 seconds its no longer possible to delete the message.
	 *
	 * @return The consumer that adds the emoji.
	 */
	@NotNull
	public static Consumer<Message> deleteEmbedButton(@NotNull MessageReceivedEvent event, String consumerId) {
		return (message) -> {
			message.addReaction("❌").queue();
			ListenerAdapter listener = new ListenerAdapter() {
				@Override
				public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
					String messageId = event.getMessageId();
					if (Objects.requireNonNull(event.getUser()).getId().equals(consumerId) &&
							event.getReactionEmote().getAsReactionCode().equals("❌") && message.getId().equals(messageId)) {
						event.getChannel().deleteMessageById(messageId).queue();
						event.getJDA().removeEventListener(this);
					}
				}
			};
			message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(listener), 1, TimeUnit.MINUTES);
			message.getJDA().addEventListener(listener);
		};
	}

	/**
	 * Applies some default styling to an embed.
	 */
	public static void styleEmbed(@NotNull MessageReceivedEvent event, @NotNull EmbedBuilder embed) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

		embed.setColor(Color.BLUE);
		embed.setAuthor(event.getAuthor().getName(), null, event.getAuthor().getAvatarUrl());
		embed.setFooter(dtf.format(LocalDateTime.now()));
	}

	/**
	 * Adds a simple paginator to the specified message.
	 */
	public static void createPaginator(@NotNull MessageReceivedEvent event, String title, @NotNull ArrayList<EmbedBuilder> pages,
									   @NotNull Message message, String consumerId) {
		message.clearReactions().queue();
		EmbedBuilder embedBuilder = pages.get(0);
		message.editMessageEmbeds(embedBuilder.build()).queue(message1 -> {
					final int[] currentPage = {0};
					if (pages.size() > 1) {
						message.addReaction("◀").queue();
						message.addReaction("▶").queue();
					}
					message.addReaction("❌").queue();
					ListenerAdapter totalGames = new ListenerAdapter() {
						@Override
						public void onMessageReactionAdd(@NotNull MessageReactionAddEvent eventReaction2) {
							if (Objects.requireNonNull(eventReaction2.getUser()).getId().equals(consumerId)
									&& message.getId().equals(message1.getId())) {
								String asReactionCode = eventReaction2.getReactionEmote().getAsReactionCode();
								EmbedBuilder newEmbed = new EmbedBuilder();
								newEmbed.setTitle(title);
								EmbedUtils.styleEmbed(event, newEmbed);
								switch (asReactionCode) {
									case "◀":
										message.removeReaction(asReactionCode, eventReaction2.getUser()).queue();
										if (!(currentPage[0] - 1 < 0)) {
											currentPage[0]--;
											newEmbed.setDescription(pages.get(currentPage[0]).getDescriptionBuilder());
											message.editMessageEmbeds(newEmbed.build()).queue();
										}
										break;
									case "▶":
										message.removeReaction(asReactionCode, eventReaction2.getUser()).queue();
										if (!(currentPage[0] + 1 == pages.size())) {
											currentPage[0]++;
											newEmbed.setDescription(pages.get(currentPage[0]).getDescriptionBuilder());
											message.editMessageEmbeds(newEmbed.build()).queue();
										}
										break;
									case "❌":
										event.getJDA().removeEventListener(this);
										event.getChannel().deleteMessageById(message.getId()).queue();
										break;
								}
							}
						}
					};
					message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(totalGames),
							2, TimeUnit.MINUTES);
					message.getJDA().addEventListener(totalGames);
				}
		);
	}
}
