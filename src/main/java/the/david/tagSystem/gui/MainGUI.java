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
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

	private static final MiniMessage mm = MiniMessage.miniMessage();

	// --- Border glass items (gradient: corner → edge → inner) ---
	private static GuiItem glass(Material mat){
		ItemStack item = new ItemStack(mat);
		item.editMeta(m -> m.displayName(Component.empty()));
		return new GuiItem(item, e -> e.setCancelled(true));
	}

	private static final Material CORNER = Material.BLACK_STAINED_GLASS_PANE;
	private static final Material EDGE = Material.GRAY_STAINED_GLASS_PANE;
	private static final Material ACCENT = Material.CYAN_STAINED_GLASS_PANE;
	private static final Material INNER = Material.LIGHT_BLUE_STAINED_GLASS_PANE;

	/*
	 * Layout (6×9):
	 *
	 * Row 0:  CO  ED  AC  IN  [HEAD] IN  AC  ED  CO
	 * Row 1:  ED  .   .   .   .      .   .   .   ED
	 * Row 2:  AC  .   .   .   .      .   .   .   AC
	 * Row 3:  AC  .   .   .   .      .   .   .   AC
	 * Row 4:  ED  .   .   .   .      .   .   .   ED
	 * Row 5:  CO  [◄] [P] ED  [★]   ED  [S] [►] CO
	 *
	 * CO=corner, ED=edge, AC=accent, IN=inner
	 * [HEAD]=player skull, [◄►]=page, [P]=clear prefix, [S]=clear suffix, [★]=info
	 */

	public static void showGUI(Player player){
		ChestGui gui = new ChestGui(6, "✦ 稱號系統 ✦");

		// ============ Background border ============
		StaticPane border = new StaticPane(0, 0, 9, 6, Pane.Priority.LOWEST);

		// Row 0: gradient top
		Material[] topRow = {CORNER, EDGE, ACCENT, INNER, INNER, INNER, ACCENT, EDGE, CORNER};
		for(int i = 0; i < 9; i++){
			border.addItem(glass(topRow[i]), i, 0);
		}
		// Row 1 & 4: edge sides
		border.addItem(glass(EDGE), 0, 1);
		border.addItem(glass(EDGE), 8, 1);
		border.addItem(glass(EDGE), 0, 4);
		border.addItem(glass(EDGE), 8, 4);
		// Row 2 & 3: accent sides
		border.addItem(glass(ACCENT), 0, 2);
		border.addItem(glass(ACCENT), 8, 2);
		border.addItem(glass(ACCENT), 0, 3);
		border.addItem(glass(ACCENT), 8, 3);
		// Row 5: bottom bar
		Material[] botRow = {CORNER, EDGE, EDGE, EDGE, EDGE, EDGE, EDGE, EDGE, CORNER};
		for(int i = 0; i < 9; i++){
			border.addItem(glass(botRow[i]), i, 5);
		}

		gui.addPane(border);

		// ============ Control pane (row 0 center + row 5 buttons) ============
		StaticPane controls = new StaticPane(0, 0, 9, 6, Pane.Priority.HIGH);

		Tag playerPrefixTag = PlayerTagManager.getPlayerPrefixTag(player);
		Tag playerSuffixTag = PlayerTagManager.getPlayerSuffixTag(player);

		// --- Player head (center top) ---
		ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();
		skullMeta.setOwningPlayer(player);
		headItem.setItemMeta(skullMeta);
		headItem.editMeta(meta -> {
			meta.displayName(mm.deserialize("<aqua><bold>✦ 我的稱號 ✦</bold></aqua>").decoration(TextDecoration.ITALIC, false));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.empty());
			lore.add(mm.deserialize("<gray>━━━━━━━━━━━━━━━━━━</gray>").decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text()
					.append(mm.deserialize("<gray>  🏷 </gray><gold>前綴: </gold>"))
					.append(playerPrefixTag == null
							? mm.deserialize("<dark_gray>— 未裝備</dark_gray>")
							: mm.deserialize(PlaceholderAPI.setPlaceholders(player, playerPrefixTag.getText())))
					.decoration(TextDecoration.ITALIC, false)
					.build());
			lore.add(Component.text()
					.append(mm.deserialize("<gray>  🏷 </gray><gold>後綴: </gold>"))
					.append(playerSuffixTag == null
							? mm.deserialize("<dark_gray>— 未裝備</dark_gray>")
							: mm.deserialize(PlaceholderAPI.setPlaceholders(player, playerSuffixTag.getText())))
					.decoration(TextDecoration.ITALIC, false)
					.build());
			lore.add(mm.deserialize("<gray>━━━━━━━━━━━━━━━━━━</gray>").decoration(TextDecoration.ITALIC, false));
			lore.add(Component.empty());
			lore.add(mm.deserialize("<dark_gray>☛ 左鍵點擊下方稱號以裝備</dark_gray>").decoration(TextDecoration.ITALIC, false));
			lore.add(mm.deserialize("<dark_gray>☛ 使用底部按鈕清除稱號</dark_gray>").decoration(TextDecoration.ITALIC, false));
			meta.lore(lore);
		});
		controls.addItem(new GuiItem(headItem, e -> e.setCancelled(true)), 4, 0);

		// --- Clear prefix button ---
		ItemStack clearPrefixItem = new ItemStack(Material.HONEYCOMB);
		clearPrefixItem.editMeta(meta -> {
			meta.displayName(mm.deserialize("<red><bold>✘</bold> <red>清除前綴</red>").decoration(TextDecoration.ITALIC, false));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.empty());
			if(playerPrefixTag != null){
				lore.add(Component.text()
						.append(mm.deserialize("<gray>📌 目前: </gray>"))
						.append(mm.deserialize(playerPrefixTag.getText()))
						.decoration(TextDecoration.ITALIC, false)
						.build());
			}else{
				lore.add(mm.deserialize("<dark_gray>📌 目前無前綴稱號</dark_gray>").decoration(TextDecoration.ITALIC, false));
			}
			lore.add(Component.empty());
			lore.add(mm.deserialize("<yellow>⚡ 點擊以清除</yellow>").decoration(TextDecoration.ITALIC, false));
			meta.lore(lore);
		});
		controls.addItem(new GuiItem(clearPrefixItem, e -> {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			PlayerTagManager.clearPlayerPrefixTag(p);
			p.sendMessage(mm.deserialize("<green>已清除前綴稱號</green>"));
			showGUI(p);
		}), 2, 5);

		// --- Clear suffix button ---
		ItemStack clearSuffixItem = new ItemStack(Material.HONEYCOMB);
		clearSuffixItem.editMeta(meta -> {
			meta.displayName(mm.deserialize("<red><bold>✘</bold> <red>清除後綴</red>").decoration(TextDecoration.ITALIC, false));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.empty());
			if(playerSuffixTag != null){
				lore.add(Component.text()
						.append(mm.deserialize("<gray>📌 目前: </gray>"))
						.append(mm.deserialize(playerSuffixTag.getText()))
						.decoration(TextDecoration.ITALIC, false)
						.build());
			}else{
				lore.add(mm.deserialize("<dark_gray>📌 目前無後綴稱號</dark_gray>").decoration(TextDecoration.ITALIC, false));
			}
			lore.add(Component.empty());
			lore.add(mm.deserialize("<yellow>⚡ 點擊以清除</yellow>").decoration(TextDecoration.ITALIC, false));
			meta.lore(lore);
		});
		controls.addItem(new GuiItem(clearSuffixItem, e -> {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			PlayerTagManager.clearPlayerSuffixTag(p);
			p.sendMessage(mm.deserialize("<green>已清除後綴稱號</green>"));
			showGUI(p);
		}), 6, 5);

		// --- Center decoration (bottom bar) ---
		ItemStack starItem = new ItemStack(Material.NETHER_STAR);
		starItem.editMeta(meta -> {
			meta.displayName(mm.deserialize("<gradient:gold:yellow:gold>✦ 稱號系統 ✦</gradient>").decoration(TextDecoration.ITALIC, false));
			meta.lore(List.of(
					Component.empty(),
					mm.deserialize("<gray>🌟 收集並展示你的專屬稱號！</gray>").decoration(TextDecoration.ITALIC, false),
					mm.deserialize("<dark_gray>📦 共 " + TagManager.getAllTags().size() + " 個稱號</dark_gray>").decoration(TextDecoration.ITALIC, false)
			));
		});
		controls.addItem(new GuiItem(starItem, e -> e.setCancelled(true)), 4, 5);

		gui.addPane(controls);

		// ============ Tag items (paginated, 7×4) ============
		PaginatedPane tagPane = new PaginatedPane(1, 1, 7, 4);
		List<GuiItem> tagItems = new ArrayList<>();

		TagManager.getAllTags().forEach(tag -> {
			GuiItem guiItem;
			if(TagManager.hasTagPermission(player, tag)){
				ItemStack itemStack = tag.getIcon().clone();
				ItemMeta meta = itemStack.getItemMeta();
				if(meta != null){
					// Apply PAPI placeholders
					if(meta.hasDisplayName()){
						Component nameComponent = meta.displayName();
						if(nameComponent != null){
							String displayName = mm.serialize(nameComponent);
							displayName = PlaceholderAPI.setPlaceholders(player, displayName);
							meta.displayName(mm.deserialize(displayName));
						}
					}
					if(meta.hasLore()){
						List<Component> lore = meta.lore();
						if(lore != null){
							List<Component> newLore = new ArrayList<>();
							for(Component c : lore){
								String line = mm.serialize(c);
								line = PlaceholderAPI.setPlaceholders(player, line);
								newLore.add(mm.deserialize(line));
							}
							meta.lore(newLore);
						}
					}
					itemStack.setItemMeta(meta);
				}

				// Mark active tags
				boolean isActive = false;
				String activeLabel = "";
				if(playerPrefixTag != null && playerPrefixTag.equals(tag)){
					isActive = true;
					activeLabel = "<green> ✔ 前綴使用中</green>";
				}else if(playerSuffixTag != null && playerSuffixTag.equals(tag)){
					isActive = true;
					activeLabel = "<green> ✔ 後綴使用中</green>";
				}

				if(isActive){
					String label = activeLabel;
					itemStack.editMeta(itemMeta -> {
						Component currentName = itemMeta.displayName();
						if(currentName == null) currentName = Component.empty();
						itemMeta.displayName(currentName.append(mm.deserialize(label)));

						// Add glow
						itemMeta.addEnchant(Enchantment.INFINITY, 1, false);
						itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

						// Append status to lore
						List<Component> lore = itemMeta.lore();
						if(lore == null) lore = new ArrayList<>();
						lore.add(Component.empty());
						lore.add(mm.deserialize("<green>━━━━━━━━━━━━━━━</green>").decoration(TextDecoration.ITALIC, false));
						lore.add(mm.deserialize("<green><bold>  ✔ 已裝備</bold></green>").decoration(TextDecoration.ITALIC, false));
						lore.add(mm.deserialize("<gray>  ☛ 點擊其他稱號以更換</gray>").decoration(TextDecoration.ITALIC, false));
						itemMeta.lore(lore);
					});
				}else{
					// Hint lore for unequipped
					itemStack.editMeta(itemMeta -> {
						List<Component> lore = itemMeta.lore();
						if(lore == null) lore = new ArrayList<>();
						lore.add(Component.empty());
						lore.add(mm.deserialize("<yellow>━━━━━━━━━━━━━━━</yellow>").decoration(TextDecoration.ITALIC, false));
						lore.add(mm.deserialize("<yellow>  ☛ 點擊以裝備</yellow>").decoration(TextDecoration.ITALIC, false));
						itemMeta.lore(lore);
					});
				}

				guiItem = new GuiItem(itemStack, e -> {
					e.setCancelled(true);
					PlayerTagManager.setPlayerTag(player, tag);
					showGUI(player);
				});
			}else{
				// No permission — locked tag
				ItemStack lockedItem = new ItemStack(Material.GRAY_DYE);
				lockedItem.editMeta(itemMeta -> {
					Component tagName = mm.deserialize(tag.getText());
					itemMeta.displayName(Component.text()
							.append(mm.deserialize("<dark_gray><strikethrough>"))
							.append(tagName)
							.append(mm.deserialize("</strikethrough></dark_gray>"))
							.decoration(TextDecoration.ITALIC, false)
							.build());
					itemMeta.lore(List.of(
							Component.empty(),
							mm.deserialize("<red>━━━━━━━━━━━━━━━</red>").decoration(TextDecoration.ITALIC, false),
							mm.deserialize("<red>  🔒 尚未解鎖</red>").decoration(TextDecoration.ITALIC, false),
							mm.deserialize("<dark_gray>  需要權限: tagsystem.tag." + tag.getId() + "</dark_gray>").decoration(TextDecoration.ITALIC, false)
					));
				});
				guiItem = new GuiItem(lockedItem, e -> e.setCancelled(true));
			}
			tagItems.add(guiItem);
		});

		tagPane.populateWithGuiItems(tagItems);

		// ============ Paging buttons ============
		PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(1, 5), 7, Pane.Priority.HIGHEST, tagPane);

		ItemStack backArrow = new ItemStack(Material.SPECTRAL_ARROW);
		backArrow.editMeta(m -> {
			m.displayName(mm.deserialize("<aqua><bold>◄</bold> <aqua>上一頁</aqua>").decoration(TextDecoration.ITALIC, false));
			m.lore(List.of(mm.deserialize("<dark_gray>☛ 點擊翻頁</dark_gray>").decoration(TextDecoration.ITALIC, false)));
		});
		pagingButtons.setBackwardButton(new GuiItem(backArrow));

		ItemStack forwardArrow = new ItemStack(Material.SPECTRAL_ARROW);
		forwardArrow.editMeta(m -> {
			m.displayName(mm.deserialize("<aqua>下一頁</aqua> <aqua><bold>►</bold>").decoration(TextDecoration.ITALIC, false));
			m.lore(List.of(mm.deserialize("<dark_gray>☛ 點擊翻頁</dark_gray>").decoration(TextDecoration.ITALIC, false)));
		});
		pagingButtons.setForwardButton(new GuiItem(forwardArrow));

		gui.addPane(pagingButtons);
		gui.addPane(tagPane);
		gui.show(player);
	}
}