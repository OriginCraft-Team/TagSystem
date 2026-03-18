package the.david.tagSystem.command.commands.manage;

import org.bukkit.entity.Player;
import the.david.tagSystem.command.SubCommand;
import the.david.tagSystem.impl.Tag;
import the.david.tagSystem.manager.TagManager;

import java.util.Map;

public class SetTagWeight implements SubCommand{
	@Override
	public Boolean opOnly(){
		return true;
	}

	@Override
	public void execute(Player player, Map<String, String> parsedArgs){
		Tag tag = TagManager.getTag(parsedArgs.get("id"));
		if(tag == null){
			player.sendMessage("Tag not found");
			return;
		}
		try{
			int weight = Integer.parseInt(parsedArgs.get("value"));
			TagManager.setTagWeight(tag, weight);
			player.sendMessage("Weight set to " + weight);
		}catch(NumberFormatException e){
			player.sendMessage("Invalid weight: " + parsedArgs.get("value"));
		}
	}
}
