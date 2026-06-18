package the.david.tagSystem.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import the.david.tagSystem.impl.Tag;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static the.david.tagSystem.Main.luckPerms;

public class PlayerTagManager{
	// ── Custom suffix ──
	/** Reserved tag id used for the player-defined custom suffix. Must not collide with any real tag in TagConfig.yml. */
	public static final String CUSTOM_SUFFIX_ID = "custom_suffix";
	/** Permission node prefix that stores the custom suffix MiniMessage content in its key. Persistent, never auto-removed. */
	public static final String CONTENT_NODE_PREFIX = "tagsystem.custom_suffix.";
	/** Fixed hover text shown on every custom suffix. */
	public static final String FIXED_HOVER = "這是玩家自訂稱號（MVP 階級專屬功能，VIP 階級可另外加購解鎖）";
	/**
	 * Cache of synthetic custom-suffix tags keyed by their MiniMessage content. A custom suffix tag is fully
	 * determined by its content plus the fixed hover, so identical content can safely share one immutable Tag
	 * instance across all players. This avoids rebuilding the icon (ItemStack + MiniMessage parsing) on every
	 * placeholder request, and is bounded by the number of distinct custom suffix strings rather than players.
	 */
	private static final Map<String, Tag> CUSTOM_SUFFIX_CACHE = new ConcurrentHashMap<>();
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
		Optional<String> suffixId = user.getNodes().stream()
				.filter(n -> n.getKey().startsWith("tagsystem.suffix.tagid."))
				.findFirst()
				.map(n -> n.getKey().replaceFirst("tagsystem\\.suffix\\.tagid\\.", ""));
		if(suffixId.isEmpty()) return null;
		if(suffixId.get().equals(CUSTOM_SUFFIX_ID)){
			return buildCustomSuffixTag(player);
		}
		return TagManager.getTag(suffixId.get());
	}

	/** Reads the persisted custom suffix MiniMessage content, or {@code null} if the player has none. */
	public static String getPlayerCustomSuffixContent(Player player){
		User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
		return user.getNodes().stream()
				.filter(n -> n.getKey().startsWith(CONTENT_NODE_PREFIX))
				.findFirst()
				.map(n -> n.getKey().substring(CONTENT_NODE_PREFIX.length()))
				.orElse(null);
	}

	/**
	 * Builds a synthetic {@link Tag} representing the player's currently equipped custom suffix.
	 * The content comes from the persisted content node and the hover is forced to {@link #FIXED_HOVER}.
	 * If the in-use pointer exists but no content node is found, the stale pointer is cleared and {@code null} returned.
	 */
	private static Tag buildCustomSuffixTag(Player player){
		String content = getPlayerCustomSuffixContent(player);
		if(content == null){
			clearPlayerSuffixTag(player);
			return null;
		}
		return CUSTOM_SUFFIX_CACHE.computeIfAbsent(content,
				c -> new Tag(CUSTOM_SUFFIX_ID, c, FIXED_HOVER, Material.NAME_TAG, Tag.TagType.SUFFIX, true, false, 0));
	}

	/**
	 * Persists {@code content} as the player's custom suffix and equips it (occupying the suffix slot,
	 * mutually exclusive with normal suffixes). The content node is persistent and survives loss of eligibility.
	 */
	public static void setPlayerCustomSuffix(Player player, String content){
		if(!player.hasPermission("tagsystem.tag." + CUSTOM_SUFFIX_ID)){
			player.sendMessage(Component.text("您沒有自訂後綴稱號的權限！", NamedTextColor.RED));
			return;
		}
		luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
			user.data().clear(e -> e.getKey().startsWith(CONTENT_NODE_PREFIX));
			user.data().add(Node.builder(CONTENT_NODE_PREFIX + content).build());
			user.data().clear(e -> e.getKey().startsWith("tagsystem.suffix.tagid"));
			user.data().add(Node.builder("tagsystem.suffix.tagid." + CUSTOM_SUFFIX_ID).build());
		});
	}

	/** Re-equips the already-saved custom suffix without modifying the stored content. */
	public static void equipExistingCustomSuffix(Player player){
		if(!player.hasPermission("tagsystem.tag." + CUSTOM_SUFFIX_ID)){
			player.sendMessage(Component.text("您沒有自訂後綴稱號的權限！", NamedTextColor.RED));
			return;
		}
		if(getPlayerCustomSuffixContent(player) == null){
			player.sendMessage(Component.text("您尚未設定自訂後綴稱號。", NamedTextColor.RED));
			return;
		}
		luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
			user.data().clear(e -> e.getKey().startsWith("tagsystem.suffix.tagid"));
			user.data().add(Node.builder("tagsystem.suffix.tagid." + CUSTOM_SUFFIX_ID).build());
		});
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
