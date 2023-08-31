package jp.ne.clane.commons;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jp.ne.clane.espAnvil.EspAnvilMain;
import net.minecraft.client.Minecraft;

public abstract class ConfigBase {
	private Class<? extends ConfigBase> configClass = null;
	
	public ConfigBase(Class<? extends ConfigBase> conClass) {
		configClass = conClass;
	}
	
	public void saveConfig(File configFileName) throws IllegalAccessException, IOException {
		List<String> saveStrings = new LinkedList<String>();
		for (Field member : configClass.getFields()) {
			try {
				saveStrings.add(member.getName() + " = " + member.getBoolean(null));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw e;
			}
		}
		try {
			Files.write(configFileName.toPath(), saveStrings,StandardOpenOption.WRITE,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
				throw e;
		}
	}

	public void loadConfig(File configFileName) throws IllegalAccessException,IOException {
		List<String> loadStrings = Files.readAllLines(configFileName.toPath());
		Map<String, String> loadMap = new HashMap<String, String>();
		for (String line : loadStrings) {
			String[] keyValue = line.split("=",2);
			loadMap.putIfAbsent(keyValue[0].strip(), keyValue[1].strip());
		}
		for (Field member : configClass.getFields()) {
			try {
				if (loadMap.containsKey(member.getName())) {
					member.setBoolean(null, Boolean.valueOf(loadMap.get(member.getName())));
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw e;
			}
		}
	}
	  
	public static File getConfigFile() {
		return getConfigFile(EspAnvilMain.MOD_ID);
	}

	public static File getConfigFile(String saveFileName) {
		  return getConfigFile("", saveFileName);
	}

	public static File getConfigFile(String relativePath, String saveFileName) {
		  return getConfigFile(relativePath, saveFileName, "txt");
	}

	public static File getConfigFile(String relativePath, String saveFileName, String extention) {
		  Minecraft mc = Minecraft.getInstance();
		  return Paths.get(mc.gameDirectory.getPath(),"config",relativePath,saveFileName + "." + extention).toFile();
	}
}
