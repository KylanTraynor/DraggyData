package com.kylantraynor.DraggyData;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerData {
	
	private static Map<UUID, PlayerData> all = new HashMap<UUID, PlayerData>();

	public static PlayerData get(UUID id){
		if(all.containsKey(id)){
			return all.get(id);
		} else {
			new PlayerData(id);
			return all.get(id);
		}
	}

	private UUID playerId;
	private YamlConfiguration config = new YamlConfiguration();
	private boolean hasChanged = true;
	
	public PlayerData(UUID id) {
		playerId = id;
		File f = getFile();
		try {
			config.load(f);
			all.put(id, this);
		} catch (IOException | InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public File getFile(){
		File f = new File(DraggyData.getPlayerDataDirectory(), playerId.toString() + ".yml");
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return f;
	}
	
	public void update(){
		if(Math.random() < 0.01){
			takeSkillExperience("Alcohol", 1);
			takeSkillExperience("Lock Picking", 1);
		}
		if(hasChanged){
			try{save();} catch (Exception e){e.printStackTrace();}
		}
	}
	
	public void save(){
		File f = getFile();
		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getLevelForExperience(int exp){
		return Math.max((int) (Math.floor(Math.sqrt(exp) / 10.0)), 1);
	}
	
	public int getSkillExperience(String skill){
		if(config.contains("Skills." + skill)){
			return Math.max(config.getInt("Skills." + skill), 1);
		} else {
			return 1;
		}
	}
	
	public void giveSkillExperience(String skill, int amount){
		int oldLevel = getSkillLevel(skill);
		int currentExp = getSkillExperience(skill);
		config.set("Skills." + skill, Math.max(currentExp + amount, 1));
		hasChanged = true;
		if(oldLevel != getSkillLevel(skill)){
			Player p = Bukkit.getPlayer(playerId);
			if(p != null){
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
				p.sendMessage(ChatColor.GOLD + "[" + ChatColor.AQUA + skill + ChatColor.GOLD + "] Skill leveled up!");
			}
		}
	}
	
	public int getSkillExperienceForLevel(int level){
		return (int) ((level * 10.0) * (level * 10.0));
	}
	
	public int getSkillExpToNextLevel(String skill){
		int level = getSkillLevel(skill);
		if(level <= 0) return 1;
		return getSkillExperienceForLevel(level + 1) - getSkillExperienceForLevel(level);
	}
	
	public int getSkillExpToNextLevel(int level){
		if(level <= 0) return 1;
		return getSkillExperienceForLevel(level + 1) - getSkillExperienceForLevel(level);
	}
	
	public int getSkillLevelExp(String skill){
		return getSkillExperience(skill) - getSkillExperienceForLevel(getSkillLevel(skill));
	}
	
	public void takeSkillExperience(String skill, int amount){
		giveSkillExperience(skill, -amount);
	}

	public static void updateAll() {
		for(PlayerData pd : all.values()){
			pd.update();
		}
	}

	public int getSkillLevel(String skill) {
		return getLevelForExperience(getSkillExperience(skill));
	}
	
	public String getString(String attribute){
		if(this.config.contains("General." + attribute)){
			return this.config.getString("General." + attribute);
		}
		return null;
	}
	
	public void set(String attribute, Object value){
		this.config.set("General." + attribute, value);
	}
	
	public int getStat(String stat){
		return getStat(stat, 0);
	}
	
	public int getStat(String stat, int defaultValue){
		if(this.config.contains("General.Stats." + stat)){
			return this.config.getInt("General.Stats." + stat);
		}
		return defaultValue;
	}
	
	public void setStat(String stat, int newValue){
		this.config.set("General.Stats." + stat, newValue);
		hasChanged = true;
	}
	
	public boolean hasAchievement(Achievement a){
		return this.config.contains("Achievements." + a.getId());
	}
	
	public boolean giveAchievement(Achievement a){
		if(!hasAchievement(a)){
			this.config.set("Achievements." + a.getId(), Instant.now().toString());
			hasChanged = true;
			return true;
		} else {
			return false;
		}
	}
}