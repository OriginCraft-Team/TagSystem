package the.david.tagSystem.command.commands.manage;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import the.david.tagSystem.command.SubCommand;
import the.david.tagSystem.impl.Tag;
import the.david.tagSystem.manager.TagManager;

import java.util.Map;

public class AddTag implements SubCommand{
	@Override
	public Boolean opOnly(){
		return true;
	}

	@Override
	public void execute(Player player, Map<String, String> parsedArgs){
		String type = parsedArgs.get("type");
		Tag.TagType tagType;
		try{
			tagType = Tag.TagType.valueOf(type.toUpperCase());
		}catch(IllegalArgumentException e){
			player.sendMessage("type not found");
			return;
		}
		String id = parsedArgs.get("id");
		String text = parsedArgs.get("text");
		String description = parsedArgs.get("description");
		boolean hoverDescription = Boolean.parseBoolean(parsedArgs.get("hover_description"));
		Material iconMaterial = player.getInventory().getItemInMainHand().getType();
		if(iconMaterial == Material.AIR) iconMaterial = Material.NAME_TAG;
		Tag tag = new Tag(id, text, description, iconMaterial, tagType, hoverDescription, true, 0);
		TagManager.addTag(tag);
	}
}
