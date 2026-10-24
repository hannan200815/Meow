package dev.xoperr.blissgems;

import dev.xoperr.blissgems.BlissGems.CustomItems.Item;
import dev.xoperr.blissgems.BlissGems.GemItems.GemType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class BlissGems extends JavaPlugin implements Listener, TabCompleter {

    private GemItems gems;
    private FireGem fire;
    private LifeGem life;
    private SpeedGem speed;
    private PuffGem puff;
    private StrengthGem strength;
    private WealthGem wealth;
    private AstraGem astra;
    private CustomItems customItems;
    private CustomMenu menu;
    private CustomWeapons weapons;
    private FluxGem flux;
    private RecipeManager recipes;
    private EnergyManager energy;
    private GemRitual ritual;
    private VillagerDiscount villagerDiscount;
    private OrbitalStrike orbital;
    private StatsManager stats;
    private AutoEnchant autoEnchant;
    private final java.util.Map<UUID, java.util.Set<Achievement>> achieved = new HashMap<>();
    private File dataFile;

    // Flux Beam state, per player
    private final Map<UUID, BukkitTask> fluxCharging = new HashMap<>();
    private final Map<UUID, Integer> fluxCharge = new HashMap<>();
    private final Map<UUID, Long> fluxCooldownUntil = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        gems = new GemItems(this);
        energy = new EnergyManager(this);
        ritual = new GemRitual(this);
        villagerDiscount = new VillagerDiscount(this);
        orbital = new OrbitalStrike(this, customItems);
        stats = new StatsManager(this);
        autoEnchant = new AutoEnchant(this);
        autoEnchant.start();
        fire = new FireGem(this);
        life = new LifeGem(this);
        speed = new SpeedGem(this);
        puff = new PuffGem(this);
        strength = new StrengthGem(this);
        wealth = new WealthGem(this);
        astra = new AstraGem(this);
        flux = new FluxGem(this);
        customItems = new CustomItems(this);
        menu = new CustomMenu(this, customItems);
        weapons = new CustomWeapons(this, customItems);
        recipes = new RecipeManager(this, customItems);
        recipes.register();
        dataFile = new File(getDataFolder(), "data.yml");

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(fire, this);
        getServer().getPluginManager().registerEvents(life, this);
        getServer().getPluginManager().registerEvents(speed, this);
        getServer().getPluginManager().registerEvents(puff, this);
        getServer().getPluginManager().registerEvents(strength, this);
        getServer().getPluginManager().registerEvents(wealth, this);
        getServer().getPluginManager().registerEvents(astra, this);
        getServer().getPluginManager().registerEvents(customItems, this);
        getServer().getPluginManager().registerEvents(menu, this);
        getServer().getPluginManager().registerEvents(weapons, this);
        getServer().getPluginManager().registerEvents(ritual, this);
        getServer().getPluginManager().registerEvents(villagerDiscount, this);
        getServer().getPluginManager().registerEvents(orbital, this);
        getServer().getPluginManager().registerEvents(flux, this);

        getCommand("bliss").setExecutor(this::onBlissCommand);
        getCommand("bliss").setTabCompleter(this);

        long interval = Math.max(1, getConfig().getLong("passives.update-interval", 20));
        Bukkit.getScheduler().runTaskTimer(this, this::tickPassives, interval, interval);
        Bukkit.getScheduler().runTaskTimer(this, puff::tickFlight, 2L, 2L);

        getLogger().info("BlissGems enabled.");
    }

    @Override
    public void onDisable() {
        fire.shutdown();
        life.shutdown();
        recipes.unregister();
        for (BukkitTask task : fluxCharging.values()) {
            task.cancel();
        }
        fluxCharging.clear();
        fluxCharge.clear();
    }

    public GemItems gems() {
        return gems;
    }

    public EnergyManager energy() {
        return energy;
    }

    /** Adds to a tracked number for a player and returns the new total. */
    public double addProgress(UUID id, String key, double amount) {
        String path = "progress." + key + "." + id;
        YamlConfiguration yml = data();
        double total = yml.getDouble(path, 0.0) + amount;
        yml.set(path, total);
        saveData(yml);
        return total;
    }

    /** Sets a tracked number for a player. */
    public void setProgress(UUID id, String key, double value) {
        YamlConfiguration yml = data();
        yml.set("progress." + key + "." + id, value);
        saveData(yml);
    }

    /** Grants an achievement once and announces it. */
    public void unlockAchievement(Player player, Achievement achievement) {
        String path = "achievements." + player.getUniqueId() + "." + achievement.name();
        YamlConfiguration yml = data();
        if (yml.getBoolean(path, false)) {
            return;
        }
        yml.set(path, true);
        saveData(yml);
        BlissGems.fx(player, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.sendMessage(gradient("★ Achievement unlocked: " + achievement.title(), "#ffd700", "#ff8c00"));
    }

    public boolean hasAchievement(UUID id, Achievement achievement) {
        return data().getBoolean("achievements." + id + "." + achievement.name(), false);
    }

    /** Player data file (pockets, pending gems). */
    public YamlConfiguration data() {
        return YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData(YamlConfiguration yml) {
        writeData(yml);
    }

    /** Double debris: ancient debris smelted near a Wealth gem holder gives 2 netherite scrap. */
    @EventHandler
    public void onFurnaceSmelt(org.bukkit.event.inventory.FurnaceSmeltEvent event) {
        if (event.getSource().getType() != Material.ANCIENT_DEBRIS || event.getResult().getType() != Material.NETHERITE_SCRAP) {
            return;
        }
        org.bukkit.Location where = event.getBlock().getLocation();
        for (Player player : where.getWorld().getPlayers()) {
            if (player.getLocation().distance(where) > 10) {
                continue;
            }
            for (ItemStack item : player.getInventory().getContents()) {
                if (gems.typeOf(item) == GemType.WEALTH && getConfig().getBoolean(
                        "passives.wealth.tier" + gems.tierOf(item) + ".double-debris", true)) {
                    event.getResult().setAmount(2);
                    return;
                }
            }
        }
    }

    private void tickPassives() {
        fire.tickPassives();
        life.tickPassives();
        speed.tickPassives();
        strength.tickPassives();
        wealth.tickPassives();
        flux.tickPassives();
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /** Colors text with a smooth RGB gradient from one hex color to another, e.g. gradient("Hi", "#ff00ff", "#00ffff"). */
    public static String gradient(String text, String fromHex, String toHex) {
        int[] from = rgb(fromHex);
        int[] to = rgb(toHex);
        StringBuilder out = new StringBuilder();
        int n = Math.max(1, text.length() - 1);
        for (int i = 0; i < text.length(); i++) {
            double t = (double) i / n;
            int r = (int) Math.round(from[0] + (to[0] - from[0]) * t);
            int g = (int) Math.round(from[1] + (to[1] - from[1]) * t);
            int b = (int) Math.round(from[2] + (to[2] - from[2]) * t);
            out.append("§x");
            for (char c : String.format("%02x%02x%02x", r, g, b).toCharArray()) {
                out.append('§').append(c);
            }
            out.append(text.charAt(i));
        }
        return out.toString();
    }

    private static int[] rgb(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        return new int[]{
                Integer.parseInt(h.substring(0, 2), 16),
                Integer.parseInt(h.substring(2, 4), 16),
                Integer.parseInt(h.substring(4, 6), 16)};
    }

    /** Plays a sound at the player's location. */
    public static void fx(Player player, org.bukkit.Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /** One sound per gem type, played when you use one of its abilities. */
    private static org.bukkit.Sound castSound(GemType type) {
        return switch (type) {
            case FIRE -> org.bukkit.Sound.ENTITY_BLAZE_SHOOT;
            case FLUX -> org.bukkit.Sound.BLOCK_BEACON_POWER_SELECT;
            case LIFE -> org.bukkit.Sound.ENTITY_WITCH_DRINK;
            case SPEED -> org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP;
            case PUFF -> org.bukkit.Sound.ENTITY_BAT_TAKEOFF;
            case STRENGTH -> org.bukkit.Sound.ENTITY_IRON_GOLEM_ATTACK;
            case WEALTH -> org.bukkit.Sound.ENTITY_VILLAGER_YES;
            case ASTRA -> org.bukkit.Sound.ENTITY_EVOKER_CAST_SPELL;
        };
    }

    /** Action-bar charge bar. */
    public Component bar(String label, int pct) {
        int filled = pct / 5;
        StringBuilder sb = new StringBuilder("&b" + label + " &e");
        for (int i = 0; i < 20; i++) {
            if (i == filled) {
                sb.append("&8");
            }
            sb.append('█');
        }
        sb.append(" &b").append(pct).append('%');
        return LegacyComponentSerializer.legacySection().deserialize(color(sb.toString()));
    }

    // ------------------------------------------------------------ Commands

    private boolean onBlissCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && (args[0].equalsIgnoreCase("spook") || args[0].equalsIgnoreCase("tag"))) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&cOnly players can use this."));
                return true;
            }
            if (args[0].equalsIgnoreCase("spook")) {
                astra.spook(player);
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(color("&cUsage: /bliss tag <player>"));
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(color("&cPlayer not found."));
                return true;
            }
            astra.tag(player, target);
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("achievements")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&cOnly players can use this."));
                return true;
            }
            int unlocked = 0;
            sender.sendMessage(color("&6&l⭐ Achievements (" + Achievement.values().length + ")"));
            for (Achievement a : Achievement.values()) {
                boolean done = hasAchievement(player.getUniqueId(), a);
                if (done) {
                    unlocked++;
                }
                sender.sendMessage(color((done ? "&a✔ &e" : "&8✘ &7") + a.title() + " &8- &7" + a.description()));
            }
            sender.sendMessage(color("&7" + unlocked + "/" + Achievement.values().length + " unlocked"));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("stats")) {
            String mode = args.length > 1 ? args[1].toLowerCase() : "me";
            if (mode.equals("top")) {
                sender.sendMessage(color("&6&m" + "═".repeat(50)));
                sender.sendMessage(color("&e&l🏆 TOP KILLERS 🏆"));
                sender.sendMessage(color("&6&m" + "═".repeat(50)));
                List<java.util.Map.Entry<String, Integer>> top = stats.topKillers(10);
                if (top.isEmpty()) {
                    sender.sendMessage(color("&7No kills recorded yet."));
                }
                int rank = 1;
                for (java.util.Map.Entry<String, Integer> entry : top) {
                    sender.sendMessage(color("&e" + rank++ + " &7#" + (rank - 1) + " &f" + entry.getKey() + " &7- &f" + entry.getValue() + " kills"));
                }
                sender.sendMessage(color("&6&m" + "═".repeat(50)));
                sender.sendMessage(color("&8Use &7/bliss stats me &8for your personal stats"));
                sender.sendMessage(color("&8Use &7/bliss stats gems &8for gem distribution"));
                return true;
            }
            if (mode.equals("gems")) {
                sender.sendMessage(color("&6&m" + "═".repeat(50)));
                sender.sendMessage(color("&e&l💎 GEM DISTRIBUTION 💎"));
                sender.sendMessage(color("&6&m" + "═".repeat(50)));
                for (java.util.Map.Entry<String, Integer> entry : stats.gemDistribution().entrySet()) {
                    sender.sendMessage(color("&7" + entry.getKey() + ": &f" + entry.getValue() + " player(s)"));
                }
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&cOnly players can use this."));
                return true;
            }
            int kills = stats.kills(player.getUniqueId());
            int deaths = stats.deaths(player.getUniqueId());
            String kd = deaths == 0 ? String.valueOf(kills) : String.format("%.2f", (double) kills / deaths);
            sender.sendMessage(color("&6&m" + "═".repeat(50)));
            sender.sendMessage(color("&e&l📊 YOUR STATS 📊"));
            sender.sendMessage(color("&6&m" + "═".repeat(50)));
            sender.sendMessage(color("&7Kills: &f" + kills));
            sender.sendMessage(color("&7Deaths: &f" + deaths));
            sender.sendMessage(color("&7K/D Ratio: &f" + kd));
            sender.sendMessage(color("&7Energy: &f" + energy.energyOf(player) + "&8/&f" + energy.max()
                    + " &7(" + energy.state(player.getUniqueId()) + ")"));
            sender.sendMessage(color("&6&m" + "═".repeat(50)));
            return true;
        }
        if (args.length > 0 && args[0].toLowerCase().startsWith("ability:")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&cOnly players can use this."));
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "ability:main" -> runGemAbility(player, false, false);
                case "ability:secondary" -> runGemAbility(player, true, false);
                case "ability:tertiary" -> runGemAbility(player, false, true);
                case "ability:quaternary" -> runGemAbility(player, true, true);
                default -> sender.sendMessage(color("&cUnknown ability."));
            }
            return true;
        }
        if (args.length > 0 && (args[0].equalsIgnoreCase("souls") || args[0].equalsIgnoreCase("release")
                || args[0].equalsIgnoreCase("toggle_click"))) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&cOnly players can use this."));
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "souls" -> {
                    int count = astra.soulCount(player);
                    if (count == 0) {
                        sender.sendMessage(color("&7No souls captured. Sneak + hit a mob to capture it."));
                    } else {
                        sender.sendMessage(color("&d&lCaptured Souls: &f" + count));
                        sender.sendMessage(color("&7Use &d/bliss release &7to release all captured souls."));
                    }
                }
                case "release" -> {
                    int released = astra.releaseSouls(player);
                    sender.sendMessage(color(released == 0 ? "&c&oNo souls captured!"
                            : "&d&lReleased " + released + " captured soul(s)!"));
                }
                default -> {
                    boolean nowEnabled = !clickEnabled(player.getUniqueId());
                    data().set("click." + player.getUniqueId(), nowEnabled);
                    saveData(data());
                    sender.sendMessage(color(nowEnabled
                            ? "&aClick activation &l&aENABLED&r&a! You can now use right-click to activate abilities."
                            : "&cClick activation &l&cDISABLED&r&c! Use /bliss toggle_click to turn it back on."));
                }
            }
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("energy")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&cOnly players can use this."));
                return true;
            }
            sender.sendMessage(color("&7Energy: &f" + energy.energyOf(player) + "/" + energy.max()
                    + " &7(" + energy.state(player.getUniqueId()) + ")"));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("withdraw")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&cOnly players can use this."));
                return true;
            }
            if (energy.energyOf(player) <= 0) {
                sender.sendMessage(color("&cYou have no energy to withdraw."));
                return true;
            }
            energy.remove(player.getUniqueId(), 1);
            giveItem(player, customItems.create(CustomItems.Item.ENERGY_BOTTLE));
            sender.sendMessage(color("&bWithdrew 1 energy into an Energy Bottle."));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("trusted")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&cOnly players can use this."));
                return true;
            }
            Set<UUID> ids = trustedBy(player.getUniqueId());
            if (ids.isEmpty()) {
                sender.sendMessage(color("&7You don't trust anyone yet."));
                return true;
            }
            List<String> names = new ArrayList<>();
            for (UUID id : ids) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(id);
                names.add(op.getName() != null ? op.getName() : id.toString());
            }
            sender.sendMessage(color("&aTrusted players: &f" + String.join(", ", names)));
            return true;
        }
        if (args.length > 0 && (args[0].equalsIgnoreCase("trust") || args[0].equalsIgnoreCase("untrust"))) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&cOnly players can use this."));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(color("&cUsage: /bliss " + args[0].toLowerCase() + " <player>"));
                return true;
            }
            OfflinePlayer other = Bukkit.getPlayerExact(args[1]) != null
                    ? Bukkit.getPlayerExact(args[1])
                    : Bukkit.getOfflinePlayerIfCached(args[1]);
            if (other == null) {
                sender.sendMessage(color("&cPlayer not found."));
                return true;
            }
            if (other.getUniqueId().equals(player.getUniqueId())) {
                sender.sendMessage(color("&cYou can't trust yourself."));
                return true;
            }
            boolean add = args[0].equalsIgnoreCase("trust");
            setTrusted(player.getUniqueId(), other.getUniqueId(), add);
            sender.sendMessage(color(add
                    ? "&aYou now trust " + args[1] + "."
                    : "&aYou no longer trust " + args[1] + "."));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("pockets")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&cOnly players can open pockets."));
                return true;
            }
            wealth.openPockets(player);
            return true;
        }
        if (!sender.hasPermission("blissgems.admin")) {
            sender.sendMessage(color("&cYou don't have permission."));
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(sender, args);
            case "reload" -> {
                reloadConfig();
                sender.sendMessage(color("&aBlissGems config reloaded."));
            }
            case "clearcds" -> {
                Player target = targetOrSelf(sender, args, 1);
                if (target == null) {
                    sender.sendMessage(color("&cPlayer not found."));
                    return true;
                }
                clearAllCooldowns(target.getUniqueId());
                sender.sendMessage(color("&aCleared all ability cooldowns for " + target.getName() + "."));
            }
            case "fixhearts" -> {
                Player target = targetOrSelf(sender, args, 1);
                if (target == null) {
                    sender.sendMessage(color("&cPlayer not found."));
                    return true;
                }
                fixMaxHealth(target);
                sender.sendMessage(color("&aReset max health for " + target.getName() + "."));
            }
            case "info" -> {
                Player target = targetOrSelf(sender, args, 1);
                if (target == null) {
                    sender.sendMessage(color("&cPlayer not found."));
                    return true;
                }
                showInfo(sender, target);
            }
            case "reroll" -> handleReroll(sender, args);
            case "bannable" -> handleBannable(sender, args);
            case "normalise", "normalize" -> {
                int count = 0;
                for (Player online : Bukkit.getOnlinePlayers()) {
                    clearAllCooldowns(online.getUniqueId());
                    online.sendMessage(color("&e&oAbility cooldowns have been normalized by an admin."));
                    count++;
                }
                sender.sendMessage(color("&aAbility cooldowns reset for " + count + " online player(s)!"));
            }
            case "smp" -> {
                if (args.length >= 2 && args[1].equalsIgnoreCase("start")) {
                    ritual.distributeGems(sender);
                } else {
                    sender.sendMessage(color("&cUsage: /bliss smp start"));
                }
            }
            case "menu" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(color("&cOnly players can open the menu."));
                    return true;
                }
                menu.open(player);
            }
            case "item" -> {
                if (args.length < 3) {
                    sender.sendMessage(color("&cUsage: /bliss item <player> <egg_shooter|gem_upgrader|energy_bottle|repair_kit|gem_fragment|revive_beacon>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(color("&cPlayer not found."));
                    return true;
                }
                CustomItems.Item id;
                try {
                    id = CustomItems.Item.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException ex) {
                    sender.sendMessage(color("&cUnknown item."));
                    return true;
                }
                giveItem(target, customItems.create(id));
                sender.sendMessage(color("&aGave " + id.name().toLowerCase() + " to " + target.getName() + "."));
            }
            case "upgrader" -> {
                if (args.length < 2) {
                    sender.sendMessage(color("&cUsage: /bliss upgrader <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(color("&cPlayer not found."));
                    return true;
                }
                giveItem(target, gems.createUpgrader());
                sender.sendMessage(color("&aGave a Gem Upgrader to " + target.getName() + "."));
            }
            default -> sendUsage(sender);
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(color("&cUsage: /bliss energy, /bliss withdraw"));
        sender.sendMessage(color("&cUsage: /bliss reroll <player> [tier]"));
        sender.sendMessage(color("&cUsage: /bliss trust|untrust <player>, /bliss trusted"));
        sender.sendMessage(color("&cUsage: /bliss clearcds [player], /bliss fixhearts [player], /bliss info [player]"));
        sender.sendMessage(color("&cUsage: /bliss give <player> <fire|flux|life|speed|puff|strength|wealth|astra> [tier]"));
        sender.sendMessage(color("&cUsage: /bliss upgrader <player>"));
        sender.sendMessage(color("&cUsage: /bliss reload"));
        sender.sendMessage(color("&cUsage: /bliss menu"));
        sender.sendMessage(color("&cUsage: /bliss item <player> <item>"));
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender);
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(color("&cPlayer not found."));
            return;
        }
        GemType type;
        try {
            type = GemType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(color("&cUnknown gem. Available: fire, flux, life, speed, puff, strength, wealth, astra"));
            return;
        }
        int tier = 1;
        if (args.length >= 4) {
            try {
                tier = Integer.parseInt(args[3]);
            } catch (NumberFormatException ex) {
                tier = -1;
            }
        }
        if (tier < 1 || tier > 2) {
            sender.sendMessage(color("&cTier must be 1 or 2."));
            return;
        }

        removeGems(target); // one gem per player
        giveItem(target, gems.create(type, tier));
        fx(target, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        sender.sendMessage(color("&aGave a " + type.name() + " gem (Tier " + tier + ") to " + target.getName() + "."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return List.of("give", "reroll", "upgrader", "item", "menu", "pockets", "energy", "withdraw", "trust", "untrust", "trusted", "clearcds", "fixhearts", "info", "smp", "bannable", "spook", "tag", "souls", "release", "toggle_click", "normalise", "ability:main", "ability:secondary", "ability:tertiary", "ability:quaternary", "stats", "achievements", "reload");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("upgrader"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return List.of("fire", "flux", "life", "speed", "puff", "strength", "wealth", "astra");
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return List.of("1", "2");
        }
        return Collections.emptyList();
    }

    // ------------------------------------------------------------ ability:* commands

    /**
     * Runs a gem ability from the main-hand gem, the same as a click would.
     * sneaking: Shift + right-click. swap: the F key.
     */
    public void runGemAbility(Player player, boolean sneaking, boolean swap) {
        ItemStack item = player.getInventory().getItemInMainHand();
        GemType type = gems.typeOf(item);
        if (type == null) {
            player.sendMessage(color("&cYou must hold a gem to use this."));
            return;
        }
        if (!energy.abilitiesUsable(player.getUniqueId())) {
            player.sendMessage(color("&cYour gem is broken. Restore energy to use its abilities."));
            return;
        }
        if (astra.nullified(player)) {
            player.sendMessage(color("&5🕳 Your gem abilities are nullified by a Dimensional Void!"));
            return;
        }
        int tier = gems.tierOf(item);
        if (!swap) {
            switch (type) {
                case FIRE -> fire.onRightClick(player, tier, sneaking);
                case FLUX -> { if (tier >= 2 && sneaking) flux.onShiftRightClick(player); else fluxRightClick(player); }
                case LIFE -> life.onRightClick(player, tier, sneaking);
                case SPEED -> speed.onRightClick(player, tier, sneaking);
                case PUFF -> puff.onRightClick(player, tier, sneaking);
                case STRENGTH -> strength.onRightClick(player, tier, sneaking);
                case WEALTH -> wealth.onRightClick(player, tier, sneaking);
                case ASTRA -> astra.onRightClick(player, tier, sneaking);
            }
        } else {
            switch (type) {
                case FIRE -> fire.onSwap(player, tier, sneaking);
                case FLUX -> { if (tier >= 2) flux.onSwap(player, sneaking); }
                case LIFE -> life.onSwap(player, tier, sneaking);
                case SPEED -> speed.onSwap(player, tier, sneaking);
                case PUFF -> puff.onSwap(player, tier, sneaking);
                case STRENGTH -> strength.onSwap(player, tier, sneaking);
                case WEALTH -> wealth.onSwap(player, tier, sneaking);
                case ASTRA -> astra.onSwap(player, tier, sneaking);
            }
        }
    }

    // ------------------------------------------------------------ Click activation

    /** Right-click abilities are on unless the player turned them off with /bliss toggle_click. */
    public boolean clickEnabled(UUID playerId) {
        return data().getBoolean("click." + playerId, true);
    }

    // ------------------------------------------------------------ Trust, cooldowns, hearts, info, reroll

    /** Players that this player trusts (they count as allies). */
    public Set<UUID> trustedBy(UUID owner) {
        Set<UUID> out = new HashSet<>();
        for (String text : data().getStringList("trusted." + owner)) {
            try {
                out.add(UUID.fromString(text));
            } catch (IllegalArgumentException ignored) {
                // skip bad entries
            }
        }
        return out;
    }

    /** True if other counts as an ally of owner: yourself, or someone you trust. */
    public boolean isAlly(Player owner, Player other) {
        return owner.equals(other) || trustedBy(owner.getUniqueId()).contains(other.getUniqueId());
    }

    public void setTrusted(UUID owner, UUID other, boolean add) {
        YamlConfiguration yml = data();
        Set<String> set = new HashSet<>(yml.getStringList("trusted." + owner));
        if (add) {
            set.add(other.toString());
        } else {
            set.remove(other.toString());
        }
        yml.set("trusted." + owner, new ArrayList<>(set));
        saveData(yml);
    }

    private Player targetOrSelf(CommandSender sender, String[] args, int index) {
        if (args.length > index) {
            return Bukkit.getPlayerExact(args[index]);
        }
        return sender instanceof Player p ? p : null;
    }

    private void clearAllCooldowns(UUID id) {
        fire.clearCooldowns(id);
        life.clearCooldowns(id);
        speed.clearCooldowns(id);
        puff.clearCooldowns(id);
        strength.clearCooldowns(id);
        wealth.clearCooldowns(id);
        astra.clearCooldowns(id);
        flux.clearCooldowns(id);
        weapons.clearCooldowns(id);
        fluxCooldownUntil.remove(id);
    }

    /** Removes the timed Life max health changes and sets max health back to vanilla (20). */
    private void fixMaxHealth(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) {
            return;
        }
        for (AttributeModifier modifier : new ArrayList<>(attr.getModifiers())) {
            if (modifier.getKey().getKey().startsWith("life-")) {
                attr.removeModifier(modifier);
            }
        }
        attr.setBaseValue(20.0);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private void showInfo(CommandSender sender, Player target) {
        sender.sendMessage(color("&6Info for " + target.getName()));
        ItemStack gem = null;
        for (ItemStack item : target.getInventory().getContents()) {
            if (gems.typeOf(item) != null) {
                gem = item;
                break;
            }
        }
        if (gem == null) {
            sender.sendMessage(color("&7Gem: &fnone"));
        } else {
            sender.sendMessage(color("&7Gem: &f" + gems.typeOf(gem).name() + " &7Tier &f" + gems.tierOf(gem)));
        }
        sender.sendMessage(color("&7Trusted players: &f" + trustedBy(target.getUniqueId()).size()));
    }

    /** /bliss reroll <player> [tier]: gives a random gem. */
    /** /bliss bannable true|false: whether reaching 0 energy on death bans the player. Off by default. */
    private void handleBannable(CommandSender sender, String[] args) {
        boolean current = getConfig().getBoolean("energy.ban-on-zero-energy", false);
        if (args.length < 2) {
            sender.sendMessage(color("&cUsage: /bliss bannable <true/false>"));
            sender.sendMessage(color("&7Current status: " + (current ? "&aEnabled" : "&cDisabled")));
            return;
        }
        String value = args[1].toLowerCase();
        boolean on;
        if (value.equals("true") || value.equals("on") || value.equals("yes") || value.equals("1")) {
            on = true;
        } else if (value.equals("false") || value.equals("off") || value.equals("no") || value.equals("0")) {
            on = false;
        } else {
            sender.sendMessage(color("&cInvalid value! Use true or false."));
            return;
        }
        getConfig().set("energy.ban-on-zero-energy", on);
        saveConfig();
        sender.sendMessage(color(on
                ? "&aBan-on-zero-energy is enabled. Players who reach 0 energy will be banned."
                : "&cBan-on-zero-energy is disabled."));
    }

    private void handleReroll(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(color("&cUsage: /bliss reroll <player> [tier]"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(color("&cPlayer not found."));
            return;
        }
        int tier = 1;
        if (args.length >= 3) {
            try {
                tier = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                tier = -1;
            }
        }
        if (tier < 1 || tier > 2) {
            sender.sendMessage(color("&cTier must be 1 or 2."));
            return;
        }
        GemType[] types = GemType.values();
        GemType type = types[ThreadLocalRandom.current().nextInt(types.length)];
        removeGems(target);
        giveItem(target, gems.create(type, tier));
        unlockAchievement(target, Achievement.REAWAKENING);
        sender.sendMessage(color("&aRerolled " + target.getName() + " to " + type.name() + " (Tier " + tier + ")."));
    }

    // ------------------------------------------------------------ Gem items

    private void removeGems(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (gems.typeOf(inv.getItem(i)) != null) {
                inv.setItem(i, null);
            }
        }
    }

    /** Adds the item; anything that doesn't fit is dropped at the player's feet, never lost. */
    private void giveItem(Player player, ItemStack item) {
        for (ItemStack leftover : player.getInventory().addItem(item).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (gems.typeOf(event.getItemDrop().getItemStack()) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(color("&cYou can't drop your gem."));
        }
    }

    // ------------------------------------------------------------ Keep on death

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        cancelFluxCharge(player);
        fire.cancelFor(player);
        life.cancelFor(player);
        puff.cancelFor(player);

        if (getConfig().getBoolean("death.drop-head", true)) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            if (head.getItemMeta() instanceof SkullMeta skull) {
                skull.setOwningPlayer(player);
                head.setItemMeta(skull);
            }
            event.getDrops().add(head);
        }

        energy.remove(player.getUniqueId(), getConfig().getInt("energy.loss-on-death", 1));
        stats.addDeath(player.getUniqueId());
        if (energy.energyOf(player) <= 0) {
            unlockAchievement(player, Achievement.SHATTERED);
        }
        if (energy.energyOf(player) <= 0 && getConfig().getBoolean("energy.ban-on-zero-energy", false)) {
            String reason = "You have been banned for reaching 0 energy! Your gem is completely broken.";
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), reason, null, null);
            player.kickPlayer(reason);
        }
        Player killer = player.getKiller();
        if (killer != null && !killer.equals(player)) {
            energy.add(killer.getUniqueId(), getConfig().getInt("energy.gain-on-kill", 1));
            stats.addKill(killer.getUniqueId());
            if (energy.energyOf(killer) >= energy.max()) {
                unlockAchievement(killer, Achievement.OVERFLOWING);
            }
            // Energy Bottle drop: a killer already at max energy may drop a bottle where the victim died
            if (energy.energyOf(killer) >= energy.max()
                    && getConfig().getBoolean("energy-bottle.drop-enabled", true)) {
                player.getWorld().dropItemNaturally(player.getLocation(), customItems.create(CustomItems.Item.ENERGY_BOTTLE));
            }
        }

        List<ItemStack> saved = new ArrayList<>();
        event.getDrops().removeIf(item -> {
            if (gems.typeOf(item) != null) {
                saved.add(item.clone());
                return true;
            }
            return false;
        });

        if (!saved.isEmpty()) {
            savePending(player.getUniqueId(), saved);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<ItemStack> saved = takePending(player.getUniqueId());
        if (saved.isEmpty()) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (ItemStack gem : saved) {
                giveItem(player, gem);
            }
            player.sendMessage(color("&bYour gem was returned to you."));
        }, 1L);
    }

    private void savePending(UUID id, List<ItemStack> items) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        yml.set(id.toString(), items);
        writeData(yml);
    }

    private List<ItemStack> takePending(UUID id) {
        List<ItemStack> out = new ArrayList<>();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        if (!yml.contains(id.toString())) {
            return out;
        }
        for (Object obj : yml.getList(id.toString(), List.of())) {
            if (obj instanceof ItemStack stack) {
                out.add(stack);
            }
        }
        yml.set(id.toString(), null);
        writeData(yml);
        return out;
    }

    private void writeData(YamlConfiguration yml) {
        try {
            yml.save(dataFile);
        } catch (IOException ex) {
            getLogger().severe("Could not save data.yml: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------ Ability input

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        GemType type = gems.typeOf(item);
        if (type == null) {
            return;
        }
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!energy.abilitiesUsable(player.getUniqueId())) {
            player.sendMessage(color("&cYour gem is broken. Restore energy to use its abilities."));
            return;
        }
        if (!clickEnabled(player.getUniqueId())) {
            player.sendMessage(color("&cClick activation is disabled! Use /bliss toggle_click to turn it back on."));
            return;
        }
        if (astra.nullified(player)) {
            player.sendMessage(color("&5🕳 Your gem abilities are nullified by a Dimensional Void!"));
            return;
        }
        int tier = gems.tierOf(item);
        fx(player, castSound(type), 0.6f, 1.2f);
        switch (type) {
            case FIRE -> fire.onRightClick(player, tier, player.isSneaking());
            case FLUX -> {
                if (tier >= 2 && player.isSneaking()) {
                    flux.onShiftRightClick(player);
                } else {
                    fluxRightClick(player);
                }
            }
            case LIFE -> life.onRightClick(player, tier, player.isSneaking());
            case SPEED -> speed.onRightClick(player, tier, player.isSneaking());
            case PUFF -> puff.onRightClick(player, tier, player.isSneaking());
            case STRENGTH -> strength.onRightClick(player, tier, player.isSneaking());
            case WEALTH -> wealth.onRightClick(player, tier, player.isSneaking());
            case ASTRA -> astra.onRightClick(player, tier, player.isSneaking());
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        ItemStack main = event.getMainHandItem();
        GemType type = gems.typeOf(main);
        if (type == null) {
            return;
        }
        if (!energy.abilitiesUsable(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(color("&cYour gem is broken. Restore energy to use its abilities."));
            return;
        }
        if (astra.nullified(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(color("&5🕳 Your gem abilities are nullified by a Dimensional Void!"));
            return;
        }
        int tier = gems.tierOf(main);
        Player player = event.getPlayer();
        boolean sneaking = player.isSneaking();
        switch (type) {
            case FIRE -> {
                event.setCancelled(true);
                fire.onSwap(player, tier, sneaking);
            }
            case STRENGTH -> {
                if (tier >= 2) {
                    event.setCancelled(true);
                    strength.onSwap(player, tier, sneaking);
                }
            }
            case LIFE -> {
                if (tier >= 2) {
                    event.setCancelled(true);
                    life.onSwap(player, tier, sneaking);
                }
            }
            case WEALTH -> {
                if (tier >= 2) {
                    event.setCancelled(true);
                    wealth.onSwap(player, tier, sneaking);
                }
            }
            case FLUX -> {
                if (tier >= 2) {
                    event.setCancelled(true);
                    flux.onSwap(player, sneaking);
                }
            }
            case ASTRA -> {
                if (tier >= 2) {
                    event.setCancelled(true);
                    astra.onSwap(player, tier, sneaking);
                }
            }
            case SPEED -> {
                if (tier >= 2) {
                    event.setCancelled(true);
                    speed.onSwap(player, tier, sneaking);
                }
            }
            case PUFF -> {
                if (tier >= 2) {
                    event.setCancelled(true);
                    puff.onSwap(player, tier, sneaking);
                }
            }
            default -> {
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelFluxCharge(event.getPlayer());
        fire.cancelFor(event.getPlayer());
        life.cancelFor(event.getPlayer());
    }

    // ------------------------------------------------------------ Flux Beam

    private void fluxRightClick(Player player) {
        if (fluxCharging.containsKey(player.getUniqueId())) {
            fluxFire(player);
        } else {
            fluxStartCharge(player);
        }
    }

    private void fluxStartCharge(Player player) {
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        long until = fluxCooldownUntil.getOrDefault(id, 0L);
        if (now < until) {
            player.sendMessage(color("&cFlux Beam recharging: " + ((until - now) / 1000 + 1) + "s"));
            return;
        }

        int chargeTicks = Math.max(1, getConfig().getInt("flux.charge-ticks", 60));
        int maxHoldTicks = Math.max(chargeTicks, getConfig().getInt("flux.max-hold-ticks", 200));

        fluxCharge.put(id, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
        player.sendMessage(color("&b⚡ Charging Flux Beam... right-click again to fire!"));

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()
                        || gems.typeOf(player.getInventory().getItemInMainHand()) != GemType.FLUX) {
                    cancelFluxCharge(player);
                    return;
                }
                ticks++;
                int pct = Math.min(100, ticks * 100 / chargeTicks);
                fluxCharge.put(id, pct);
                player.sendActionBar(bar("⚡ Flux Beam", pct));
                if (ticks >= maxHoldTicks) {
                    player.sendMessage(color("&7Charge expired."));
                    cancelFluxCharge(player);
                }
            }
        }.runTaskTimer(this, 0L, 1L);

        fluxCharging.put(id, task);
    }

    private void fluxFire(Player player) {
        UUID id = player.getUniqueId();
        int pct = fluxCharge.getOrDefault(id, 0);
        cancelFluxCharge(player);
        if (pct >= 100) {
            unlockAchievement(player, Achievement.OVERCHARGED);
        }

        if (pct < 10) {
            player.sendMessage(color("&cNot enough charge!"));
            return;
        }

        double range = getConfig().getDouble("flux.range", 30.0);
        double base = getConfig().getDouble("abilities.damage.flux-beam-base", 5.0);
        long cooldownMs = getConfig().getLong("abilities.cooldowns.flux-beam", 240) * 1000L;

        RayTraceResult hit = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                0.5,
                entity -> entity instanceof LivingEntity && !entity.equals(player)
        );

        if (hit == null || !(hit.getHitEntity() instanceof LivingEntity target)) {
            player.sendMessage(color("&cNo target found!"));
            return;
        }
        if (target instanceof Player targetPlayer
                && (targetPlayer.getGameMode() == GameMode.CREATIVE || targetPlayer.getGameMode() == GameMode.SPECTATOR)) {
            player.sendMessage(color("&cCan't hit that player right now."));
            return;
        }

        double damage = base * (1.0 + pct / 50.0);
        target.damage(damage, player);
        fluxCooldownUntil.put(id, System.currentTimeMillis() + cooldownMs);

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
        player.sendMessage(color("&b⚡ Flux Beam hit " + target.getName() + " for "
                + String.format("%.1f", damage) + " damage (" + pct + "% charge)"));
    }

    private void cancelFluxCharge(Player player) {
        BukkitTask task = fluxCharging.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        fluxCharge.remove(player.getUniqueId());
    }

    // ---- Achievement ----
    /** The original plugin's achievements. */
    public enum Achievement {
        SHATTERED("Shattered", "Reach the Broken energy stage"),
        ZIP_AWAY("Zip Away", "Travel across 1,914 copper blocks using Conduction"),
        PIERCING_PRECISION("Piercing Precision", "Hit a player with 5 consecutive Phantom Daggers"),
        SAVED_BY_THE_DICE("Saved By The Dice", "Avoid a fatal blow with Phasing"),
        OVERFLOWING("Overflowing", "Reach the Pristine +5 energy stage"),
        ALMOST_FELL_FROM_GRACE("Almost Fell From Grace", "Prevent 30,000 points of fall damage with Puff Gem"),
        GOOD_AS_NEW("Good As New!", "Draw 10 energy from the Energy Pool using a Repair Kit"),
        REAWAKENING("Reawakening", "Successfully complete a Restoration Ritual"),
        ITS_RABBIT_SEASON("It's Rabbit Season", "Track the same player with Shadow Stalker 17 times"),
        TIME_FOR_A_CHANGE("Time For A Change", "Use a Trader to switch gem types"),
        THE_NEXT_LEVEL("The Next Level", "Use an Upgrader to obtain a Tier 2 gem"),
        OVERCHARGED("Overcharged", "Charge a Flux Gem to 100W"),
        BOUNDARY_BREAK("Boundary Break", "Boost enchantments beyond vanilla levels using Amplification");

        private final String title;
        private final String description;

        Achievement(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String title() {
            return title;
        }

        public String description() {
            return description;
        }
    }

    // ---- AstraGem ----
    /**
     * Astra gem.
     * Offhand passive: 10% phase chance, 1 heart back on every kill, and Soul Healing
     *   (spends a soul to heal 2 hearts when you drop to 30% health or below).
     * Tier 1: right-click Astral Daggers. Shift + left click on a mob captures its soul.
     * Tier 2: Shift + right-click Astral Projection. F Dimensional Drift.
     *   Shift + F Dimensional Void (nullifies other players' gem abilities nearby).
     */
    public static final class AstraGem implements Listener {

        private static final double VOID_RADIUS = 12.0;

        private record Projection(Location origin, GameMode mode) {}

        private record Zone(UUID owner, Location center, double radius, long until) {}

        private final BlissGems plugin;
        private final NamespacedKey soulsKey;
        private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
        private final Map<UUID, Projection> projecting = new HashMap<>();
        private final Map<UUID, Entity> drifts = new HashMap<>();
        private final List<Zone> voidZones = new ArrayList<>();

        public AstraGem(BlissGems plugin) {
            this.plugin = plugin;
            this.soulsKey = new NamespacedKey(plugin, "souls");
        }

        // ------------------------------------------------------------ Inputs

        /** Right-click: Astral Daggers. Shift + right-click at Tier 2: Astral Projection. */
        public void onRightClick(Player player, int tier, boolean sneaking) {
            if (tier >= 2 && sneaking) {
                projection(player);
            } else {
                astralDaggers(player);
            }
        }

        /** Total souls held across your astra gems. */
        public int soulCount(Player player) {
            int total = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (plugin.gems().typeOf(item) == GemType.ASTRA) {
                    total += readSouls(item);
                }
            }
            return total;
        }

        /** Clears every captured soul from your astra gems. Returns how many were released. */
        public int releaseSouls(Player player) {
            int released = 0;
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (plugin.gems().typeOf(item) == GemType.ASTRA && readSouls(item) > 0) {
                    released += readSouls(item);
                    setSouls(item, 0);
                    inv.setItem(i, item);
                }
            }
            return released;
        }

        public boolean isProjecting(UUID id) {
            return projecting.containsKey(id);
        }

        /** /bliss spook (while projecting): blinds and nauseates players near your projection for a few seconds. */
        public void spook(Player caster) {
            if (!isProjecting(caster.getUniqueId())) {
                caster.sendMessage(BlissGems.color("&cYou must be in Astral Projection to use Spook."));
                return;
            }
            if (!ready(caster, "astra-spook", 10)) {
                return;
            }
            double radius = plugin.getConfig().getDouble("abilities.astra-spook.radius", 8.0);
            int seconds = plugin.getConfig().getInt("abilities.durations.astra-spook", 3);
            Location c = caster.getLocation();
            for (Entity entity : c.getWorld().getNearbyEntities(c, radius, radius, radius)) {
                if (entity instanceof Player target && !target.equals(caster)) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, seconds * 20, 0, true, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, seconds * 20, 0, true, false));
                }
            }
            c.getWorld().spawnParticle(Particle.SMOKE, c.clone().add(0, 1, 0), 60, radius / 2, 1.0, radius / 2, 0.02);
            caster.sendMessage(BlissGems.color("&5👻 Spook!"));
        }

        /** /bliss tag <player> (while projecting): the target glows, and you see how far away they are for 30 seconds. */
        public void tag(Player caster, Player target) {
            if (!isProjecting(caster.getUniqueId())) {
                caster.sendMessage(BlissGems.color("&cYou must be in Astral Projection to use Tag."));
                return;
            }
            if (target.equals(caster) || !target.isOnline()) {
                caster.sendMessage(BlissGems.color("&cThat player can't be tagged."));
                return;
            }
            if (!ready(caster, "astra-tag", 15)) {
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.astra-tag", 30);
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, seconds * 20, 0, true, false));
            caster.sendMessage(BlissGems.color("&5🏷 Tagged " + target.getName() + " for " + seconds + "s."));

            new BukkitRunnable() {
                int elapsed = 0;

                @Override
                public void run() {
                    if (elapsed++ >= seconds / 2 || !caster.isOnline() || !target.isOnline()) {
                        cancel();
                        return;
                    }
                    if (target.getWorld() == caster.getWorld()) {
                        int blocks = (int) caster.getLocation().distance(target.getLocation());
                        caster.sendActionBar(net.kyori.adventure.text.Component.text(
                                "Tagged " + target.getName() + ": " + blocks + " blocks"));
                    }
                }
            }.runTaskTimer(plugin, 0L, 40L);
        }

        /** F at Tier 2: Dimensional Drift. Shift + F at Tier 2: Dimensional Void. */
        public void onSwap(Player player, int tier, boolean sneaking) {
            if (tier < 2) {
                return;
            }
            if (sneaking) {
                dimensionalVoid(player);
            } else {
                drift(player);
            }
        }

        // ------------------------------------------------------------ Astral Daggers (T1)

        private void astralDaggers(Player caster) {
            if (!ready(caster, "astra-daggers", 15)) {
                return;
            }
            double total = plugin.getConfig().getDouble("abilities.damage.astra-daggers", 12.0);
            double perDagger = total / 3.0;
            caster.playSound(caster.getLocation(), Sound.ENTITY_PHANTOM_SWOOP, 1.0f, 1.2f);

            Vector base = caster.getEyeLocation().getDirection();
            double[] angles = {-Math.toRadians(12), 0.0, Math.toRadians(12)};
            int playerHits = 0;
            for (double angle : angles) {
                Vector direction = base.clone().rotateAroundY(angle).normalize();
                RayTraceResult hit = caster.getWorld().rayTraceEntities(
                        caster.getEyeLocation(), direction, 16, 0.4,
                        entity -> entity instanceof LivingEntity && !entity.equals(caster));
                if (hit != null && hit.getHitEntity() instanceof LivingEntity target) {
                    target.damage(perDagger, caster);
                    if (target instanceof Player) {
                        playerHits++;
                    }
                }
            }
            // Piercing Precision: 5 player hits in a row. A cast with no player hit resets the count.
            if (playerHits == 0) {
                plugin.setProgress(caster.getUniqueId(), "dagger-streak", 0);
            } else if (plugin.addProgress(caster.getUniqueId(), "dagger-streak", playerHits) >= 5) {
                plugin.unlockAchievement(caster, Achievement.PIERCING_PRECISION);
            }
        }

        // ------------------------------------------------------------ Astral Projection (T2)

        /** You become a spectator for 10 seconds, then return to where you started. */
        private void projection(Player caster) {
            UUID id = caster.getUniqueId();
            if (projecting.containsKey(id)) {
                caster.sendMessage(BlissGems.color("&cYou are already projecting."));
                return;
            }
            if (!ready(caster, "astra-projection", 60)) {
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.astra-projection", 10);
            projecting.put(id, new Projection(caster.getLocation(), caster.getGameMode()));
            caster.setGameMode(GameMode.SPECTATOR);
            caster.sendMessage(BlissGems.color("&b👻 Astral Projection for " + seconds + "s"));

            Bukkit().runTaskLater(plugin, () -> endProjection(caster), seconds * 20L);
        }

        /** Returns a projecting player to their body. Safe to call when they aren't projecting. */
        public void endProjection(Player player) {
            Projection projection = projecting.remove(player.getUniqueId());
            if (projection == null) {
                return;
            }
            if (player.isOnline()) {
                player.teleport(projection.origin());
                player.setGameMode(projection.mode());
                player.sendMessage(BlissGems.color("&b👻 Projection ended."));
            }
        }

        // ------------------------------------------------------------ Dimensional Drift (T2)

        /** Invisible, riding an invisible horse, for 15 seconds. */
        private void drift(Player caster) {
            UUID id = caster.getUniqueId();
            if (drifts.containsKey(id)) {
                caster.sendMessage(BlissGems.color("&cDrift is already active."));
                return;
            }
            if (!ready(caster, "astra-drift", 90)) {
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.astra-drift", 15);
            caster.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, seconds * 20, 0, true, false));
            Horse horse = caster.getWorld().spawn(caster.getLocation(), Horse.class, h -> {
                h.setAdult();
                h.setTamed(true);
                h.setOwner(caster);
                h.setInvisible(true);
                h.setSilent(true);
                h.setInvulnerable(true);
                h.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            });
            horse.addPassenger(caster);
            drifts.put(id, horse);
            caster.sendMessage(BlissGems.color("&b🌀 Dimensional Drift!"));

            Bukkit().runTaskLater(plugin, () -> endDrift(caster), seconds * 20L);
        }

        public void endDrift(Player player) {
            Entity horse = drifts.remove(player.getUniqueId());
            if (horse != null) {
                player.leaveVehicle();
                horse.remove();
            }
        }

        // ------------------------------------------------------------ Dimensional Void (T2)

        /** For 12 seconds, other players' gem abilities don't work within 12 blocks of this spot. */
        private void dimensionalVoid(Player caster) {
            if (!ready(caster, "astra-void", 120)) {
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.astra-void", 12);
            Location center = caster.getLocation();
            voidZones.add(new Zone(caster.getUniqueId(), center, VOID_RADIUS,
                    System.currentTimeMillis() + seconds * 1000L));
            ring(center.clone().add(0, 0.2, 0), Particle.PORTAL, VOID_RADIUS, 48);
            center.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.6f);
            caster.sendMessage(BlissGems.color("&5🕳 Dimensional Void for " + seconds + "s"));
        }

        /** True if this player is inside another player's active Dimensional Void. */
        public boolean nullified(Player player) {
            long now = System.currentTimeMillis();
            voidZones.removeIf(zone -> zone.until() < now);
            for (Zone zone : voidZones) {
                if (zone.owner().equals(player.getUniqueId())) {
                    continue;
                }
                if (zone.center().getWorld() == player.getWorld()
                        && zone.center().distance(player.getLocation()) <= zone.radius()) {
                    return true;
                }
            }
            return false;
        }

        // ------------------------------------------------------------ Soul capture (T1)

        /** Shift + left click on a mob with the astra gem in the main hand. */
        @EventHandler
        public void onCapture(EntityDamageByEntityEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK) {
                return;
            }
            if (!(event.getDamager() instanceof Player player) || !player.isSneaking()) {
                return;
            }
            if (event.getEntity() instanceof Player || !(event.getEntity() instanceof LivingEntity mob)) {
                return;
            }
            ItemStack main = player.getInventory().getItemInMainHand();
            if (plugin.gems().typeOf(main) != GemType.ASTRA) {
                return;
            }
            event.setCancelled(true);

            int tier = plugin.gems().tierOf(main);
            int maxSouls = plugin.getConfig().getInt("passives.astra.tier" + tier + ".max-souls", 10);
            int souls = Math.min(maxSouls, readSouls(main) + 1);
            Location where = mob.getLocation();
            mob.remove();
            writeSouls(player, main, souls);

            heal(player, plugin.getConfig().getDouble("passives.astra.heart-healing", 2.0));
            where.getWorld().playSound(where, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.5f);
            player.sendActionBar(net.kyori.adventure.text.Component.text("Soul captured (" + souls + "/" + maxSouls + ")"));
        }

        private int readSouls(ItemStack gem) {
            ItemMeta meta = gem.getItemMeta();
            if (meta == null) {
                return 0;
            }
            Integer value = meta.getPersistentDataContainer().get(soulsKey, PersistentDataType.INTEGER);
            return value == null ? 0 : value;
        }

        private void setSouls(ItemStack gem, int souls) {
            ItemMeta meta = gem.getItemMeta();
            meta.getPersistentDataContainer().set(soulsKey, PersistentDataType.INTEGER, souls);
            gem.setItemMeta(meta);
        }

        private void writeSouls(Player player, ItemStack gem, int souls) {
            setSouls(gem, souls);
            player.getInventory().setItemInMainHand(gem);
        }

        // ------------------------------------------------------------ Soul Healing (passive)

        /** When you drop to 30% health or below (and survive), spend a soul to heal 2 hearts. */
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSoulHealing(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }
            double after = player.getHealth() - event.getFinalDamage();
            if (after <= 0 || after > player.getMaxHealth() * 0.3) {
                return;
            }
            if (!consumeSoul(player)) {
                return;
            }
            Bukkit().runTask(plugin, () -> heal(player, 4.0));
            player.sendMessage(BlissGems.color("&5👻 Soul Healing!"));
        }

        /** Uses one soul from the first astra gem in the inventory that has any. */
        private boolean consumeSoul(Player player) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (plugin.gems().typeOf(item) != GemType.ASTRA) {
                    continue;
                }
                int souls = readSouls(item);
                if (souls > 0) {
                    setSouls(item, souls - 1);
                    inv.setItem(i, item);
                    return true;
                }
            }
            return false;
        }

        // ------------------------------------------------------------ Kill healing

        /** Any kill (player or mob) with the astra gem in the offhand heals the killer by 1 heart. */
        @EventHandler
        public void onKill(EntityDeathEvent event) {
            Player killer = event.getEntity().getKiller();
            if (killer == null) {
                return;
            }
            ItemStack offhand = killer.getInventory().getItemInOffHand();
            if (plugin.gems().typeOf(offhand) != GemType.ASTRA) {
                return;
            }
            heal(killer, plugin.getConfig().getDouble("passives.astra.heart-on-kill", 2.0));
        }

        private void heal(Player player, double hp) {
            if (player.isDead()) {
                return;
            }
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + hp));
        }

        // ------------------------------------------------------------ Phase passive

        @EventHandler
        public void onDamage(EntityDamageEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK && event.getCause() != DamageCause.PROJECTILE) {
                return;
            }
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (plugin.gems().typeOf(offhand) != GemType.ASTRA) {
                return;
            }
            int tier = plugin.gems().tierOf(offhand);
            double chance = plugin.getConfig().getDouble("passives.astra.tier" + tier + ".phase-chance", tier >= 2 ? 0.15 : 0.10);
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                event.setCancelled(true);
                if (player.getHealth() - event.getFinalDamage() <= 0) {
                    plugin.unlockAchievement(player, Achievement.SAVED_BY_THE_DICE);
                }
            }
        }

        // ------------------------------------------------------------ Quit cleanup

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            endProjection(event.getPlayer());
            endDrift(event.getPlayer());
        }

        // ------------------------------------------------------------ Helpers

        private org.bukkit.scheduler.BukkitScheduler Bukkit() {
            return org.bukkit.Bukkit.getScheduler();
        }

        /** A flat ring of particles around a point. */
        private void ring(Location center, Particle particle, double radius, int points) {
            for (int i = 0; i < points; i++) {
                double angle = (double) i / points * Math.PI * 2;
                Location at = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                center.getWorld().spawnParticle(particle, at, 1, 0, 0, 0, 0);
            }
        }

        private boolean ready(Player player, String ability, int fallbackSeconds) {
            Map<String, Long> mine = cooldowns.getOrDefault(player.getUniqueId(), Collections.emptyMap());
            long until = mine.getOrDefault(ability, 0L);
            long now = System.currentTimeMillis();
            if (now < until) {
                player.sendMessage(BlissGems.color("&cThat ability is recharging: " + ((until - now) / 1000 + 1) + "s"));
                return false;
            }
            int seconds = plugin.getConfig().getInt("abilities.cooldowns." + ability, fallbackSeconds);
            cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(ability, now + seconds * 1000L);
            return true;
        }

        public void clearCooldowns(UUID playerId) {
            cooldowns.remove(playerId);
        }
    }

    // ---- AutoEnchant ----
    /**
     * Gems enchant the items you carry while the gem is in your offhand. Enchants added this way are
     * remembered on the item and removed when the gem leaves your offhand.
     */
    public static final class AutoEnchant {

        private final BlissGems plugin;
        private final NamespacedKey markerKey;

        public AutoEnchant(BlissGems plugin) {
            this.plugin = plugin;
            this.markerKey = new NamespacedKey(plugin, "auto_enchants");
        }

        public void start() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        tick(player);
                    }
                }
            }.runTaskTimer(plugin, 40L, 40L);
        }

        private void tick(Player player) {
            if (!plugin.getConfig().getBoolean("auto-enchant.enabled", true)) {
                return;
            }
            ItemStack offhand = player.getInventory().getItemInOffHand();
            GemType type = plugin.gems().typeOf(offhand);
            boolean active = type != null && plugin.energy().passivesActive(player.getUniqueId());
            int tier = plugin.gems().tierOf(offhand);

            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item == null || item.getType().isAir()) {
                    continue;
                }
                Map<Enchantment, Integer> wanted = active ? enchantsFor(type, tier, item.getType()) : Map.of();
                if (active && !wanted.isEmpty()) {
                    apply(item, wanted);
                } else {
                    strip(item);
                }
                player.getInventory().setItem(i, item);
            }
        }

        /** Marker format: "key=originalLevel,key=originalLevel". Level 0 means the item didn't have it. */
        private void apply(ItemStack item, Map<Enchantment, Integer> wanted) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }
            Map<String, Integer> recorded = readMarker(meta);
            for (Map.Entry<Enchantment, Integer> entry : wanted.entrySet()) {
                String key = entry.getKey().getKey().getKey();
                if (item.getEnchantmentLevel(entry.getKey()) >= entry.getValue()) {
                    continue;
                }
                recorded.putIfAbsent(key, item.getEnchantmentLevel(entry.getKey()));
                item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
            ItemMeta updated = item.getItemMeta();
            updated.getPersistentDataContainer().set(markerKey, PersistentDataType.STRING, writeMarker(recorded));
            item.setItemMeta(updated);
        }

        /** Puts back the levels the item had before the gem added its enchants. */
        private void strip(ItemStack item) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }
            Map<String, Integer> recorded = readMarker(meta);
            if (recorded.isEmpty()) {
                return;
            }
            for (Map.Entry<String, Integer> entry : recorded.entrySet()) {
                Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(entry.getKey()));
                if (enchant == null) {
                    continue;
                }
                item.removeEnchantment(enchant);
                if (entry.getValue() > 0) {
                    item.addUnsafeEnchantment(enchant, entry.getValue());
                }
            }
            ItemMeta updated = item.getItemMeta();
            updated.getPersistentDataContainer().remove(markerKey);
            item.setItemMeta(updated);
        }

        private Map<String, Integer> readMarker(ItemMeta meta) {
            Map<String, Integer> out = new LinkedHashMap<>();
            String text = meta.getPersistentDataContainer().get(markerKey, PersistentDataType.STRING);
            if (text == null || text.isEmpty()) {
                return out;
            }
            for (String part : text.split(",")) {
                String[] kv = part.split("=");
                if (kv.length == 2) {
                    out.put(kv[0], Integer.parseInt(kv[1]));
                }
            }
            return out;
        }

        private String writeMarker(Map<String, Integer> recorded) {
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : recorded.entrySet()) {
                parts.add(entry.getKey() + "=" + entry.getValue());
            }
            return String.join(",", parts);
        }

        private Map<Enchantment, Integer> enchantsFor(GemType type, int tier, Material m) {
            Map<Enchantment, Integer> out = new LinkedHashMap<>();
            boolean tool = isTool(m);
            boolean sword = m.name().endsWith("_SWORD");
            boolean axe = m.name().endsWith("_AXE");
            boolean bow = m == Material.BOW;
            boolean boots = m.name().endsWith("_BOOTS");
            boolean weaponOrTool = tool || sword || axe || bow || m == Material.CROSSBOW || m == Material.TRIDENT;
            boolean armor = isArmor(m);
            switch (type) {
                case SPEED -> {
                    if (cfg("speed.efficiency") && tool) out.put(Enchantment.EFFICIENCY, 5);
                }
                case WEALTH -> {
                    if (cfg("wealth.fortune") && (m.name().endsWith("_PICKAXE") || m.name().endsWith("_SHOVEL") || axe))
                        out.put(Enchantment.FORTUNE, 3);
                    if (cfg("wealth.looting") && sword) out.put(Enchantment.LOOTING, 3);
                    if (cfg("wealth.mending") && (weaponOrTool || armor)) out.put(Enchantment.MENDING, 1);
                }
                case FIRE -> {
                    if (cfg("fire.flame") && bow) out.put(Enchantment.FLAME, 1);
                    if (cfg("fire.fire-aspect") && sword) out.put(Enchantment.FIRE_ASPECT, 2);
                }
                case PUFF -> {
                    if (cfg("puff.feather-falling") && boots) out.put(Enchantment.FEATHER_FALLING, 4);
                    if (cfg("puff.power") && bow) out.put(Enchantment.POWER, 5);
                    if (cfg("puff.punch") && bow) out.put(Enchantment.PUNCH, 2);
                }
                case STRENGTH -> {
                    if (cfg("strength.sharpness") && (sword || axe)) out.put(Enchantment.SHARPNESS, tier >= 2 ? 5 : 2);
                }
                case LIFE -> {
                    if (cfg("life.unbreaking") && (weaponOrTool || armor)) out.put(Enchantment.UNBREAKING, 3);
                }
                default -> {
                }
            }
            return out;
        }

        private boolean cfg(String key) {
            return plugin.getConfig().getBoolean("auto-enchant." + key, true);
        }

        private static boolean isTool(Material m) {
            String n = m.name();
            return n.endsWith("_PICKAXE") || n.endsWith("_SHOVEL") || n.endsWith("_AXE") || n.endsWith("_HOE");
        }

        private static boolean isArmor(Material m) {
            String n = m.name();
            return n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS");
        }
    }

    // ---- CustomItems ----
    /** Custom items shown in /bliss menu. Egg Shooter is the only one with an effect so far. */
    public static final class CustomItems implements Listener {

        public enum Item {
            EGG_SHOOTER, GEM_UPGRADER, ENERGY_BOTTLE, REPAIR_KIT, GEM_FRAGMENT, REVIVE_BEACON,
            FROST_SCEPTER, THUNDER_HAMMER, STUN_BATON, BLAST_CANNON, LEVITATION_WAND,
            VOID_LANCE, EMBER_BLADE, VENOM_DAGGER, GALE_AXE, SOUL_REAPER, GEM_TRADER, ORBITAL_SNOWBALL
        }

        private static final MiniMessage MM = MiniMessage.miniMessage();
        private static final String NOT_ACTIVE = "§8(Effect not added yet)";

        private record Def(Material material, int modelData, String name, List<String> lore) {}

        private static final Def EGG_SHOOTER = new Def(Material.CROSSBOW, 5001,
                "<gradient:#ffd700:#ff8c00><b><i>Egg Shooter</i></b></gradient>",
                List.of("§7Fires explosive eggs.", "§7Right-click to shoot.", "§8Explosions never break blocks."));

        private static final Def GEM_UPGRADER = new Def(Material.ENCHANTED_BOOK, 3001,
                "<gradient:#ff00ff:#00ffff><b><i>Gem Upgrader</i></b></gradient>",
                List.of("§7Right Click to upgrade any Tier 1 gem to Tier 2", "§8(Upgrade not added yet)"));

        private static final Def ENERGY_BOTTLE = new Def(Material.GHAST_TEAR, 4001,
                "<gradient:#00ffea:#0077ff><b><i>Energy Bottle</i></b></gradient>",
                List.of("§7Restores 1 energy when used."));

        private static final Def REPAIR_KIT = new Def(Material.BEACON, 4003,
                "<gradient:#a8ff78:#78ffd6><b><i>Repair Kit</i></b></gradient>",
                List.of("§7Restores 1 energy per second, up to 10 total."));

        private static final Def GEM_FRAGMENT = new Def(Material.PRISMARINE_SHARD, 4004,
                "<gradient:#8e2de2:#4a00e0><b><i>Gem Fragment</i></b></gradient>",
                List.of("§7A shard of a broken gem.", NOT_ACTIVE));

        private static final Def REVIVE_BEACON = new Def(Material.BEACON, 4005,
                "<gradient:#f7971e:#ffd200><b><i>Revive Beacon</i></b></gradient>",
                List.of("§7A powerful beacon that can revive players", "§7from the brink of death.", "",
                        "§6Right-click to activate", NOT_ACTIVE));

        private static final Def ORBITAL_SNOWBALL = new Def(Material.SNOWBALL, 7001,
                "<gradient:#ff9a00:#ff3d00><b><i>Orbital Snowball</i></b></gradient>",
                List.of("§7Right-click to throw.", "§7Where it lands, anvils, dripstone, and TNT fall from the sky.",
                        "§8TNT never breaks blocks."));

        private static final Def GEM_TRADER = new Def(Material.EMERALD, 4002,
                "<gradient:#11998e:#38ef7d><b><i>Gem Trader</i></b></gradient>",
                List.of("§7Right-click to swap your gem for a random different one.", "§8Uses up the trader."));

        // ------------------------------------------------------------ Custom weapons (see CustomWeapons)

        private static final Def FROST_SCEPTER = new Def(Material.NETHERITE_SWORD, 6001,
                "<gradient:#a1ffff:#4facfe><b><i>Frost Scepter</i></b></gradient>",
                List.of("§7Hits freeze the target.", "§7Right-click: Frost Nova (freezes everything near you)."));
        private static final Def THUNDER_HAMMER = new Def(Material.MACE, 6002,
                "<gradient:#fceabb:#f8b500><b><i>Thunder Hammer</i></b></gradient>",
                List.of("§7Hits call down lightning.", "§7Right-click: Thunder Slam (3 lightning strikes nearby)."));
        private static final Def STUN_BATON = new Def(Material.BREEZE_ROD, 6003,
                "<gradient:#00f260:#0575e6><b><i>Stun Baton</i></b></gradient>",
                List.of("§7Hits stun the target for 1 second.", "§7Right-click: Stun Dash (charge forward, stunning anyone you pass)."));
        private static final Def BLAST_CANNON = new Def(Material.BOW, 6004,
                "<gradient:#ff416c:#ff4b2b><b><i>Blast Cannon</i></b></gradient>",
                List.of("§7Hits explode with no block damage.", "§7Right-click: fires explosive arrows."));
        private static final Def LEVITATION_WAND = new Def(Material.STICK, 6005,
                "<gradient:#c3cfe2:#f5f7fa><b><i>Levitation Wand</i></b></gradient>",
                List.of("§7Hits levitate the target for 2 seconds.", "§7Right-click: float up yourself for 2 seconds."));
        private static final Def VOID_LANCE = new Def(Material.NETHERITE_SWORD, 6006,
                "<gradient:#2b0059:#8e2de2><b><i>Void Lance</i></b></gradient>",
                List.of("§7Hits wither the target.", "§7Right-click: Void Step (teleport up to 8 blocks)."));
        private static final Def EMBER_BLADE = new Def(Material.NETHERITE_SWORD, 6007,
                "<gradient:#ff512f:#dd2476><b><i>Ember Blade</i></b></gradient>",
                List.of("§7Hits set the target on fire.", "§7Right-click: Ember Ring (burns everything near you)."));
        private static final Def VENOM_DAGGER = new Def(Material.NETHERITE_SWORD, 6008,
                "<gradient:#56ab2f:#a8e063><b><i>Venom Dagger</i></b></gradient>",
                List.of("§7Hits poison and nauseate the target.", "§7Right-click: Venom Cloud (blinds and poisons nearby)."));
        private static final Def GALE_AXE = new Def(Material.NETHERITE_SWORD, 6009,
                "<gradient:#83a4d4:#b6fbff><b><i>Gale Axe</i></b></gradient>",
                List.of("§7Hits launch the target forward.", "§7Right-click: Gale Burst (blows nearby enemies away)."));
        private static final Def SOUL_REAPER = new Def(Material.DIAMOND_HOE, 6010,
                "<gradient:#434343:#ad5389><b><i>Soul Reaper</i></b></gradient>",
                List.of("§7Hits weaken the target and heal you.", "§7Right-click: Soul Pull (drags nearby enemies to you)."));

        private final BlissGems plugin;
        private final NamespacedKey idKey;
        private final Set<UUID> eggs = new HashSet<>();
        private final Map<UUID, Long> eggCooldown = new HashMap<>();

        public CustomItems(BlissGems plugin) {
            this.plugin = plugin;
            this.idKey = new NamespacedKey(plugin, "custom_item");
        }

        public ItemStack create(Item item) {
            Def def = switch (item) {
                case EGG_SHOOTER -> EGG_SHOOTER;
                case GEM_UPGRADER -> GEM_UPGRADER;
                case ENERGY_BOTTLE -> ENERGY_BOTTLE;
                case REPAIR_KIT -> REPAIR_KIT;
                case GEM_FRAGMENT -> GEM_FRAGMENT;
                case REVIVE_BEACON -> REVIVE_BEACON;
                case FROST_SCEPTER -> FROST_SCEPTER;
                case THUNDER_HAMMER -> THUNDER_HAMMER;
                case STUN_BATON -> STUN_BATON;
                case BLAST_CANNON -> BLAST_CANNON;
                case LEVITATION_WAND -> LEVITATION_WAND;
                case VOID_LANCE -> VOID_LANCE;
                case EMBER_BLADE -> EMBER_BLADE;
                case VENOM_DAGGER -> VENOM_DAGGER;
                case GALE_AXE -> GALE_AXE;
                case SOUL_REAPER -> SOUL_REAPER;
                case GEM_TRADER -> GEM_TRADER;
                case ORBITAL_SNOWBALL -> ORBITAL_SNOWBALL;
            };
            ItemStack stack = new ItemStack(def.material());
            ItemMeta meta = stack.getItemMeta();
            meta.displayName(MM.deserialize(def.name()));
            meta.setCustomModelData(def.modelData());
            meta.setLore(def.lore());
            meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, item.name());
            stack.setItemMeta(meta);
            return stack;
        }

        /** Returns which custom item this is, or null. */
        public Item idOf(ItemStack stack) {
            if (stack == null || !stack.hasItemMeta()) {
                return null;
            }
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                return null;
            }
            String value = meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
            if (value == null) {
                return null;
            }
            try {
                return Item.valueOf(value);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        // ------------------------------------------------------------ Gem Trader

        private final Map<UUID, Long> traderCooldown = new HashMap<>();

        /** Swaps your gem for a random different type. Keeps its tier. Uses up one trader. */
        private void useGemTrader(Player player) {
            long now = System.currentTimeMillis();
            long cooldownMs = plugin.getConfig().getLong("trader.cooldown", 5) * 1000L;
            Long last = traderCooldown.get(player.getUniqueId());
            if (last != null && now - last < cooldownMs) {
                long left = (cooldownMs - (now - last)) / 1000 + 1;
                player.sendMessage(BlissGems.color("&cTrade cooldown: " + left + "s"));
                return;
            }

            var inv = player.getInventory();
            int gemSlot = -1;
            for (int i = 0; i < inv.getSize(); i++) {
                if (plugin.gems().typeOf(inv.getItem(i)) != null) {
                    gemSlot = i;
                    break;
                }
            }
            if (gemSlot < 0) {
                player.sendMessage(BlissGems.color("&cYou need a gem to trade!"));
                return;
            }

            ItemStack gem = inv.getItem(gemSlot);
            GemItems.GemType current = plugin.gems().typeOf(gem);
            int tier = plugin.gems().tierOf(gem);
            java.util.List<GemItems.GemType> options = new java.util.ArrayList<>();
            for (GemItems.GemType type : GemItems.GemType.values()) {
                if (type != current) {
                    options.add(type);
                }
            }
            GemItems.GemType next = options.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(options.size()));
            inv.setItem(gemSlot, plugin.gems().create(next, tier));

            int held = inv.getHeldItemSlot();
            ItemStack trader = inv.getItem(held);
            if (trader != null) {
                if (trader.getAmount() <= 1) {
                    inv.setItem(held, null);
                } else {
                    trader.setAmount(trader.getAmount() - 1);
                }
            }
            traderCooldown.put(player.getUniqueId(), now);
            plugin.unlockAchievement(player, Achievement.TIME_FOR_A_CHANGE);
            BlissGems.fx(player, org.bukkit.Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
            player.sendMessage(BlissGems.gradient("🔁 Your gem changed to " + next.name() + "!", "#11998e", "#38ef7d"));
        }

        // ------------------------------------------------------------ Repair Kit

        /** Uses up the kit in your hand and restores energy 1 per second, up to 10 in total. */
        private void useRepairKit(Player player) {
            var energy = plugin.energy();
            var inv = player.getInventory();
            int held = inv.getHeldItemSlot();
            ItemStack kit = inv.getItem(held);
            if (kit != null) {
                if (kit.getAmount() <= 1) {
                    inv.setItem(held, null);
                } else {
                    kit.setAmount(kit.getAmount() - 1);
                }
            }
            int perSecond = plugin.getConfig().getInt("repair-kit.energy-per-second", 1);
            int maxTotal = plugin.getConfig().getInt("repair-kit.max-total-energy", 10);
            player.sendMessage(BlissGems.gradient("🔧 Repair Kit: restoring energy...", "#a8ff78", "#78ffd6"));
            new BukkitRunnable() {
                int drawn = 0;

                @Override
                public void run() {
                    if (drawn >= maxTotal || !player.isOnline() || energy.energyOf(player) >= energy.max()) {
                        cancel();
                        return;
                    }
                    energy.add(player.getUniqueId(), perSecond);
                    drawn += perSecond;
                    if (plugin.addProgress(player.getUniqueId(), "repair-drawn", perSecond) >= 10) {
                        plugin.unlockAchievement(player, Achievement.GOOD_AS_NEW);
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }

        // ------------------------------------------------------------ Energy Bottle

        /** Restores 1 energy and uses up the bottle in your hand. */
        private void useEnergyBottle(Player player) {
            var energy = plugin.energy();
            if (energy.energyOf(player) >= energy.max()) {
                player.sendMessage(BlissGems.color("&cYour energy is already full."));
                return;
            }
            energy.add(player.getUniqueId(), 1);
            var inv = player.getInventory();
            int held = inv.getHeldItemSlot();
            ItemStack bottle = inv.getItem(held);
            if (bottle != null) {
                if (bottle.getAmount() <= 1) {
                    inv.setItem(held, null);
                } else {
                    bottle.setAmount(bottle.getAmount() - 1);
                }
            }
            BlissGems.fx(player, org.bukkit.Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.2f);
            player.sendMessage(BlissGems.gradient("+1 energy (" + energy.energyOf(player) + "/" + energy.max() + ")", "#00ffea", "#0077ff"));
        }

        // ------------------------------------------------------------ Gem Upgrader

        /** Upgrades the first Tier 1 gem in your inventory and uses up the upgrader in your hand. */
        private void upgradeGem(Player player) {
            var inv = player.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack gem = inv.getItem(i);
                GemItems.GemType type = plugin.gems().typeOf(gem);
                if (type == null || plugin.gems().tierOf(gem) >= 2) {
                    continue;
                }
                inv.setItem(i, plugin.gems().create(type, 2));
                int held = inv.getHeldItemSlot();
                ItemStack upgrader = inv.getItem(held);
                if (upgrader != null) {
                    if (upgrader.getAmount() <= 1) {
                        inv.setItem(held, null);
                    } else {
                        upgrader.setAmount(upgrader.getAmount() - 1);
                    }
                }
                plugin.unlockAchievement(player, Achievement.THE_NEXT_LEVEL);
            BlissGems.fx(player, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage(BlissGems.gradient("⬆ Gem upgraded to Tier 2!", "#ffd700", "#ff8c00"));
                return;
            }
            player.sendMessage(BlissGems.color("&cNo Tier 1 gem in your inventory to upgrade."));
        }

        // ------------------------------------------------------------ Egg Shooter

        @EventHandler
        public void onRightClick(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            if (idOf(event.getItem()) == Item.GEM_TRADER) {
                event.setCancelled(true);
                useGemTrader(event.getPlayer());
                return;
            }
            if (idOf(event.getItem()) == Item.REPAIR_KIT) {
                event.setCancelled(true);
                useRepairKit(event.getPlayer());
                return;
            }
            if (idOf(event.getItem()) == Item.ENERGY_BOTTLE) {
                event.setCancelled(true);
                useEnergyBottle(event.getPlayer());
                return;
            }
            if (idOf(event.getItem()) == Item.GEM_UPGRADER) {
                event.setCancelled(true);
                upgradeGem(event.getPlayer());
                return;
            }
            if (idOf(event.getItem()) != Item.EGG_SHOOTER) {
                return;
            }
            event.setCancelled(true);

            Player player = event.getPlayer();
            long now = System.currentTimeMillis();
            long until = eggCooldown.getOrDefault(player.getUniqueId(), 0L);
            if (now < until) {
                return;
            }
            eggCooldown.put(player.getUniqueId(), now + 1000L);

            Vector velocity = player.getEyeLocation().getDirection().multiply(2.5);
            Egg egg = player.launchProjectile(Egg.class, velocity);
            eggs.add(egg.getUniqueId());
            player.playSound(player.getLocation(), Sound.ENTITY_CROSSBOW_SHOOT, 1.0f, 1.2f);
        }

        /** Explosion effect with damage only: the explosion never breaks blocks. */
        @EventHandler
        public void onEggHit(ProjectileHitEvent event) {
            if (!eggs.remove(event.getEntity().getUniqueId())) {
                return;
            }
            Location loc = event.getEntity().getLocation();
            World world = loc.getWorld();
            world.spawnParticle(Particle.EXPLOSION, loc, 1);
            world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

            // 25% of a TNT minecart's damage. The value is set in config (custom-items.egg-shooter.tnt-cart-damage).
            double tntCartDamage = plugin.getConfig().getDouble("custom-items.egg-shooter.tnt-cart-damage", 40.0);
            double damage = tntCartDamage * 0.25;
            double radius = 3.0;

            Player shooter = event.getEntity().getShooter() instanceof Player sp ? sp : null;
            for (Entity entity : world.getNearbyEntities(loc, radius, radius, radius)) {
                if (!(entity instanceof LivingEntity living) || entity.equals(shooter)) {
                    continue;
                }
                double distance = entity.getLocation().distance(loc);
                double scaled = damage * (1.0 - distance / (radius + 1.0));
                if (scaled <= 0) {
                    continue;
                }
                if (shooter != null) {
                    living.damage(scaled, shooter);
                } else {
                    living.damage(scaled);
                }
            }
        }
    }

    // ---- CustomMenu ----
    /** /bliss menu: click an item to take a copy. */
    public static final class CustomMenu implements Listener {

        private static final MiniMessage MM = MiniMessage.miniMessage();

        private final BlissGems plugin;
        private final CustomItems items;

        public CustomMenu(BlissGems plugin, CustomItems items) {
            this.plugin = plugin;
            this.items = items;
        }

        private static final class MenuHolder implements InventoryHolder {
            private Inventory inventory;

            @Override
            public Inventory getInventory() {
                return inventory;
            }
        }

        public void open(Player player) {
            MenuHolder holder = new MenuHolder();
            Inventory inv = Bukkit.createInventory(holder, 54,
                    MM.deserialize("<gradient:#ffd700:#00ffff><b>BlissGems Items</b></gradient>"));
            holder.inventory = inv;

            CustomItems.Item[] order = CustomItems.Item.values();
            for (int i = 0; i < order.length && i < inv.getSize(); i++) {
                inv.setItem(i, items.create(order[i]));
            }
            BlissGems.fx(player, org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            player.openInventory(inv);
        }

        private static boolean isMenu(org.bukkit.inventory.InventoryView view) {
            return view.getTopInventory().getHolder() instanceof MenuHolder;
        }

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!isMenu(event.getView())) {
                return;
            }
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }
            ItemStack clicked = event.getCurrentItem();
            CustomItems.Item id = items.idOf(clicked);
            if (id == null) {
                return;
            }
            for (ItemStack leftover : player.getInventory().addItem(items.create(id)).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
            BlissGems.fx(player, org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            player.sendMessage(BlissGems.gradient("Took a copy of " + id.name().toLowerCase().replace('_', ' ') + ".", "#ffd700", "#00ffff"));
        }

        @EventHandler
        public void onDrag(InventoryDragEvent event) {
            if (isMenu(event.getView())) {
                event.setCancelled(true);
            }
        }
    }

    // ---- CustomWeapons ----
    /**
     * Ten custom weapons. Each one has an on-hit effect (with particles) and a unique right-click ability.
     * Hits only count when the weapon is in your main hand.
     */
    public static final class CustomWeapons implements Listener {

        private static final Set<Item> WEAPONS = EnumSet.of(
                Item.FROST_SCEPTER, Item.THUNDER_HAMMER, Item.STUN_BATON, Item.BLAST_CANNON, Item.LEVITATION_WAND,
                Item.VOID_LANCE, Item.EMBER_BLADE, Item.VENOM_DAGGER, Item.GALE_AXE, Item.SOUL_REAPER);

        /** Right-click cooldowns in seconds. */
        private static final Map<Item, Integer> COOLDOWN = new EnumMap<>(Map.ofEntries(
                Map.entry(Item.FROST_SCEPTER, 15), Map.entry(Item.THUNDER_HAMMER, 20),
                Map.entry(Item.STUN_BATON, 8), Map.entry(Item.BLAST_CANNON, 3),
                Map.entry(Item.LEVITATION_WAND, 12), Map.entry(Item.VOID_LANCE, 10),
                Map.entry(Item.EMBER_BLADE, 12), Map.entry(Item.VENOM_DAGGER, 18),
                Map.entry(Item.GALE_AXE, 10), Map.entry(Item.SOUL_REAPER, 20)));

        private final BlissGems plugin;
        private final CustomItems items;
        private final Map<UUID, Map<Item, Long>> cooldowns = new HashMap<>();
        private final Set<UUID> cannonShells = new HashSet<>();

        public CustomWeapons(BlissGems plugin, CustomItems items) {
            this.plugin = plugin;
            this.items = items;
        }

        // ------------------------------------------------------------ On-hit effects

        @EventHandler
        public void onWeaponHit(EntityDamageByEntityEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getDamager() instanceof Player attacker)) {
                return;
            }
            if (!(event.getEntity() instanceof LivingEntity target) || target.equals(attacker)) {
                return;
            }
            Item item = items.idOf(attacker.getInventory().getItemInMainHand());
            if (item == null || !WEAPONS.contains(item)) {
                return;
            }
            Location at = target.getLocation().add(0, 1, 0);
            BlissGems.fx(attacker, weaponSound(item), 1.0f, 1.0f);
            switch (item) {
                case FROST_SCEPTER -> {
                    // Freeze: the target can barely move for 3 seconds
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255, true, false));
                    spawn(Particle.SNOWFLAKE, at, 30);
                }
                case THUNDER_HAMMER -> {
                    // Lightning: a visible strike plus 4 extra damage
                    target.getWorld().strikeLightningEffect(target.getLocation());
                    event.setDamage(event.getDamage() + 4.0);
                    spawn(Particle.ELECTRIC_SPARK, at, 40);
                }
                case STUN_BATON -> {
                    // Stun: no movement for 1 second
                    target.setVelocity(new Vector(0, 0, 0));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 255, true, false));
                    spawn(Particle.CRIT, at, 25);
                }
                case BLAST_CANNON -> {
                    // Explosion: damages nearby things, never breaks blocks
                    explodeNoBlocks(target.getLocation(), 2.5, 6.0, attacker);
                }
                case LEVITATION_WAND -> {
                    // Levitation: lifted for 2 seconds
                    target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 0, true, false));
                    spawn(Particle.PORTAL, at, 30);
                }
                case VOID_LANCE -> {
                    // Wither: 3 seconds of wither
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0, true, false));
                    spawn(Particle.DRAGON_BREATH, at, 25);
                }
                case EMBER_BLADE -> {
                    target.setFireTicks(100);
                    spawn(Particle.FLAME, at, 30);
                }
                case VENOM_DAGGER -> {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1, true, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0, true, false));
                    spawn(Particle.WITCH, at, 20);
                }
                case GALE_AXE -> {
                    // Launch: knocked forward and up
                    Vector push = attacker.getLocation().getDirection().setY(0.4).normalize().multiply(1.3);
                    target.setVelocity(push);
                    spawn(Particle.CLOUD, at, 25);
                }
                case SOUL_REAPER -> {
                    // Weakness on the target, and heal you 1 heart
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0, true, false));
                    attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + 2.0));
                    spawn(Particle.SOUL, at, 25);
                }
                default -> {
                }
            }
        }

        // ------------------------------------------------------------ Right-click abilities

        @EventHandler
        public void onWeaponRightClick(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            Item item = items.idOf(event.getItem());
            if (item == null || !WEAPONS.contains(item)) {
                return;
            }
            event.setCancelled(true);
            Player p = event.getPlayer();
            if (!ready(p, item)) {
                return;
            }
            BlissGems.fx(p, weaponSound(item), 1.0f, 1.3f);
            switch (item) {
                case FROST_SCEPTER -> frostNova(p);
                case THUNDER_HAMMER -> thunderSlam(p);
                case STUN_BATON -> stunDash(p);
                case BLAST_CANNON -> fireShell(p);
                case LEVITATION_WAND -> selfLift(p);
                case VOID_LANCE -> voidStep(p);
                case EMBER_BLADE -> emberRing(p);
                case VENOM_DAGGER -> venomCloud(p);
                case GALE_AXE -> galeBurst(p);
                case SOUL_REAPER -> soulPull(p);
                default -> {
                }
            }
        }

        /** Freezes everything within 4 blocks for 3 seconds. */
        private void frostNova(Player p) {
            Location c = p.getLocation();
            for (Entity e : c.getWorld().getNearbyEntities(c, 4, 4, 4)) {
                if (e instanceof LivingEntity living && !e.equals(p)) {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255, true, false));
                }
            }
            ring(c.clone().add(0, 0.5, 0), Particle.SNOWFLAKE, 4, 24);
            c.getWorld().playSound(c, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.4f);
        }

        /** Three lightning strikes at random spots within 5 blocks. */
        private void thunderSlam(Player p) {
            World world = p.getWorld();
            Location c = p.getLocation();
            for (int i = 0; i < 3; i++) {
                double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
                double dist = 5 * Math.sqrt(ThreadLocalRandom.current().nextDouble());
                Location strike = c.clone().add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
                world.strikeLightningEffect(strike);
                for (Entity e : world.getNearbyEntities(strike, 2, 2, 2)) {
                    if (e instanceof LivingEntity living && !e.equals(p)) {
                        living.damage(5.0, p);
                    }
                }
            }
            ring(c.clone().add(0, 0.2, 0), Particle.ELECTRIC_SPARK, 5, 32);
        }

        /** Charge forward 4 blocks. Anyone you pass through takes 3 damage and is stunned. */
        private void stunDash(Player p) {
            Vector dir = p.getLocation().getDirection().setY(0.05).normalize().multiply(1.6);
            Set<UUID> hit = new HashSet<>();
            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks++ >= 5 || !p.isOnline() || p.isDead()) {
                        cancel();
                        return;
                    }
                    p.setVelocity(dir);
                    p.setFallDistance(0);
                    spawn(Particle.CRIT, p.getLocation().add(0, 1, 0), 8);
                    for (Entity e : p.getNearbyEntities(1.2, 1.2, 1.2)) {
                        if (e instanceof LivingEntity living && hit.add(living.getUniqueId())) {
                            living.damage(3.0, p);
                            living.setVelocity(new Vector(0, 0, 0));
                            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 255, true, false));
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        /** Fires an explosive arrow that blows up where it lands (no block damage). */
        private void fireShell(Player p) {
            Vector velocity = p.getEyeLocation().getDirection().multiply(3.0);
            Arrow shell = p.launchProjectile(Arrow.class, velocity);
            shell.setDamage(0.0);
            shell.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            cannonShells.add(shell.getUniqueId());
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.6f);
        }

        @EventHandler
        public void onShellHit(ProjectileHitEvent event) {
            if (!cannonShells.remove(event.getEntity().getUniqueId())) {
                return;
            }
            Player shooter = event.getEntity().getShooter() instanceof Player sp ? sp : null;
            if (shooter == null) {
                return;
            }
            explodeNoBlocks(event.getEntity().getLocation(), 3.0, 8.0, shooter);
        }

        /** Floats you up for 2 seconds. */
        private void selfLift(Player p) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 0, true, false));
            spawn(Particle.PORTAL, p.getLocation().add(0, 1, 0), 40);
        }

        /** Teleports you up to 8 blocks forward, to the spot you are looking at. */
        private void voidStep(Player p) {
            Block target = p.getTargetBlockExact(8);
            Location dest = target != null
                    ? target.getLocation().add(0.5, 1, 0.5)
                    : p.getLocation().add(p.getLocation().getDirection().multiply(8));
            dest.setYaw(p.getLocation().getYaw());
            dest.setPitch(p.getLocation().getPitch());
            spawn(Particle.PORTAL, p.getLocation().add(0, 1, 0), 40);
            p.teleport(dest);
            spawn(Particle.PORTAL, dest.clone().add(0, 1, 0), 40);
        }

        /** Sets everything within 3 blocks on fire and does 3 damage. */
        private void emberRing(Player p) {
            Location c = p.getLocation();
            for (Entity e : c.getWorld().getNearbyEntities(c, 3, 3, 3)) {
                if (e instanceof LivingEntity living && !e.equals(p)) {
                    living.setFireTicks(80);
                    living.damage(3.0, p);
                }
            }
            ring(c.clone().add(0, 0.5, 0), Particle.FLAME, 3, 24);
        }

        /** Blinds and poisons everything within 5 blocks. */
        private void venomCloud(Player p) {
            Location c = p.getLocation();
            for (Entity e : c.getWorld().getNearbyEntities(c, 5, 5, 5)) {
                if (e instanceof LivingEntity living && !e.equals(p)) {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, true, false));
                    living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0, true, false));
                }
            }
            c.getWorld().spawnParticle(Particle.SMOKE, c.clone().add(0, 1, 0), 80, 2.5, 1.0, 2.5, 0.02);
        }

        /** Blows everything within 6 blocks away from you. */
        private void galeBurst(Player p) {
            Location c = p.getLocation();
            for (Entity e : c.getWorld().getNearbyEntities(c, 6, 6, 6)) {
                if (!(e instanceof LivingEntity living) || e.equals(p)) {
                    continue;
                }
                Vector push = living.getLocation().toVector().subtract(c.toVector()).setY(0);
                if (push.lengthSquared() < 1e-6) {
                    push = new Vector(0, 0, 1);
                }
                living.setVelocity(push.normalize().multiply(1.5).setY(0.5));
            }
            ring(c.clone().add(0, 1, 0), Particle.CLOUD, 6, 36);
        }

        /** Drags enemies within 8 blocks that are in front of you toward you. */
        private void soulPull(Player p) {
            Location c = p.getLocation();
            Vector facing = c.getDirection().setY(0).normalize();
            for (Entity e : c.getWorld().getNearbyEntities(c, 8, 8, 8)) {
                if (!(e instanceof LivingEntity living) || e.equals(p)) {
                    continue;
                }
                Vector toCaster = c.toVector().subtract(living.getLocation().toVector()).setY(0);
                if (toCaster.lengthSquared() < 1e-6) {
                    continue;
                }
                Vector toTarget = toCaster.clone().multiply(-1).normalize();
                if (toTarget.dot(facing) < 0.3) {
                    continue; // only things in front of you
                }
                living.setVelocity(toCaster.normalize().multiply(0.9).setY(0.3));
                spawn(Particle.SOUL, living.getLocation().add(0, 1, 0), 15);
            }
        }

        // ------------------------------------------------------------ Helpers

        /** One sound per weapon, used for both hits and abilities. */
        private static org.bukkit.Sound weaponSound(Item item) {
            return switch (item) {
                case FROST_SCEPTER -> org.bukkit.Sound.BLOCK_GLASS_BREAK;
                case THUNDER_HAMMER -> org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
                case STUN_BATON -> org.bukkit.Sound.ENTITY_IRON_GOLEM_HURT;
                case BLAST_CANNON -> org.bukkit.Sound.ENTITY_GENERIC_EXPLODE;
                case LEVITATION_WAND -> org.bukkit.Sound.ENTITY_SHULKER_SHOOT;
                case VOID_LANCE -> org.bukkit.Sound.ENTITY_WITHER_SHOOT;
                case EMBER_BLADE -> org.bukkit.Sound.ITEM_FIRECHARGE_USE;
                case VENOM_DAGGER -> org.bukkit.Sound.ENTITY_SPIDER_AMBIENT;
                case GALE_AXE -> org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP;
                case SOUL_REAPER -> org.bukkit.Sound.ENTITY_ILLUSIONER_MIRROR_MOVE;
                default -> org.bukkit.Sound.UI_BUTTON_CLICK;
            };
        }

        /** Explosion damage in a radius, with falloff. No block damage. */
        private void explodeNoBlocks(Location loc, double radius, double maxDamage, Player source) {
            World world = loc.getWorld();
            world.spawnParticle(Particle.EXPLOSION, loc, 1);
            world.spawnParticle(Particle.SMOKE, loc, 30, 0.8, 0.8, 0.8, 0.05);
            world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            for (Entity e : world.getNearbyEntities(loc, radius, radius, radius)) {
                if (!(e instanceof LivingEntity living) || e.equals(source)) {
                    continue;
                }
                double distance = e.getLocation().distance(loc);
                double dmg = maxDamage * (1.0 - distance / (radius + 1.0));
                if (dmg > 0) {
                    living.damage(dmg, source);
                }
            }
        }

        private void spawn(Particle particle, Location at, int count) {
            at.getWorld().spawnParticle(particle, at, count, 0.3, 0.3, 0.3, 0.02);
        }

        /** A flat ring of particles around a point. */
        private void ring(Location center, Particle particle, double radius, int points) {
            World world = center.getWorld();
            for (int i = 0; i < points; i++) {
                double angle = (double) i / points * Math.PI * 2;
                Location at = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                world.spawnParticle(particle, at, 1, 0, 0, 0, 0);
            }
        }

        private boolean ready(Player p, Item item) {
            Map<Item, Long> mine = cooldowns.computeIfAbsent(p.getUniqueId(), k -> new EnumMap<>(Item.class));
            long until = mine.getOrDefault(item, 0L);
            long now = System.currentTimeMillis();
            if (now < until) {
                p.sendMessage(BlissGems.color("&cThat ability is recharging: " + ((until - now) / 1000 + 1) + "s"));
                return false;
            }
            mine.put(item, now + COOLDOWN.getOrDefault(item, 10) * 1000L);
            return true;
        }

        public void clearCooldowns(UUID playerId) {
            cooldowns.remove(playerId);
        }
    }

    // ---- EnergyManager ----
    /**
     * Player energy (0 to max). Stored in data.yml under "energy.<uuid>" and cached in memory.
     * Passives stop at or below ruined-threshold. Abilities stop at or below broken-threshold.
     */
    public static final class EnergyManager {

        private final BlissGems plugin;
        private final Map<UUID, Integer> cache = new HashMap<>();

        public EnergyManager(BlissGems plugin) {
            this.plugin = plugin;
        }

        public int max() {
            return plugin.getConfig().getInt("energy.max-energy", 10);
        }

        private int starting() {
            return plugin.getConfig().getInt("energy.starting-energy", 10);
        }

        public int getEnergy(UUID id) {
            Integer cached = cache.get(id);
            if (cached != null) {
                return cached;
            }
            int value = plugin.data().getInt("energy." + id, starting());
            cache.put(id, value);
            return value;
        }

        public int energyOf(Player player) {
            return getEnergy(player.getUniqueId());
        }

        public void setEnergy(UUID id, int value) {
            int clamped = Math.max(0, Math.min(max(), value));
            cache.put(id, clamped);
            var yml = plugin.data();
            yml.set("energy." + id, clamped);
            plugin.saveData(yml);
        }

        public void add(UUID id, int amount) {
            setEnergy(id, getEnergy(id) + amount);
        }

        public void remove(UUID id, int amount) {
            setEnergy(id, getEnergy(id) - amount);
        }

        public boolean passivesActive(UUID id) {
            return getEnergy(id) > plugin.getConfig().getInt("energy.ruined-threshold", 1);
        }

        public boolean abilitiesUsable(UUID id) {
            return getEnergy(id) > plugin.getConfig().getInt("energy.broken-threshold", 0);
        }

        /** Pristine, Ruined, or Broken. */
        public String state(UUID id) {
            int energy = getEnergy(id);
            if (energy <= plugin.getConfig().getInt("energy.broken-threshold", 0)) {
                return "Broken";
            }
            if (energy <= plugin.getConfig().getInt("energy.ruined-threshold", 1)) {
                return "Ruined";
            }
            return "Pristine";
        }
    }

    // ---- FireGem ----
    /** Fire gem: passives, Charged Fireball (T1), Campfire / Crisp / Meteor Shower (T2). */
    public static final class FireGem implements Listener {

        private static final Map<Material, Material> SMELT = Map.of(
                Material.IRON_ORE, Material.IRON_INGOT,
                Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT,
                Material.GOLD_ORE, Material.GOLD_INGOT,
                Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT,
                Material.COPPER_ORE, Material.COPPER_INGOT,
                Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT,
                Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP
        );

        private final BlissGems plugin;

        private final Map<UUID, BukkitTask> charging = new HashMap<>();
        private final Map<UUID, Integer> charge = new HashMap<>();
        private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

        /** Fireballs/meteors we launched, mapped to the direct-hit damage they deal. */
        private final Map<UUID, Double> projectileDamage = new HashMap<>();

        public FireGem(BlissGems plugin) {
            this.plugin = plugin;
        }

        public void shutdown() {
            for (BukkitTask task : charging.values()) {
                task.cancel();
            }
            charging.clear();
            charge.clear();
        }

        public void cancelFor(Player player) {
            BukkitTask task = charging.remove(player.getUniqueId());
            if (task != null) {
                task.cancel();
            }
            charge.remove(player.getUniqueId());
        }

        // ------------------------------------------------------------ Input

        /** Right-click with a fire gem in the main hand. */
        public void onRightClick(Player player, int tier, boolean sneaking) {
            if (tier >= 2 && sneaking) {
                campfire(player);
            } else {
                fireballCharge(player);
            }
        }

        /** F key with a fire gem in the main hand (Tier 2 only). */
        public void onSwap(Player player, int tier, boolean sneaking) {
            if (tier < 2) {
                return;
            }
            if (sneaking) {
                meteorShower(player);
            } else {
                crisp(player);
            }
        }

        // ------------------------------------------------------------ Charged Fireball

        private void fireballCharge(Player player) {
            UUID id = player.getUniqueId();
            if (charging.containsKey(id)) {
                fireballFire(player);
                return;
            }
            if (!ready(player, "fire-fireball")) {
                return;
            }

            int chargeTicks = Math.max(1, plugin.getConfig().getInt("abilities.fire-fireball.charge-duration-ticks", 200));
            charge.put(id, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.7f, 0.8f);
            player.sendMessage(BlissGems.color("&6🔥 Charging Fireball... right-click again to fire!"));

            BukkitTask task = new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (!player.isOnline() || player.isDead()
                            || plugin.gems().typeOf(player.getInventory().getItemInMainHand()) != GemType.FIRE) {
                        cancelFor(player);
                        return;
                    }
                    ticks++;
                    int pct = Math.min(100, ticks * 100 / chargeTicks);
                    charge.put(id, pct);
                    player.sendActionBar(plugin.bar("🔥 Fireball", pct));
                    if (ticks >= chargeTicks * 3 / 2) {
                        player.sendMessage(BlissGems.color("&7Charge expired."));
                        cancelFor(player);
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);

            charging.put(id, task);
        }

        private void fireballFire(Player player) {
            UUID id = player.getUniqueId();
            int pct = charge.getOrDefault(id, 0);
            cancelFor(player);

            if (pct < 10) {
                player.sendMessage(BlissGems.color("&cNot enough charge!"));
                return;
            }

            double yieldBase = plugin.getConfig().getDouble("abilities.fire-fireball.yield-base", 1.5);
            double yieldPerPercent = plugin.getConfig().getDouble("abilities.fire-fireball.yield-per-percent", 0.025);
            float yield = (float) (yieldBase + pct * yieldPerPercent);

            LargeFireball fireball = player.launchProjectile(LargeFireball.class);
            fireball.setYield(yield);
            fireball.setIsIncendiary(true);
            projectileDamage.put(fireball.getUniqueId(),
                    plugin.getConfig().getDouble("abilities.damage.fire-fireball", 9.0));

            startCooldown(player, "fire-fireball", cooldownSeconds("fire-fireball", 10));
            player.sendMessage(BlissGems.color("&6🔥 Fireball launched (" + pct + "% charge)"));
        }

        /** A fireball never hurts the player who shot it. */
        @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
        public void onOwnFireballDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Fireball fireball) || !(event.getEntity() instanceof Player victim)) {
                return;
            }
            if (fireball.getShooter() instanceof Player shooter && shooter.equals(victim)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onProjectileHit(ProjectileHitEvent event) {
            Double damage = projectileDamage.remove(event.getEntity().getUniqueId());
            if (damage == null) {
                return;
            }
            if (event.getHitEntity() instanceof LivingEntity target) {
                if (event.getEntity().getShooter() instanceof Player shooter) {
                    target.damage(damage, shooter);
                } else {
                    target.damage(damage);
                }
            }
        }

        // ------------------------------------------------------------ Campfire (Tier 2)

        private void campfire(Player player) {
            if (!ready(player, "fire-campfire")) {
                return;
            }
            Block block = player.getLocation().getBlock();
            if (!block.getType().isAir()) {
                player.sendMessage(BlissGems.color("&cNo room for a campfire here."));
                return;
            }

            int duration = plugin.getConfig().getInt("abilities.durations.fire-campfire", 15);
            double radius = plugin.getConfig().getDouble("abilities.fire-campfire.radius", 4.0);
            double healPerSecond = plugin.getConfig().getDouble("abilities.fire-campfire.heal-amount", 0.4);
            int foodPerSecond = plugin.getConfig().getInt("abilities.fire-campfire.food-amount", 1);
            int burnSeconds = plugin.getConfig().getInt("abilities.fire-campfire.burn-duration", 3);
            double damagePerSecond = plugin.getConfig().getDouble("abilities.damage.fire-campfire", 2.0);

            block.setType(Material.CAMPFIRE, false);
            startCooldown(player, "fire-campfire", cooldownSeconds("fire-campfire", 60));
            player.sendMessage(BlissGems.color("&6🥾 Cozy campfire lit!"));

            Location center = block.getLocation().add(0.5, 0.5, 0.5);
            new BukkitRunnable() {
                int elapsed = 0;

                @Override
                public void run() {
                    if (elapsed++ >= duration) {
                        if (block.getType() == Material.CAMPFIRE) {
                            block.setType(Material.AIR, false);
                        }
                        cancel();
                        return;
                    }
                    for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                        if (entity instanceof Player ally && plugin.isAlly(player, ally)) {
                            // Allies (players): heal, feed, and give Regeneration IV
                            ally.setHealth(Math.min(ally.getMaxHealth(), ally.getHealth() + healPerSecond * 2));
                            ally.setFoodLevel(Math.min(20, ally.getFoodLevel() + foodPerSecond));
                            ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 3, true, false));
                        } else if (entity instanceof LivingEntity enemy) {
                            enemy.damage(damagePerSecond, player);
                            enemy.setFireTicks(burnSeconds * 20);
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }

        // ------------------------------------------------------------ Crisp (Tier 2)

        private void crisp(Player player) {
            if (!ready(player, "fire-crisp")) {
                return;
            }
            int radius = (int) plugin.getConfig().getDouble("abilities.fire-crisp.radius", 10);
            int duration = plugin.getConfig().getInt("abilities.durations.fire-crisp", 15);
            double radiusSq = (double) radius * radius;

            Location center = player.getLocation();
            int cx = center.getBlockX();
            int cy = center.getBlockY();
            int cz = center.getBlockZ();

            List<Block> evaporated = new ArrayList<>();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx * dx + dy * dy + dz * dz > radiusSq) {
                            continue;
                        }
                        Block block = center.getWorld().getBlockAt(cx + dx, cy + dy, cz + dz);
                        if (block.getType() == Material.WATER) {
                            block.setType(Material.AIR, false);
                            evaporated.add(block);
                        }
                    }
                }
            }

            startCooldown(player, "fire-crisp", cooldownSeconds("fire-crisp", 90));
            player.sendMessage(BlissGems.color("&6🧊 Crisp! Water evaporated for " + duration + "s"));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Block block : evaporated) {
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.WATER, false);
                    }
                }
            }, duration * 20L);
        }

        // ------------------------------------------------------------ Meteor Shower (Tier 2)

        private void meteorShower(Player player) {
            if (!ready(player, "fire-meteor-shower")) {
                return;
            }
            Block target = player.getTargetBlockExact(50);
            Location center = target != null
                    ? target.getLocation().add(0.5, 1, 0.5)
                    : player.getLocation().add(player.getLocation().getDirection().multiply(15));

            double radius = plugin.getConfig().getDouble("abilities.fire-meteor-shower.radius", 8.0);
            double meteorDamage = plugin.getConfig().getDouble("abilities.damage.fire-meteor-shower", 8.0);
            int duration = plugin.getConfig().getInt("abilities.durations.fire-meteor-shower", 8);

            startCooldown(player, "fire-meteor-shower", cooldownSeconds("fire-meteor-shower", 120));
            player.sendMessage(BlissGems.color("&c☄ Meteor shower incoming!"));

            new BukkitRunnable() {
                int elapsed = 0;

                @Override
                public void run() {
                    if (elapsed++ >= duration * 2) {
                        cancel();
                        return;
                    }
                    for (int i = 0; i < 2; i++) {
                        double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
                        double dist = radius * Math.sqrt(ThreadLocalRandom.current().nextDouble());
                        Location spawn = center.clone().add(Math.cos(angle) * dist, 25, Math.sin(angle) * dist);
                        LargeFireball meteor = center.getWorld().spawn(spawn, LargeFireball.class, fb -> {
                            fb.setDirection(new Vector(0, -1, 0));
                            fb.setYield(1.5f);
                            fb.setIsIncendiary(true);
                            fb.setShooter(player);
                        });
                        projectileDamage.put(meteor.getUniqueId(), meteorDamage);
                    }
                }
            }.runTaskTimer(plugin, 0L, 10L);
        }

        // ------------------------------------------------------------ Passives

        /** Called every passives.update-interval ticks. Applies passives for fire gems in the offhand. */
        public void tickPassives() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (plugin.gems().typeOf(offhand) != GemType.FIRE || !plugin.energy().passivesActive(player.getUniqueId())) {
                    continue;
                }
                int tier = plugin.gems().tierOf(offhand);
                if (plugin.getConfig().getBoolean("passives.fire.tier" + tier + ".fire-resistance", true)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 120, 0, true, false));
                }
            }
        }

        /** Flame & Fire Aspect: melee hits set the target on fire while a fire gem is in the offhand. */
        @EventHandler
        public void onMeleeHit(EntityDamageByEntityEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK) {
                return;
            }
            if (!(event.getDamager() instanceof Player attacker)) {
                return;
            }
            if (plugin.gems().typeOf(attacker.getInventory().getItemInOffHand()) != GemType.FIRE) {
                return;
            }
            if (event.getEntity() instanceof LivingEntity target) {
                target.setFireTicks(Math.max(target.getFireTicks(), 80));
            }
        }

        /** Auto-smelt: ores broken with a fire gem in the offhand drop their smelted item. */
        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (plugin.gems().typeOf(offhand) != GemType.FIRE || !plugin.energy().passivesActive(player.getUniqueId())) {
                return;
            }
            int tier = plugin.gems().tierOf(offhand);
            if (!plugin.getConfig().getBoolean("passives.fire.tier" + tier + ".auto-smelt", true)) {
                return;
            }
            Material smelted = SMELT.get(event.getBlock().getType());
            if (smelted == null) {
                return;
            }
            event.setDropItems(false);
            event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(smelted));
        }

        // ------------------------------------------------------------ Cooldown helpers

        /** Used by /bliss clearcds. */
        public void clearCooldowns(UUID playerId) {
            cooldowns.remove(playerId);
        }

        private boolean ready(Player player, String ability) {
            Map<String, Long> mine = cooldowns.getOrDefault(player.getUniqueId(), Collections.emptyMap());
            long until = mine.getOrDefault(ability, 0L);
            long now = System.currentTimeMillis();
            if (now < until) {
                player.sendMessage(BlissGems.color("&cThat ability is recharging: " + ((until - now) / 1000 + 1) + "s"));
                return false;
            }
            return true;
        }

        private void startCooldown(Player player, String ability, int seconds) {
            cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(ability, System.currentTimeMillis() + seconds * 1000L);
        }

        private int cooldownSeconds(String ability, int fallback) {
            return plugin.getConfig().getInt("abilities.cooldowns." + ability, fallback);
        }
    }

    // ---- FluxGem ----
    /**
     * Flux gem Tier 2 abilities.
     * - Shift + right-click: Ground (freezes nearby enemies in place).
     * - F: Flashbang (blindness and nausea to nearby enemies).
     * - Shift + F: Kinetic Burst (radial knockback with a sonic boom).
     * The Flux Beam stays on right-click.
     */
    public static final class FluxGem implements Listener {

        private static final double RADIUS = 6.0;

        private final BlissGems plugin;
        private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

        public FluxGem(BlissGems plugin) {
            this.plugin = plugin;
        }

        /** Called every passives.update-interval ticks. Removes Slowness and Weakness from flux holders. */
        public void tickPassives() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (plugin.gems().typeOf(offhand) != GemType.FLUX || !plugin.energy().passivesActive(player.getUniqueId())) {
                    continue;
                }
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                player.removePotionEffect(PotionEffectType.WEAKNESS);
            }
        }

        /** Arrows from a flux holder can shock: extra damage and a short stun. */
        @EventHandler(priority = org.bukkit.event.EventPriority.HIGH, ignoreCancelled = true)
        public void onShockingArrow(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Arrow arrow) || !(arrow.getShooter() instanceof Player shooter)) {
                return;
            }
            if (!(event.getEntity() instanceof LivingEntity victim) || victim.equals(shooter)) {
                return;
            }
            ItemStack offhand = shooter.getInventory().getItemInOffHand();
            if (plugin.gems().typeOf(offhand) != GemType.FLUX || !plugin.energy().passivesActive(shooter.getUniqueId())) {
                return;
            }
            int tier = plugin.gems().tierOf(offhand);
            double chance = plugin.getConfig().getDouble("passives.flux.tier" + tier + ".shocking-chance", 0.15);
            if (ThreadLocalRandom.current().nextDouble() >= chance) {
                return;
            }
            double extra = plugin.getConfig().getDouble("passives.flux.tier" + tier + ".shocking-arrow-damage", tier >= 2 ? 3.0 : 2.0);
            event.setDamage(event.getDamage() + extra);

            int stun = plugin.getConfig().getInt("abilities.durations.flux-shocking-stun", 1);
            victim.setVelocity(new Vector(0, 0, 0));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stun * 20, 255, true, false));
            victim.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, victim.getLocation().add(0, 1, 0), 40, 0.4, 0.6, 0.4, 0.05);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.6f, 1.6f);
            shooter.sendMessage(BlissGems.color("&b&oShocking Arrow hit!"));
        }

        /** Tier 2: right-click a copper block with the flux gem in your offhand to teleport onto it. */
        @EventHandler
        public void onConduction(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.OFF_HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            if (event.getClickedBlock() == null || !event.getClickedBlock().getType().name().contains("COPPER")) {
                return;
            }
            Player player = event.getPlayer();
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (plugin.gems().typeOf(offhand) != GemType.FLUX || plugin.gems().tierOf(offhand) < 2) {
                return;
            }
            if (!plugin.energy().passivesActive(player.getUniqueId())) {
                return;
            }
            event.setCancelled(true);
            if (!ready(player, "flux-conduction", 3)) {
                return;
            }
            Location dest = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);
            if (dest.getBlock().getType().isSolid()) {
                player.sendMessage(BlissGems.color("&cNo room to teleport there."));
                return;
            }
            dest.setYaw(player.getLocation().getYaw());
            dest.setPitch(player.getLocation().getPitch());
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 40, 0.4, 0.6, 0.4, 0.2);
            double travelled = player.getLocation().distance(dest);
            double total = plugin.addProgress(player.getUniqueId(), "conduction", travelled);
            if (total >= 1914) {
                plugin.unlockAchievement(player, Achievement.ZIP_AWAY);
            }
            player.teleport(dest);
            player.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
        }

        /** Flux holders don't get hungry. */
        @EventHandler
        public void onExhaustion(EntityExhaustionEvent event) {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }
            if (plugin.gems().typeOf(player.getInventory().getItemInOffHand()) == GemType.FLUX
                    && plugin.energy().passivesActive(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }

        /** Shift + right-click at Tier 2. */
        public void onShiftRightClick(Player player) {
            ground(player);
        }

        /** F at Tier 2. Shift + F is Kinetic Burst. */
        public void onSwap(Player player, boolean sneaking) {
            if (sneaking) {
                kineticBurst(player);
            } else {
                flashbang(player);
            }
        }

        // ------------------------------------------------------------ Ground

        /** Freezes every enemy within 6 blocks in place for 3 seconds. */
        private void ground(Player caster) {
            if (!ready(caster, "flux-ground", 45)) {
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.flux-ground-freeze", 3);
            Location c = caster.getLocation();
            for (Entity entity : c.getWorld().getNearbyEntities(c, RADIUS, RADIUS, RADIUS)) {
                if (entity instanceof LivingEntity living && !entity.equals(caster)) {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, seconds * 20, 255, true, false));
                    living.getWorld().spawnParticle(Particle.CLOUD, living.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.02);
                }
            }
            c.getWorld().playSound(c, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.6f);
            caster.sendMessage(BlissGems.color("&b🌀 Ground! Enemies frozen for " + seconds + "s."));
        }

        // ------------------------------------------------------------ Flashbang

        /** Blindness and nausea to every enemy within 6 blocks. */
        private void flashbang(Player caster) {
            if (!ready(caster, "flux-flashbang", 45)) {
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.flux-flashbang", 5);
            Location c = caster.getLocation().add(0, 1, 0);
            for (Entity entity : c.getWorld().getNearbyEntities(c, RADIUS, RADIUS, RADIUS)) {
                if (entity instanceof LivingEntity living && !entity.equals(caster)) {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, seconds * 20, 0, true, false));
                    living.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, seconds * 20, 0, true, false));
                }
            }
            c.getWorld().spawnParticle(Particle.FLASH, c, 1);
            c.getWorld().playSound(c, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.8f);
            caster.sendMessage(BlissGems.color("&b💥 Flashbang!"));
        }

        // ------------------------------------------------------------ Kinetic Burst

        /** Pushes every enemy within 6 blocks away from you, with a sonic boom. */
        private void kineticBurst(Player caster) {
            if (!ready(caster, "flux-kinetic-burst", 30)) {
                return;
            }
            Location c = caster.getLocation();
            for (Entity entity : c.getWorld().getNearbyEntities(c, RADIUS, RADIUS, RADIUS)) {
                if (!(entity instanceof LivingEntity living) || entity.equals(caster)) {
                    continue;
                }
                Vector push = living.getLocation().toVector().subtract(c.toVector()).setY(0);
                if (push.lengthSquared() < 1e-6) {
                    push = new Vector(0, 0, 1);
                }
                living.setVelocity(push.normalize().multiply(1.2).setY(0.5));
            }
            c.getWorld().spawnParticle(Particle.SONIC_BOOM, c.clone().add(0, 1, 0), 1);
            c.getWorld().playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
            caster.sendMessage(BlissGems.color("&b⚡ Kinetic Burst!"));
        }

        // ------------------------------------------------------------ Cooldowns

        private boolean ready(Player player, String ability, int fallbackSeconds) {
            Map<String, Long> mine = cooldowns.getOrDefault(player.getUniqueId(), Collections.emptyMap());
            long until = mine.getOrDefault(ability, 0L);
            long now = System.currentTimeMillis();
            if (now < until) {
                player.sendMessage(BlissGems.color("&cThat ability is recharging: " + ((until - now) / 1000 + 1) + "s"));
                return false;
            }
            int seconds = plugin.getConfig().getInt("abilities.cooldowns." + ability, fallbackSeconds);
            cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(ability, now + seconds * 1000L);
            return true;
        }

        public void clearCooldowns(UUID playerId) {
            cooldowns.remove(playerId);
        }
    }

    // ---- GemItems ----
    /** Creates and identifies all BlissGems items. */
    public static final class GemItems {

        public enum GemType { FIRE, FLUX, LIFE, SPEED, PUFF, STRENGTH, WEALTH, ASTRA }

        private final NamespacedKey typeKey;
        private final NamespacedKey tierKey;

        public GemItems(BlissGems plugin) {
            this.typeKey = new NamespacedKey(plugin, "gem_type");
            this.tierKey = new NamespacedKey(plugin, "gem_tier");
        }

        private record Def(Material material, int modelData, String name, List<String> lore) {}

        private static final Def FIRE_T1 = new Def(Material.ECHO_SHARD, 1002, "§d§lFIRE GEM", List.of(
                "§f§lMANIPULATE FIRE",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Fire Resistance",
                "§7- Flame & Fire Aspect on weapons",
                "§7- Auto-smelt ores",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l🔥 CHARGED FIREBALL",
                "§7Right-click to charge, right-click again to fire",
                "",
                "§8Upgrade to Tier 2 for Campfire,",
                "§8Crisp & Meteor Shower!"));

        private static final Def FIRE_T2 = new Def(Material.ECHO_SHARD, 2002, "§d§lFIRE GEM", List.of(
                "§f§lMANIPULATE FIRE",
                "§a§o(Pristine)",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Fire Resistance",
                "§7- Flame & Fire Aspect on weapons",
                "§7- Auto-smelt ores",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l🔥 CHARGED FIREBALL",
                "§7Right-click to charge, right-click again to fire",
                "§b§l🥾 COZY CAMPFIRE §7(Shift + Right-click)",
                "§7Spawns a campfire healing allies and burning enemies",
                "§b§l🧊 CRISP §7(F)",
                "§7Evaporate water around you",
                "§b§l🧨 METEOR SHOWER §7(Shift + F)",
                "§7Rain fire on a target area"));

        private static final Def FLUX_T1 = new Def(Material.ECHO_SHARD, 1003, "§d§lFLUX GEM", List.of(
                "§f§lWITH GREAT POWER COMES GREAT RESPONSIBILITY",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Immune to Weakness, Slowness & Hunger",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l✠ FLUX BEAM",
                "§7Right-click to charge, right-click again to fire",
                "§7Beam deals more damage the longer it is charged"));

        private static final Def FLUX_T2 = new Def(Material.ECHO_SHARD, 2003, "§d§lFLUX GEM", List.of(
                "§f§lWITH GREAT POWER COMES GREAT RESPONSIBILITY",
                "§a§o(Pristine)",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Immune to Weakness, Slowness & Hunger",
                "",
                "§b🌟 §b§lABILITY",
                "§7- Conduction (teleport to copper blocks)",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l✠ FLUX BEAM",
                "§7Chargeable beam dealing massive armor damage",
                "",
                "§b§l🌀 GROUND",
                "§7Freeze enemies in place",
                "",
                "§b§l💥 FLASHBANG",
                "§7Blindness and Nausea to enemies in radius",
                "",
                "§b§l⚡ KINETIC BURST",
                "§7Radial knockback with sonic boom"));

        private static final Def LIFE_T1 = new Def(Material.ECHO_SHARD, 1004, "§d§lLIFE GEM", List.of(
                "§f§lCONTROL THE BALANCE OF LIFE",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Wither immunity",
                "§7- Continuous healing",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l💘 HEART DRAINER",
                "§7Siphon health from your enemies",
                "",
                "§8Upgrade to Tier 2 for Life Circle,",
                "§8Vitality Vortex & Heart Lock!"));

        private static final Def LIFE_T2 = new Def(Material.ECHO_SHARD, 2004, "§d§lLIFE GEM", List.of(
                "§f§lCONTROL THE BALANCE OF LIFE",
                "§a§o(Pristine)",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Wither immunity",
                "§7- Continuous healing",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l💘 HEART DRAINER",
                "§7Siphon health from your enemies",
                "",
                "§b§l✨ CIRCLE OF LIFE",
                "§7Zone that decreases enemy max HP",
                "§7and increases yours and allies' HP",
                "",
                "§b§l💫 VITALITY VORTEX",
                "§7Grants effects based on your surroundings",
                "",
                "§b§l🔒 HEART LOCK",
                "§7Cap enemy max HP at their current HP"));

        private static final Def SPEED_T1 = new Def(Material.ECHO_SHARD, 1006, "§d§lSPEED GEM", List.of(
                "§f§lWATCH THE WORLD TURN INTO A BLUR",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Speed & Dolphin's Grace",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l⚡ BLUR",
                "§7Summons successive lightning strikes",
                "§7dealing damage and knockback",
                "",
                "§8Upgrade to Tier 2 for Speed Storm",
                "§8& Terminal Velocity!"));

        private static final Def SPEED_T2 = new Def(Material.ECHO_SHARD, 2006, "§d§lSPEED GEM", List.of(
                "§f§lWATCH THE WORLD TURN INTO A BLUR",
                "§a§o(Pristine)",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Speed & Dolphin's Grace",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l⚡ BLUR",
                "§7Summons successive lightning strikes",
                "§7dealing damage and knockback",
                "",
                "§b§l🌩 SPEED STORM",
                "§7Freezes enemies while granting allies",
                "§7Speed and Haste",
                "",
                "§b§l💨 TERMINAL VELOCITY",
                "§7Speed III + Haste II for a short duration"));

        private static final Def PUFF_T1 = new Def(Material.ECHO_SHARD, 1005, "§d§lPUFF GEM", List.of(
                "§f§lBE THE BIGGEST BIRD",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- No fall damage",
                "",
                "§b🌟 §b§lABILITY",
                "§7- Double Jump",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l☁ DASH",
                "§7Dashes in the direction you're looking",
                "§7Deals damage if passing through enemies",
                "",
                "§8Upgrade to Tier 2 for Breezy Bash!"));

        private static final Def PUFF_T2 = new Def(Material.ECHO_SHARD, 2005, "§d§lPUFF GEM", List.of(
                "§f§lBE THE BIGGEST BIRD",
                "§a§o(Pristine)",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- No fall damage",
                "",
                "§b🌟 §b§lABILITY",
                "§7- Double Jump",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l☁ DASH",
                "§7Dashes in the direction you're looking",
                "§7Deals damage if passing through enemies",
                "",
                "§b§l⏫ BREEZY BASH",
                "§7Launch an enemy skyward then slam them",
                "",
                "§b§l🌪 GROUP BREEZY BASH",
                "§7Send all nearby enemies flying away"));

        private static final Def ASTRA_T1 = new Def(Material.ECHO_SHARD, 1001, "§d§lASTRA GEM", List.of(
                "§f§lMANAGE THE TIDES OF THE COSMOS",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Soul Capture & Soul Healing",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l🔪 ASTRAL DAGGERS",
                "§7Fire 3 phantom daggers that deal damage",
                "",
                "§8Upgrade to Tier 2 for Astral Projection,",
                "§8Dimensional Drift & Void!"));

        private static final Def ASTRA_T2 = new Def(Material.ECHO_SHARD, 2001, "§d§lASTRA GEM", List.of(
                "§f§lMANAGE THE TIDES OF THE COSMOS",
                "§a§o(Pristine)",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Soul Capture & Soul Healing",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l🔪 ASTRAL DAGGERS",
                "§7Fire 3 phantom daggers that deal damage",
                "",
                "§b§l👻 ASTRAL PROJECTION",
                "§7Scout in spectator mode",
                "§7Sub-abilities: §dSpook §7& §dTag",
                "",
                "§b§l🌀 DIMENSIONAL DRIFT",
                "§7Ride an invisible horse while invisible",
                "",
                "§b§l🕳 DIMENSIONAL VOID",
                "§7Nullify enemy gem abilities in radius"));

        private static final Def STRENGTH_T1 = new Def(Material.ECHO_SHARD, 1007, "§d§lSTRENGTH GEM", List.of(
                "§f§lHAVE THE STRENGTH OF AN ARMY",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Strength I",
                "§7- Sharpness II on weapons",
                "§7- Bloodthorns (more damage at low HP)",
                "",
                "§8No active abilities at Tier 1",
                "§8Upgrade to Tier 2 for Nullify, Frailer",
                "§8& Shadow Stalker!"));

        private static final Def STRENGTH_T2 = new Def(Material.ECHO_SHARD, 2007, "§d§lSTRENGTH GEM", List.of(
                "§f§lHAVE THE STRENGTH OF AN ARMY",
                "§a§o(Pristine)",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Strength I",
                "§7- Sharpness V on weapons",
                "§7- Bloodthorns (more damage at low HP)",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l🚫 NULLIFY",
                "§7Strip all potion effects from a target",
                "",
                "§b§l⚔ FRAILER",
                "§7Clear target's potions, apply Weakness I",
                "§7(20s) and Wither I (40s)",
                "",
                "§b§l🔍 SHADOW STALKER",
                "§7Consume a player head or owned item",
                "§7to track a player's location"));

        private static final Def WEALTH_T1 = new Def(Material.ECHO_SHARD, 1008, "§d§lWEALTH GEM", List.of(
                "§f§lFUEL AN EMPIRE",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Luck & Hero of the Village",
                "§7- Mending, Fortune & Looting on tools",
                "§7- Durability Chip (extra armor damage)",
                "",
                "§8No active abilities at Tier 1",
                "§8Upgrade to Tier 2 for Pockets,",
                "§8Unfortunate & more!"));

        private static final Def WEALTH_T2 = new Def(Material.ECHO_SHARD, 2008, "§d§lWEALTH GEM", List.of(
                "§f§lFUEL AN EMPIRE",
                "§a§o(Pristine)",
                "",
                "§a🌟 §a§lPASSIVES",
                "§7- Luck & Hero of the Village",
                "§7- Mending, Fortune & Looting on tools",
                "§7- Durability Chip & Armor Mend",
                "",
                "§d🌟 §d§lPOWERS",
                "§b§l💸 UNFORTUNATE",
                "§7Chance to disable enemy actions",
                "",
                "§b§l🎒 POCKETS",
                "§79 extra inventory slots (/bliss pockets)",
                "",
                "§b§l🔒 ITEM LOCK",
                "§7Lock an enemy's held item temporarily",
                "",
                "§b§l✨ AMPLIFICATION",
                "§7Boost all enchantments for 45s",
                "",
                "§b§l🍀 RICH RUSH",
                "§7Increased drop rates for ~3 min"));

        private static final Def UPGRADER = new Def(Material.ENCHANTED_BOOK, 3001, "§6§l§nGem Upgrader", List.of(
                "§7Right Click to upgrade any Tier 1 gem to Tier 2",
                "",
                "§8Works for ALL gem types:",
                "§5Astra §8• §cFire §8• §bFlux §8• §dLife",
                "§fPuff §8• §aSpeed §8• §6Strength §8• §eWealth"));

        public ItemStack create(GemType type, int tier) {
            boolean t2 = tier >= 2;
            Def def = switch (type) {
                case FIRE -> t2 ? FIRE_T2 : FIRE_T1;
                case FLUX -> t2 ? FLUX_T2 : FLUX_T1;
                case LIFE -> t2 ? LIFE_T2 : LIFE_T1;
                case SPEED -> t2 ? SPEED_T2 : SPEED_T1;
                case PUFF -> t2 ? PUFF_T2 : PUFF_T1;
                case ASTRA -> t2 ? ASTRA_T2 : ASTRA_T1;
                case STRENGTH -> t2 ? STRENGTH_T2 : STRENGTH_T1;
                case WEALTH -> t2 ? WEALTH_T2 : WEALTH_T1;
            };
            ItemStack item = new ItemStack(def.material());
            ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(def.modelData());
            meta.setDisplayName(def.name());
            meta.setLore(def.lore());
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());
            meta.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, t2 ? 2 : 1);
            item.setItemMeta(meta);
            return item;
        }

        public ItemStack createUpgrader() {
            ItemStack item = new ItemStack(UPGRADER.material());
            ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(UPGRADER.modelData());
            meta.setDisplayName(UPGRADER.name());
            meta.setLore(UPGRADER.lore());
            item.setItemMeta(meta);
            return item;
        }

        /** Returns the gem type, or null if the item is not a BlissGems gem. */
        public GemType typeOf(ItemStack item) {
            if (item == null || !item.hasItemMeta()) {
                return null;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return null;
            }
            String value = meta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
            if (value == null) {
                return null;
            }
            try {
                return GemType.valueOf(value);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        /** Returns 1 or 2 for a gem, or 0 if the item is not a gem. */
        public int tierOf(ItemStack item) {
            if (typeOf(item) == null) {
                return 0;
            }
            Integer tier = item.getItemMeta().getPersistentDataContainer().get(tierKey, PersistentDataType.INTEGER);
            return tier == null ? 1 : tier;
        }
    }

    // ---- GemRitual ----
    /**
     * The join ritual and SMP start. A new player gets one random gem on their first join,
     * but only after an admin runs /bliss smp start.
     */
    public static final class GemRitual implements Listener {

        private final BlissGems plugin;

        public GemRitual(BlissGems plugin) {
            this.plugin = plugin;
        }

        public boolean smpStarted() {
            return plugin.data().getBoolean("smp.started", false);
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            if (plugin.data().getBoolean("first-gem." + player.getUniqueId(), false) || hasGem(player)) {
                return;
            }
            if (!smpStarted()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.sendMessage(BlissGems.color("&7The SMP hasn't started yet."));
                    }
                }, 40L);
                return;
            }

            GemType type = randomType();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage("");
                    player.sendMessage(BlissGems.gradient("WELCOME TO BLISSGEMS!", "#ff00ff", "#00ffff"));
                    BlissGems.fx(player, org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.8f);
                    player.sendMessage(BlissGems.color("&7&oThe ancient gem ritual begins..."));
                    player.sendMessage(BlissGems.color("&7&oYour destiny is being forged..."));
                    player.sendMessage("");
                }
            }, 40L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                giveGem(player, type);
                markFirstGem(player);
                BlissGems.fx(player, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                player.sendMessage(BlissGems.gradient("» Your gem has been chosen: " + type.name() + " «", "#ff00ff", "#00ffff"));
            }, 100L);
        }

        /** /bliss smp start: turns the SMP on and gives a gem to every online player who doesn't have one yet. */
        public void distributeGems(CommandSender sender) {
            YamlConfiguration yml = plugin.data();
            yml.set("smp.started", true);
            plugin.saveData(yml);

            int given = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (hasGem(player) || plugin.data().getBoolean("first-gem." + player.getUniqueId(), false)) {
                    continue;
                }
                GemType type = randomType();
                giveGem(player, type);
                markFirstGem(player);
                BlissGems.fx(player, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                player.sendMessage(BlissGems.gradient("» Your gem has been chosen: " + type.name() + " «", "#ff00ff", "#00ffff"));
                given++;
            }
            sender.sendMessage(BlissGems.gradient("SMP started. Gave gems to " + given + " player(s).", "#11998e", "#38ef7d"));
        }

        private GemType randomType() {
            GemType[] types = GemType.values();
            return types[ThreadLocalRandom.current().nextInt(types.length)];
        }

        private boolean hasGem(Player player) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (plugin.gems().typeOf(item) != null) {
                    return true;
                }
            }
            return false;
        }

        private void giveGem(Player player, GemType type) {
            for (ItemStack leftover : player.getInventory().addItem(plugin.gems().create(type, 1)).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }

        private void markFirstGem(Player player) {
            YamlConfiguration yml = plugin.data();
            yml.set("first-gem." + player.getUniqueId(), true);
            plugin.saveData(yml);
        }
    }

    // ---- LifeGem ----
    /** Life gem: wither immunity, continuous healing, undead damage bonus, Heart Drainer (T1). */
    public static final class LifeGem implements Listener {

        private final BlissGems plugin;
        private final Map<UUID, BukkitTask> drains = new HashMap<>();
        private final Map<UUID, Integer> healTicks = new HashMap<>();
        private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

        public LifeGem(BlissGems plugin) {
            this.plugin = plugin;
        }

        public void shutdown() {
            for (BukkitTask task : drains.values()) {
                task.cancel();
            }
            drains.clear();
            healTicks.clear();
        }

        public void cancelFor(Player player) {
            BukkitTask task = drains.remove(player.getUniqueId());
            if (task != null) {
                task.cancel();
            }
        }

        // ------------------------------------------------------------ Input

        /** Right-click: Heart Drainer. Shift + right-click at Tier 2: Circle of Life. */
        public void onRightClick(Player player, int tier, boolean sneaking) {
            if (tier >= 2 && sneaking) {
                circleOfLife(player);
            } else {
                heartDrainer(player);
            }
        }

        /** F at Tier 2: Vitality Vortex. Shift + F at Tier 2: Heart Lock. */
        public void onSwap(Player player, int tier, boolean sneaking) {
            if (tier < 2) {
                return;
            }
            if (sneaking) {
                heartLock(player);
            } else {
                vitalityVortex(player);
            }
        }

        // ------------------------------------------------------------ Circle of Life (T2)

        /** For 10 seconds: players nearby gain max health, other living things nearby lose max health. */
        private void circleOfLife(Player caster) {
            if (!ready(caster, "life-circle-of-life")) {
                return;
            }
            double radius = plugin.getConfig().getDouble("abilities.life-circle.radius", 8.0);
            double gain = plugin.getConfig().getDouble("abilities.life-circle.max-health-increase", 4.0);
            double loss = plugin.getConfig().getDouble("abilities.life-circle.max-health-decrease", 4.0);
            int seconds = plugin.getConfig().getInt("abilities.durations.life-circle", 10);
            startCooldown(caster, "life-circle-of-life", cooldownSeconds("life-circle-of-life", 60));
            caster.sendMessage(BlissGems.color("&d✨ Circle of Life!"));

            Location center = caster.getLocation();
            for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (!(entity instanceof LivingEntity living)) {
                    continue;
                }
                if (entity instanceof Player p && plugin.isAlly(caster, p)) {
                    timedMaxHealth(living, gain, seconds);
                } else {
                    timedMaxHealth(living, -loss, seconds);
                }
            }
        }

        // ------------------------------------------------------------ Vitality Vortex (T2)

        /**
         * Pulses every 3 seconds for 15 seconds. Players nearby get Regeneration I.
         * Mobs nearby get Slowness I. If 3 or more mobs are nearby, you get Strength I.
         */
        private void vitalityVortex(Player caster) {
            if (!ready(caster, "life-vitality-vortex")) {
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.life-vitality-vortex", 15);
            double radius = 8.0;
            int pulses = Math.max(1, seconds / 3);
            startCooldown(caster, "life-vitality-vortex", cooldownSeconds("life-vitality-vortex", 45));
            caster.sendMessage(BlissGems.color("&d💫 Vitality Vortex!"));

            new BukkitRunnable() {
                int pulse = 0;

                @Override
                public void run() {
                    if (pulse++ >= pulses || !caster.isOnline() || caster.isDead()) {
                        cancel();
                        return;
                    }
                    Location center = caster.getLocation();
                    int mobs = 0;
                    for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                        if (entity.equals(caster)) {
                            continue;
                        }
                        if (entity instanceof Player ally && plugin.isAlly(caster, ally)) {
                            ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, false));
                        } else if (entity instanceof LivingEntity mob && !(entity instanceof Player)) {
                            mobs++;
                            mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0, true, false));
                        }
                    }
                    caster.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, false));
                    if (mobs >= 3) {
                        caster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, true, false));
                    }
                }
            }.runTaskTimer(plugin, 0L, 60L);
        }

        // ------------------------------------------------------------ Heart Lock (T2)

        /** Caps the target's max health at its current health for 10 seconds. */
        private void heartLock(Player caster) {
            if (!ready(caster, "life-heart-lock")) {
                return;
            }
            double range = plugin.getConfig().getDouble("abilities.life-heart-lock.range", 15);
            LivingEntity target = findTarget(caster, range);
            if (target == null) {
                caster.sendMessage(BlissGems.color("&cNo target in range!"));
                return;
            }
            AttributeInstance attr = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr == null) {
                return;
            }
            double cap = Math.min(0.0, target.getHealth() - attr.getValue());
            int seconds = plugin.getConfig().getInt("abilities.durations.life-heart-lock", 10);
            timedMaxHealthChange(target, cap, seconds);
            startCooldown(caster, "life-heart-lock", cooldownSeconds("life-heart-lock", 60));
            caster.sendMessage(BlissGems.color("&d🔒 Heart Lock on " + target.getName()));
        }

        // ------------------------------------------------------------ Max health helpers

        private void timedMaxHealth(LivingEntity entity, double amount, int seconds) {
            timedMaxHealthChange(entity, amount, seconds);
        }

        /** Adds a max health change that is removed after the given number of seconds. */
        private void timedMaxHealthChange(LivingEntity entity, double amount, int seconds) {
            AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr == null) {
                return;
            }
            AttributeModifier modifier = new AttributeModifier(
                    new NamespacedKey(plugin, "life-" + UUID.randomUUID()),
                    amount,
                    AttributeModifier.Operation.ADD_NUMBER);
            attr.addModifier(modifier);
            Bukkit.getScheduler().runTaskLater(plugin, () -> attr.removeModifier(modifier), seconds * 20L);
        }

        private LivingEntity findTarget(Player caster, double range) {
            RayTraceResult hit = caster.getWorld().rayTraceEntities(
                    caster.getEyeLocation(),
                    caster.getEyeLocation().getDirection(),
                    range,
                    0.5,
                    entity -> entity instanceof LivingEntity && !entity.equals(caster));
            if (hit == null || !(hit.getHitEntity() instanceof LivingEntity target)) {
                return null;
            }
            return target;
        }

        // ------------------------------------------------------------ Heart Drainer

        private void heartDrainer(Player caster) {
            UUID id = caster.getUniqueId();
            if (drains.containsKey(id)) {
                caster.sendMessage(BlissGems.color("&cHeart Drainer is already active."));
                return;
            }
            if (!ready(caster, "life-drainer")) {
                return;
            }

            RayTraceResult hit = caster.getWorld().rayTraceEntities(
                    caster.getEyeLocation(),
                    caster.getEyeLocation().getDirection(),
                    8,
                    0.5,
                    entity -> entity instanceof LivingEntity && !entity.equals(caster));

            if (hit == null || !(hit.getHitEntity() instanceof LivingEntity)) {
                caster.sendMessage(BlissGems.color("&cNo target in range!"));
                return;
            }
            LivingEntity target = (LivingEntity) hit.getHitEntity();

            double total = plugin.getConfig().getDouble("abilities.damage.life-drainer", 4.0);
            int seconds = Math.max(1, plugin.getConfig().getInt("abilities.durations.life-drainer", 20));
            double perSecond = total / seconds;

            startCooldown(caster, "life-drainer", cooldownSeconds("life-drainer", 60));
            caster.sendMessage(BlissGems.color("&d💘 Heart Drainer on " + target.getName()));

            double[] stolen = {0.0};
            BukkitTask task = new BukkitRunnable() {
                int elapsed = 0;

                @Override
                public void run() {
                    boolean stop = elapsed++ >= seconds
                            || !caster.isOnline() || caster.isDead()
                            || !target.isValid() || target.isDead()
                            || caster.getLocation().distance(target.getLocation()) > 12
                            || plugin.gems().typeOf(caster.getInventory().getItemInMainHand()) != GemType.LIFE;
                    if (stop) {
                        drains.remove(id);
                        cancel();
                        scheduleReturn(target, stolen[0]);
                        return;
                    }
                    target.damage(perSecond, caster);
                    stolen[0] += perSecond;
                    caster.setHealth(Math.min(caster.getMaxHealth(), caster.getHealth() + perSecond));
                }
            }.runTaskTimer(plugin, 0L, 20L);

            drains.put(id, task);
        }

        /** Gives the stolen health back to the target after the return delay (default 2 minutes). */
        private void scheduleReturn(LivingEntity target, double amount) {
            if (amount <= 0) {
                return;
            }
            int delaySeconds = plugin.getConfig().getInt("abilities.life-drainer-return-seconds", 120);
            if (target instanceof Player player) {
                player.sendMessage(BlissGems.color("&d💘 Stolen health will return in " + delaySeconds + " seconds."));
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!target.isValid() || target.isDead()) {
                    return;
                }
                target.setHealth(Math.min(target.getMaxHealth(), target.getHealth() + amount));
                if (target instanceof Player player) {
                    player.sendMessage(BlissGems.color("&d💘 Your stolen health has been returned."));
                }
            }, delaySeconds * 20L);
        }

        // ------------------------------------------------------------ Passives

        /** Called every passives.update-interval ticks. Wither immunity and continuous healing. */
        public void tickPassives() {
            int update = (int) Math.max(1, plugin.getConfig().getLong("passives.update-interval", 20));
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID id = player.getUniqueId();
                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (plugin.gems().typeOf(offhand) != GemType.LIFE || !plugin.energy().passivesActive(player.getUniqueId())) {
                    healTicks.remove(id);
                    continue;
                }
                int tier = plugin.gems().tierOf(offhand);
                String path = "passives.life.tier" + tier + ".";

                if (player.hasPotionEffect(PotionEffectType.WITHER)) {
                    player.removePotionEffect(PotionEffectType.WITHER);
                }

                int interval = plugin.getConfig().getInt(path + "heal-interval", 100);
                double healHearts = plugin.getConfig().getDouble(path + "heal-amount", 0.3);
                int elapsed = healTicks.merge(id, update, Integer::sum);
                if (elapsed >= interval) {
                    healTicks.put(id, 0);
                    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healHearts * 2));
                }
            }
        }

        /** Undead take extra damage from melee hits while a life gem is in the offhand. */
        @EventHandler
        public void onMeleeHit(EntityDamageByEntityEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK) {
                return;
            }
            if (!(event.getDamager() instanceof Player attacker)) {
                return;
            }
            ItemStack offhand = attacker.getInventory().getItemInOffHand();
            if (plugin.gems().typeOf(offhand) != GemType.LIFE || !plugin.energy().passivesActive(player.getUniqueId())) {
                return;
            }
            if (!(event.getEntity() instanceof LivingEntity target) || !isUndead(target)) {
                return;
            }
            int tier = plugin.gems().tierOf(offhand);
            double multiplier = plugin.getConfig().getDouble("passives.life.tier" + tier + ".undead-damage-multiplier", 2.0);
            event.setDamage(event.getDamage() * multiplier);
        }

        private static boolean isUndead(LivingEntity entity) {
            String name = entity.getType().name();
            return name.contains("ZOMBIE") || name.contains("SKELETON") || name.contains("PHANTOM")
                    || name.equals("WITHER") || name.equals("HUSK") || name.equals("DROWNED") || name.equals("STRAY");
        }

        // ------------------------------------------------------------ Cooldown helpers

        /** Used by /bliss clearcds. */
        public void clearCooldowns(UUID playerId) {
            cooldowns.remove(playerId);
        }

        private boolean ready(Player player, String ability) {
            Map<String, Long> mine = cooldowns.getOrDefault(player.getUniqueId(), Collections.emptyMap());
            long until = mine.getOrDefault(ability, 0L);
            long now = System.currentTimeMillis();
            if (now < until) {
                player.sendMessage(BlissGems.color("&cThat ability is recharging: " + ((until - now) / 1000 + 1) + "s"));
                return false;
            }
            return true;
        }

        private void startCooldown(Player player, String ability, int seconds) {
            cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(ability, System.currentTimeMillis() + seconds * 1000L);
        }

        private int cooldownSeconds(String ability, int fallback) {
            return plugin.getConfig().getInt("abilities.cooldowns." + ability, fallback);
        }
    }

    // ---- OrbitalStrike ----
    /**
     * Orbital Snowball. Right-click to throw. Where it lands, anvils, pointed dripstone, and TNT fall
     * from the sky for a few seconds. Falling blocks vanish on landing and TNT never breaks blocks.
     */
    public static final class OrbitalStrike implements Listener {

        private static final double RADIUS = 6.0;

        private final BlissGems plugin;
        private final CustomItems items;
        /** Snowball id -> the player who threw it. */
        private final Map<UUID, UUID> snowballs = new HashMap<>();
        /** Falling block / TNT id -> the player who called the strike. Used so the owner is never hurt. */
        private final Map<UUID, UUID> owners = new HashMap<>();
        private final Map<UUID, Long> cooldown = new HashMap<>();

        public OrbitalStrike(BlissGems plugin, CustomItems items) {
            this.plugin = plugin;
            this.items = items;
        }

        @EventHandler
        public void onThrow(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            if (items.idOf(event.getItem()) != Item.ORBITAL_SNOWBALL) {
                return;
            }
            event.setCancelled(true);
            Player player = event.getPlayer();
            long now = System.currentTimeMillis();
            long cooldownMs = plugin.getConfig().getLong("custom-items.orbital-snowball.cooldown", 20) * 1000L;
            Long last = cooldown.get(player.getUniqueId());
            if (last != null && now - last < cooldownMs) {
                player.sendMessage(BlissGems.color("&cOrbital Snowball recharging: " + ((cooldownMs - (now - last)) / 1000 + 1) + "s"));
                return;
            }
            cooldown.put(player.getUniqueId(), now);

            Snowball ball = player.launchProjectile(Snowball.class);
            snowballs.put(ball.getUniqueId(), player.getUniqueId());
            BlissGems.fx(player, Sound.ENTITY_SNOWBALL_THROW, 1.0f, 0.8f);
        }

        @EventHandler
        public void onLand(ProjectileHitEvent event) {
            UUID owner = snowballs.remove(event.getEntity().getUniqueId());
            if (owner == null) {
                return;
            }
            Location center = event.getHitEntity() != null
                    ? event.getHitEntity().getLocation()
                    : event.getEntity().getLocation();
            strike(center, owner);
        }

        /** Drops anvils, dripstone, and TNT from the sky over the center point for a few seconds. */
        private void strike(Location center, UUID owner) {
            World world = center.getWorld();
            int seconds = plugin.getConfig().getInt("custom-items.orbital-snowball.duration", 8);
            world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.7f);
            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks++ >= seconds * 4) {
                        cancel();
                        return;
                    }
                    for (int i = 0; i < 3; i++) {
                        double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
                        double dist = RADIUS * Math.sqrt(ThreadLocalRandom.current().nextDouble());
                        Location drop = center.clone().add(Math.cos(angle) * dist, 30, Math.sin(angle) * dist);
                        double roll = ThreadLocalRandom.current().nextDouble();
                        if (roll < 0.45) {
                            dropBlock(world, drop, Material.ANVIL, owner);
                        } else if (roll < 0.85) {
                            dropBlock(world, drop, Material.POINTED_DRIPSTONE, owner);
                        } else {
                            TNTPrimed primed = world.spawn(drop, TNTPrimed.class, t -> t.setFuseTicks(40));
                            owners.put(primed.getUniqueId(), owner);
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 5L);
        }

        private void dropBlock(World world, Location at, Material material, UUID owner) {
            FallingBlock falling = world.spawnFallingBlock(at, material.createBlockData());
            falling.setDropItem(false);
            falling.setHurtEntities(true);
            owners.put(falling.getUniqueId(), owner);
        }

        /** The player who called a strike is never hurt by its falling blocks or TNT. */
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onOwnStrikeDamage(EntityDamageByEntityEvent event) {
            if (!(event.getEntity() instanceof Player victim)) {
                return;
            }
            UUID owner = owners.get(event.getDamager().getUniqueId());
            if (owner != null && owner.equals(victim.getUniqueId())) {
                event.setCancelled(true);
            }
        }

        /** Falling blocks vanish when they land instead of placing a block. */
        @EventHandler
        public void onFallingBlockLand(EntityChangeBlockEvent event) {
            if (owners.remove(event.getEntity().getUniqueId()) != null) {
                event.setCancelled(true);
                event.getEntity().remove();
            }
        }

        /** Our TNT explodes and damages things, but never breaks blocks. */
        @EventHandler
        public void onTntExplode(EntityExplodeEvent event) {
            Entity exploded = event.getEntity();
            if (owners.containsKey(exploded.getUniqueId())) {
                event.blockList().clear();
                // Forget it a moment later, after its damage has been applied
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> owners.remove(exploded.getUniqueId()), 2L);
            }
        }
    }

    // ---- PuffGem ----
    /** Puff gem: no fall damage, double jump, Dash (T1). */
    public static final class PuffGem implements Listener {

        private final BlissGems plugin;
        private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

        /** Players whose flight permission we turned on for double jump. */
        private final Set<UUID> flightManaged = new HashSet<>();
        /** Players who already used their double jump this time in the air. */
        private final Set<UUID> airJumpUsed = new HashSet<>();

        public PuffGem(BlissGems plugin) {
            this.plugin = plugin;
        }

        public void cancelFor(Player player) {
            flightManaged.remove(player.getUniqueId());
            airJumpUsed.remove(player.getUniqueId());
        }

        /** Right-click: Dash. Shift + right-click at Tier 2: Breezy Bash. */
        public void onRightClick(Player player, int tier, boolean sneaking) {
            if (tier >= 2 && sneaking) {
                breezyBash(player);
            } else {
                dash(player);
            }
        }

        /** F key at Tier 2: Group Breezy Bash. */
        public void onSwap(Player player, int tier, boolean sneaking) {
            if (tier >= 2) {
                groupBash(player);
            }
        }

        // ------------------------------------------------------------ Dash

        private void dash(Player player) {
            if (!ready(player, "puff-dash")) {
                return;
            }
            Vector direction = player.getLocation().getDirection().setY(0.2).normalize().multiply(1.6);
            double damage = plugin.getConfig().getDouble("abilities.damage.puff-dash", 2.0);
            startCooldown(player, "puff-dash", cooldownSeconds("puff-dash", 5));

            Set<UUID> hit = new HashSet<>();
            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks++ >= 6 || !player.isOnline() || player.isDead()) {
                        cancel();
                        return;
                    }
                    player.setVelocity(direction);
                    player.setFallDistance(0);
                    for (Entity entity : player.getNearbyEntities(1.2, 1.2, 1.2)) {
                        if (entity instanceof LivingEntity living && hit.add(living.getUniqueId())) {
                            living.damage(damage, player);
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        // ------------------------------------------------------------ Breezy Bash (T2)

        /** Launches one target up, then slams them down. The slam deals damage when they land. */
        private void breezyBash(Player caster) {
            if (!ready(caster, "puff-breezy-bash")) {
                return;
            }
            RayTraceResult hit = caster.getWorld().rayTraceEntities(
                    caster.getEyeLocation(),
                    caster.getEyeLocation().getDirection(),
                    6,
                    0.5,
                    entity -> entity instanceof LivingEntity && !entity.equals(caster));
            if (hit == null || !(hit.getHitEntity() instanceof LivingEntity)) {
                caster.sendMessage(BlissGems.color("&cNo target in range!"));
                return;
            }
            LivingEntity target = (LivingEntity) hit.getHitEntity();

            double launch = plugin.getConfig().getDouble("passives.puff.tier2.launch-velocity", 4.5);
            startCooldown(caster, "puff-breezy-bash", cooldownSeconds("puff-breezy-bash", 10));
            caster.sendMessage(BlissGems.color("&b⏫ Breezy Bash!"));
            target.setVelocity(new Vector(0, launch * 0.12, 0));

            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    ticks++;
                    if (ticks > 100 || !target.isValid() || target.isDead()) {
                        cancel();
                        return;
                    }
                    if (ticks == 20) {
                        target.setVelocity(new Vector(0, -3.0, 0));
                    }
                    if (ticks > 20 && target.isOnGround()) {
                        target.damage(6.0, caster);
                        target.getWorld().playSound(target.getLocation(),
                                org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.4f);
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        // ------------------------------------------------------------ Group Breezy Bash (T2)

        /** Blows every nearby living thing away from you. */
        private void groupBash(Player caster) {
            if (!ready(caster, "puff-group-bash")) {
                return;
            }
            double radius = plugin.getConfig().getDouble("abilities.puff-group-bash.radius", 10.0);
            double knockback = plugin.getConfig().getDouble("abilities.puff-group-bash.knockback", 2.5);
            startCooldown(caster, "puff-group-bash", cooldownSeconds("puff-group-bash", 30));
            caster.sendMessage(BlissGems.color("&b🌪 Group Breezy Bash!"));

            Location center = caster.getLocation();
            for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (entity.equals(caster) || !(entity instanceof LivingEntity living)) {
                    continue;
                }
                Vector push = living.getLocation().toVector().subtract(center.toVector());
                push.setY(0);
                if (push.lengthSquared() < 1e-6) {
                    push = new Vector(0, 0, 1);
                }
                living.setVelocity(push.normalize().multiply(knockback * 0.3).setY(0.6));
            }
        }

        // ------------------------------------------------------------ Double jump

        /** Runs every 2 ticks. Lets airborne puff holders press jump once to launch again. */
        public void tickFlight() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID id = player.getUniqueId();
                ItemStack offhand = player.getInventory().getItemInOffHand();
                boolean holding = plugin.gems().typeOf(offhand) == GemType.PUFF;
                boolean creative = player.getGameMode() == GameMode.CREATIVE
                        || player.getGameMode() == GameMode.SPECTATOR;
                boolean enabled = plugin.getConfig().getBoolean(
                        "passives.puff.tier" + plugin.gems().tierOf(offhand) + ".double-jump-enabled", true);

                if (!holding || creative || !enabled) {
                    if (flightManaged.remove(id)) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                    }
                    airJumpUsed.remove(id);
                    continue;
                }

                if (player.isOnGround()) {
                    airJumpUsed.remove(id);
                    if (flightManaged.remove(id)) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                    }
                } else if (!airJumpUsed.contains(id) && !player.getAllowFlight()) {
                    player.setAllowFlight(true);
                    flightManaged.add(id);
                }
            }
        }

        @EventHandler
        public void onToggleFlight(PlayerToggleFlightEvent event) {
            Player player = event.getPlayer();
            UUID id = player.getUniqueId();
            if (!flightManaged.contains(id) || player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            event.setCancelled(true);
            player.setFlying(false);
            player.setAllowFlight(false);
            airJumpUsed.add(id);

            int tier = plugin.gems().tierOf(player.getInventory().getItemInOffHand());
            double boost = plugin.getConfig().getDouble("passives.puff.tier" + tier + ".double-jump-velocity", 0.6);
            Vector velocity = player.getVelocity();
            player.setVelocity(new Vector(velocity.getX(), boost, velocity.getZ()));
        }

        // ------------------------------------------------------------ Passives

        /** Hitting a player with a puff gem in the offhand launches them upward. */
        @EventHandler
        public void onLaunchHit(EntityDamageByEntityEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getDamager() instanceof Player attacker)) {
                return;
            }
            if (!(event.getEntity() instanceof Player target)) {
                return;
            }
            ItemStack offhand = attacker.getInventory().getItemInOffHand();
            if (plugin.gems().typeOf(offhand) != GemType.PUFF) {
                return;
            }
            int tier = plugin.gems().tierOf(offhand);
            double launch = plugin.getConfig().getDouble("passives.puff.tier" + tier + ".launch-velocity", 3.5);
            Vector velocity = target.getVelocity();
            target.setVelocity(new Vector(velocity.getX(), launch * 0.12, velocity.getZ()));
        }

        /** No fall damage while a puff gem is in the offhand. */
        @EventHandler
        public void onFallDamage(EntityDamageEvent event) {
            if (event.getCause() != DamageCause.FALL || !(event.getEntity() instanceof Player player)) {
                return;
            }
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (plugin.gems().typeOf(offhand) != GemType.PUFF) {
                return;
            }
            int tier = plugin.gems().tierOf(offhand);
            if (plugin.getConfig().getBoolean("passives.puff.tier" + tier + ".fall-damage-immunity", true)) {
                event.setCancelled(true);
                double prevented = plugin.addProgress(player.getUniqueId(), "fall-prevented", event.getDamage());
                if (prevented >= 30000) {
                    plugin.unlockAchievement(player, Achievement.ALMOST_FELL_FROM_GRACE);
                }
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            cancelFor(event.getPlayer());
        }

        // ------------------------------------------------------------ Cooldown helpers

        /** Used by /bliss clearcds. */
        public void clearCooldowns(UUID playerId) {
            cooldowns.remove(playerId);
        }

        private boolean ready(Player player, String ability) {
            Map<String, Long> mine = cooldowns.getOrDefault(player.getUniqueId(), Collections.emptyMap());
            long until = mine.getOrDefault(ability, 0L);
            long now = System.currentTimeMillis();
            if (now < until) {
                player.sendMessage(BlissGems.color("&cThat ability is recharging: " + ((until - now) / 1000 + 1) + "s"));
                return false;
            }
            return true;
        }

        private void startCooldown(Player player, String ability, int seconds) {
            cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(ability, System.currentTimeMillis() + seconds * 1000L);
        }

        private int cooldownSeconds(String ability, int fallback) {
            return plugin.getConfig().getInt("abilities.cooldowns." + ability, fallback);
        }
    }

    // ---- RecipeManager ----
    /**
     * Crafting recipes from the original plugin. Gems themselves are not crafted: they come from the
     * join ritual and villager trades. Gem Trader is not in this version yet.
     */
    public static final class RecipeManager {

        private final BlissGems plugin;
        private final CustomItems items;
        private final List<NamespacedKey> keys = new ArrayList<>();

        public RecipeManager(BlissGems plugin, CustomItems items) {
            this.plugin = plugin;
            this.items = items;
        }

        public void register() {
            if (!plugin.getConfig().getBoolean("crafting.enabled", true)) {
                plugin.getLogger().info("Crafting recipes are disabled in config.");
                return;
            }

            add("gem_fragment", items.create(CustomItems.Item.GEM_FRAGMENT),
                    new String[]{"DAD", "EIE", "DAD"},
                    'D', Material.DIAMOND,
                    'A', Material.AMETHYST_CLUSTER,
                    'E', Material.EMERALD,
                    'I', Material.IRON_BLOCK);

            add("repair_kit", items.create(CustomItems.Item.REPAIR_KIT),
                    new String[]{"FAF", "NTN", "FAF"},
                    'F', Material.PRISMARINE_SHARD,
                    'A', Material.ANVIL,
                    'N', Material.NETHERITE_INGOT,
                    'T', Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);

            add("revive_beacon", items.create(CustomItems.Item.REVIVE_BEACON),
                    new String[]{"ETE", "TBT", "ETE"},
                    'E', Material.ECHO_SHARD,
                    'T', Material.TOTEM_OF_UNDYING,
                    'B', Material.BEACON);

            add("gem_trader", items.create(CustomItems.Item.GEM_TRADER),
                    new String[]{"BDB", "DSD", "BDB"},
                    'B', Material.DIAMOND_BLOCK,
                    'D', Material.DRAGON_BREATH,
                    'S', Material.SCULK_CATALYST);

            add("gem_upgrader", items.create(CustomItems.Item.GEM_UPGRADER),
                    new String[]{"BBB", "BSB", "BBB"},
                    'B', Material.DIAMOND_BLOCK,
                    'S', Material.NETHER_STAR);
        }

        private void add(String id, ItemStack result, String[] shape, Object... ingredients) {
            NamespacedKey key = new NamespacedKey(plugin, id);
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape(shape);
            for (int i = 0; i < ingredients.length; i += 2) {
                recipe.setIngredient((Character) ingredients[i], (Material) ingredients[i + 1]);
            }
            if (Bukkit.addRecipe(recipe)) {
                keys.add(key);
            } else {
                plugin.getLogger().warning("Could not register recipe: " + id);
            }
        }

        public void unregister() {
            for (NamespacedKey key : keys) {
                Bukkit.removeRecipe(key);
            }
            keys.clear();
        }
    }

    // ---- SpeedGem ----
    /** Speed gem: Speed and Dolphin's Grace passives. Blur (T1), Speed Storm and Terminal Velocity (T2). */
    public static final class SpeedGem implements Listener {

        private final BlissGems plugin;
        private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

        public SpeedGem(BlissGems plugin) {
            this.plugin = plugin;
        }

        /** Right-click: Blur. Shift + right-click at Tier 2: Speed Storm. */
        public void onRightClick(Player player, int tier, boolean sneaking) {
            if (tier >= 2 && sneaking) {
                speedStorm(player);
            } else {
                blur(player);
            }
        }

        /** F key at Tier 2: Terminal Velocity. */
        public void onSwap(Player player, int tier, boolean sneaking) {
            if (tier >= 2) {
                terminalVelocity(player);
            }
        }

        // ------------------------------------------------------------ Blur (T1)

        private void blur(Player caster) {
            if (!ready(caster, "speed-blur")) {
                return;
            }
            Block target = caster.getTargetBlockExact(20);
            Location center = target != null
                    ? target.getLocation().add(0.5, 0, 0.5)
                    : caster.getLocation().add(caster.getLocation().getDirection().multiply(6));

            double damage = plugin.getConfig().getDouble("abilities.damage.blur", 4.0);
            startCooldown(caster, "speed-blur", cooldownSeconds("speed-blur", 15));
            caster.sendMessage(BlissGems.color("&a⚡ Blur!"));

            new BukkitRunnable() {
                int strikes = 0;

                @Override
                public void run() {
                    if (strikes++ >= 3) {
                        cancel();
                        return;
                    }
                    double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
                    double dist = 5 * Math.sqrt(ThreadLocalRandom.current().nextDouble());
                    Location strike = center.clone().add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
                    strike.getWorld().strikeLightningEffect(strike);

                    for (Entity entity : strike.getWorld().getNearbyEntities(strike, 2, 2, 2)) {
                        if (!(entity instanceof LivingEntity living) || entity.equals(caster)) {
                            continue;
                        }
                        living.damage(damage, caster);
                        Vector push = living.getLocation().toVector().subtract(strike.toVector());
                        push.setY(0);
                        if (push.lengthSquared() < 1e-6) {
                            push = new Vector(0, 0, 1);
                        }
                        living.setVelocity(push.normalize().multiply(0.8).setY(0.4));
                    }
                }
            }.runTaskTimer(plugin, 0L, 10L);
        }

        // ------------------------------------------------------------ Speed Storm (T2)

        /** For 10 seconds, players nearby get Speed VI and Haste II, and other living things are slowed to a near stop. */
        private void speedStorm(Player caster) {
            if (!ready(caster, "speed-storm")) {
                return;
            }
            double radius = plugin.getConfig().getDouble("abilities.speed-storm.radius", 10.0);
            int seconds = plugin.getConfig().getInt("abilities.durations.speed-storm", 10);
            startCooldown(caster, "speed-storm", cooldownSeconds("speed-storm", 135));
            caster.sendMessage(BlissGems.color("&a🌩 Speed Storm!"));

            Location center = caster.getLocation();
            new BukkitRunnable() {
                int elapsed = 0;

                @Override
                public void run() {
                    if (elapsed++ >= seconds || !caster.isOnline()) {
                        cancel();
                        return;
                    }
                    for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                        if (entity instanceof Player ally && plugin.isAlly(caster, ally)) {
                            ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 5, true, false));
                            ally.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, true, false));
                        } else if (entity instanceof LivingEntity enemy) {
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 255, true, false));
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }

        // ------------------------------------------------------------ Terminal Velocity (T2)

        private void terminalVelocity(Player caster) {
            if (!ready(caster, "speed-terminal")) {
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.terminal-velocity", 10);
            startCooldown(caster, "speed-terminal", cooldownSeconds("speed-terminal", 60));
            caster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, seconds * 20, 2, true, false));
            caster.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, seconds * 20, 1, true, false));
            caster.sendMessage(BlissGems.color("&b💨 Terminal Velocity!"));
        }

        // ------------------------------------------------------------ Passives

        /** Called every passives.update-interval ticks. Speed and Dolphin's Grace while in the offhand. */
        public void tickPassives() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (plugin.gems().typeOf(offhand) != GemType.SPEED || !plugin.energy().passivesActive(player.getUniqueId())) {
                    continue;
                }
                int tier = plugin.gems().tierOf(offhand);
                int level = plugin.getConfig().getInt("passives.speed.tier" + tier + ".speed-level", tier >= 2 ? 2 : 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, level, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 120, 0, true, false));
                // Soul sand and soul soil slow you down. A stronger Speed cancels that out while you stand on them.
                Material below = player.getLocation().subtract(0, 0.1, 0).getBlock().getType();
                if (below == Material.SOUL_SAND || below == Material.SOUL_SOIL) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, level + 1, true, false));
                }
            }
        }

        // ------------------------------------------------------------ Cooldown helpers

        /** Used by /bliss clearcds. */
        public void clearCooldowns(UUID playerId) {
            cooldowns.remove(playerId);
        }

        private boolean ready(Player player, String ability) {
            Map<String, Long> mine = cooldowns.getOrDefault(player.getUniqueId(), Collections.emptyMap());
            long until = mine.getOrDefault(ability, 0L);
            long now = System.currentTimeMillis();
            if (now < until) {
                player.sendMessage(BlissGems.color("&cThat ability is recharging: " + ((until - now) / 1000 + 1) + "s"));
                return false;
            }
            return true;
        }

        private void startCooldown(Player player, String ability, int seconds) {
            cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(ability, System.currentTimeMillis() + seconds * 1000L);
        }

        private int cooldownSeconds(String ability, int fallback) {
            return plugin.getConfig().getInt("abilities.cooldowns." + ability, fallback);
        }
    }

    // ---- StatsManager ----
    /** Kills and deaths, stored in data.yml under "stats.kills.<uuid>" and "stats.deaths.<uuid>". */
    public static final class StatsManager {

        private final BlissGems plugin;

        public StatsManager(BlissGems plugin) {
            this.plugin = plugin;
        }

        public void addKill(UUID id) {
            increment("stats.kills." + id);
        }

        public void addDeath(UUID id) {
            increment("stats.deaths." + id);
        }

        public int kills(UUID id) {
            return plugin.data().getInt("stats.kills." + id, 0);
        }

        public int deaths(UUID id) {
            return plugin.data().getInt("stats.deaths." + id, 0);
        }

        private void increment(String path) {
            YamlConfiguration yml = plugin.data();
            yml.set(path, yml.getInt(path, 0) + 1);
            plugin.saveData(yml);
        }

        /** Top kills, highest first. Each entry is "name" -> kills. */
        public List<Map.Entry<String, Integer>> topKillers(int limit) {
            Map<String, Integer> scores = new TreeMap<>();
            ConfigurationSection section = plugin.data().getConfigurationSection("stats.kills");
            if (section != null) {
                for (String id : section.getKeys(false)) {
                    OfflinePlayer op;
                    try {
                        op = Bukkit.getOfflinePlayer(UUID.fromString(id));
                    } catch (IllegalArgumentException ex) {
                        continue;
                    }
                    scores.put(op.getName() != null ? op.getName() : id, section.getInt(id, 0));
                }
            }
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(scores.entrySet());
            sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
            return sorted.subList(0, Math.min(limit, sorted.size()));
        }

        /** How many online players hold each gem type right now. */
        public Map<String, Integer> gemDistribution() {
            Map<String, Integer> counts = new TreeMap<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (ItemStack item : player.getInventory().getContents()) {
                    if (plugin.gems().typeOf(item) != null) {
                        counts.merge(plugin.gems().typeOf(item).name(), 1, Integer::sum);
                        break;
                    }
                }
            }
            return counts;
        }
    }

    // ---- StrengthGem ----
    /**
     * Strength gem.
     * - Offhand passive: Strength, Sharpness-style melee bonus, Bloodthorns.
     * - Tier 2 right-click: Nullify. Shift + right-click: Frailer. F: Shadow Stalker (uses a player head).
     */
    public static final class StrengthGem implements Listener {

        private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

        private final BlissGems plugin;
        private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

        public StrengthGem(BlissGems plugin) {
            this.plugin = plugin;
        }

        /** Tier 1 has no active ability. Tier 2: Nullify, or Frailer while sneaking. */
        public void onRightClick(Player player, int tier, boolean sneaking) {
            if (tier < 2) {
                return;
            }
            if (sneaking) {
                frailer(player);
            } else {
                nullify(player);
            }
        }

        /** F key at Tier 2: Shadow Stalker. */
        public void onSwap(Player player, int tier, boolean sneaking) {
            if (tier >= 2) {
                shadowStalker(player);
            }
        }

        // ------------------------------------------------------------ Nullify (T2)

        /** Strips every potion effect from the target. */
        private void nullify(Player caster) {
            if (!ready(caster, "strength-nullify")) {
                return;
            }
            LivingEntity target = findTarget(caster, 8);
            if (target == null) {
                caster.sendMessage(BlissGems.color("&cNo target in range!"));
                return;
            }
            for (PotionEffect effect : target.getActivePotionEffects()) {
                target.removePotionEffect(effect.getType());
            }
            startCooldown(caster, "strength-nullify", cooldownSeconds("strength-nullify", 60));
            caster.sendMessage(BlissGems.color("&c🚫 Nullified " + target.getName()));
        }

        // ------------------------------------------------------------ Frailer (T2)

        /** Clears the target's potions, then applies Weakness I and Wither I. */
        private void frailer(Player caster) {
            if (!ready(caster, "strength-frailer")) {
                return;
            }
            LivingEntity target = findTarget(caster, 8);
            if (target == null) {
                caster.sendMessage(BlissGems.color("&cNo target in range!"));
                return;
            }
            for (PotionEffect effect : target.getActivePotionEffects()) {
                target.removePotionEffect(effect.getType());
            }
            int weakness = plugin.getConfig().getInt("abilities.durations.strength-frailer-weakness", 20);
            int wither = plugin.getConfig().getInt("abilities.durations.strength-frailer-wither", 40);
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, weakness * 20, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, wither * 20, 0));
            startCooldown(caster, "strength-frailer", cooldownSeconds("strength-frailer", 45));
            caster.sendMessage(BlissGems.color("&c⚔ Frailer on " + target.getName()));
        }

        // ------------------------------------------------------------ Shadow Stalker (T2)

        /** Uses one player head from your inventory to show a player's direction and distance. */
        private void shadowStalker(Player caster) {
            if (!ready(caster, "strength-shadow-stalker")) {
                return;
            }
            Inventory inv = caster.getInventory();
            int headSlot = -1;
            Player found = null;
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item == null || item.getType() != Material.PLAYER_HEAD) {
                    continue;
                }
                if (!(item.getItemMeta() instanceof SkullMeta skull)) {
                    continue;
                }
                OfflinePlayer owner = skull.getOwningPlayer();
                if (owner != null && owner.getPlayer() != null) {
                    headSlot = i;
                    found = owner.getPlayer();
                    break;
                }
            }
            if (found == null) {
                caster.sendMessage(BlissGems.color("&cNo player head in your inventory."));
                return;
            }

            ItemStack head = inv.getItem(headSlot);
            if (head.getAmount() <= 1) {
                inv.setItem(headSlot, null);
            } else {
                head.setAmount(head.getAmount() - 1);
            }

            final Player target = found;
            if (plugin.addProgress(caster.getUniqueId(), "stalk." + target.getUniqueId(), 1) >= 17) {
                plugin.unlockAchievement(caster, Achievement.ITS_RABBIT_SEASON);
            }
            int duration = plugin.getConfig().getInt("abilities.durations.strength-shadow-stalker", 60);
            startCooldown(caster, "strength-shadow-stalker", cooldownSeconds("strength-shadow-stalker", 120));
            caster.sendMessage(BlissGems.color("&5🔍 Tracking " + target.getName() + " for " + duration + "s"));

            new BukkitRunnable() {
                int elapsed = 0;

                @Override
                public void run() {
                    if (elapsed++ >= duration || !caster.isOnline() || !target.isOnline()) {
                        caster.sendActionBar(Component.text("Tracking ended"));
                        cancel();
                        return;
                    }
                    if (target.getWorld() != caster.getWorld()) {
                        caster.sendActionBar(Component.text("Tracking " + target.getName() + ": other world"));
                        return;
                    }
                    int blocks = (int) caster.getLocation().distance(target.getLocation());
                    caster.sendActionBar(Component.text(
                            "Tracking " + target.getName() + ": " + blocks + " blocks " + arrowTo(caster, target)));
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }

        /** Arrow showing where the target is relative to where the caster is facing. */
        private static String arrowTo(Player caster, Player target) {
            Vector to = target.getLocation().toVector().subtract(caster.getLocation().toVector());
            double targetYaw = Math.toDegrees(Math.atan2(-to.getX(), to.getZ()));
            double diff = targetYaw - caster.getLocation().getYaw();
            diff = ((diff % 360) + 540) % 360 - 180;
            int sector = (int) Math.round(diff / 45.0);
            return ARROWS[((sector % 8) + 8) % 8];
        }

        // ------------------------------------------------------------ Passives (Tier 1 and up)

        /** Called every passives.update-interval ticks. Strength while the gem is in the offhand. */
        public void tickPassives() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (plugin.gems().typeOf(offhand) != GemType.STRENGTH || !plugin.energy().passivesActive(player.getUniqueId())) {
                    continue;
                }
                int tier = plugin.gems().tierOf(offhand);
                int level = plugin.getConfig().getInt("passives.strength.tier" + tier + ".strength-level", tier >= 2 ? 1 : 0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120, level, true, false));
            }
        }

        /** Sharpness-style bonus on every melee hit, plus Bloodthorns (more damage the lower your health). */
        @EventHandler
        public void onMeleeHit(EntityDamageByEntityEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK) {
                return;
            }
            if (!(event.getDamager() instanceof Player attacker)) {
                return;
            }
            ItemStack offhand = attacker.getInventory().getItemInOffHand();
            if (plugin.gems().typeOf(offhand) != GemType.STRENGTH || !plugin.energy().passivesActive(player.getUniqueId())) {
                return;
            }
            int tier = plugin.gems().tierOf(offhand);

            double sharpness = tier >= 2 ? 4.5 : 2.5; // approximates Sharpness V / II
            double maxBloodthorns = plugin.getConfig().getDouble(
                    "passives.strength.tier" + tier + ".bloodthorns-max-bonus-damage", tier >= 2 ? 10.0 : 7.0);
            double missingRatio = 1.0 - attacker.getHealth() / attacker.getMaxHealth();
            double bloodthorns = maxBloodthorns * Math.max(0.0, Math.min(1.0, missingRatio));

            event.setDamage(event.getDamage() + sharpness + bloodthorns);
        }

        // ------------------------------------------------------------ Helpers

        /** The living thing you are looking at, within range, or null. */
        private LivingEntity findTarget(Player caster, double range) {
            RayTraceResult hit = caster.getWorld().rayTraceEntities(
                    caster.getEyeLocation(),
                    caster.getEyeLocation().getDirection(),
                    range,
                    0.5,
                    entity -> entity instanceof LivingEntity && !entity.equals(caster));
            if (hit == null || !(hit.getHitEntity() instanceof LivingEntity target)) {
                return null;
            }
            return target;
        }

        /** Used by /bliss clearcds. */
        public void clearCooldowns(UUID playerId) {
            cooldowns.remove(playerId);
        }

        private boolean ready(Player player, String ability) {
            Map<String, Long> mine = cooldowns.getOrDefault(player.getUniqueId(), Collections.emptyMap());
            long until = mine.getOrDefault(ability, 0L);
            long now = System.currentTimeMillis();
            if (now < until) {
                player.sendMessage(BlissGems.color("&cThat ability is recharging: " + ((until - now) / 1000 + 1) + "s"));
                return false;
            }
            return true;
        }

        private void startCooldown(Player player, String ability, int seconds) {
            cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(ability, System.currentTimeMillis() + seconds * 1000L);
        }

        private int cooldownSeconds(String ability, int fallback) {
            return plugin.getConfig().getInt("abilities.cooldowns." + ability, fallback);
        }
    }

    // ---- VillagerDiscount ----
    /**
     * Wealth gem villager discount. If a player with a Wealth gem is within 10 blocks of a villager,
     * that villager's new trades cost less (10% at Tier 1, 20% at Tier 2).
     */
    public static final class VillagerDiscount implements Listener {

        private static final double RANGE = 10.0;

        private final BlissGems plugin;

        public VillagerDiscount(BlissGems plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onAcquireTrade(VillagerAcquireTradeEvent event) {
            AbstractVillager villager = event.getEntity();
            Location where = villager.getLocation();
            Player wealthy = null;
            int tier = 0;
            for (Player player : where.getWorld().getPlayers()) {
                if (player.getLocation().distance(where) > RANGE) {
                    continue;
                }
                int found = wealthTier(player);
                if (found > tier) {
                    wealthy = player;
                    tier = found;
                }
            }
            if (wealthy == null) {
                return;
            }

            double discount = plugin.getConfig().getDouble("passives.wealth.tier" + tier + ".villager-discount",
                    tier >= 2 ? 0.20 : 0.10);
            event.setRecipe(applyDiscount(event.getRecipe(), discount));
        }

        /** Highest Wealth gem tier in the player's inventory, or 0. */
        private int wealthTier(Player player) {
            int best = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (plugin.gems().typeOf(item) == GemType.WEALTH) {
                    best = Math.max(best, plugin.gems().tierOf(item));
                }
            }
            return best;
        }

        /** Lowers each ingredient amount by the discount, never below 1. */
        private MerchantRecipe applyDiscount(MerchantRecipe original, double discount) {
            MerchantRecipe recipe = new MerchantRecipe(original.getResult(), original.getUses(), original.getMaxUses(),
                    original.hasExperienceReward(), original.getVillagerExperience(), original.getPriceMultiplier());
            for (ItemStack ingredient : original.getIngredients()) {
                if (ingredient == null) {
                    continue;
                }
                ItemStack copy = ingredient.clone();
                copy.setAmount(Math.max(1, (int) (ingredient.getAmount() * (1.0 - discount))));
                recipe.addIngredient(copy);
            }
            return recipe;
        }
    }

    // ---- WealthGem ----
    /**
     * Wealth gem.
     * - Offhand passive: Luck, Hero of the Village, armor mend and durability chip.
     * - Right-click (Tier 2): Unfortunate on a player you aim at. Shift + right-click: Item Lock.
     * - F (Tier 2): Amplification. Shift + F: Rich Rush.
     * - /bliss pockets (Tier 2): 9-slot storage saved to disk. Gems can't be stored in it.
     */
    public static final class WealthGem implements Listener {

        private static final MiniMessage MM = MiniMessage.miniMessage();
        private static final int POCKET_SLOTS = 9;

        private final BlissGems plugin;
        private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
        private final Map<UUID, Long> unfortunateUntil = new HashMap<>();
        private final Map<UUID, Long> itemLockUntil = new HashMap<>();
        private final Map<UUID, Long> amplifyUntil = new HashMap<>();
        private final Map<UUID, Long> richRushUntil = new HashMap<>();
        private final Map<UUID, Map<Integer, Map<Enchantment, Integer>>> boosted = new HashMap<>();
        private final Map<UUID, BukkitTask> boostTasks = new HashMap<>();

        public WealthGem(BlissGems plugin) {
            this.plugin = plugin;
        }

        /** Right-click: Unfortunate. Shift + right-click at Tier 2: Item Lock. Tier 1 has no ability. */
        public void onRightClick(Player player, int tier, boolean sneaking) {
            if (tier < 2) {
                return;
            }
            if (sneaking) {
                itemLock(player);
            } else {
                unfortunate(player);
            }
        }

        /** F at Tier 2: Amplification. Shift + F at Tier 2: Rich Rush. */
        public void onSwap(Player player, int tier, boolean sneaking) {
            if (tier < 2) {
                return;
            }
            if (sneaking) {
                richRush(player);
            } else {
                amplification(player);
            }
        }

        // ------------------------------------------------------------ Unfortunate (T2)

        /** Makes the player you are aiming at fail many actions for 40 seconds. */
        private void unfortunate(Player caster) {
            if (!ready(caster, "wealth-unfortunate")) {
                return;
            }
            Player target = findPlayer(caster, 15);
            if (target == null) {
                caster.sendMessage(BlissGems.color("&e&oNo target found! &7(Aim at a player to use Unfortunate)"));
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.wealth-unfortunate", 40);
            unfortunateUntil.put(target.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
            startCooldown(caster, "wealth-unfortunate", cooldownSeconds("wealth-unfortunate", 90));
            caster.sendMessage(BlissGems.color("&6💸 Unfortunate on " + target.getName()));
            target.sendMessage(BlissGems.color("&c💸 Luck has abandoned you for " + seconds + " seconds."));
        }

        /** True if this player's action should fail, based on Unfortunate chances in config. */
        private boolean fails(UUID playerId, String chanceKey, double fallback) {
            Long until = unfortunateUntil.get(playerId);
            if (until == null) {
                return false;
            }
            if (System.currentTimeMillis() > until) {
                unfortunateUntil.remove(playerId);
                return false;
            }
            double chance = plugin.getConfig().getDouble("passives.unfortunate." + chanceKey, fallback);
            return ThreadLocalRandom.current().nextDouble() < chance;
        }

        @EventHandler
        public void onUnfortunateAttack(EntityDamageByEntityEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getDamager() instanceof Player attacker)) {
                return;
            }
            if (locked(attacker.getUniqueId()) && isMainHandAttack(attacker)) {
                event.setCancelled(true);
                return;
            }
            if (fails(attacker.getUniqueId(), "attack-fail-chance", 0.75)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onUnfortunateBreak(BlockBreakEvent event) {
            if (fails(event.getPlayer().getUniqueId(), "block-break-fail-chance", 0.50)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onUnfortunatePlace(BlockPlaceEvent event) {
            if (fails(event.getPlayer().getUniqueId(), "block-place-fail-chance", 1.0)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onUnfortunateEat(PlayerItemConsumeEvent event) {
            if (fails(event.getPlayer().getUniqueId(), "eat-fail-chance", 1.0)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onUnfortunateSprint(PlayerToggleSprintEvent event) {
            if (event.isSprinting() && fails(event.getPlayer().getUniqueId(), "sprint-fail-chance", 0.50)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onUnfortunateDrop(PlayerDropItemEvent event) {
            if (fails(event.getPlayer().getUniqueId(), "drop-item-fail-chance", 0.80)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onUnfortunatePickup(EntityPickupItemEvent event) {
            if (event.getEntity() instanceof Player player
                    && fails(player.getUniqueId(), "pickup-item-fail-chance", 0.70)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onUnfortunateInteract(PlayerInteractEvent event) {
            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            if (event.getHand() == EquipmentSlot.HAND && locked(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
                return;
            }
            if (fails(event.getPlayer().getUniqueId(), "interact-fail-chance", 0.60)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onUnfortunateCraft(CraftItemEvent event) {
            if (event.getWhoClicked() instanceof Player player
                    && fails(player.getUniqueId(), "craft-fail-chance", 0.90)) {
                event.setCancelled(true);
            }
        }

        // ------------------------------------------------------------ Item Lock (T2)

        /** Locks the main-hand item of the player you aim at for 10 seconds (no use, no attacks). */
        private void itemLock(Player caster) {
            if (!ready(caster, "wealth-item-lock")) {
                return;
            }
            Player target = findPlayer(caster, 15);
            if (target == null) {
                caster.sendMessage(BlissGems.color("&cNo player target in range!"));
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.wealth-item-lock", 10);
            itemLockUntil.put(target.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
            startCooldown(caster, "wealth-item-lock", cooldownSeconds("wealth-item-lock", 45));
            caster.sendMessage(BlissGems.color("&6🔒 Locked " + target.getName() + "'s held item"));
            target.sendMessage(BlissGems.color("&c🔒 Your held item is locked for " + seconds + " seconds."));
        }

        private boolean locked(UUID playerId) {
            Long until = itemLockUntil.get(playerId);
            return until != null && System.currentTimeMillis() < until;
        }

        private static boolean isMainHandAttack(Player player) {
            return player.getInventory().getItemInMainHand() != null
                    && !player.getInventory().getItemInMainHand().getType().isAir();
        }

        // ------------------------------------------------------------ Amplification (T2)

        /** For 45 seconds: +25% melee damage and Haste II. */
        private void amplification(Player caster) {
            if (!ready(caster, "wealth-amplification")) {
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.wealth-amplification", 45);
            amplifyUntil.put(caster.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
            caster.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, seconds * 20, 1, true, false));
            startCooldown(caster, "wealth-amplification", cooldownSeconds("wealth-amplification", 180));
            caster.sendMessage(BlissGems.color("&e✨ Amplification for " + seconds + "s!"));
            boostEnchants(caster, seconds);
        }

        /** Adds 1 level to every enchant on your held, offhand, and armor items, then restores them. */
        private void boostEnchants(Player caster, int seconds) {
            UUID id = caster.getUniqueId();
            if (boosted.containsKey(id)) {
                return;
            }
            org.bukkit.inventory.PlayerInventory inv = caster.getInventory();
            int[] slots = {36, 37, 38, 39, 40, inv.getHeldItemSlot()};
            Map<Integer, Map<Enchantment, Integer>> originals = new HashMap<>();
            for (int slot : slots) {
                ItemStack item = inv.getItem(slot);
                if (item == null || item.getEnchantments().isEmpty()) {
                    continue;
                }
                Map<Enchantment, Integer> before = new HashMap<>(item.getEnchantments());
                for (Map.Entry<Enchantment, Integer> entry : before.entrySet()) {
                    item.addUnsafeEnchantment(entry.getKey(), entry.getValue() + 1);
                }
                inv.setItem(slot, item);
                originals.put(slot, before);
            }
            if (originals.isEmpty()) {
                return;
            }
            boosted.put(id, originals);
            plugin.unlockAchievement(caster, Achievement.BOUNDARY_BREAK);
            boostTasks.put(id, Bukkit.getScheduler().runTaskLater(plugin, () -> restoreBoost(caster), seconds * 20L));
        }

        /** Puts back the original enchant levels. Runs when the boost ends, on quit, and on death. */
        private void restoreBoost(Player player) {
            UUID id = player.getUniqueId();
            Map<Integer, Map<Enchantment, Integer>> originals = boosted.remove(id);
            BukkitTask task = boostTasks.remove(id);
            if (task != null) {
                task.cancel();
            }
            if (originals == null) {
                return;
            }
            for (Map.Entry<Integer, Map<Enchantment, Integer>> entry : originals.entrySet()) {
                ItemStack item = player.getInventory().getItem(entry.getKey());
                if (item == null) {
                    continue;
                }
                for (Map.Entry<Enchantment, Integer> level : entry.getValue().entrySet()) {
                    item.removeEnchantment(level.getKey());
                    item.addUnsafeEnchantment(level.getKey(), level.getValue());
                }
                player.getInventory().setItem(entry.getKey(), item);
            }
            if (player.isOnline()) {
                player.sendMessage(BlissGems.color("&e&oAmplification has worn off. Enchantments restored."));
            }
        }

        @EventHandler
        public void onBoostQuit(PlayerQuitEvent event) {
            restoreBoost(event.getPlayer());
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onBoostDeath(PlayerDeathEvent event) {
            restoreBoost(event.getEntity());
        }

        private boolean amplified(UUID playerId) {
            Long until = amplifyUntil.get(playerId);
            return until != null && System.currentTimeMillis() < until;
        }

        @EventHandler
        public void onAmplifiedHit(EntityDamageByEntityEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getDamager() instanceof Player attacker)) {
                return;
            }
            if (amplified(attacker.getUniqueId())) {
                event.setDamage(event.getDamage() * 1.25);
            }
        }

        // ------------------------------------------------------------ Rich Rush (T2)

        /** For 2 minutes: block and mob drops are doubled. */
        private void richRush(Player caster) {
            if (!ready(caster, "wealth-rich-rush")) {
                return;
            }
            int seconds = plugin.getConfig().getInt("abilities.durations.wealth-rich-rush", 120);
            richRushUntil.put(caster.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
            startCooldown(caster, "wealth-rich-rush", cooldownSeconds("wealth-rich-rush", 540));
            caster.sendMessage(BlissGems.color("&a🍀 Rich Rush for " + seconds + "s!"));
        }

        private boolean richRushing(UUID playerId) {
            Long until = richRushUntil.get(playerId);
            return until != null && System.currentTimeMillis() < until;
        }

        @EventHandler
        public void onRichBlockDrop(BlockDropItemEvent event) {
            if (!richRushing(event.getPlayer().getUniqueId())) {
                return;
            }
            for (org.bukkit.entity.Item item : event.getItems()) {
                ItemStack stack = item.getItemStack();
                stack.setAmount(Math.min(stack.getMaxStackSize(), stack.getAmount() * 2));
                item.setItemStack(stack);
            }
        }

        @EventHandler
        public void onRichMobDrop(EntityDeathEvent event) {
            Player killer = event.getEntity().getKiller();
            if (killer == null || !richRushing(killer.getUniqueId())) {
                return;
            }
            for (ItemStack drop : event.getDrops()) {
                drop.setAmount(Math.min(drop.getMaxStackSize(), drop.getAmount() * 2));
            }
        }

        // ------------------------------------------------------------ Passives (Tier 1 and up)

        /** Called every passives.update-interval ticks. Luck and Hero of the Village while in the offhand. */
        public void tickPassives() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (plugin.gems().typeOf(offhand) != GemType.WEALTH || !plugin.energy().passivesActive(player.getUniqueId())) {
                    continue;
                }
                int tier = plugin.gems().tierOf(offhand);
                int luck = plugin.getConfig().getInt("passives.wealth.tier" + tier + ".luck-level", tier >= 2 ? 1 : 0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 120, luck, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 120, 0, true, false));
            }
        }

        /** Each hit mends your own armor, and chips the armor of a player you hit. */
        @EventHandler
        public void onMeleeHit(EntityDamageByEntityEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK) {
                return;
            }
            if (!(event.getDamager() instanceof Player attacker)) {
                return;
            }
            ItemStack offhand = attacker.getInventory().getItemInOffHand();
            if (plugin.gems().typeOf(offhand) != GemType.WEALTH || !plugin.energy().passivesActive(player.getUniqueId())) {
                return;
            }
            int tier = plugin.gems().tierOf(offhand);
            int mend = plugin.getConfig().getInt("passives.wealth.tier" + tier + ".armor-mend-amount", 2);
            int chip = plugin.getConfig().getInt("passives.wealth.tier" + tier + ".durability-chip-extra-damage", 1);

            adjustArmor(attacker, -mend);
            if (event.getEntity() instanceof Player victim) {
                adjustArmor(victim, chip);
            }
        }

        /** Changes the durability damage on every armor piece. Negative values repair, positive values wear. */
        private void adjustArmor(Player player, int delta) {
            ItemStack[] armor = player.getInventory().getArmorContents();
            boolean changed = false;
            for (ItemStack piece : armor) {
                if (piece == null || piece.getType().isAir()) {
                    continue;
                }
                ItemMeta meta = piece.getItemMeta();
                if (!(meta instanceof Damageable damageable)) {
                    continue;
                }
                int max = piece.getType().getMaxDurability();
                int newDamage = Math.max(0, Math.min(max - 1, damageable.getDamage() + delta));
                if (newDamage == damageable.getDamage()) {
                    continue;
                }
                damageable.setDamage(newDamage);
                piece.setItemMeta(meta);
                changed = true;
            }
            if (changed) {
                player.getInventory().setArmorContents(armor);
            }
        }

        // ------------------------------------------------------------ Pockets (T2)

        private static final class PocketsHolder implements InventoryHolder {
            private final UUID owner;
            private Inventory inventory;

            PocketsHolder(UUID owner) {
                this.owner = owner;
            }

            @Override
            public Inventory getInventory() {
                return inventory;
            }
        }

        /** Opens your pockets. Requires a Tier 2 Wealth gem somewhere in your inventory. */
        public void openPockets(Player player) {
            if (!hasTier2Wealth(player)) {
                player.sendMessage(BlissGems.color("&cPockets requires a Tier 2 Wealth gem!"));
                return;
            }
            PocketsHolder holder = new PocketsHolder(player.getUniqueId());
            Inventory inv = Bukkit.createInventory(holder, POCKET_SLOTS,
                    MM.deserialize("<gradient:#ffd700:#ffa500><b>Pockets</b></gradient>"));
            holder.inventory = inv;

            List<?> saved = plugin.data().getList("pockets." + player.getUniqueId(), List.of());
            for (int i = 0; i < Math.min(POCKET_SLOTS, saved.size()); i++) {
                if (saved.get(i) instanceof ItemStack stack) {
                    inv.setItem(i, stack);
                }
            }
            player.openInventory(inv);
        }

        private boolean hasTier2Wealth(Player player) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (plugin.gems().typeOf(item) == GemType.WEALTH && plugin.gems().tierOf(item) >= 2) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isPockets(Inventory inventory) {
            return inventory.getHolder() instanceof PocketsHolder;
        }

        /** Gems can't be put into pockets. */
        @EventHandler
        public void onPocketsClick(InventoryClickEvent event) {
            if (!isPockets(event.getView().getTopInventory())) {
                return;
            }
            boolean gemInHand = plugin.gems().typeOf(event.getCursor()) != null;
            boolean gemShifted = event.isShiftClick() && plugin.gems().typeOf(event.getCurrentItem()) != null;
            if (gemInHand || gemShifted) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(BlissGems.color("&cGems can't be stored in pockets."));
            }
        }

        /** Saves the pockets to disk when they close. */
        @EventHandler
        public void onPocketsClose(InventoryCloseEvent event) {
            if (!(event.getInventory().getHolder() instanceof PocketsHolder holder)) {
                return;
            }
            ItemStack[] contents = event.getInventory().getContents();
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < POCKET_SLOTS; i++) {
                list.add(i < contents.length ? contents[i] : null);
            }
            YamlConfiguration yml = plugin.data();
            yml.set("pockets." + holder.owner, list);
            plugin.saveData(yml);
        }

        // ------------------------------------------------------------ Helpers

        /** The player you are looking at, within range, or null. */
        private Player findPlayer(Player caster, double range) {
            RayTraceResult hit = caster.getWorld().rayTraceEntities(
                    caster.getEyeLocation(),
                    caster.getEyeLocation().getDirection(),
                    range,
                    0.5,
                    entity -> entity instanceof Player && !entity.equals(caster));
            if (hit == null || !(hit.getHitEntity() instanceof Player target)) {
                return null;
            }
            return target;
        }

        /** Used by /bliss clearcds. */
        public void clearCooldowns(UUID playerId) {
            cooldowns.remove(playerId);
        }

        private boolean ready(Player player, String ability) {
            Map<String, Long> mine = cooldowns.getOrDefault(player.getUniqueId(), Collections.emptyMap());
            long until = mine.getOrDefault(ability, 0L);
            long now = System.currentTimeMillis();
            if (now < until) {
                player.sendMessage(BlissGems.color("&cThat ability is recharging: " + ((until - now) / 1000 + 1) + "s"));
                return false;
            }
            return true;
        }

        private void startCooldown(Player player, String ability, int seconds) {
            cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(ability, System.currentTimeMillis() + seconds * 1000L);
        }

        private int cooldownSeconds(String ability, int fallback) {
            return plugin.getConfig().getInt("abilities.cooldowns." + ability, fallback);
        }
    }
}
