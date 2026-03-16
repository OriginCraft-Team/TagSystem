package the.david.tagSystem.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import org.bukkit.entity.Player;
import the.david.tagSystem.impl.Tag;

import java.util.Collection;

import static the.david.tagSystem.Main.luckPerms;

public class PlayerTagManager{
	public PlayerTagManager(){

	}
	public static void setPlayerTag(Player player, Tag tag){
		if(!TagManager.hasTagPermission(player, tag)){
			player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
			return;
		}
		if(tag.getTagType().equals(Tag.TagType.SUFFIX)){
			luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
				user.data().clear(e -> e.getKey().startsWith("tagsystem.suffix.tagid"));
				user.data().clear(e -> e.getType() == NodeType.SUFFIX);
				user.data().add(SuffixNode.builder(tag.getLuckPermsText(), 1).build());
				user.data().add(Node.builder("tagsystem.suffix.tagid." + tag.getId()).build());
			});
		}else if(tag.getTagType().equals(Tag.TagType.PREFIX)){
			luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
				user.data().clear(e -> e.getKey().startsWith("tagsystem.prefix.tagid"));
				user.data().clear(e -> e.getType() == NodeType.PREFIX);
				user.data().add(PrefixNode.builder(tag.getLuckPermsText(), 1).build());
				user.data().add(Node.builder("tagsystem.prefix.tagid." + tag.getId()).build());
			});
		}
		player.sendMessage(Component.text("成功設定稱號為 ", NamedTextColor.GREEN).append(MiniMessage.miniMessage().deserialize(tag.getText())));
	}

	public static void clearPlayerSuffixTag(Player player){
		luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
			user.data().clear(e -> e.getKey().startsWith("tagsystem.suffix.tagid"));
			user.data().clear(e -> e.getType() == NodeType.SUFFIX);
		});
	}
	public static void clearPlayerPrefixTag(Player player){
		luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
			user.data().clear(e -> e.getKey().startsWith("tagsystem.prefix.tagid"));
			user.data().clear(e -> e.getType() == NodeType.PREFIX);
		});
	}

	public static Tag getPlayerSuffixTag(Player player){
		User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
		Collection<Node> nodes = user.getNodes();
		Node tagIdNode = null;
		for(Node node : nodes){
			if(!node.getKey().startsWith("tagsystem.suffix.tagid")){
				continue;
			}
			if(!node.getType().equals(NodeType.SUFFIX)){
				tagIdNode = node;
			}
		}
		if(tagIdNode == null){
			return null;
		}
		String tagId = tagIdNode.getKey().replaceFirst("tagsystem.suffix.tagid.", "");
		return TagManager.getTag(tagId);
	}
	public static Tag getPlayerPrefixTag(Player player){
		User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
		Collection<Node> nodes = user.getNodes();
		Node tagIdNode = null;
		for(Node node : nodes){
			if(!node.getKey().startsWith("tagsystem.prefix.tagid")){
				continue;
			}
			if(!node.getType().equals(NodeType.PREFIX)){
				tagIdNode = node;
			}
		}
		if(tagIdNode == null){
			return null;
		}
		String tagId = tagIdNode.getKey().replaceFirst("tagsystem.prefix.tagid.", "");
		return TagManager.getTag(tagId);
	}

	public static void checkAndClearInvalidTags(Player player) {
		Tag prefixTag = getPlayerPrefixTag(player);
		if (prefixTag != null && !TagManager.hasTagPermission(player, prefixTag)) {
			clearPlayerPrefixTag(player);
			player.sendMessage(Component.text("您已失去前綴稱號 ", NamedTextColor.RED)
					.append(MiniMessage.miniMessage().deserialize(prefixTag.getText()))
					.append(Component.text(" 的權限", NamedTextColor.RED)));
		}

		Tag suffixTag = getPlayerSuffixTag(player);
		if (suffixTag != null && !TagManager.hasTagPermission(player, suffixTag)) {
			clearPlayerSuffixTag(player);
			player.sendMessage(Component.text("您已失去後綴稱號 ", NamedTextColor.RED)
					.append(MiniMessage.miniMessage().deserialize(suffixTag.getText()))
					.append(Component.text(" 的權限", NamedTextColor.RED)));
		}
	}
}
