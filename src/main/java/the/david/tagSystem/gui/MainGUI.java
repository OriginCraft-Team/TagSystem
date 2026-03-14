package the.david.tagSystem.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.component.PagingButtons;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import the.david.tagSystem.Main;
import the.david.tagSystem.impl.Tag;
import the.david.tagSystem.manager.PlayerTagManager;
import the.david.tagSystem.manager.TagManager;

import java.util.ArrayList;
import java.util.List;

public class MainGUI{
	Main plugin;
	public MainGUI(Main plugin){
		this.plugin = plugin;
	}
	static final GuiItem outlineItem = new GuiItem(
			new ItemStack(Material.BLACK_STAINED_GLASS_PANE),
			(e) -> e.setCancelled(true)
	);
	static final ItemStack clearSuffixTagItem = new ItemStack(Material.BARRIER){{
		editMeta(meta -> {
			meta.displayName(
					Component.text("清除後綴稱號", NamedTextColor.RED)
							.decoration(TextDecoration.ITALIC, false)
							.decoration(TextDecoration.BOLD, true)
			);
		});
	}};
	static final ItemStack clearPrefixTagItem = new ItemStack(Material.BARRIER){{
		editMeta(meta -> {
			meta.displayName(
					Component.text("清除前綴稱號", NamedTextColor.RED)
							.decoration(TextDecoration.ITALIC, false)
							.decoration(TextDecoration.BOLD, true)
			);
		});
	}};
	static final GuiItem clearSuffixTagGuiItem = new GuiItem(
			clearSuffixTagItem,
			(e) -> {
				Player player = (Player) e.getWhoClicked();
				PlayerTagManager.clearPlayerSuffixTag(player);
				player.sendMessage(
						Component.text()
								.append(Component.text("已清除稱號", NamedTextColor.GREEN))
				);
				showGUI(player);
				e.setCancelled(true);
			}
	);
	static final GuiItem clearPrefixTagGuiItem = new GuiItem(
			clearPrefixTagItem,
			(e) -> {
				Player player = (Player) e.getWhoClicked();
				PlayerTagManager.clearPlayerPrefixTag(player);
				player.sendMessage(
						Component.text()
								.append(Component.text("已清除稱號", NamedTextColor.GREEN))
				);
				showGUI(player);
				e.setCancelled(true);
			}
	);
	public static void showGUI(Player player){
		ChestGui gui = new ChestGui(6, "稱號系統");
		StaticPane outlinePane = new StaticPane(0,0, 9, 6, Pane.Priority.LOWEST);
		for(int i = 0; i < 9; i++){
			outlinePane.addItem(outlineItem, i, 0);
		}
		for(int i = 1; i < 6; i++){
			outlinePane.addItem(outlineItem, 0, i);
		}
		for(int i = 1; i < 6; i++){
			outlinePane.addItem(outlineItem, 8, i);
		}
		for(int i = 1; i < 8; i++){
			outlinePane.addItem(outlineItem, i, 5);
		}
		outlinePane.addItem(clearPrefixTagGuiItem, 2, 5);
		outlinePane.addItem(clearSuffixTagGuiItem, 6, 5);
		Tag playerSuffixTag = PlayerTagManager.getPlayerSuffixTag(player);
		Tag playerPrefixTag = PlayerTagManager.getPlayerPrefixTag(player);
		ItemStack showTagItem = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) showTagItem.getItemMeta();
		skullMeta.setOwningPlayer(player);
		showTagItem.setItemMeta(skullMeta);
		showTagItem.editMeta(meta ->{
			meta.displayName(
					Component.text()
							.append(Component.text("稱號資訊", NamedTextColor.AQUA))
							.decoration(TextDecoration.ITALIC, false)
							.build()
			);
			meta.lore(new ArrayList<>(){{
				add(
						Component.text()
								.append(Component.text("前綴: ", NamedTextColor.GOLD))
								.append(
										playerPrefixTag == null ?
												Component.text("無", NamedTextColor.RED) :
												LegacyComponentSerializer.legacy('&').deserialize(PlaceholderAPI.setPlaceholders(player, playerPrefixTag.getText()))
								)
								.decoration(TextDecoration.ITALIC, false)
								.color(NamedTextColor.WHITE)
								.build()
				);
				add(
						Component.text()
								.append(Component.text("後綴: ", NamedTextColor.GOLD))
								.append(
										playerSuffixTag == null ?
												Component.text("無", NamedTextColor.RED) :
												LegacyComponentSerializer.legacy('&').deserialize(PlaceholderAPI.setPlaceholders(player, playerSuffixTag.getText()))
								)
								.decoration(TextDecoration.ITALIC, false)
								.color(NamedTextColor.WHITE)
								.build()
				);
			}});
		});
		GuiItem showTagGuiItem = new GuiItem(
				showTagItem,
				e -> e.setCancelled(true)
		);
		outlinePane.addItem(showTagGuiItem, 4, 0);
		gui.addPane(outlinePane);
		PaginatedPane pane = new PaginatedPane(1, 1, 7, 4);
		List<GuiItem> tagItems = new ArrayList<>();
		TagManager.getAllTags().forEach(tag -> {
			GuiItem guiItem;
			if(TagManager.hasTagPermission(player, tag)){
				ItemStack itemStack = tag.getIcon().clone();
				ItemMeta meta = itemStack.getItemMeta();
				if (meta != null) {
					LegacyComponentSerializer serializer = LegacyComponentSerializer.legacy('&');
					if (meta.hasDisplayName()) {
						Component nameComponent = meta.displayName();
						if (nameComponent != null) {
							String displayName = serializer.serialize(nameComponent);
							displayName = PlaceholderAPI.setPlaceholders(player, displayName);
							meta.displayName(serializer.deserialize(displayName));
						}
					}
					if (meta.hasLore()) {
						List<Component> lore = meta.lore();
						if (lore != null) {
							List<Component> newLore = new ArrayList<>();
							for (Component c : lore) {
								String line = serializer.serialize(c);
								line = PlaceholderAPI.setPlaceholders(player, line);
								newLore.add(serializer.deserialize(line));
							}
							meta.lore(newLore);
						}
					}
					itemStack.setItemMeta(meta);
				}
				itemStack.removeEnchantments();
				if(playerPrefixTag != null){
					if(playerPrefixTag.equals(tag)){
						itemStack.editMeta(itemMeta -> {
							Component currentName = itemMeta.displayName();
							if (currentName == null) currentName = Component.empty();
							itemMeta.displayName(currentName.append(Component.text(" (前綴使用中) ", NamedTextColor.GREEN)));
							itemMeta.addEnchant(Enchantment.INFINITY, 1, false);
							itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
						});
					}
				}
				if(playerSuffixTag != null){
					if(playerSuffixTag.equals(tag)){
						itemStack.editMeta(itemMeta -> {
							Component currentName = itemMeta.displayName();
							if (currentName == null) currentName = Component.empty();
							itemMeta.displayName(currentName.append(Component.text(" (後綴使用中) ", NamedTextColor.GREEN)));
							itemMeta.addEnchant(Enchantment.INFINITY, 1, false);
							itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
						});
					}
				}
				guiItem = new GuiItem(
						itemStack,
						(e) -> {
							e.setCancelled(true);
							PlayerTagManager.setPlayerTag(player, tag);
							showGUI(player);
						}
				);
			}else{
				ItemMeta itemMeta = tag.getIcon().getItemMeta();
				ItemStack itemStack = new ItemStack(Material.BARRIER);
				itemStack.setItemMeta(itemMeta);
				guiItem = new GuiItem(
						itemStack,
						(e) -> {
							e.setCancelled(true);
							showGUI(player);
						}
				);
			}
			tagItems.add(guiItem);
		});
		pane.populateWithGuiItems(tagItems);
		PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(1, 5), 7, Pane.Priority.HIGH, pane);
		ItemStack backwardButtonItemStack = new ItemStack(Material.ARROW);
		backwardButtonItemStack.editMeta(itemMeta -> {
			itemMeta.displayName(Component.text("上一頁").decoration(TextDecoration.ITALIC, false));
		});
		pagingButtons.setBackwardButton(new GuiItem(backwardButtonItemStack));
		ItemStack forwardButtonItemStack = new ItemStack(Material.ARROW);
		forwardButtonItemStack.editMeta(itemMeta -> {
			itemMeta.displayName(Component.text("下一頁").decoration(TextDecoration.ITALIC, false));
		});
		pagingButtons.setForwardButton(new GuiItem(forwardButtonItemStack));
		gui.addPane(pagingButtons);
		gui.addPane(pane);
		gui.show(player);
	}
}
