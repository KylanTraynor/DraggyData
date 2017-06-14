package com.kylantraynor.draggydata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * Created by charliej on 14/05/2017.
 * Last modification DiscowZombie on 5/06/2017.
 */

public class AdvancementAPI {

    private NamespacedKey id;
    private String title, parent, trigger, description, background;
    private ItemStack icon;
    private boolean announce, toast;
    private FrameType frame;
    private List<ItemStack> items;
    private Map<String, TriggerType> criterias = new HashMap<String, TriggerType>();

    public AdvancementAPI(NamespacedKey id) {
        this.id = id;
        this.items = Lists.newArrayList();
        this.announce = true;
        this.toast = true;
    }

    public String getID() {
        return id.toString();
    }

    public Map<String, TriggerType> getCriterias(){
    	return criterias;
    }
    
    public AdvancementAPI withCriterias(Map<String, TriggerType> map){
    	this.criterias = map;
    	return this;
    }
    
    public ItemStack getIcon() {
        return icon;
    }

    public AdvancementAPI withIcon(ItemStack icon) {
        this.icon = icon;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AdvancementAPI withDescription(String description) {
        this.description = description;
        return this;
    }

    public String getBackground() {
        return background;
    }

    public AdvancementAPI withBackground(String url) {
        this.background = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public AdvancementAPI withTitle(String title) {
        this.title = title;
        return this;
    }

    public String getParent() {
        return parent;
    }

    public AdvancementAPI withParent(String parent) {
        this.parent = parent;
        return this;
    }

    public String getTrigger() {
        return trigger;
    }

    public AdvancementAPI withTrigger(String trigger) {
        this.trigger = trigger;
        return this;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public AdvancementAPI withItem(ItemStack is) {
        items.add(is);
        return this;
    }

    public FrameType getFrame() {
        return frame;
    }

    public AdvancementAPI withFrame(FrameType frame) {
        this.frame = frame;
        return this;
    }

    public boolean getAnnouncement(){
        return announce;
    }
    
    public AdvancementAPI withAnnouncement(boolean announce){
        this.announce = announce;
        return this;
    }
    
    public boolean getToast(){
    	return toast;
    }
    
    public AdvancementAPI withToast(boolean toast){
    	this.toast = toast;
    	return this;
    }
    
    private boolean hasDisplay(){
    	return getTitle() != null || getIcon() != null || getDescription() != null || getBackground() != null;
    }

	public String getJSON() {
        JSONObject json = new JSONObject();

        JSONObject icon = new JSONObject();
        icon.put("item", "minecraft:" + getIcon().getType().name().toLowerCase());

        JSONObject display = new JSONObject();
        display.put("icon", icon);
        display.put("title", getTitle());
        display.put("description", getDescription());
        display.put("background", getBackground());
        display.put("frame", getFrame().toString());
        display.put("announce_to_chat", getAnnouncement());
        display.put("show_toast", getToast());

        json.put("parent", getParent());

        JSONObject criteria = new JSONObject();
        JSONObject conditions = new JSONObject();
        //JSONObject elytra = new JSONObject();

        JSONArray itemArray = new JSONArray();
        JSONObject itemJSON = new JSONObject();

        for(ItemStack i : getItems()) {
            itemJSON.put("item", "minecraft:"+ i.getType().name().toLowerCase());
            itemJSON.put("amount", i.getAmount());
            itemArray.add(itemJSON);
        }

        /**
         * Define each criteria, for each criteria in list,
         * add items, trigger and conditions
         */
        //conditions.put("items", itemArray);
        //elytra.put("trigger", getTrigger());
        //elytra.put("conditions", conditions);
        for(Entry<String, TriggerType> e : criterias.entrySet()){
        	JSONObject crit = new JSONObject();
        	crit.put("trigger", e.getValue().toString());
        	
        	criteria.put(e.getKey(), crit);
        }
        //criteria.put("elytra", elytra);

        json.put("criteria", criteria);
        
        if(hasDisplay())
        	json.put("display", display);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    public void save(String world) {
        this.save(Bukkit.getWorld(world));
    }
    
    public Advancement load(){
    	return Bukkit.getUnsafe().loadAdvancement(this.id, getJSON());
    }
    
    public void save(World world) {
    	try {
			Files.createDirectories(Paths.get(world.getWorldFolder() + File.separator + "data" + File.separator + "advancements"
					+ File.separator + id.getNamespace()));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
    	File file = new File(world.getWorldFolder() + File.separator + "data" + File.separator + "advancements"
    			+ File.separator + id.getNamespace() + File.separator + id.getKey() + ".json");
    	try{
			file.createNewFile();
			FileWriter writer = new FileWriter(file);
			writer.write(getJSON());
    		writer.flush();
    		writer.close();
    		Bukkit.getLogger().info("[AdvancementAPI] Created " + id.toString());
		}catch(IOException e){
			e.printStackTrace();
		}
    }
    
    
    public enum FrameType {
    	TASK("task"),
    	GOAL("goal"),
    	CHALLENGE("challenge");
    	
    	private String name = "task";
    	
    	private FrameType(String name){
    	  this.name = name;
    	}  
    	
    	public String toString(){
    	  return name;
    	}
    }
    
    public enum TriggerType {
    	
    	IMPOSSIBLE("impossible");
    	
    	private String name = "impossible";
    	
    	private TriggerType(String name){
    		this.name = name;
    	}
    	
    	public String toString(){
    		return "minecraft:"+name;
    	}
    }
    
}