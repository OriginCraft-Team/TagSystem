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
import org.bukkit.Bukkit;
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

import java.util.*;

public class MainGUI{
	private static final MiniMessage mm = MiniMessage.miniMessage();

	// ── Filter enums ──
	public enum TypeFilter{ ALL, PREFIX, SUFFIX }
	public enum OwnerFilter{ ALL, OWNED, UNOWNED }

	private static final Map<UUID, TypeFilter> typeFilters = new HashMap<>();
	private static final Map<UUID, OwnerFilter> ownerFilters = new HashMap<>();

	// ── Helpers ──
	private static Component noItalic(String miniMessage){
		return mm.deserialize(miniMessage).decoration(TextDecoration.ITALIC, false);
	}

	private static GuiItem filler(){
		ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		item.editMeta(m -> m.displayName(Component.empty()));
		return new GuiItem(item, e -> e.setCancelled(true));
	}

	private static GuiItem decorGlass(Material mat){
		ItemStack item = new ItemStack(mat);
		item.editMeta(m -> m.displayName(Component.empty()));
		return new GuiItem(item, e -> e.setCancelled(true));
	}

	// ── Entry point ──
	public static void showGUI(Player player){
		showGUI(player,
				typeFilters.getOrDefault(player.getUniqueId(), TypeFilter.ALL),
				ownerFilters.getOrDefault(player.getUniqueId(), OwnerFilter.ALL));
	}

	/*
	 * ┌──────────────────────────────────────────┐
	 * │ BK  BK  GD  BK  HEAD  BK  GD  BK  BK   │  Row 0  Header
	 * │ BK  .   .   .   .     .   .   .   BK   │  Row 1  Tags
	 * │ BK  .   .   .   .     .   .   .   BK   │  Row 2  Tags
	 * │ BK  .   .   .   .     .   .   .   BK   │  Row 3  Tags
	 * │ BK  .   .   .   .     .   .   .   BK   │  Row 4  Tags
	 * │ BK  ◄   CP  BK  ★    BK  CS  ►   BK   │  Row 5  Controls
	 * └──────────────────────────────────────────┘
	 * BK = black glass, GD = gold accent, ★ = filter
	 */
	public static void showGUI(Player player, TypeFilter typeFilter, OwnerFilter ownerFilter){
		typeFilters.put(player.getUniqueId(), typeFilter);
		ownerFilters.put(player.getUniqueId(), ownerFilter);

		ChestGui gui = new ChestGui(6, "✦ 稱號系統 ✦");

		// ═══════════════ Background (全黑玻璃) ═══════════════
		StaticPane bg = new StaticPane(0, 0, 9, 6, Pane.Priority.LOWEST);
		for(int x = 0; x < 9; x++) bg.addItem(filler(), x, 0);   // top
		for(int x = 0; x < 9; x++) bg.addItem(filler(), x, 5);   // bottom
		for(int y = 1; y < 5; y++){ bg.addItem(filler(), 0, y); bg.addItem(filler(), 8, y); } // sides
		gui.addPane(bg);

		// ═══════════════ Decorative accents ═══════════════
		StaticPane decor = new StaticPane(0, 0, 9, 6, Pane.Priority.NORMAL);
		// Gold glass accents flanking head
		decor.addItem(decorGlass(Material.ORANGE_STAINED_GLASS_PANE), 2, 0);
		decor.addItem(decorGlass(Material.ORANGE_STAINED_GLASS_PANE), 6, 0);
		gui.addPane(decor);

		// ═══════════════ Controls ═══════════════
		StaticPane ctrl = new StaticPane(0, 0, 9, 6, Pane.Priority.HIGH);

		Tag playerPrefixTag = PlayerTagManager.getPlayerPrefixTag(player);
		Tag playerSuffixTag = PlayerTagManager.getPlayerSuffixTag(player);

		// ─── Player Head ───
		ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();
		skullMeta.setOwningPlayer(player);
		headItem.setItemMeta(skullMeta);
		headItem.editMeta(meta -> {
			meta.displayName(noItalic("<gradient:gold:yellow>✦ " + player.getName() + " 的稱號 ✦</gradient>"));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.empty());
			lore.add(Component.text().append(noItalic("<gold>  ▸ 前綴  </gold>")).append(
					playerPrefixTag == null ? noItalic("<dark_gray>尚未裝備</dark_gray>")
							: mm.deserialize(PlaceholderAPI.setPlaceholders(player, playerPrefixTag.getText()))
			).decoration(TextDecoration.ITALIC, false).build());
			lore.add(Component.text().append(noItalic("<gold>  ▸ 後綴  </gold>")).append(
					playerSuffixTag == null ? noItalic("<dark_gray>尚未裝備</dark_gray>")
							: mm.deserialize(PlaceholderAPI.setPlaceholders(player, playerSuffixTag.getText()))
			).decoration(TextDecoration.ITALIC, false).build());
			lore.add(Component.empty());
			lore.add(noItalic("<dark_gray>  ☛ 點擊稱號以裝備</dark_gray>"));
			meta.lore(lore);
		});
		ctrl.addItem(new GuiItem(headItem, e -> e.setCancelled(true)), 4, 0);

		// ─── Clear Prefix ───
		boolean hasOwnPrefixNode = Main.luckPerms.getPlayerAdapter(Player.class).getUser(player)
				.getNodes().stream().anyMatch(n -> n.getKey().startsWith("tagsystem.prefix.tagid."));
		ItemStack clearPfx = new ItemStack(hasOwnPrefixNode ? Material.LIME_DYE : Material.RED_DYE);
		clearPfx.editMeta(meta -> {
			meta.displayName(noItalic(hasOwnPrefixNode
					? "<green>✔ 清除前綴稱號</green>"
					: "<red>✘ 清除前綴稱號</red>"));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.empty());
			if(playerPrefixTag != null){
				Component currentLine = Component.text().append(noItalic("<gray>  目前 ▸ </gray>"))
						.append(mm.deserialize(playerPrefixTag.getText()))
						.decoration(TextDecoration.ITALIC, false).build();
				if(!hasOwnPrefixNode){
					currentLine = currentLine.append(noItalic("<dark_gray> (預設稱號)</dark_gray>"));
				}
				lore.add(currentLine);
				if(hasOwnPrefixNode){
					lore.add(Component.empty());
					lore.add(noItalic("<yellow>  ⚡ 左鍵清除</yellow>"));
				}
			}else{
				lore.add(noItalic("<dark_gray>  目前無前綴</dark_gray>"));
			}
			meta.lore(lore);
		});
		ctrl.addItem(new GuiItem(clearPfx, e -> {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			PlayerTagManager.clearPlayerPrefixTag(p);
			p.sendMessage(noItalic("<green>✔ 已清除前綴稱號</green>"));
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> showGUI(p), 1L);
		}), 2, 5);

		// ─── Clear Suffix ───
		ItemStack clearSfx = new ItemStack(playerSuffixTag != null ? Material.LIME_DYE : Material.RED_DYE);
		clearSfx.editMeta(meta -> {
			meta.displayName(noItalic(playerSuffixTag != null
					? "<green>✔ 清除後綴稱號</green>" : "<red>✘ 清除後綴稱號</red>"));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.empty());
			if(playerSuffixTag != null){
				lore.add(Component.text().append(noItalic("<gray>  目前 ▸ </gray>"))
						.append(mm.deserialize(playerSuffixTag.getText()))
						.decoration(TextDecoration.ITALIC, false).build());
				lore.add(Component.empty());
				lore.add(noItalic("<yellow>  ⚡ 左鍵清除</yellow>"));
			}else{
				lore.add(noItalic("<dark_gray>  目前無後綴</dark_gray>"));
			}
			meta.lore(lore);
		});
		ctrl.addItem(new GuiItem(clearSfx, e -> {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			PlayerTagManager.clearPlayerSuffixTag(p);
			p.sendMessage(noItalic("<green>✔ 已清除後綴稱號</green>"));
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> showGUI(p), 1L);
		}), 6, 5);

		// ─── Filter (烈焰粉末 — 比 Nether Star 更不突兀) ───
		ItemStack filterItem = new ItemStack(Material.BLAZE_POWDER);
		filterItem.editMeta(meta -> {
			meta.displayName(noItalic("<gradient:gold:yellow>✦ 篩選與排序 ✦</gradient>"));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.empty());

			// Type filter — 用 ▶ 指示當前選擇
			lore.add(noItalic("<gold> ┌ 類型</gold>"));
			lore.add(noItalic(typeFilter == TypeFilter.ALL
					? " <gold>│</gold> <white><bold>▶ 全部</bold></white>  <dark_gray>前綴  後綴</dark_gray>"
					: typeFilter == TypeFilter.PREFIX
					? " <gold>│</gold> <dark_gray>全部</dark_gray>  <aqua><bold>▶ 前綴</bold></aqua>  <dark_gray>後綴</dark_gray>"
					: " <gold>│</gold> <dark_gray>全部  前綴</dark_gray>  <light_purple><bold>▶ 後綴</bold></light_purple>"));
			lore.add(noItalic("<gold> │</gold>"));

			// Owner filter
			lore.add(noItalic("<gold> ├ 擁有</gold>"));
			lore.add(noItalic(ownerFilter == OwnerFilter.ALL
					? " <gold>│</gold> <white><bold>▶ 全部</bold></white>  <dark_gray>已擁有  未擁有</dark_gray>"
					: ownerFilter == OwnerFilter.OWNED
					? " <gold>│</gold> <dark_gray>全部</dark_gray>  <green><bold>▶ 已擁有</bold></green>  <dark_gray>未擁有</dark_gray>"
					: " <gold>│</gold> <dark_gray>全部  已擁有</dark_gray>  <red><bold>▶ 未擁有</bold></red>"));
			lore.add(noItalic("<gold> └━━━━━━━━━━━━━━━</gold>"));

			lore.add(Component.empty());
			lore.add(noItalic("<yellow>  ☛ 左鍵 </yellow><gray>切換類型</gray>"));
			lore.add(noItalic("<yellow>  ☛ 右鍵 </yellow><gray>切換擁有</gray>"));
			meta.lore(lore);
		});
		ctrl.addItem(new GuiItem(filterItem, e -> {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			if(e.isLeftClick()){
				TypeFilter next = switch(typeFilter){
					case ALL -> TypeFilter.PREFIX;
					case PREFIX -> TypeFilter.SUFFIX;
					case SUFFIX -> TypeFilter.ALL;
				};
				showGUI(p, next, ownerFilter);
			}else if(e.isRightClick()){
				OwnerFilter next = switch(ownerFilter){
					case ALL -> OwnerFilter.OWNED;
					case OWNED -> OwnerFilter.UNOWNED;
					case UNOWNED -> OwnerFilter.ALL;
				};
				showGUI(p, typeFilter, next);
			}
		}), 4, 5);

		gui.addPane(ctrl);

		// ═══════════════ Tag items (7×4 paginated) ═══════════════
		PaginatedPane tagPane = new PaginatedPane(1, 1, 7, 4);
		List<GuiItem> tagItems = new ArrayList<>();

		TagManager.getAllTags().forEach(tag -> {
			if(!tag.isShowInGui()) return;
			if(typeFilter == TypeFilter.PREFIX && tag.getTagType() != Tag.TagType.PREFIX) return;
			if(typeFilter == TypeFilter.SUFFIX && tag.getTagType() != Tag.TagType.SUFFIX) return;

			boolean hasPermission = TagManager.hasTagPermission(player, tag);
			if(ownerFilter == OwnerFilter.OWNED && !hasPermission) return;
			if(ownerFilter == OwnerFilter.UNOWNED && hasPermission) return;

			// Type badge
			boolean isPrefix = tag.getTagType() == Tag.TagType.PREFIX;
			String typeBadge = isPrefix
					? "<aqua>▍</aqua><dark_gray> 前綴 ━ 名稱前方</dark_gray>"
					: "<light_purple>▍</light_purple><dark_gray> 後綴 ━ 名稱後方</dark_gray>";

			GuiItem guiItem;
			if(hasPermission){
				ItemStack itemStack = tag.getIcon().clone();
				ItemMeta meta = itemStack.getItemMeta();
				if(meta != null){
					// PAPI on display name
					if(meta.hasDisplayName()){
						Component nameComp = meta.displayName();
						if(nameComp != null){
							String dn = mm.serialize(nameComp);
							dn = PlaceholderAPI.setPlaceholders(player, dn);
							meta.displayName(mm.deserialize(dn));
						}
					}
					// Rebuild lore
					List<Component> newLore = new ArrayList<>();
					newLore.add(noItalic(typeBadge));
					newLore.add(Component.empty());
					if(meta.hasLore()){
						List<Component> orig = meta.lore();
						if(orig != null){
							for(Component c : orig){
								String line = mm.serialize(c);
								line = PlaceholderAPI.setPlaceholders(player, line);
								newLore.add(mm.deserialize(line));
							}
						}
					}
					meta.lore(newLore);
					itemStack.setItemMeta(meta);
				}

				// Active check
				boolean isActive = (playerPrefixTag != null && playerPrefixTag.equals(tag))
						|| (playerSuffixTag != null && playerSuffixTag.equals(tag));

				if(isActive){
					itemStack.editMeta(im -> {
						Component name = im.displayName();
						if(name == null) name = Component.empty();
						im.displayName(name.append(noItalic(" <green>✔</green>")));

						im.addEnchant(Enchantment.INFINITY, 1, false);
						im.addItemFlags(ItemFlag.HIDE_ENCHANTS);

						List<Component> lore = im.lore();
						if(lore == null) lore = new ArrayList<>();
						lore.add(Component.empty());
						lore.add(noItalic("<green>┌──────────────┐</green>"));
						lore.add(noItalic("<green>│  ✔  已裝備                    │</green>"));
						lore.add(noItalic("<green>└──────────────┘</green>"));
						lore.add(noItalic("<dark_gray>  ☛ 選擇其他稱號以更換</dark_gray>"));
						im.lore(lore);
					});
				}else{
					itemStack.editMeta(im -> {
						List<Component> lore = im.lore();
						if(lore == null) lore = new ArrayList<>();
						lore.add(Component.empty());
						lore.add(noItalic("<gold>┌──────────────┐</gold>"));
						lore.add(noItalic("<gold>│</gold> <yellow>☛ 點擊裝備</yellow>                   <gold> │</gold>"));
						lore.add(noItalic("<gold>└──────────────┘</gold>"));
						im.lore(lore);
					});
				}

				guiItem = new GuiItem(itemStack, e -> {
					e.setCancelled(true);
					PlayerTagManager.setPlayerTag(player, tag);
					Bukkit.getScheduler().runTaskLater(Main.plugin, () -> showGUI(player), 1L);
				});
			}else{
				// Locked
				ItemStack locked = new ItemStack(Material.GRAY_DYE);
				locked.editMeta(im -> {
					im.displayName(mm.deserialize(tag.getText()).decoration(TextDecoration.ITALIC, false));
					List<Component> lore = new ArrayList<>();
					lore.add(noItalic(typeBadge));
					lore.add(Component.empty());
					if(tag.isHoverDescription()){
						for(String line : tag.getDescription().split("\\n|\\\\n")){
							lore.add(mm.deserialize(line).decoration(TextDecoration.ITALIC, false));
						}
						lore.add(Component.empty());
					}
					lore.add(noItalic("<red>┌──────────────┐</red>"));
					lore.add(noItalic("<red>│  🔒 尚未解鎖                   │</red>"));
					lore.add(noItalic("<red>└──────────────┘</red>"));
					im.lore(lore);
				});
				guiItem = new GuiItem(locked, e -> e.setCancelled(true));
			}
			tagItems.add(guiItem);
		});

		tagPane.populateWithGuiItems(tagItems);

		// ═══════════════ Paging ═══════════════
		PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(1, 5), 7, Pane.Priority.HIGHEST, tagPane);

		ItemStack backArrow = new ItemStack(Material.ARROW);
		backArrow.editMeta(m -> {
			m.displayName(noItalic("<gold>◄ 上一頁</gold>"));
		});
		pagingButtons.setBackwardButton(new GuiItem(backArrow));

		ItemStack fwdArrow = new ItemStack(Material.ARROW);
		fwdArrow.editMeta(m -> {
			m.displayName(noItalic("<gold>下一頁 ►</gold>"));
		});
		pagingButtons.setForwardButton(new GuiItem(fwdArrow));

		gui.addPane(pagingButtons);
		gui.addPane(tagPane);
		gui.show(player);
	}
}
