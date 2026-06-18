package the.david.tagSystem.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import the.david.tagSystem.impl.Tag;
import the.david.tagSystem.manager.PlayerTagManager;

public class TagSystemExpansion extends PlaceholderExpansion {

    /** Fixed dark_gray bracket frame applied to every suffix on placeholder output (not stored in config / GUI). */
    private static final String SUFFIX_BRACKET_OPEN  = "<dark_gray>[</dark_gray>";
    private static final String SUFFIX_BRACKET_CLOSE = "<dark_gray>]</dark_gray>";

    @Override public @NotNull String getIdentifier() { return "tagsystem"; }
    @Override public @NotNull String getAuthor()     { return "david"; }
    @Override public @NotNull String getVersion()    { return "1.0"; }
    @Override public boolean persist()               { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        return switch (params) {
            case "prefix"       -> getTagText(PlayerTagManager.getPlayerPrefixTag(player));
            case "prefix_plain" -> getTagPlain(PlayerTagManager.getPlayerPrefixTag(player));
            case "prefix_legacy" -> getTagLegacy(PlayerTagManager.getPlayerPrefixTag(player));
            case "suffix"       -> getTagText(PlayerTagManager.getPlayerSuffixTag(player));
            case "suffix_plain"    -> getTagPlain(PlayerTagManager.getPlayerSuffixTag(player));
            case "suffix_legacy" -> getTagLegacy(PlayerTagManager.getPlayerSuffixTag(player));
            case "prefix_weight"   -> getTagWeight(PlayerTagManager.getPlayerPrefixTag(player));
            default                -> null;
        };
    }

    /**
     * Builds the MiniMessage string actually shown in chat/tab/etc. Suffix tags are wrapped in the fixed
     * dark_gray bracket frame here rather than in the tag's configured text, so the frame appears on placeholder
     * output only and never needs to be stored in config (and is therefore absent from the GUI icon).
     */
    private String getDisplayText(Tag tag) {
        if (tag.getTagType() == Tag.TagType.SUFFIX) {
            return SUFFIX_BRACKET_OPEN + tag.getText() + SUFFIX_BRACKET_CLOSE;
        }
        return tag.getText();
    }

    private String getTagText(Tag tag) {
        if (tag == null) return "";
        String text = getDisplayText(tag);
        if (tag.isHoverDescription()) {
            String description = tag.getDescription().replace("'", "\\'");
            return "<hover:show_text:'" + description + "'>" + text + "</hover>";
        }
        return text;
    }

    private String getTagPlain(Tag tag) {
        if (tag == null) return "";
        return PlainTextComponentSerializer.plainText()
                .serialize(MiniMessage.miniMessage().deserialize(getDisplayText(tag)));
    }

    private String getTagLegacy(Tag tag) {
        if (tag == null) return "";
        // 將 MiniMessage 反序列化為 Component，再序列化為傳統顏色文字
        return LegacyComponentSerializer.builder()
                .character('§')
                .hexColors()
                .build()
                .serialize(MiniMessage.miniMessage().deserialize(getDisplayText(tag)));
    }

    private String getTagWeight(Tag tag) {
        return tag != null ? String.valueOf(tag.getWeight()) : "0";
    }

}
