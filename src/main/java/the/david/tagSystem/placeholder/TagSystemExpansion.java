package the.david.tagSystem.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import the.david.tagSystem.impl.Tag;
import the.david.tagSystem.manager.PlayerTagManager;

public class TagSystemExpansion extends PlaceholderExpansion {

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
            case "suffix"       -> getTagText(PlayerTagManager.getPlayerSuffixTag(player));
            case "suffix_plain"    -> getTagPlain(PlayerTagManager.getPlayerSuffixTag(player));
            case "prefix_weight"   -> getTagWeight(PlayerTagManager.getPlayerPrefixTag(player));
            default                -> null;
        };
    }

    private String getTagText(Tag tag) {
        return tag != null ? tag.getText() : "";
    }

    private String getTagWeight(Tag tag) {
        return tag != null ? String.valueOf(tag.getWeight()) : "0";
    }

    private String getTagPlain(Tag tag) {
        if (tag == null) return "";
        return PlainTextComponentSerializer.plainText()
                .serialize(MiniMessage.miniMessage().deserialize(tag.getText()));
    }
}
