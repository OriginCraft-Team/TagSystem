package the.david.tagSystem.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import the.david.tagSystem.command.commands.*;
import the.david.tagSystem.command.commands.manage.AddTag;
import the.david.tagSystem.command.commands.manage.SetTagDescription;
import the.david.tagSystem.command.commands.manage.SetTagIcon;
import the.david.tagSystem.command.commands.manage.SetTagText;
import the.david.tagSystem.command.commands.manage.SetTagWeight;
import the.david.tagSystem.impl.Pair;

import java.util.HashMap;
import java.util.Map;

public class CommandManager implements CommandExecutor{
	public CommandManager(){
		subCommands.put("manage add {type} {id} {text} {description} {hover_description}", new AddTag());
		subCommands.put("manage set icon {id}", new SetTagIcon());
		subCommands.put("manage set text {id} {text}", new SetTagText());
		subCommands.put("manage set description {id} {description}", new SetTagDescription());
		subCommands.put("manage set weight {id} {value}", new SetTagWeight());
		subCommands.put("set {id}", new SetTag());
		subCommands.put("reload", new Reload());
		subCommands.put("menu", new Menu());
		subCommands.put("get suffix", new GetSuffixTag());
		subCommands.put("get prefix", new GetPrefixTag());
		subCommands.put("clear suffix", new ClearSuffixTag());
		subCommands.put("clear prefix", new ClearPrefixTag());
	}

	public static final Map<String, SubCommand> subCommands = new HashMap<>();

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args){
		if(!(sender instanceof Player)){
			return false;
		}
		Player player = (Player) sender;

		Pair<SubCommand, Map<String, String>> matchedData = matchCommand(args, player.isOp());
		if(matchedData != null){
			matchedData.getKey().execute(player, matchedData.getValue());
			return true;
		}

		return false;
	}

	private Pair<SubCommand, Map<String, String>> matchCommand(String[] args, Boolean isOp){
		for(Map.Entry<String, SubCommand> entry : subCommands.entrySet()){
			if(entry.getValue().opOnly() && !isOp){
				continue;
			}
			String[] commandParts = entry.getKey().split(" ");
			int needLength = commandParts.length;
			int maxLength = commandParts.length;
			for(String commandPart : commandParts){
				if(commandPart.startsWith("(") && commandPart.endsWith(")")){
					needLength--;
				}
			}
			if(args.length < needLength || args.length > maxLength){
				continue;
			}

			Map<String, String> parsedArgs = new HashMap<>();
			boolean matches = true;
			for(int i = 0; i < commandParts.length; i++){
				if(commandParts[i].startsWith("{") && commandParts[i].endsWith("}")){
					String paramName = commandParts[i].substring(1, commandParts[i].length() - 1);
					parsedArgs.put(paramName, args[i]);

				}else if(commandParts[i].startsWith("(") && commandParts[i].endsWith(")")){
					String paramName = commandParts[i].substring(1, commandParts[i].length() - 1);
					if(args.length > i){
						parsedArgs.put(paramName, args[i]);
					}
				}else if(!commandParts[i].equals(args[i])){
					matches = false;
					break;
				}
			}

			if(matches){
				return new Pair<>(entry.getValue(), parsedArgs);
			}
		}
		return null;
	}
}
