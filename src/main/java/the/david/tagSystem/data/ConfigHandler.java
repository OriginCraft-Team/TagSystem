package the.david.tagSystem.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import the.david.tagSystem.Main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigHandler{
	public ConfigHandler(String filePath){
		this.filePath = filePath;
		createCustomConfigFile();
	}

	String filePath;
	private File dataConfigFile;
	private FileConfiguration dataConfig;

	public void createCustomConfigFile(){
		dataConfigFile = new File(Main.instance.getDataFolder(), filePath);
		if(!dataConfigFile.exists()){
			dataConfigFile.getParentFile().mkdirs();
			if(Main.instance.getResource(filePath) != null){
				Main.instance.saveResource(filePath, false);
			}else{
				try{
					dataConfigFile.createNewFile();
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
		}
		dataConfig = new YamlConfiguration();
		try{
			dataConfig.load(dataConfigFile);
		}catch(IOException | InvalidConfigurationException e){
			throw new RuntimeException(e);
		}
		try{
			dataConfig.save(dataConfigFile);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public void saveFile(){
		try{
			dataConfig.save(dataConfigFile);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public void setComments(String key, List<String> comments){
		dataConfig.setComments(key, comments);
		saveFile();
	}

	public void setObject(String key, Object value){
		dataConfig.set(key, value);
		saveFile();
	}

	public boolean getBoolean(String key){
		return dataConfig.getBoolean(key);
	}

	public int getInteger(String key){
		return dataConfig.getInt(key);
	}

	public Set<String> getKeys(String path){
		ConfigurationSection section = dataConfig.getConfigurationSection(path);
		if(section == null){
			return null;
		}else{
			return section.getKeys(false);
		}
	}

	public String getString(String key){
		return dataConfig.getString(key);
	}

	public List<String> getStringList(String key){
		return dataConfig.getStringList(key);
	}

	public boolean hasKey(String path){
		return dataConfig.contains(path);
	}
}
