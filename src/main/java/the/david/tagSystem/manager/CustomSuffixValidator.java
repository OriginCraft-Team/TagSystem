package the.david.tagSystem.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared rules and validation for the player-defined custom suffix tag (MiniMessage).
 * Used by both the in-game command and the info dialog. The online editor at
 * {@link #WEBUI_URL} must mirror these exact rules — see {@code detail.md}.
 */
public final class CustomSuffixValidator{
	private CustomSuffixValidator(){}

	/** Online tag editor where players design their suffix style. */
	public static final String WEBUI_URL = "https://tag.origincraft.tw";

	/**
	 * 標籤白名單：只允許基本顏色與樣式。hex {@code <#rrggbb>} 與 {@code <color:…>} 另行特判。
	 * 任何不在此集合的標籤（hover/click/insertion/font/key/translatable/selector/score/nbt…）一律拒絕。
	 */
	public static final Set<String> ALLOWED_TAGS = Set.of(
			// 16 named colors
			"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
			"gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
			"yellow", "white",
			// color helpers
			"color", "c", "gradient", "rainbow",
			// decorations + shorthands
			"bold", "b", "italic", "i", "em",
			"underlined", "u", "strikethrough", "st", "obfuscated", "obf",
			"reset", "r"
	);

	/** MiniMessage 原始字串長度上限（玩家實際輸入）。 */
	public static final int MAX_SOURCE_LEN = 170;
	/** 處理後可見字數上限（扣除標籤後的純文字）。 */
	public static final int MAX_VISIBLE_LEN = 10;

	/** Restricted MiniMessage instance that only resolves the whitelisted tag families. */
	public static final MiniMessage RESTRICTED_MM = MiniMessage.builder()
			.tags(TagResolver.resolver(
					StandardTags.color(),
					StandardTags.decorations(),
					StandardTags.gradient(),
					StandardTags.rainbow(),
					StandardTags.reset()))
			.build();

	private static final Pattern TAG_TOKEN = Pattern.compile("<[^>]+>");

	/** @return error message if invalid, otherwise {@code null}. */
	public static String validate(String input){
		// 1) 不得含空白
		if(input.chars().anyMatch(Character::isWhitespace)){
			return "稱號不可包含空格。";
		}
		// 2) 原始長度上限
		if(input.codePointCount(0, input.length()) > MAX_SOURCE_LEN){
			return "稱號原始長度不可超過 " + MAX_SOURCE_LEN + " 字。";
		}
		// 3) 標籤白名單
		Matcher m = TAG_TOKEN.matcher(input);
		while(m.find()){
			String token = m.group();                     // e.g. "<gradient:red:gold>" or "</bold>"
			String name = token.substring(1, token.length() - 1); // strip < >
			if(name.startsWith("/")) name = name.substring(1);    // closing tag
			int colon = name.indexOf(':');
			if(colon >= 0) name = name.substring(0, colon);       // tag name before first ':'
			name = name.toLowerCase();
			if(name.startsWith("#")) continue;            // hex color <#rrggbb>
			if(!ALLOWED_TAGS.contains(name)){
				return "不允許的標籤 <" + name + ">，只能使用顏色與粗細等基本樣式。";
			}
		}
		// 4) 語法解析 + 可見字數
		Component parsed;
		try{
			parsed = RESTRICTED_MM.deserialize(input);
		}catch(Exception e){
			return "MiniMessage 語法錯誤。";
		}
		String plain = PlainTextComponentSerializer.plainText().serialize(parsed);
		if(plain.codePointCount(0, plain.length()) > MAX_VISIBLE_LEN){
			return "可見字數不可超過 " + MAX_VISIBLE_LEN + " 字（目前 " + plain.codePointCount(0, plain.length()) + " 字）。";
		}
		return null;
	}
}
