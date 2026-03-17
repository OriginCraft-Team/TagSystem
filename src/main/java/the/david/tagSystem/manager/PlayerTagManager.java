package the.david.tagSystem.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;
import the.david.tagSystem.impl.Tag;

import java.util.Optional;

import static the.david.tagSystem.Main.luckPerms;

public class PlayerTagManager{
	public static void setPlayerTag(Player player, Tag tag){
		if(!TagManager.hasTagPermission(player, tag)){
			player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
			return;
		}
		if(tag.getTagType().equals(Tag.TagType.SUFFIX)){
			luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
				user.data().clear(e -> e.getKey().startsWith("tagsystem.suffix.tagid"));
				user.data().add(Node.builder("tagsystem.suffix.tagid." + tag.getId()).build());
			});
		}else if(tag.getTagType().equals(Tag.TagType.PREFIX)){
			luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
				user.data().clear(e -> e.getKey().startsWith("tagsystem.prefix.tagid"));
				user.data().add(Node.builder("tagsystem.prefix.tagid." + tag.getId()).build());
			});
		}
		player.sendMessage(Component.text("成功設定稱號為 ", NamedTextColor.GREEN).append(MiniMessage.miniMessage().deserialize(tag.getText())));
	}

	public static void clearPlayerSuffixTag(Player player){
		luckPerms.getUserManager().modifyUser(player.getUniqueId(), user ->
			user.data().clear(e -> e.getKey().startsWith("tagsystem.suffix.tagid")));
	}
	public static void clearPlayerPrefixTag(Player player){
		luckPerms.getUserManager().modifyUser(player.getUniqueId(), user ->
			user.data().clear(e -> e.getKey().startsWith("tagsystem.prefix.tagid")));
	}

	public static Tag getPlayerSuffixTag(Player player){
		User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
		return user.getNodes().stream()
				.filter(n -> n.getKey().startsWith("tagsystem.suffix.tagid."))
				.findFirst()
				.map(n -> TagManager.getTag(n.getKey().replaceFirst("tagsystem\\.suffix\\.tagid\\.", "")))
				.orElse(null);
	}

	public static Tag getPlayerPrefixTag(Player player){
		User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
		Optional<Node> ownNode = user.getNodes().stream()
				.filter(n -> n.getKey().startsWith("tagsystem.prefix.tagid."))
				.findFirst();
		if(ownNode.isPresent()){
			return TagManager.getTag(ownNode.get().getKey().replaceFirst("tagsystem\\.prefix\\.tagid\\.", ""));
		}
		return user.resolveInheritedNodes(QueryOptions.nonContextual()).stream()
				.filter(n -> n.getKey().startsWith("tagsystem.prefix.tagid."))
				.findFirst()
				.map(n -> TagManager.getTag(n.getKey().replaceFirst("tagsystem\\.prefix\\.tagid\\.", "")))
				.orElse(null);
	}

	public static void checkAndClearInvalidTags(Player player) {
		User lpUser = luckPerms.getPlayerAdapter(Player.class).getUser(player);

		Optional<Node> ownPrefixNode = lpUser.getNodes().stream()
				.filter(n -> n.getKey().startsWith("tagsystem.prefix.tagid."))
				.findFirst();
		if(ownPrefixNode.isPresent()){
			Tag prefixTag = TagManager.getTag(ownPrefixNode.get().getKey().replaceFirst("tagsystem\\.prefix\\.tagid\\.", ""));
			if(prefixTag != null && !TagManager.hasTagPermission(player, prefixTag)){
				luckPerms.getUserManager().modifyUser(player.getUniqueId(), user ->
					user.data().clear(e -> e.getKey().startsWith("tagsystem.prefix.tagid")));
				player.sendMessage(Component.text("您已失去前綴稱號 ", NamedTextColor.RED)
						.append(MiniMessage.miniMessage().deserialize(prefixTag.getText()))
						.append(Component.text(" 的權限", NamedTextColor.RED)));
			}
		}

		Tag suffixTag = getPlayerSuffixTag(player);
		if(suffixTag != null && !TagManager.hasTagPermission(player, suffixTag)){
			clearPlayerSuffixTag(player);
			player.sendMessage(Component.text("您已失去後綴稱號 ", NamedTextColor.RED)
					.append(MiniMessage.miniMessage().deserialize(suffixTag.getText()))
					.append(Component.text(" 的權限", NamedTextColor.RED)));
		}
	}
}
