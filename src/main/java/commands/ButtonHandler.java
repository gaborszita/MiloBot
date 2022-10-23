package commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Users;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class ButtonHandler extends ListenerAdapter {

    public enum DeferType {
        NONE, REPLY, EDIT
    }
    private record ButtonRecord(boolean onlyOnUserMatch, DeferType deferType, ExecutorService service, Consumer<ButtonClickEvent> action) {}
    private final HashMap<String, ButtonRecord> buttons = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ButtonHandler.class);

    public void registerButton(String id, boolean onlyOnUserMatch, DeferType deferType,
                               ExecutorService service, Consumer<ButtonClickEvent> action) {
        buttons.put(id, new ButtonRecord(onlyOnUserMatch, deferType, service, action));
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        String[] id = event.getComponentId().split(":");
        if (id.length < 2) {
            logger.warn("Button id is invalid: " + event.getComponentId());
            return;
        }
        String authorId = id[0];
        String type = id[1];
        User user = event.getUser();
        Users.getInstance().addUserIfNotExists(event.getUser().getIdLong());

        if (buttons.containsKey(type)) {
            ButtonRecord record = buttons.get(type);
            if (authorId.equals(user.getId()) || !record.onlyOnUserMatch()) {
                if (record.deferType() == DeferType.REPLY) {
                    event.deferReply().queue();
                } else if (record.deferType() == DeferType.EDIT) {
                    event.deferEdit().queue();
                }
                record.service().submit(() -> record.action().accept(event));
            }
        }
    }
}
