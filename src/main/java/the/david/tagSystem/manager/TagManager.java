package the.david.tagSystem.manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import the.david.tagSystem.data.ConfigHandler;
import the.david.tagSystem.data.ConfigManager;
import the.david.tagSystem.impl.Tag;

import javax.annotation.Nullable;
import java.util.*;

public class TagManager{
	private static final Map<String, Tag> tags = new HashMap<>();
	private static final List<String> tagList = new ArrayList<>();

	public TagManager(){

	}

	public static void loadTags(){
		ConfigHandler config = ConfigManager.tagConfig;
		if(config.getKeys("tags") == null){
			return;
		}
		tags.clear();
		tagList.clear();
		config.getKeys("tags").forEach(key -> {
			String id = key;
			String text = config.getString("tags." + key + ".text");
			String description = config.getString("tags." + key + ".description");
			String type = config.getString("tags." + key + ".type");
			Tag.TagType tagType = Tag.TagType.valueOf(type);
			String iconName = config.getString("tags." + key + ".icon");
			Material iconMaterial = Material.matchMaterial(iconName);
			if(iconMaterial == null) iconMaterial = Material.NAME_TAG;
			boolean hoverDescription = config.getBoolean("tags." + key + ".hover_description");
			Tag tag = new Tag(id, text, description, iconMaterial, tagType, hoverDescription);
			tags.put(id, tag);
			tagList.add(id);
		});
	}

	public static void addTag(Tag tag){
		tags.put(tag.getId(), tag);
		tagList.add(tag.getId());
		ConfigManager.setTagToConfig(tag);
	}

	@Nullable
	public static Tag getTag(String tagId){
		return tags.get(tagId);
	}

	public static Collection<Tag> getAllTags(){
		List<Tag> tmp = new ArrayList<>();
		tagList.forEach(k -> {
			tmp.add(tags.get(k));
		});
		return tmp;
	}

	public static Collection<Tag> getTagCollection(){
		return tags.values();
	}

	public static boolean hasTagPermission(Player player, Tag tag){
		return player.hasPermission("tagsystem.tag." + tag.getId());
	}

	public static void setTagText(Tag tag, String text){
		tag.setText(text);
		ConfigManager.setTagToConfig(tag);
	}

	public static void setTagDescription(Tag tag, String text){
		tag.setDescription(text);
		ConfigManager.setTagToConfig(tag);
	}
}
