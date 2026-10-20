package dev.xoperr.blissgems;

import dev.xoperr.blissgems.a.a;
import dev.xoperr.blissgems.a.b;
import dev.xoperr.blissgems.a.c;
import dev.xoperr.blissgems.a.d;
import dev.xoperr.blissgems.a.e;
import dev.xoperr.blissgems.a.f;
import dev.xoperr.blissgems.a.g;
import dev.xoperr.blissgems.a.h;
import dev.xoperr.blissgems.api.BlissGemsAPI;
import dev.xoperr.blissgems.api.CooldownEntry;
import dev.xoperr.blissgems.api.GemDefinition;
import dev.xoperr.blissgems.commands.BlissCommand;
import dev.xoperr.blissgems.commands.FixHeartsCommand;
import dev.xoperr.blissgems.libs.faststats.bukkit.BukkitMetrics;
import dev.xoperr.blissgems.libs.faststats.core.Metrics;
import dev.xoperr.blissgems.libs.faststats.core.data.Metric;
import dev.xoperr.blissgems.listeners.AutoEnchantListener;
import dev.xoperr.blissgems.listeners.ComprehensiveGemProtectionListener;
import dev.xoperr.blissgems.listeners.GemInteractListener;
import dev.xoperr.blissgems.listeners.KillTrackingListener;
import dev.xoperr.blissgems.listeners.PassiveListener;
import dev.xoperr.blissgems.listeners.PlayerDeathListener;
import dev.xoperr.blissgems.listeners.PlayerJoinListener;
import dev.xoperr.blissgems.listeners.RepairKitListener;
import dev.xoperr.blissgems.listeners.ReviveBeaconListener;
import dev.xoperr.blissgems.listeners.StunListener;
import dev.xoperr.blissgems.listeners.SwapHandAbilityListener;
import dev.xoperr.blissgems.listeners.TeleportListener;
import dev.xoperr.blissgems.listeners.UpgraderListener;
import dev.xoperr.blissgems.listeners.VillagerTradeListener;
import dev.xoperr.blissgems.managers.AbilityBindingManager;
import dev.xoperr.blissgems.managers.AbilityManager;
import dev.xoperr.blissgems.managers.AchievementManager;
import dev.xoperr.blissgems.managers.ClickActivationManager;
import dev.xoperr.blissgems.managers.CooldownDisplayManager;
import dev.xoperr.blissgems.managers.CriticalHitManager;
import dev.xoperr.blissgems.managers.EnergyManager;
import dev.xoperr.blissgems.managers.EnhancedGuiManager;
import dev.xoperr.blissgems.managers.FlowStateManager;
import dev.xoperr.blissgems.managers.GemManager;
import dev.xoperr.blissgems.managers.GemRegistryImpl;
import dev.xoperr.blissgems.managers.GemRitualManager;
import dev.xoperr.blissgems.managers.PassiveManager;
import dev.xoperr.blissgems.managers.PluginMessagingManager;
import dev.xoperr.blissgems.managers.RecipeManager;
import dev.xoperr.blissgems.managers.RepairKitManager;
import dev.xoperr.blissgems.managers.ReviveBeaconManager;
import dev.xoperr.blissgems.managers.SoulManager;
import dev.xoperr.blissgems.managers.StatsManager;
import dev.xoperr.blissgems.managers.TrustedPlayersManager;
import dev.xoperr.blissgems.utils.ConfigManager;
import dev.xoperr.blissgems.utils.CustomItemManager;
import dev.xoperr.blissgems.utils.GemType;
import java.io.File;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class BlissGems extends JavaPlugin implements BlissGemsAPI {
   private ConfigManager configManager;
   private BlissCommand blissCommand;
   private GemRegistryImpl gemRegistry;
   private EnergyManager energyManager;
   private GemManager gemManager;
   private AbilityManager abilityManager;
   private AbilityBindingManager abilityBindingManager;
   private PassiveManager passiveManager;
   private ClickActivationManager clickActivationManager;
   private TrustedPlayersManager trustedPlayersManager;
   private CooldownDisplayManager cooldownDisplayManager;
   private EnhancedGuiManager enhancedGuiManager;
   private StatsManager statsManager;
   private RecipeManager recipeManager;
   private RepairKitManager repairKitManager;
   private ReviveBeaconManager reviveBeaconManager;
   private SoulManager soulManager;
   private FlowStateManager flowStateManager;
   private CriticalHitManager criticalHitManager;
   private PluginMessagingManager pluginMessagingManager;
   private a astraAbilities;
   private b fireAbilities;
   private c fluxAbilities;
   private d lifeAbilities;
   private e puffAbilities;
   private f speedAbilities;
   private g strengthAbilities;
   private h wealthAbilities;
   private dev.xoperr.blissgems.core.b.c protectionManager;
   private dev.xoperr.blissgems.core.b.b particleManager;
   private dev.xoperr.blissgems.core.b.e textManager;
   private dev.xoperr.blissgems.core.b.a autoEnchantManager;
   private dev.xoperr.blissgems.core.b.d regionManager;
   private AchievementManager achievementManager;
   private GemRitualManager gemRitualManager;
   private Metrics metrics;

   public void onEnable() {
      this.saveDefaultConfig();

      try {
         CustomItemManager.initialize(this);
      } catch (Exception var41) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: CustomItemManager ===");
         this.getLogger().severe(var41.getMessage());
         var41.printStackTrace();
      }

      try {
         this.protectionManager = new dev.xoperr.blissgems.core.b.c(this);
      } catch (Exception var40) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: ProtectionManager ===");
         this.getLogger().severe(var40.getMessage());
         var40.printStackTrace();
      }

      try {
         this.particleManager = new dev.xoperr.blissgems.core.b.b(this);
      } catch (Exception var39) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: ParticleManager ===");
         this.getLogger().severe(var39.getMessage());
         var39.printStackTrace();
      }

      try {
         this.textManager = new dev.xoperr.blissgems.core.b.e(this);
      } catch (Exception var38) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: TextManager ===");
         this.getLogger().severe(var38.getMessage());
         var38.printStackTrace();
      }

      try {
         this.autoEnchantManager = new dev.xoperr.blissgems.core.b.a(this);
      } catch (Exception var37) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: AutoEnchantManager ===");
         this.getLogger().severe(var37.getMessage());
         var37.printStackTrace();
      }

      try {
         this.regionManager = new dev.xoperr.blissgems.core.b.d(this);
      } catch (Exception var36) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: RegionManager ===");
         this.getLogger().severe(var36.getMessage());
         var36.printStackTrace();
      }

      try {
         dev.xoperr.blissgems.core.a.c.a.a(this.protectionManager);
         dev.xoperr.blissgems.core.a.b.b.a(this.particleManager);
         dev.xoperr.blissgems.core.a.e.a.a(this.textManager);
         dev.xoperr.blissgems.core.a.a.a.a(this.autoEnchantManager);
         dev.xoperr.blissgems.core.a.d.a.a(this.regionManager);
      } catch (Exception var35) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: Core APIs ===");
         this.getLogger().severe(var35.getMessage());
         var35.printStackTrace();
      }

      try {
         this.configManager = new ConfigManager(this);
      } catch (Exception var34) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: ConfigManager ===");
         this.getLogger().severe(var34.getMessage());
         var34.printStackTrace();
      }

      try {
         this.energyManager = new EnergyManager(this);
      } catch (Exception var33) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: EnergyManager ===");
         this.getLogger().severe(var33.getMessage());
         var33.printStackTrace();
      }

      try {
         this.gemManager = new GemManager(this);
      } catch (Exception var32) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: GemManager ===");
         this.getLogger().severe(var32.getMessage());
         var32.printStackTrace();
      }

      try {
         this.abilityManager = new AbilityManager(this);
      } catch (Exception var31) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: AbilityManager ===");
         this.getLogger().severe(var31.getMessage());
         var31.printStackTrace();
      }

      try {
         this.abilityBindingManager = new AbilityBindingManager(this);
      } catch (Exception var30) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: AbilityBindingManager ===");
         this.getLogger().severe(var30.getMessage());
         var30.printStackTrace();
      }

      try {
         this.passiveManager = new PassiveManager(this);
      } catch (Exception var29) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: PassiveManager ===");
         this.getLogger().severe(var29.getMessage());
         var29.printStackTrace();
      }

      try {
         this.clickActivationManager = new ClickActivationManager(this);
      } catch (Exception var28) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: ClickActivationManager ===");
         this.getLogger().severe(var28.getMessage());
         var28.printStackTrace();
      }

      try {
         this.trustedPlayersManager = new TrustedPlayersManager(this);
      } catch (Exception var27) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: TrustedPlayersManager ===");
         this.getLogger().severe(var27.getMessage());
         var27.printStackTrace();
      }

      try {
         this.repairKitManager = new RepairKitManager(this);
      } catch (Exception var26) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: RepairKitManager ===");
         this.getLogger().severe(var26.getMessage());
         var26.printStackTrace();
      }

      try {
         this.reviveBeaconManager = new ReviveBeaconManager(this);
      } catch (Exception var25) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: ReviveBeaconManager ===");
         this.getLogger().severe(var25.getMessage());
         var25.printStackTrace();
      }

      try {
         this.soulManager = new SoulManager(this);
      } catch (Exception var24) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: SoulManager ===");
         this.getLogger().severe(var24.getMessage());
         var24.printStackTrace();
      }

      try {
         this.flowStateManager = new FlowStateManager(this);
      } catch (Exception var23) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: FlowStateManager ===");
         this.getLogger().severe(var23.getMessage());
         var23.printStackTrace();
      }

      try {
         this.criticalHitManager = new CriticalHitManager(this);
      } catch (Exception var22) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: CriticalHitManager ===");
         this.getLogger().severe(var22.getMessage());
         var22.printStackTrace();
      }

      try {
         this.pluginMessagingManager = new PluginMessagingManager(this);
      } catch (Exception var21) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: PluginMessagingManager ===");
         this.getLogger().severe(var21.getMessage());
         var21.printStackTrace();
      }

      try {
         this.astraAbilities = new a(this);
      } catch (Exception var20) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: AstraAbilities ===");
         this.getLogger().severe(var20.getMessage());
         var20.printStackTrace();
      }

      try {
         this.fireAbilities = new b(this);
      } catch (Exception var19) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: FireAbilities ===");
         this.getLogger().severe(var19.getMessage());
         var19.printStackTrace();
      }

      try {
         this.fluxAbilities = new c(this);
      } catch (Exception var18) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: FluxAbilities ===");
         this.getLogger().severe(var18.getMessage());
         var18.printStackTrace();
      }

      try {
         this.lifeAbilities = new d(this);
      } catch (Exception var17) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: LifeAbilities ===");
         this.getLogger().severe(var17.getMessage());
         var17.printStackTrace();
      }

      try {
         this.puffAbilities = new e(this);
      } catch (Exception var16) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: PuffAbilities ===");
         this.getLogger().severe(var16.getMessage());
         var16.printStackTrace();
      }

      try {
         this.speedAbilities = new f(this);
      } catch (Exception var15) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: SpeedAbilities ===");
         this.getLogger().severe(var15.getMessage());
         var15.printStackTrace();
      }

      try {
         this.strengthAbilities = new g(this);
      } catch (Exception var14) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: StrengthAbilities ===");
         this.getLogger().severe(var14.getMessage());
         var14.printStackTrace();
      }

      try {
         this.wealthAbilities = new h(this);
      } catch (Exception var13) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: WealthAbilities ===");
         this.getLogger().severe(var13.getMessage());
         var13.printStackTrace();
      }

      try {
         this.gemRegistry = new GemRegistryImpl(this);
         this.registerBuiltInGems();
         this.getServer().getServicesManager().register(BlissGemsAPI.class, this, this, ServicePriority.Normal);
         this.getLogger().info("BlissGems Addon API registered via ServicesManager");
      } catch (Exception var12) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: GemRegistry/API ===");
         this.getLogger().severe(var12.getMessage());
         var12.printStackTrace();
      }

      try {
         this.cooldownDisplayManager = new CooldownDisplayManager(this);
      } catch (Exception var11) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: CooldownDisplayManager ===");
         this.getLogger().severe(var11.getMessage());
         var11.printStackTrace();
      }

      try {
         this.statsManager = new StatsManager(this);
      } catch (Exception var10) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: StatsManager ===");
         this.getLogger().severe(var10.getMessage());
         var10.printStackTrace();
      }

      try {
         this.achievementManager = new AchievementManager(this);
      } catch (Exception var9) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: AchievementManager ===");
         this.getLogger().severe(var9.getMessage());
         var9.printStackTrace();
      }

      try {
         this.enhancedGuiManager = new EnhancedGuiManager(this);
      } catch (Exception var8) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: EnhancedGuiManager ===");
         this.getLogger().severe(var8.getMessage());
         var8.printStackTrace();
      }

      try {
         this.recipeManager = new RecipeManager(this);
         this.gemRitualManager = new GemRitualManager(this);
      } catch (Exception var7) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: RecipeManager/GemRitualManager ===");
         this.getLogger().severe(var7.getMessage());
         var7.printStackTrace();
      }

      try {
         if (this.recipeManager != null) {
            this.recipeManager.registerRecipes();
         }
      } catch (Exception var6) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: Recipe Registration ===");
         this.getLogger().severe(var6.getMessage());
         var6.printStackTrace();
      }

      try {
         this.checkAutoStartSmp();
      } catch (Exception var5) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: SMP Auto-Start Check ===");
         this.getLogger().severe(var5.getMessage());
         var5.printStackTrace();
      }

      try {
         this.registerListeners();
      } catch (Exception var4) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: Listener Registration ===");
         this.getLogger().severe(var4.getMessage());
         var4.printStackTrace();
      }

      try {
         this.registerCommands();
      } catch (Exception var3) {
         this.getLogger().severe("=== BLISSGEMS FAILED TO INITIALIZE: Command Registration ===");
         this.getLogger().severe(var3.getMessage());
         var3.printStackTrace();
      }

      if (this.getConfig().getBoolean("send-anonymous-metrics", true)) {
         try {
            this.metrics = ((BukkitMetrics.Factory)((BukkitMetrics.Factory)BukkitMetrics.factory().token("33b82f6ed2f61ee4be22345da22fbf24")).addMetric(Metric.number("active_gem_players", () -> {
               int var1 = 0;

               for(Player var3 : this.getServer().getOnlinePlayers()) {
                  if (this.gemManager != null && this.gemManager.hasGemInOffhand(var3)) {
                     ++var1;
                  }
               }

               return var1;
            }))).create((Plugin)this);
            this.metrics.ready();
         } catch (Exception var2) {
            this.getLogger().warning("FastStats metrics failed to initialize: " + var2.getMessage());
         }
      }

      this.getLogger().info("BlissGems has been enabled!");
      this.getLogger().info("Version: " + this.getDescription().getVersion());
      this.getLogger().info("Using custom item system with vanilla Minecraft items");
   }

   public void onDisable() {
      if (this.particleManager != null) {
         this.particleManager.c();
      }

      if (this.textManager != null) {
         this.textManager.a();
      }

      if (this.autoEnchantManager != null) {
         this.autoEnchantManager.b();
      }

      if (this.cooldownDisplayManager != null) {
         this.cooldownDisplayManager.stop();
      }

      if (this.repairKitManager != null) {
         this.repairKitManager.cleanup();
      }

      if (this.reviveBeaconManager != null) {
         this.reviveBeaconManager.cleanup();
      }

      if (this.pluginMessagingManager != null) {
         this.pluginMessagingManager.shutdown();
      }

      if (this.recipeManager != null) {
         this.recipeManager.unregisterRecipes();
      }

      for(Player var2 : Bukkit.getOnlinePlayers()) {
         if (this.astraAbilities != null) {
            this.astraAbilities.cleanup(var2);
         }

         if (this.fireAbilities != null) {
            this.fireAbilities.cleanup(var2);
         }

         if (this.fluxAbilities != null) {
            this.fluxAbilities.cleanup(var2);
         }

         if (this.strengthAbilities != null) {
            this.strengthAbilities.cleanup(var2);
         }

         if (this.lifeAbilities != null) {
            this.lifeAbilities.cleanup(var2);
         }
      }

      if (this.metrics != null) {
         this.metrics.shutdown();
      }

      this.energyManager.saveAll();
      if (this.wealthAbilities != null) {
         this.wealthAbilities.a();
      }

      if (this.abilityManager != null) {
         this.abilityManager.saveAllCooldowns();
      }

      if (this.achievementManager != null) {
         this.achievementManager.saveAll();
      }

      this.getLogger().info("BlissGems has been disabled!");
   }

   private void checkAutoStartSmp() {
      if (this.configManager != null && !this.configManager.isSmpStarted()) {
         File var1 = new File(this.getDataFolder(), "playerdata");
         if (var1.exists() && var1.isDirectory()) {
            int var2 = this.configManager.getSmpAutoStartThreshold();
            int var3 = 0;
            File[] var4 = var1.listFiles((var0, var1x) -> var1x.endsWith(".yml"));
            if (var4 != null) {
               for(File var8 : var4) {
                  YamlConfiguration var9 = YamlConfiguration.loadConfiguration(var8);
                  if (((FileConfiguration)var9).getBoolean("received-first-gem", false)) {
                     ++var3;
                     if (var3 >= var2) {
                        this.configManager.setSmpStarted(true);
                        this.getLogger().info("SMP auto-started! Found " + var3 + " players with gems (threshold: " + var2 + ").");
                        return;
                     }
                  }
               }

            }
         }
      }
   }

   private void registerListeners() {
      this.getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
      this.getServer().getPluginManager().registerEvents(new ComprehensiveGemProtectionListener(this), this);
      this.getServer().getPluginManager().registerEvents(new GemInteractListener(this), this);
      this.getServer().getPluginManager().registerEvents(new UpgraderListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PassiveListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
      this.getServer().getPluginManager().registerEvents(new AutoEnchantListener(this), this);
      this.getServer().getPluginManager().registerEvents(new StunListener(this), this);
      this.getServer().getPluginManager().registerEvents(new RepairKitListener(this), this);
      this.getServer().getPluginManager().registerEvents(new ReviveBeaconListener(this), this);
      this.getServer().getPluginManager().registerEvents(new KillTrackingListener(this), this);
      this.getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
      this.getServer().getPluginManager().registerEvents(new VillagerTradeListener(this), this);
      this.getServer().getPluginManager().registerEvents(new SwapHandAbilityListener(this), this);
      this.getServer().getPluginManager().registerEvents(this.enhancedGuiManager, this);
   }

   private void registerCommands() {
      this.blissCommand = new BlissCommand(this);
      this.getCommand("bliss").setExecutor(this.blissCommand);
      this.getCommand("bliss").setTabCompleter(this.blissCommand);
      FixHeartsCommand var1 = new FixHeartsCommand(this);
      if (this.getCommand("fixhearts") != null) {
         this.getCommand("fixhearts").setExecutor(var1);
         this.getCommand("fixhearts").setTabCompleter(var1);
      }

   }

   public BlissCommand getBlissCommand() {
      return this.blissCommand;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public EnergyManager getEnergyManager() {
      return this.energyManager;
   }

   public GemManager getGemManager() {
      return this.gemManager;
   }

   public AbilityManager getAbilityManager() {
      return this.abilityManager;
   }

   public AbilityBindingManager getAbilityBindingManager() {
      return this.abilityBindingManager;
   }

   public PassiveManager getPassiveManager() {
      return this.passiveManager;
   }

   public RecipeManager getRecipeManager() {
      return this.recipeManager;
   }

   public GemRitualManager getGemRitualManager() {
      return this.gemRitualManager;
   }

   public a getAstraAbilities() {
      return this.astraAbilities;
   }

   public b getFireAbilities() {
      return this.fireAbilities;
   }

   public c getFluxAbilities() {
      return this.fluxAbilities;
   }

   public d getLifeAbilities() {
      return this.lifeAbilities;
   }

   public e getPuffAbilities() {
      return this.puffAbilities;
   }

   public f getSpeedAbilities() {
      return this.speedAbilities;
   }

   public g getStrengthAbilities() {
      return this.strengthAbilities;
   }

   public h getWealthAbilities() {
      return this.wealthAbilities;
   }

   public RepairKitManager getRepairKitManager() {
      return this.repairKitManager;
   }

   public ReviveBeaconManager getReviveBeaconManager() {
      return this.reviveBeaconManager;
   }

   public SoulManager getSoulManager() {
      return this.soulManager;
   }

   public FlowStateManager getFlowStateManager() {
      return this.flowStateManager;
   }

   public CriticalHitManager getCriticalHitManager() {
      return this.criticalHitManager;
   }

   public ClickActivationManager getClickActivationManager() {
      return this.clickActivationManager;
   }

   public TrustedPlayersManager getTrustedPlayersManager() {
      return this.trustedPlayersManager;
   }

   public EnhancedGuiManager getEnhancedGuiManager() {
      return this.enhancedGuiManager;
   }

   public StatsManager getStatsManager() {
      return this.statsManager;
   }

   public AchievementManager getAchievementManager() {
      return this.achievementManager;
   }

   public PluginMessagingManager getPluginMessagingManager() {
      return this.pluginMessagingManager;
   }

   public dev.xoperr.blissgems.core.b.c getProtectionManager() {
      return this.protectionManager;
   }

   public dev.xoperr.blissgems.core.b.b getParticleManager() {
      return this.particleManager;
   }

   public dev.xoperr.blissgems.core.b.e getTextManager() {
      return this.textManager;
   }

   public dev.xoperr.blissgems.core.b.a getAutoEnchantManager() {
      return this.autoEnchantManager;
   }

   public dev.xoperr.blissgems.core.b.d getRegionManager() {
      return this.regionManager;
   }

   public GemRegistryImpl getGemRegistry() {
      return this.gemRegistry;
   }

   public boolean playerHasGem(Player var1, String var2) {
      GemManager.ActiveGem var3 = this.gemManager.getActiveGem(var1);
      return var3 == null ? false : var2.equals(var3.getGemId());
   }

   private void registerBuiltInGems() {
      for(GemType var4 : GemType.values()) {
         GemDefinition var5 = (new GemDefinition.Builder(var4.getId())).displayName(var4.getDisplayName()).description(var4.getDescription()).color(var4.getColor()).plugin("BlissGems").maxTier(2).build();
         this.gemRegistry.registerGem(var5);
      }

      if (this.astraAbilities != null) {
         this.gemRegistry.registerAbilities("astra", this.astraAbilities);
      }

      if (this.fireAbilities != null) {
         this.gemRegistry.registerAbilities("fire", this.fireAbilities);
      }

      if (this.fluxAbilities != null) {
         this.gemRegistry.registerAbilities("flux", this.fluxAbilities);
      }

      if (this.lifeAbilities != null) {
         this.gemRegistry.registerAbilities("life", this.lifeAbilities);
      }

      if (this.puffAbilities != null) {
         this.gemRegistry.registerAbilities("puff", this.puffAbilities);
      }

      if (this.speedAbilities != null) {
         this.gemRegistry.registerAbilities("speed", this.speedAbilities);
      }

      if (this.strengthAbilities != null) {
         this.gemRegistry.registerAbilities("strength", this.strengthAbilities);
      }

      if (this.wealthAbilities != null) {
         this.gemRegistry.registerAbilities("wealth", this.wealthAbilities);
      }

      if (this.passiveManager != null) {
         this.passiveManager.registerBuiltInHandlers(this.gemRegistry);
      }

      this.gemRegistry.registerCooldowns("astra", List.of(new CooldownEntry("astra-daggers", "Daggers"), new CooldownEntry("astra-projection", "Projection")));
      this.gemRegistry.registerCooldowns("fire", List.of(new CooldownEntry("fire-fireball", "Fireball"), new CooldownEntry("fire-campfire", "Campfire"), new CooldownEntry("fire-crisp", "Crisp"), new CooldownEntry("fire-meteor-shower", "Meteor")));
      this.gemRegistry.registerCooldowns("flux", List.of(new CooldownEntry("flux-beam", "Beam"), new CooldownEntry("flux-ground", "Ground"), new CooldownEntry("flux-flashbang", "Flash"), new CooldownEntry("flux-kinetic-burst", "Kinetic")));
      this.gemRegistry.registerCooldowns("life", List.of(new CooldownEntry("life-drainer", "Drainer"), new CooldownEntry("life-circle-of-life", "Circle"), new CooldownEntry("life-vitality-vortex", "Vortex"), new CooldownEntry("life-heart-lock", "Lock")));
      this.gemRegistry.registerCooldowns("puff", List.of(new CooldownEntry("puff-dash", "Dash"), new CooldownEntry("puff-breezy-bash", "Bash"), new CooldownEntry("puff-group-bash", "Group")));
      this.gemRegistry.registerCooldowns("speed", List.of(new CooldownEntry("speed-blur", "Blur"), new CooldownEntry("speed-storm", "Storm"), new CooldownEntry("speed-terminal", "Terminal")));
      this.gemRegistry.registerCooldowns("strength", List.of(new CooldownEntry("strength-nullify", "Nullify"), new CooldownEntry("strength-frailer", "Frailer"), new CooldownEntry("strength-shadow-stalker", "Stalker")));
      this.gemRegistry.registerCooldowns("wealth", List.of(new CooldownEntry("wealth-unfortunate", "Unfortunate"), new CooldownEntry("wealth-rich-rush", "Rush"), new CooldownEntry("wealth-item-lock", "Lock"), new CooldownEntry("wealth-amplification", "Amplify")));
   }
}
