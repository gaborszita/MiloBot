package commands;

import commands.utility.HelpCommand;
import commands.utility.InviteCommand;
import commands.utility.StatusCommand;

import java.util.*;

/**
 * Loads in every command to a static map.
 * @author Ruben Eekhof - rubeneehof@gmail.com
 */
public class CommandLoader {

    public static Map<List<String>, Command> commandList = new HashMap<>();

    public static void loadAllCommands() {
        // utility commands
        HelpCommand helpCommand = new HelpCommand();
        ArrayList<String> helpCommandKeys = new ArrayList<>(List.of(helpCommand.aliases));
        helpCommandKeys.add(helpCommand.commandName);
        commandList.put(helpCommandKeys, helpCommand);

        InviteCommand inviteCommand = new InviteCommand();
        ArrayList<String> inviteCommandKeys = new ArrayList<>(List.of(inviteCommand.aliases));
        inviteCommandKeys.add(inviteCommand.commandName);
        commandList.put(inviteCommandKeys, inviteCommand);

        StatusCommand statusCommand = new StatusCommand();
        ArrayList<String> statusCommandKeys = new ArrayList<>(List.of(statusCommand.aliases));
        statusCommandKeys.add(statusCommand.commandName);
        commandList.put(statusCommandKeys, statusCommand);
    }

}
