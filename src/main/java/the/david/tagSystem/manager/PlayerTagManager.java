package the.david.tagSystem.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
		User user = luckPerms.getUserManager().getUser(player.getUniqueId());
		if(user == null){
			return;
		}
		if(tag.getTagType().equals(Tag.TagType.SUFFIX)){
			clearPlayerSuffixTag(player);
			SuffixNode suffixNode = SuffixNode.builder(tag.getText(), 1).build();
			Node node = Node.builder("tagsystem.suffix.tagid." + tag.getId()).build();
			user.data().add(suffixNode);
			user.data().add(node);
		}else if(tag.getTagType().equals(Tag.TagType.PREFIX)){
			clearPlayerPrefixTag(player);
			PrefixNode prefixNode = PrefixNode.builder(tag.getText(), 1).build();
			Node node = Node.builder("tagsystem.prefix.tagid." + tag.getId()).build();
			user.data().add(prefixNode);
			user.data().add(node);
		}
		luckPerms.getUserManager().saveUser(user);
		player.sendMessage(Component.text("成功設定稱號為 ", NamedTextColor.GREEN).append(LegacyComponentSerializer.legacy('&').deserialize(tag.getText())));
	}

	public static void clearPlayerSuffixTag(Player player){
		User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
		user.data().clear(e -> e.getKey().startsWith("tagsystem.suffix.tagid"));
		user.data().clear(e -> e.getType() == NodeType.SUFFIX);
		luckPerms.getUserManager().saveUser(user);
	}
	public static void clearPlayerPrefixTag(Player player){
		User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
		user.data().clear(e -> e.getKey().startsWith("tagsystem.prefix.tagid"));
		user.data().clear(e -> e.getType() == NodeType.PREFIX);
		luckPerms.getUserManager().saveUser(user);
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
					.append(LegacyComponentSerializer.legacy('&').deserialize(prefixTag.getText()))
					.append(Component.text(" 的權限", NamedTextColor.RED)));
		}

		Tag suffixTag = getPlayerSuffixTag(player);
		if (suffixTag != null && !TagManager.hasTagPermission(player, suffixTag)) {
			clearPlayerSuffixTag(player);
			player.sendMessage(Component.text("您已失去後綴稱號 ", NamedTextColor.RED)
					.append(LegacyComponentSerializer.legacy('&').deserialize(suffixTag.getText()))
					.append(Component.text(" 的權限", NamedTextColor.RED)));
		}
	}
}
