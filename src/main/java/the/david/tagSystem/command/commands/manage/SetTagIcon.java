package the.david.tagSystem.command.commands.manage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import the.david.tagSystem.command.SubCommand;
import the.david.tagSystem.data.ConfigManager;
import the.david.tagSystem.impl.Tag;
import the.david.tagSystem.manager.TagManager;

import java.util.Map;

public class SetTagIcon implements SubCommand{
	@Override
	public Boolean opOnly(){
		return true;
	}

	@Override
	public void execute(Player player, Map<String, String> parsedArgs){
		String id = parsedArgs.get("id");
		Tag tag = TagManager.getTag(id);
		if(tag == null){
			player.sendMessage(Component.text("找不到稱號: " + id, NamedTextColor.RED));
			return;
		}
		Material material = player.getInventory().getItemInMainHand().getType();
		if(material == Material.AIR){
			player.sendMessage(Component.text("請手持一個物品！", NamedTextColor.RED));
			return;
		}
		tag.setIconMaterial(material);
		ConfigManager.setTagToConfig(tag);
		player.sendMessage(Component.text("已將稱號 " + id + " 的圖示設為 " + material.name(), NamedTextColor.GREEN));
	}
}