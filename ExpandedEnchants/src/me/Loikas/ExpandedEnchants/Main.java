package me.Loikas.ExpandedEnchants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.Loikas.ExpandedEnchants.Commands.CommandHandler;
import me.Loikas.ExpandedEnchants.Commands.CustomEnchantsTabCompleter;
import me.Loikas.ExpandedEnchants.Inventories.InventoryManager;
import me.Loikas.ExpandedEnchants.UpdateChecker.UpdateChecker;
import me.Loikas.ExpandedEnchants.Util.AssassinInfo;
import me.Loikas.ExpandedEnchants.Util.CustomEnchantmentRecipe;
import net.milkbowl.vault.economy.Economy;


public class Main extends JavaPlugin
{
	
	private static Main plugin;
	
	public static EventsClass eventsClass;
	public static RecipeManager recipeManager;
	public static ItemManager itemManager;
	public static InventoryManager inventoryManager;
	
	public static Economy econ;
	
	public static ArrayList<CustomEnchantmentRecipe> customRecipes = new ArrayList<CustomEnchantmentRecipe>();
	
	public static void Log(String message) {
		if(getPlugin().getConfig().getBoolean("debug", false)) Bukkit.getServer().getConsoleSender().sendMessage(message);
	}
	
	@Override
	public void onEnable() 
	{
		plugin = this;
		
		this.saveDefaultConfig();
		setupEconomy();
		
		if (!this.getConfig().getBoolean("EnableVaultIntegration")) { Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "ExpandedEnchants" + ChatColor.WHITE + ": Vault integration disabled."); econ = null; }
		else if(econ == null) Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "ExpandedEnchants" + ChatColor.WHITE + ": Vault not found. Starting ExpandedEnchants without Vault.");
		else Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "ExpandedEnchants" + ChatColor.WHITE + ": Vault found. Enabling ExpandedEnchants integration with Vault.");
		
		itemManager = new ItemManager();
	
			try
			{
				new YamlConfiguration().load("plugins/ExpandedEnchants/SaveData.yml");
			} catch (IOException | InvalidConfigurationException e)
			{
				this.saveResource("SaveData.yml", false);
			} 
	
		CustomEnchantsManager.custom_enchants = new ArrayList<>();
		AddAllCustomEnchants();
		RegisterRecipes();
		
		CustomEnchantsManager.Register();
		
		eventsClass = new EventsClass();
		recipeManager = new RecipeManager(); 
		inventoryManager = new InventoryManager();
		
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "ExpandedEnchants" + ChatColor.WHITE + ": Found " + ChatColor.LIGHT_PURPLE + EventsClass.functions.GetEnabledEnchants().size() + ChatColor.WHITE + " enabled custom enchantments!");
		
		this.getServer().getPluginManager().registerEvents(eventsClass, this);
		this.getServer().getPluginManager().registerEvents(inventoryManager, this);
		
		this.getCommand("expandedenchants").setTabCompleter(new CustomEnchantsTabCompleter());
		this.getCommand("expandedenchants").setExecutor(new CommandHandler());
		
		new BukkitRunnable() {
			@Override
			public void run() {
				eventsClass.ConstantCheckMethods();
			}
		}.runTaskTimer(this, 0, 20);
		HandleLoadMaps();
		
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		if(board.getTeam(ChatColor.BLACK + "ee_oresight_coal") == null)  {
			Team team = board.registerNewTeam(ChatColor.BLACK + "ee_oresight_coal");
			team.setColor(ChatColor.BLACK);
		}
		if(board.getTeam(ChatColor.GOLD + "ee_oresight_iron") == null)  {
			Team team = board.registerNewTeam(ChatColor.GOLD + "ee_oresight_iron");
			team.setColor(ChatColor.GOLD);
		}
		if(board.getTeam(ChatColor.RED + "ee_oresight_copper") == null) {
			Team team = board.registerNewTeam(ChatColor.RED + "ee_oresight_copper");
			team.setColor(ChatColor.RED);
		}
		if(board.getTeam(ChatColor.YELLOW + "ee_oresight_gold") == null)  {
			Team team = board.registerNewTeam(ChatColor.YELLOW + "ee_oresight_gold");
			team.setColor(ChatColor.YELLOW);
		}
		if(board.getTeam(ChatColor.DARK_RED + "ee_oresight_redstone") == null)  {
			Team team = board.registerNewTeam(ChatColor.DARK_RED + "ee_oresight_redstone");
			team.setColor(ChatColor.DARK_RED);
		}
		if(board.getTeam(ChatColor.DARK_BLUE + "ee_oresight_lapis") == null)  {
			Team team = board.registerNewTeam(ChatColor.DARK_BLUE+ "ee_oresight_lapis");
			team.setColor(ChatColor.DARK_BLUE);
		}
		if(board.getTeam(ChatColor.AQUA + "ee_oresight_diamond") == null)  {
			Team team = board.registerNewTeam(ChatColor.AQUA + "ee_oresight_diamond");
			team.setColor(ChatColor.AQUA);
		}
		if(board.getTeam(ChatColor.GREEN + "ee_oresight_emerald") == null)  {
			Team team = board.registerNewTeam(ChatColor.GREEN + "ee_oresight_emerald");
			team.setColor(ChatColor.GREEN);
		}
		if(board.getTeam(ChatColor.RED + "ee_oresight_debris") == null)  {
			Team team = board.registerNewTeam(ChatColor.RED + "ee_oresight_debris");
			team.setColor(ChatColor.RED);
		}
		if(getPlugin().getConfig().getBoolean("EnableUpdateCheck"))
			UpdateChecker.init(this, 98780).setDownloadLink(98780).setDonationLink("https://www.buymeacoffee.com/loikas").setChangelogLink("https://www.spigotmc.org/resources/expandedenchants.98780/updates").setNotifyOpsOnJoin(true).checkNow();
	}
	
	public static Main getPlugin() {
		return plugin;
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	@Override
	public void onDisable() 
	{
		try { HandleSaveMaps(); } 
		catch (IOException e) { e.printStackTrace(); }
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getOpenInventory() != null) {
				if(p.getOpenInventory().getTopInventory().equals(inventoryManager.customChoose) || p.getOpenInventory().getTopInventory().equals(inventoryManager.vanillaChoose) || p.getOpenInventory().getTopInventory().equals(inventoryManager.startInv)) p.closeInventory();
				if(inventoryManager.customRecipeInv.containsKey(p.getUniqueId())) if(p.getOpenInventory().getTopInventory().equals(inventoryManager.customRecipeInv.get(p.getUniqueId()).inv)) p.closeInventory();
				if(inventoryManager.vanillaRecipeInv.containsKey(p.getUniqueId())) if(p.getOpenInventory().getTopInventory().equals(inventoryManager.vanillaRecipeInv.get(p.getUniqueId()).inv)) p.closeInventory();
			}
			
		}
		eventsClass = null;
		itemManager = null;
		recipeManager = null;
		inventoryManager = null;		
		
		Bukkit.getScoreboardManager().getMainScoreboard().getTeam(ChatColor.BLACK + "ee_oresight_coal").unregister();
		Bukkit.getScoreboardManager().getMainScoreboard().getTeam(ChatColor.RED + "ee_oresight_copper").unregister();
		Bukkit.getScoreboardManager().getMainScoreboard().getTeam(ChatColor.GOLD + "ee_oresight_iron").unregister();
		Bukkit.getScoreboardManager().getMainScoreboard().getTeam(ChatColor.YELLOW + "ee_oresight_gold").unregister();
		Bukkit.getScoreboardManager().getMainScoreboard().getTeam(ChatColor.DARK_BLUE + "ee_oresight_lapis").unregister();
		Bukkit.getScoreboardManager().getMainScoreboard().getTeam(ChatColor.DARK_RED + "ee_oresight_redstone").unregister();
		Bukkit.getScoreboardManager().getMainScoreboard().getTeam(ChatColor.AQUA + "ee_oresight_diamond").unregister();
		Bukkit.getScoreboardManager().getMainScoreboard().getTeam(ChatColor.GREEN + "ee_oresight_emerald").unregister();
		Bukkit.getScoreboardManager().getMainScoreboard().getTeam(ChatColor.RED + "ee_oresight_debris").unregister();
	}
	
	FileConfiguration saveData;
	
	public void HandleSaveMaps() throws IOException {
		saveData = new YamlConfiguration();
		this.saveResource("SaveData.yml", true);
		try { saveData.load("plugins/ExpandedEnchants/SaveData.yml"); }
		catch (IOException | InvalidConfigurationException e) { this.saveResource("SaveData.yml", true); }
		
		saveData.options().header("DO NOT EDIT THIS FILE");
		saveData.set("soulbound", null);
		for(Map.Entry<UUID, ArrayList<ItemStack>> entry : eventsClass.itemsToPreserve.entrySet()) {
			saveData.set("soulbound." + entry.getKey(), entry.getValue());
		}
		saveData.set("falldamage", null);
		if(eventsClass.noFallDamage.size() > 0) for (int i = 0; i < eventsClass.noFallDamage.size(); i++) {
			saveData.set("falldamage." + "num" + i, eventsClass.noFallDamage.get(i).toString());
			saveData.set("falldamage.max", i);
		}
		saveData.set("teleport", null);
		for(Map.Entry<UUID, Integer> entry : eventsClass.preventTeleport.entrySet()) {
			saveData.set("teleport." + entry.getKey(), entry.getValue());
		}
		saveData.set("disarm", null);
		for(Map.Entry<UUID, Integer> entry : eventsClass.disarmCountdown.entrySet()) {
			saveData.set("disarm." + entry.getKey(), entry.getValue());
		}
		saveData.set("deflect", null);
		for(Map.Entry<UUID, Integer> entry : eventsClass.deflectCountdown.entrySet()) {
			saveData.set("deflect." + entry.getKey(), entry.getValue());
		}
		saveData.set("thrust", null);
		for(Map.Entry<UUID, Integer> entry : eventsClass.thrustCooldown.entrySet()) {
			saveData.set("thrust." + entry.getKey(), entry.getValue());
		}
		saveData.set("oresight", null);
		for(Map.Entry<UUID, Integer> entry : eventsClass.spawnedShulkers.entrySet()) {
			saveData.set("oresight." + entry.getKey(), entry.getValue());
		}
		saveData.set("freeze", null);
		for(Map.Entry<UUID, Double> entry : eventsClass.canFreeze.entrySet()) {
			saveData.set("freeze." + entry.getKey(), entry.getValue());
		}
		saveData.set("split", null);
		if(eventsClass.nonSplitArrows.size() > 0) for (int i = 0; i < eventsClass.nonSplitArrows.size(); i++) {
			saveData.set("split." + "num" + i, eventsClass.nonSplitArrows.get(i).toString());
			saveData.set("split.max", i);
		}
		saveData.set("assassin", null);
		for(AssassinInfo entry : eventsClass.assassinInfo) {
			saveData.set("assassin." + entry.getPlayer() + ".countdownMax",entry.getCountdownMax());
			saveData.set("assassin." + entry.getPlayer() + ".countdown",entry.getCountdown());
			saveData.set("assassin." + entry.getPlayer() + ".shouldCountdown",entry.getShouldCountdown());
			saveData.set("assassin." + entry.getPlayer() + ".tryCountdown",entry.getTryCountdown());
			saveData.set("assassin." + entry.getPlayer() + ".shouldRefill",entry.getShouldRefill());
			saveData.set("assassin." + entry.getPlayer() + ".refill",entry.getRefill());
			saveData.set("assassin." + entry.getPlayer() + ".refillMax",entry.getRefillMax());
			saveData.set("assassin." + entry.getPlayer() + ".bar.progress",entry.bar.getProgress());
			saveData.set("assassin." + entry.getPlayer() + ".bar.title",entry.bar.getTitle());
		}
		
		saveData.save("plugins/ExpandedEnchants/SaveData.yml");
	}
	
	@SuppressWarnings("unchecked")
	public void HandleLoadMaps() {
		saveData = new YamlConfiguration();
		try { saveData.load("plugins/ExpandedEnchants/SaveData.yml"); }
		catch (IOException | InvalidConfigurationException e) { Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "ExpandedEnchants" + ChatColor.RED + ": Couldn't find SaveData.yml file. No data can be loaded."); }
		
		if(saveData.getConfigurationSection("soulbound") != null) saveData.getConfigurationSection("soulbound").getKeys(false).forEach(key ->{
			ArrayList<ItemStack> items = ((ArrayList<ItemStack>) saveData.get("soulbound." + key));
			eventsClass.itemsToPreserve.put(UUID.fromString(key), items);
		});
		if(saveData.getConfigurationSection("falldamage") != null) {
			int max = saveData.getConfigurationSection("falldamage").getInt("max");
			for(int i = 0; i < max + 1; i++) {
				String uuid = saveData.getConfigurationSection("falldamage").getString("num" + i);
				 eventsClass.noFallDamage.add(UUID.fromString(uuid));
			}
			
			
		}
		if(saveData.getConfigurationSection("split") != null) {
			int max = saveData.getConfigurationSection("split").getInt("max");
			for(int i = 0; i < max + 1; i++) {
				String uuid = saveData.getConfigurationSection("split").getString("num" + i);
				 eventsClass.noFallDamage.add(UUID.fromString(uuid));
			}
			
			
		}
		if(saveData.getConfigurationSection("teleport") != null) saveData.getConfigurationSection("teleport").getKeys(false).forEach(key ->{
			int num = ((Integer) saveData.get("teleport." + key));
			eventsClass.preventTeleport.put(UUID.fromString(key), num);
		});
		if(saveData.getConfigurationSection("disarm") != null) saveData.getConfigurationSection("disarm").getKeys(false).forEach(key ->{
			int num = ((Integer) saveData.get("disarm." + key));
			eventsClass.disarmCountdown.put(UUID.fromString(key), num);
		});
		if(saveData.getConfigurationSection("deflect") != null) saveData.getConfigurationSection("deflect").getKeys(false).forEach(key ->{
			int num = ((Integer) saveData.get("deflect." + key));
			eventsClass.deflectCountdown.put(UUID.fromString(key), num);
		});
		if(saveData.getConfigurationSection("thrust") != null) saveData.getConfigurationSection("thrust").getKeys(false).forEach(key ->{
			int num = ((Integer) saveData.get("thrust." + key));
			eventsClass.thrustCooldown.put(UUID.fromString(key), num);
		});
		if(saveData.getConfigurationSection("oresight") != null) saveData.getConfigurationSection("oresight").getKeys(false).forEach(key ->{
			int num = ((Integer) saveData.get("oresight." + key));
			eventsClass.spawnedShulkers.put(UUID.fromString(key), num);
		});
		if(saveData.getConfigurationSection("freeze") != null) saveData.getConfigurationSection("freeze").getKeys(false).forEach(key ->{
			Double num = ((Double) saveData.get("freeze." + key));
			eventsClass.canFreeze.put(UUID.fromString(key), num);
		});
		if(saveData.getConfigurationSection("assassin") != null) saveData.getConfigurationSection("assassin").getKeys(false).forEach(key ->{
			UUID uuid = UUID.fromString(key);
			ConfigurationSection config = saveData.getConfigurationSection("assassin." + key);
			double countdownMax = config.getDouble("countdownMax");
			double countdown = config.getDouble("countdown");
			boolean shouldCountdown = config.getBoolean("shouldCountdown");
			boolean tryCountdown = config.getBoolean("tryCountdown");
			boolean shouldRefill = config.getBoolean("shouldRefill");
			double refill = config.getDouble("refill");
			int refillMax = config.getInt("refillMax");
			double progress = saveData.getConfigurationSection("assassin." + key + ".bar").getDouble("progress");
			String title = saveData.getConfigurationSection("assassin." + key + ".bar").getString("title");
			BossBar bar = null;
			if(shouldCountdown) bar = Bukkit.createBossBar(title, BarColor.GREEN, BarStyle.SOLID, BarFlag.DARKEN_SKY);
			else bar = Bukkit.createBossBar(title, BarColor.YELLOW, BarStyle.SOLID, BarFlag.DARKEN_SKY);
			bar.setProgress(progress);
			AssassinInfo info = new AssassinInfo(uuid, countdownMax, refillMax, bar);
			info.setShouldRefill(shouldRefill);
			info.setRefill(refill);
			info.setCountdown(countdown);
			info.setShouldCountdown(shouldCountdown);
			info.setTryCountdown(tryCountdown);
			eventsClass.assassinInfo.add(info);
		});
		//this.saveResource("SaveData.yml", true);
	}
	
	public static CustomEnchantmentRecipe GetCustomEnchantmentRecipe(Enchantment ench) {
		for(CustomEnchantmentRecipe e : customRecipes) {
			if(e.recipe.getResult().getItemMeta().getEnchants().keySet().toArray()[0].equals(ench)) return e;
		}
		return null;
	}
	
	public void AddAllCustomEnchants() {
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.ANTIGRAVITY);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.ASSASSIN);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.AUTOSMELT);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.BEHEADING);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.DEFLECT);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.DIRECT);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.DISARMING);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.DISRUPTION);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.ELEMENTALPROTECTION);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.EXP_BOOST);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.OWLEYES);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.FEEDINGMODULE);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.GOURMAND);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.HEALTHBOOST);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.HEAVENSLIGHTNESS);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.ICY);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.LAVA_WALKER);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.LEAPING);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.LIFESTEAL);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.LUMBERJACK);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.ORESIGHT);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.REPLANTING);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.SHADOWSTEP);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.SOULBOUND);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.SPLITTING);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.STEPPING);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.STONEFISTS);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.THERMALPLATING);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.THRUSTERS);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.TRAVELER);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.NOBREAKABLE);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.VEINMINE);
		CustomEnchantsManager.custom_enchants.add(CustomEnchantsManager.WIDE);		
	}
	public void RegisterRecipes() {
		if(getPlugin().getConfig().getBoolean("LeatherSmeltEnabled")){
			FurnaceRecipe leathersmelt = new FurnaceRecipe(NamespacedKey.minecraft("ee_recipe_leather_smelt"), new ItemStack(Material.LEATHER, 1), Material.ROTTEN_FLESH, 1.0f, 5 * 20);
			Bukkit.getServer().addRecipe(leathersmelt);
			SmokingRecipe leathersmoke = new SmokingRecipe(NamespacedKey.minecraft("ee_recipe_leather_smoke"), new ItemStack(Material.LEATHER, 1), Material.ROTTEN_FLESH, 1.1f, 50);
			Bukkit.getServer().addRecipe(leathersmoke);
		}
		
		FileConfiguration recipeData = new YamlConfiguration();
		try { recipeData.load("plugins/ExpandedEnchants/recipes.yml"); }
		catch (InvalidConfigurationException e) { e.printStackTrace(); recipeData = null;}
		catch(IOException e) {recipeData = null;};
		
		ShapelessRecipe bookrecipe =  new ShapelessRecipe(NamespacedKey.minecraft("ee_recipe_enchantedbook"), new ItemStack(Material.ENCHANTED_BOOK));
		bookrecipe.addIngredient(Material.BOOK);
		bookrecipe.addIngredient(Material.EMERALD);
		Bukkit.getServer().addRecipe(bookrecipe);
		
		
		if(getPlugin().getConfig().getBoolean("ThrustersEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_thrusters"), itemManager.CreateCustomBook(CustomEnchantsManager.THRUSTERS, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_thrusters") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thrusters.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thrusters.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thrusters.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thrusters.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thrusters.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thrusters.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thrusters.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thrusters.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thrusters.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_thrusters").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8)};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.ELYTRA);
				recipe.setIngredient('D', Material.FIREWORK_ROCKET);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.FIREWORK_ROCKET);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 1, 0, 32, 1, 32, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);	
		}
		if(getPlugin().getConfig().getBoolean("OresightEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_oresight"), itemManager.CreateCustomBook(CustomEnchantsManager.ORESIGHT, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_oresight") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_oresight.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_oresight.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_oresight.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_oresight.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_oresight.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_oresight.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_oresight.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_oresight.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_oresight.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_oresight").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8)};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.NETHERITE_INGOT);
				recipe.setIngredient('D', Material.IRON_BLOCK);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.DIAMOND_ORE);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 1, 0, 10, 1, 10, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);	
		}
		if(getPlugin().getConfig().getBoolean("AntigravityEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_antigravity"), itemManager.CreateCustomBook(CustomEnchantsManager.ANTIGRAVITY, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_antigravity") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_antigravity.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_antigravity.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_antigravity.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_antigravity.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_antigravity.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_antigravity.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_antigravity.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_antigravity.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_antigravity.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_antigravity").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8)};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.DRAGON_HEAD);
				recipe.setIngredient('D', Material.FEATHER);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.AMETHYST_SHARD);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 1, 0, 48, 1, 36, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);	
		}
		if(getPlugin().getConfig().getBoolean("SplittingEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_splitting"), itemManager.CreateCustomBook(CustomEnchantsManager.SPLITTING, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_splitting") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_splitting.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_splitting.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_splitting.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_splitting.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_splitting.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_splitting.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_splitting.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_splitting.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_splitting.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_splitting").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.NETHERITE_INGOT);
				recipe.setIngredient('D', Material.ARROW);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.SPECTRAL_ARROW);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 1, 0, 64, 1, 64, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);	
		}
		if(getPlugin().getConfig().getBoolean("AssassinEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_assassin"), itemManager.CreateCustomBook(CustomEnchantsManager.ASSASSIN, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_assassin") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_assassin.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_assassin.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_assassin.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_assassin.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_assassin.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_assassin.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_assassin.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_assassin.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_assassin.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_assassin").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
			recipe.setIngredient('B', Material.POTION);
			recipe.setIngredient('D', Material.BLACK_STAINED_GLASS);
			recipe.setIngredient('E', Material.ENCHANTED_BOOK);
			recipe.setIngredient('F', Material.END_CRYSTAL);
			recipe.setIngredient('H', Material.NETHER_STAR);
			amounts = new Integer[] { 0, 1, 0, 12, 1, 12, 0, 1, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);	
		}
		
		if(getPlugin().getConfig().getBoolean("DisarmingEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_disarming"), itemManager.CreateCustomBook(CustomEnchantsManager.DISARMING, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_disarming") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disarming.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disarming.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disarming.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disarming.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disarming.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disarming.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disarming.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disarming.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disarming.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_disarming").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
			recipe.setIngredient('B', Material.DRAGON_EGG);
			recipe.setIngredient('D', Material.PURPUR_BLOCK);
			recipe.setIngredient('E', Material.ENCHANTED_BOOK);
			recipe.setIngredient('F', Material.PURPUR_BLOCK);
			recipe.setIngredient('H', Material.NETHER_STAR);
			amounts = new Integer[] { 0, 1, 0, 64, 1, 64, 0, 1, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);	
		}
		if(getPlugin().getConfig().getBoolean("DisruptionEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_disruption"), itemManager.CreateCustomBook(CustomEnchantsManager.DISRUPTION, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_disruption") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disruption.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disruption.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disruption.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disruption.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disruption.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disruption.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disruption.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disruption.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_disruption.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_disruption").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
			recipe.setIngredient('B', Material.ENDER_PEARL);
			recipe.setIngredient('D', Material.ENDER_EYE);
			recipe.setIngredient('E', Material.ENCHANTED_BOOK);
			recipe.setIngredient('F', Material.END_ROD);
			recipe.setIngredient('H', Material.LAPIS_LAZULI);
			amounts = new Integer[] { 0, 16, 0, 6, 1, 6, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);	
		}
		if(getPlugin().getConfig().getBoolean("SteppingEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_stepping"), itemManager.CreateCustomBook(CustomEnchantsManager.STEPPING, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_stepping") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stepping.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stepping.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stepping.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stepping.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stepping.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stepping.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stepping.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stepping.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stepping.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_stepping").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.DIAMOND_HORSE_ARMOR);
				recipe.setIngredient('D', Material.QUARTZ_STAIRS);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.PRISMARINE_STAIRS);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 1, 0, 16, 1, 16, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);	
		}
		if(getPlugin().getConfig().getBoolean("GourmandEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_gourmand"), itemManager.CreateCustomBook(CustomEnchantsManager.GOURMAND, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_gourmand") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_gourmand.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_gourmand.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_gourmand.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_gourmand.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_gourmand.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_gourmand.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_gourmand.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_gourmand.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_gourmand.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_gourmand").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.ENCHANTED_GOLDEN_APPLE);
				recipe.setIngredient('D', Material.COOKED_BEEF);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.HONEY_BOTTLE);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 1, 0, 64, 1, 16, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);	
		}
		if(getPlugin().getConfig().getBoolean("SoulboundEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_soulbound"), itemManager.CreateCustomBook(CustomEnchantsManager.SOULBOUND, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_soulbound") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_soulbound.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_soulbound.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_soulbound.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_soulbound.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_soulbound.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_soulbound.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_soulbound.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_soulbound.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_soulbound.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_soulbound").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.TOTEM_OF_UNDYING);
				recipe.setIngredient('D', Material.CHAIN);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.NAME_TAG);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				recipe.setIngredient('A', Material.SHULKER_SHELL);
				recipe.setIngredient('C', Material.SHULKER_SHELL);
				recipe.setIngredient('G', Material.SHULKER_SHELL);
				recipe.setIngredient('I', Material.SHULKER_SHELL);
				amounts = new Integer[] { 4, 1, 4, 32, 1, 16, 4, 16, 4 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);	
		}
		if(getPlugin().getConfig().getBoolean("AutosmeltEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_autosmelt"), itemManager.CreateCustomBook(CustomEnchantsManager.AUTOSMELT, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_autosmelt") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_autosmelt.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_autosmelt.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_autosmelt.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_autosmelt.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_autosmelt.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_autosmelt.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_autosmelt.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_autosmelt.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_autosmelt.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_autosmelt").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.MAGMA_BLOCK);
				recipe.setIngredient('D', Material.FLINT_AND_STEEL);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.LAVA_BUCKET);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 40, 0, 1, 1, 1, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("BeheadingEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_beheading"), itemManager.CreateCustomBook(CustomEnchantsManager.BEHEADING, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_beheading") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_beheading.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_beheading.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_beheading.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_beheading.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_beheading.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_beheading.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_beheading.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_beheading.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_beheading.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_beheading").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.WITHER_SKELETON_SKULL);
				recipe.setIngredient('D', Material.DIAMOND_AXE);
				recipe.setIngredient('F', Material.DIAMOND_AXE);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 2, 0, 1, 1, 1, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("DeflectEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_deflect"), itemManager.CreateCustomBook(CustomEnchantsManager.DEFLECT, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_deflect") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_deflect.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_deflect.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_deflect.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_deflect.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_deflect.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_deflect.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_deflect.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_deflect.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_deflect.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_deflect").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.SCUTE);
				recipe.setIngredient('D', Material.SHIELD);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.SHIELD);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 40, 0, 1, 1, 1, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("DirectEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_direct"), itemManager.CreateCustomBook(CustomEnchantsManager.DIRECT, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_direct") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_direct.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_direct.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_direct.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_direct.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_direct.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_direct.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_direct.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_direct.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_direct.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_direct").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.EMERALD);
				recipe.setIngredient('D', Material.CHORUS_FRUIT);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.ENDER_PEARL);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts =  new Integer[] { 0, 20, 0, 32, 1, 16, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("ElementalprotectionEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_elemental"), itemManager.CreateCustomBook(CustomEnchantsManager.ELEMENTALPROTECTION, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_elemental") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_elemental.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_elemental.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_elemental.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_elemental.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_elemental.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_elemental.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_elemental.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_elemental.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_elemental.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_elemental").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.POTION);
				recipe.setIngredient('D', Material.END_CRYSTAL);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.WITHER_ROSE);
				recipe.setIngredient('H', Material.NETHER_STAR);
				amounts = new Integer[] { 0, 1, 0, 5, 1, 5, 0, 1, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("ExperienceboostEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_expboost"), itemManager.CreateCustomBook(CustomEnchantsManager.EXP_BOOST, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_expboost") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_expboost.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_expboost.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_expboost.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_expboost.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_expboost.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_expboost.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_expboost.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_expboost.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_expboost.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_expboost").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.BOOKSHELF);
				recipe.setIngredient('D', Material.EXPERIENCE_BOTTLE);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.EXPERIENCE_BOTTLE);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 36, 0, 64, 1, 64, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("EyesofowlEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_owleyes"), itemManager.CreateCustomBook(CustomEnchantsManager.OWLEYES, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_owleyes") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_owleyes.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_owleyes.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_owleyes.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_owleyes.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_owleyes.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_owleyes.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_owleyes.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_owleyes.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_owleyes.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_owleyes").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.POTION);
				recipe.setIngredient('D', Material.ENDER_EYE);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.ENDER_EYE);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 1, 0, 25, 1, 25, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("HealthboostEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_healthboost"), itemManager.CreateCustomBook(CustomEnchantsManager.HEALTHBOOST, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_healthboost") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_healthboost.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_healthboost.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_healthboost.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_healthboost.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_healthboost.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_healthboost.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_healthboost.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_healthboost.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_healthboost.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_healthboost").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.POTION);
				recipe.setIngredient('D', Material.ENCHANTED_GOLDEN_APPLE);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.CAKE);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 1, 0, 1, 1, 1, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("HeavenslightnessEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_heavenslightness"), itemManager.CreateCustomBook(CustomEnchantsManager.HEAVENSLIGHTNESS, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_heavenslightness") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_heavenslightness.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_heavenslightness.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_heavenslightness.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_heavenslightness.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_heavenslightness.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_heavenslightness.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_heavenslightness.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_heavenslightness.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_heavenslightness.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_heavenslightness").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.POTION);
				recipe.setIngredient('D', Material.FEATHER);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.PHANTOM_MEMBRANE);
				recipe.setIngredient('H', Material.NETHER_STAR);
				amounts = new Integer[] { 0, 1, 0, 40, 1, 40, 0, 1, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("IcyEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_icy"), itemManager.CreateCustomBook(CustomEnchantsManager.ICY, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_icy") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_icy.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_icy.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_icy.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_icy.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_icy.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_icy.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_icy.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_icy.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_icy.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_icy").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.SPLASH_POTION);
				recipe.setIngredient('D', Material.BLUE_ICE);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.END_CRYSTAL);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 1, 0, 40, 1, 4, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("LavawalkerEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_lavawalker"), itemManager.CreateCustomBook(CustomEnchantsManager.LAVA_WALKER, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_lavawalker") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lavawalker.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lavawalker.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lavawalker.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lavawalker.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lavawalker.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lavawalker.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lavawalker.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lavawalker.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lavawalker.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_lavawalker").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.NETHERITE_SCRAP);
				recipe.setIngredient('D', Material.SMOOTH_BASALT);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.SMOOTH_BASALT);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts =  new Integer[] { 0, 6, 0, 64, 1, 64, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("LeapingEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_leaping"), itemManager.CreateCustomBook(CustomEnchantsManager.LEAPING, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_leaping") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_leaping.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_leaping.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_leaping.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_leaping.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_leaping.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_leaping.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_leaping.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_leaping.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_leaping.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_leaping").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.POTION);
				recipe.setIngredient('D', Material.GLOWSTONE);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.RABBIT_FOOT);
				recipe.setIngredient('H', Material.NETHER_STAR);
				amounts = new Integer[] { 0, 1, 0, 24, 1, 12, 0, 1, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("LifestealEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_lifesteal"), itemManager.CreateCustomBook(CustomEnchantsManager.LIFESTEAL, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_lifesteal") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lifesteal.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lifesteal.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lifesteal.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lifesteal.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lifesteal.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lifesteal.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lifesteal.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lifesteal.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lifesteal.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_lifesteal").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.LINGERING_POTION);
				recipe.setIngredient('D', Material.TOTEM_OF_UNDYING);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.TOTEM_OF_UNDYING);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 1, 0, 1, 1, 1, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("StonefistsEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_stonefists"), itemManager.CreateCustomBook(CustomEnchantsManager.STONEFISTS, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_stonefists") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stonefists.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stonefists.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stonefists.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stonefists.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stonefists.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stonefists.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stonefists.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stonefists.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_stonefists.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_stonefists").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.POTION);
				recipe.setIngredient('D', Material.SMOOTH_STONE);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.SMOOTH_STONE);
				recipe.setIngredient('H', Material.NETHER_STAR);
				amounts = new Integer[] { 0, 1, 0, 64, 1, 64, 0, 1, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("ThermalplatingEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_thermalplating"), itemManager.CreateCustomBook(CustomEnchantsManager.THERMALPLATING, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_thermalplating") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thermalplating.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thermalplating.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thermalplating.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thermalplating.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thermalplating.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thermalplating.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thermalplating.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thermalplating.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_thermalplating.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_thermalplating").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.POTION);
				recipe.setIngredient('D', Material.NETHERITE_SCRAP);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.MAGMA_CREAM);
				recipe.setIngredient('H', Material.NETHER_STAR);
				amounts = new Integer[] { 0, 1, 0, 5, 1, 50, 0, 1, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("TravelerEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_traveler"), itemManager.CreateCustomBook(CustomEnchantsManager.TRAVELER, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_traveler") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_traveler.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_traveler.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_traveler.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_traveler.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_traveler.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_traveler.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_traveler.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_traveler.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_traveler.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_traveler").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.POTION);
				recipe.setIngredient('D', Material.REDSTONE_BLOCK);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.GLOWSTONE);
				recipe.setIngredient('H', Material.NETHER_STAR);
				amounts = new Integer[] { 0, 1, 0, 24, 1, 24, 0, 1, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("UnbreakableEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_unbreakable"), itemManager.CreateCustomBook(CustomEnchantsManager.NOBREAKABLE, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_unbreakable") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_unbreakable.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_unbreakable.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_unbreakable.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_unbreakable.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_unbreakable.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_unbreakable.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_unbreakable.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_unbreakable.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_unbreakable.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_unbreakable").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.ENCHANTED_BOOK);
				recipe.setIngredient('D', Material.OBSIDIAN);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.CRYING_OBSIDIAN);
				recipe.setIngredient('H', Material.NETHER_STAR);
				amounts = new Integer[] { 0, 1, 0, 40, 1, 40, 0, 1, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("VeinmineEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_veinmine"), itemManager.CreateCustomBook(CustomEnchantsManager.VEINMINE, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_veinmine") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_veinmine.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_veinmine.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_veinmine.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_veinmine.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_veinmine.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_veinmine.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_veinmine.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_veinmine.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_veinmine.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_veinmine").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('A', Material.COAL);
				recipe.setIngredient('B', Material.AMETHYST_SHARD);
				recipe.setIngredient('C', Material.COPPER_INGOT);
				recipe.setIngredient('D', Material.NETHERITE_SCRAP);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.IRON_INGOT);
				recipe.setIngredient('G', Material.DIAMOND);
				recipe.setIngredient('H', Material.EMERALD);
				recipe.setIngredient('I', Material.GOLD_INGOT);
				amounts = new Integer[] { 64, 48, 32, 1, 1, 24, 4, 8, 16 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("WideEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_wide"), itemManager.CreateCustomBook(CustomEnchantsManager.WIDE, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_wide") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_wide.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_wide.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_wide.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_wide.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_wide.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_wide.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_wide.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_wide.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_wide.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_wide").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('A', Material.STONE);
				recipe.setIngredient('B', Material.DRAGON_BREATH);
				recipe.setIngredient('C', Material.DEEPSLATE);
				recipe.setIngredient('D', Material.ENCHANTED_BOOK);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.ENCHANTED_BOOK);
				recipe.setIngredient('G', Material.NETHERRACK);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				recipe.setIngredient('I', Material.END_STONE);
				amounts = new Integer[] { 64, 30, 64, 1, 1, 1, 64, 16, 64 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("LumberjackEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_lumberjack"), itemManager.CreateCustomBook(CustomEnchantsManager.LUMBERJACK, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_lumberjack") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lumberjack.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lumberjack.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lumberjack.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lumberjack.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lumberjack.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lumberjack.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lumberjack.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lumberjack.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_lumberjack.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_lumberjack").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.OAK_LOG);
				recipe.setIngredient('D', Material.DIAMOND_AXE);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.SHEARS);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 64, 0, 1, 1, 1, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("FeedingmoduleEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_feedingmodule"), itemManager.CreateCustomBook(CustomEnchantsManager.FEEDINGMODULE, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_feedingmodule") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_feedingmodule.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_feedingmodule.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_feedingmodule.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_feedingmodule.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_feedingmodule.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_feedingmodule.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_feedingmodule.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_feedingmodule.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_feedingmodule.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_feedingmodule").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.BAMBOO);
				recipe.setIngredient('D', Material.BOWL);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.GLASS_BOTTLE);
				recipe.setIngredient('H', Material.SLIME_BALL);
				amounts = new Integer[] { 0, 30, 0, 30, 1, 30, 0, 30, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("ReplantingEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_replanting"), itemManager.CreateCustomBook(CustomEnchantsManager.REPLANTING, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_replanting") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_replanting.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_replanting.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_replanting.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_replanting.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_replanting.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_replanting.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_replanting.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_replanting.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_replanting.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_replanting").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.DIAMOND_HOE);
				recipe.setIngredient('D', Material.POTATO);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.CARROT);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 1, 0, 32, 1, 32, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
		if(getPlugin().getConfig().getBoolean("ShadowstepEnabled"))
		{
			ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("ee_recipe_shadowstep"), itemManager.CreateCustomBook(CustomEnchantsManager.SHADOWSTEP, 1));
			recipe.shape("ABC","DEF", "GHI");
			Integer[] amounts = new Integer[9];
			boolean doDefault = false;
			if(recipeData != null) { 
				if(recipeData.getConfigurationSection("ee_recipe_shadowstep") != null) {
					recipe.setIngredient('A', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_shadowstep.Ingredients").getString("A", "AIR")));
					recipe.setIngredient('B', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_shadowstep.Ingredients").getString("B", "AIR")));
					recipe.setIngredient('C', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_shadowstep.Ingredients").getString("C", "AIR")));
					recipe.setIngredient('D', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_shadowstep.Ingredients").getString("D", "AIR")));
					recipe.setIngredient('E', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_shadowstep.Ingredients").getString("E", "AIR")));
					recipe.setIngredient('F', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_shadowstep.Ingredients").getString("F", "AIR")));
					recipe.setIngredient('G', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_shadowstep.Ingredients").getString("G", "AIR")));
					recipe.setIngredient('H', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_shadowstep.Ingredients").getString("H", "AIR")));
					recipe.setIngredient('I', Material.valueOf(recipeData.getConfigurationSection("ee_recipe_shadowstep.Ingredients").getString("I", "AIR")));
					List<Integer> ints = recipeData.getConfigurationSection("ee_recipe_shadowstep").getIntegerList("Amounts");
					amounts = new Integer[] {ints.get(0), ints.get(1), ints.get(2), ints.get(3), ints.get(4), ints.get(5), ints.get(6), ints.get(7), ints.get(8),};
				}
				else doDefault = true;
			}
			else doDefault = true;
			if(doDefault) {
				recipe.setIngredient('B', Material.WHITE_WOOL);
				recipe.setIngredient('D', Material.GRASS);
				recipe.setIngredient('E', Material.ENCHANTED_BOOK);
				recipe.setIngredient('F', Material.SPYGLASS);
				recipe.setIngredient('H', Material.LAPIS_LAZULI);
				amounts = new Integer[] { 0, 32, 0, 32, 1, 1, 0, 16, 0 };
			}
			customRecipes.add(new CustomEnchantmentRecipe(recipe, amounts));
			Bukkit.getServer().addRecipe(recipe);
		}
	}
}
