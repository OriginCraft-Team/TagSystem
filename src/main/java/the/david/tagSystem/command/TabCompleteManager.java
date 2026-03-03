package the.david.tagSystem.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import the.david.tagSystem.impl.Tag;
import the.david.tagSystem.manager.TagManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TabCompleteManager implements TabCompleter{
	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args){
		if(!(sender instanceof Player)){
			return null;
		}
		Player player = (Player) sender;
		return matchCommand(args, player.isOp());
	}

	private List<String> matchCommand(String[] args, Boolean isOp){
		List<String> matchList = new ArrayList<>();
		for(Map.Entry<String, SubCommand> entry : CommandManager.subCommands.entrySet()){
			if(entry.getValue().opOnly() && !isOp){
				continue;
			}
			String[] commandParts = entry.getKey().split(" ");
			int needLength = commandParts.length;
			if(args.length > needLength){
				continue;
			}

			boolean matches = true;
			for(int i = 0; i < args.length - 1; i++){
				boolean isHitArg = commandParts[i].startsWith("{") && commandParts[i].endsWith("}") || commandParts[i].startsWith("(") && commandParts[i].endsWith(")");
				if(!Objects.equals(args[i], commandParts[i]) && !isHitArg){
					matches = false;
					break;
				}
			}

			if(matches){
				if(commandParts[args.length - 1].equals("{id}")){
					for(Tag tag : TagManager.getTagCollection()){
						matchList.add(tag.getId());
					}
				}
				if(commandParts[args.length - 1].equals("{type}")){
					matchList.add("prefix");
					matchList.add("suffix");
				}
				matchList.add(commandParts[args.length - 1]);
			}
		}
		return matchList;
	}
}
