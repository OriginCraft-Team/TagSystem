package the.david.tagSystem;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import the.david.tagSystem.command.CommandManager;
import the.david.tagSystem.command.TabCompleteManager;
import the.david.tagSystem.data.ConfigManager;
import the.david.tagSystem.manager.PlayerTagManager;
import the.david.tagSystem.manager.TagManager;
import the.david.tagSystem.placeholder.TagSystemExpansion;

public final class Main extends JavaPlugin{
	public CommandManager commandManager;
	public TabCompleteManager tabCompleteManager;
	public TagManager tagManager;
	public static JavaPlugin instance;
	public static Main plugin;
	public static LuckPerms luckPerms;

	@Override
	public void onEnable(){
		// Plugin startup logic
		luckPerms = getServer().getServicesManager().load(LuckPerms.class);
		plugin = this;
		instance = this;
		commandManager = new CommandManager();
		Bukkit.getPluginCommand("tagsystem").setExecutor(commandManager);
		tabCompleteManager = new TabCompleteManager();
		Bukkit.getPluginCommand("tagsystem").setTabCompleter(tabCompleteManager);
		ConfigManager.loadConfigs();
		TagManager.loadTags();

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new TagSystemExpansion().register();
		}

		// Schedule tag permission check every minute (20 ticks * 60 seconds = 1200 ticks)
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				PlayerTagManager.checkAndClearInvalidTags(player);
			}
		}, 1200L, 1200L);
	}

	@Override
	public void onDisable(){
		// Plugin shutdown logic
	}
}
