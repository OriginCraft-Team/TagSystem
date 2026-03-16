package the.david.tagSystem.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import the.david.tagSystem.util.DebugOutputHandler;

import java.util.ArrayList;
import java.util.List;

public class Tag{
	private final String id;
	private String text;
	private String description;
	private Material iconMaterial;
	private ItemStack icon;
	private TagType tagType;
	private boolean hoverDescription;

	public Tag(String id, String text, String description, Material iconMaterial, TagType tagType, boolean hoverDescription){
		this.id = id;
		this.text = text;
		this.description = description;
		this.tagType = tagType;
		this.iconMaterial = iconMaterial;
		this.hoverDescription = hoverDescription;
		rebuildIcon();
	}

	public void setIconMaterial(@NotNull Material material){
		this.iconMaterial = material;
		rebuildIcon();
	}

	@NotNull
	public Material getIconMaterial(){
		return iconMaterial;
	}

	private void rebuildIcon(){
		ItemStack item = new ItemStack(iconMaterial);
		item.editMeta(itemMeta -> {
			itemMeta.displayName(MiniMessage.miniMessage().deserialize(getText()).decoration(TextDecoration.ITALIC, false));
			List<Component> descriptionLineList = new ArrayList<>();
			for(String oneDescriptionLine : getDescription().split("\\n|\\\\n")){
				descriptionLineList.add(MiniMessage.miniMessage().deserialize(oneDescriptionLine).decoration(TextDecoration.ITALIC, false));
				DebugOutputHandler.sendDebugOutput(oneDescriptionLine);
			}
			itemMeta.lore(descriptionLineList);
		});
		this.icon = item;
	}
	public void setType(TagType tagType){
		this.tagType = tagType;
	}

	public void setText(String text){
		this.text = text;
		rebuildIcon();
	}

	public void setDescription(String description){
		this.description = description;
		rebuildIcon();
	}

	@NotNull
	public String getId(){
		return id;
	}

	@NotNull
	public String getText(){
		return text;
	}

	@NotNull
	public String getDescription(){
		return description;
	}

	@NotNull
	public ItemStack getIcon(){
		return icon;
	}
	public TagType getTagType(){
		return tagType;
	}

	public boolean isHoverDescription(){
		return hoverDescription;
	}

	public void setHoverDescription(boolean hoverDescription){
		this.hoverDescription = hoverDescription;
	}

	/**
	 * Returns the text to be used in LuckPerms prefix/suffix nodes.
	 * If hoverDescription is enabled, wraps the text with a hover tag showing the description.
	 */
	public String getLuckPermsText(){
		if(hoverDescription){
			String hoverContent = getDescription().replace("\\n", "<br>");
			return "<hover:show_text:'" + hoverContent + "'>" + getText() + "</hover>";
		}
		return getText();
	}

	public enum TagType{
		SUFFIX,
		PREFIX
	}
}
