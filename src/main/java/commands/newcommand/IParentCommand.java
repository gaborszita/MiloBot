package commands.newcommand;

import java.util.List;

public interface IParentCommand {
    String getSubCommandsText(String prefix);
    void addSubCommand(SubCommand subCommand);
    List<SubCommand> getSubCommands();
}
