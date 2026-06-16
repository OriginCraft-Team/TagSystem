package the.david.tagSystem.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import the.david.tagSystem.command.SubCommand;
import the.david.tagSystem.manager.CustomSuffixValidator;
import the.david.tagSystem.manager.PlayerTagManager;

import java.util.Map;

/**
 * {@code /ts customsuffix <content>} — sets the player's custom suffix from a MiniMessage string.
 * This is the command exported by the online editor at {@link CustomSuffixValidator#WEBUI_URL};
 * the same content is re-validated here because players may run it without going through the website.
 */
public class CustomSuffix implements SubCommand{
	@Override
	public Boolean opOnly(){
		return false;
	}

	@Override
	public void execute(Player player, Map<String, String> parsedArgs){
		if(!player.hasPermission("tagsystem.tag." + PlayerTagManager.CUSTOM_SUFFIX_ID)){
			player.sendMessage(Component.text("您沒有自訂後綴稱號的權限！", NamedTextColor.RED));
			return;
		}

		String content = parsedArgs.get("content").strip();
		String error = CustomSuffixValidator.validate(content);
		if(error != null){
			// 用純文字 Component，避免把玩家輸入的標籤名（如 <hover>）再次當成 MiniMessage 解析
			player.sendMessage(Component.text("✘ " + error, NamedTextColor.RED));
			return;
		}

		PlayerTagManager.setPlayerCustomSuffix(player, content);
		player.sendMessage(Component.text("✔ 已設定自訂後綴稱號為 ", NamedTextColor.GREEN)
				.append(CustomSuffixValidator.RESTRICTED_MM.deserialize(content)));
	}
}
