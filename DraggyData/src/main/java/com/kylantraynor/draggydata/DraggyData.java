package com.kylantraynor.draggydata;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DraggyData extends JavaPlugin{
	private static DraggyData currentInstance;

	@Override
	public void onEnable(){
		currentInstance = this;
		
		BukkitRunnable bk = new BukkitRunnable(){
			@Override
			public void run() {
				PlayerData.updateAll();
			}
		};
		
		bk.runTaskTimer(this, 20L, 20L * 5);
	}

	public static File getPlayerDataDirectory() {
		File dir = currentInstance.getDataFolder();
		if(!dir.exists()) dir.mkdir();
		File dir1 = new File(dir, "PlayerData");
		if(!dir1.exists()) dir1.mkdir();
		return dir1;
	}
	
}