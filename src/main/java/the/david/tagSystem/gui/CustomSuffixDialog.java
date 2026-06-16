package the.david.tagSystem.gui;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import the.david.tagSystem.manager.CustomSuffixValidator;
import the.david.tagSystem.manager.PlayerTagManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Info-only Paper Dialog that points eligible players to the online tag editor.
 * Players no longer type the suffix here — they design it at {@link CustomSuffixValidator#WEBUI_URL}
 * and apply it via {@code /ts customsuffix <content>} (see {@code detail.md}).
 */
public class CustomSuffixDialog{

	private static Component noItalic(String miniMessage){
		return MiniMessage.miniMessage().deserialize(miniMessage).decoration(TextDecoration.ITALIC, false);
	}

	public static void open(Player player){
		String existing = PlayerTagManager.getPlayerCustomSuffixContent(player);

		List<DialogBody> body = new ArrayList<>();
		body.add(DialogBody.plainMessage(noItalic("<gray>前往線上稱號工坊，用點擊的方式搭配你的專屬後綴稱號。</gray>")));
		if(existing != null){
			body.add(DialogBody.plainMessage(Component.text()
					.append(noItalic("<gray>目前稱號 ▸ </gray>"))
					.append(CustomSuffixValidator.RESTRICTED_MM.deserialize(existing).decoration(TextDecoration.ITALIC, false))
					.build()));
		}
		body.add(DialogBody.plainMessage(noItalic("<gray>• 可見字數上限 <yellow>" + CustomSuffixValidator.MAX_VISIBLE_LEN + "</yellow> 字、原始上限 <yellow>" + CustomSuffixValidator.MAX_SOURCE_LEN + "</yellow> 字</gray>")));
		body.add(DialogBody.plainMessage(noItalic("<gray>• 只允許顏色與粗細等基本樣式，不可有空格或 hover/click 等互動標籤</gray>")));
		body.add(DialogBody.plainMessage(noItalic("<gray>• 設計完成後，複製網站給的指令到遊戲執行即可套用</gray>")));
		body.add(DialogBody.plainMessage(Component.text()
				.append(noItalic("<gray>網址 ▸ </gray>"))
				.append(noItalic("<aqua><underlined>" + CustomSuffixValidator.WEBUI_URL + "</underlined></aqua>"))
				.build()));

		Dialog dialog = Dialog.create(builder -> builder.empty()
				.base(DialogBase.builder(noItalic("<gradient:gold:yellow>✦ 自訂後綴稱號 ✦</gradient>"))
						.body(body)
						.build())
				.type(DialogType.confirmation(
						ActionButton.create(
								noItalic("<green>✎ 前往編輯器</green>"),
								noItalic("<gray>開啟 " + CustomSuffixValidator.WEBUI_URL + "</gray>"),
								150,
								DialogAction.staticAction(ClickEvent.openUrl(CustomSuffixValidator.WEBUI_URL))
						),
						ActionButton.create(
								noItalic("<red>✘ 關閉</red>"),
								noItalic("<gray>關閉此視窗</gray>"),
								120,
								null
						)
				))
		);

		player.showDialog(dialog);
	}
}
