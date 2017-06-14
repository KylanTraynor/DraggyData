package com.kylantraynor.draggydata;

import java.util.HashMap;
import java.util.Map;

import mkremins.fanciful.FancyMessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AchievementManager {
	public static Map<String, Achievement> achievements = new HashMap<String, Achievement>();
	
	public static boolean registerAchievement(Achievement achievement){
		if(achievements.containsKey(achievement.getId())){
			return false;
		} else {
			achievements.put(achievement.getId(), achievement);
			return true;
		}
	}
	
	public static void broadcast(Player player, Achievement achievement){
		FancyMessage fm = new FancyMessage(player.getDisplayName() + ChatColor.AQUA + " has received the achivement ");
		fm.then(achievement.getName()).color(ChatColor.GOLD).tooltip(achievement.getDescription());
		fm.then("!").color(ChatColor.AQUA);
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			fm.send(p);
		}
	}
	
	public static void giveAchievement(Player player, String id){
		if(achievements.containsKey(id)){
			if(PlayerData.get(player.getUniqueId()).giveAchievement(achievements.get(id))){
				broadcast(player, achievements.get(id));
			}
		}
	}
}
