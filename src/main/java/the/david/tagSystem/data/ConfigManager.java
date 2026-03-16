package the.david.tagSystem.data;

import the.david.tagSystem.impl.Tag;

public class ConfigManager{
	public static ConfigHandler tagConfig;

	public static void loadConfigs(){
		tagConfig = new ConfigHandler("TagConfig.yml");
	}

	public static void setTagToConfig(Tag tag){
		tagConfig.setObject("tags." + tag.getId() + ".text", tag.getText());
		tagConfig.setObject("tags." + tag.getId() + ".description", tag.getDescription());
		tagConfig.setObject("tags." + tag.getId() + ".type", tag.getTagType().toString());
		tagConfig.setObject("tags." + tag.getId() + ".icon", tag.getIconMaterial().name());
		tagConfig.setObject("tags." + tag.getId() + ".hover_description", tag.isHoverDescription());
		tagConfig.saveFile();
	}
}