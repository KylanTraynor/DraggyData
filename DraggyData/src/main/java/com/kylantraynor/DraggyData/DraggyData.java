package com.kylantraynor.DraggyData;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

public class DraggyData extends JavaPlugin{
	private static DraggyData currentInstance;

	@Override
	public void onEnable(){
		currentInstance = this;
	}

	public static File getPlayerDataDirectory() {
		File dir = currentInstance.getDataFolder();
		if(!dir.exists()) dir.mkdir();
		File dir1 = new File(dir, "PlayerData");
		if(!dir1.exists()) dir.mkdir();
		return dir1;
	}
	
}