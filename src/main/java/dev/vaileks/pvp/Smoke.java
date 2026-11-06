package dev.vaileks.pvp;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.potion.Potions;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.world.*;
import net.minecraft.world.gen.*;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.rule.GameRules;
import net.minecraft.util.math.Vec3d;
import java.util.*;
import static dev.vaileks.pvp.Feature.*;

/** Opt-in integration QA. Runs only with -Psmoke; writes to the isolated Gradle run directory. */
final class Smoke {
 static int step,worldTicks,assertions;static boolean started,inWorld,tested;static String lastShot;
 static void check(boolean result,String name){if(!result)throw new AssertionError("PVP_QA_FAILED "+name);assertions++;Core.LOG.info("QA_PASS {}",name);}
 static void tick(){
  MinecraftClient mc=MinecraftClient.getInstance();
  if(Core.ticks<80)return;
  if(!started){
   if(Core.ticks%10!=0)return;
   if(step>0&&step%4==1){Feature prev=Feature.values()[(step-1)/4];shot("qa-"+prev.id+".png");}
   if(step<Feature.values().length*4){Feature f=Feature.values()[step/4];SettingsScreen screen=new SettingsScreen(null,f);screen.page=step%4;mc.setScreen(screen);Core.LOG.info("QA_SCREEN {} page {}",f.id,step%4);step++;return;}
   mc.setScreen(null);started=true;
   LevelInfo info=new LevelInfo("PvP QA",GameMode.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(FeatureFlags.DEFAULT_ENABLED_FEATURES),DataConfiguration.SAFE_MODE);
   mc.createIntegratedServerLoader().createAndStart("pvp-qa-"+System.currentTimeMillis(),info,GeneratorOptions.createTestWorld(),WorldPresets::createTestOptions,null);
   return;
  }
  if(mc.player==null||mc.world==null)return;
  inWorld=true;worldTicks++;
  if(worldTicks==30&&!tested){test();tested=true;mc.setScreen(new InventoryScreen(mc.player));}
  if(worldTicks==45)shot("qa-inventory.png");
  if(worldTicks==70){mc.setScreen(new SettingsScreen(null,ARMOR_PANIC));}
  if(worldTicks==85)shot("qa-armor-live.png");
  if(worldTicks==110){check(!Core.pearlResults.isEmpty(),"pearl server collision / exact timer");Core.LOG.info("PVP_QA_OK {} assertions, 72 settings pages, integrated world",assertions);mc.scheduleStop();}
 }
 static void shot(String name){MinecraftClient mc=MinecraftClient.getInstance();ScreenshotRecorder.saveScreenshot(mc.runDirectory,name,mc.getFramebuffer(),1,t->Core.LOG.info("QA_SCREENSHOT {}",name));}
 static void test(){
  if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("modmenu"))MenuChecks.check();
  MinecraftClient mc=MinecraftClient.getInstance();var player=mc.player;var inv=player.getInventory();
  for(int i=0;i<inv.size();i++)inv.setStack(i,ItemStack.EMPTY);
  inv.setStack(0,new ItemStack(Items.ENDER_PEARL,2));inv.setStack(10,new ItemStack(Items.ENDER_PEARL,16));inv.setStack(1,new ItemStack(Items.GOLDEN_APPLE,8));inv.setStack(11,new ItemStack(Items.TOTEM_OF_UNDYING,2));
  ItemStack heal=PotionContentsComponent.createStack(Items.SPLASH_POTION,Potions.STRONG_HEALING),speed=PotionContentsComponent.createStack(Items.POTION,Potions.SWIFTNESS);
  inv.setStack(2,heal);inv.setStack(12,speed);
  ItemStack armor=new ItemStack(Items.DIAMOND_CHESTPLATE);armor.setDamage(armor.getMaxDamage()-20);player.equipStack(EquipmentSlot.CHEST,armor);
  check(!Core.key(heal).equals(Core.key(speed)),"different potion identities");
  Config hot=Core.config(HOTBAR_MEMORY);Core.capture(HOTBAR_MEMORY,hot);String expected=hot.hotbar.get(0).key();inv.setStack(0,new ItemStack(Items.ENDER_PEARL,1));check(expected.equals(Core.key(inv.getStack(0))),"hotbar ignores counts");
  ItemStack sword=new ItemStack(Items.DIAMOND_SWORD);String fresh=Core.key(sword);sword.setDamage(25);check(fresh.equals(Core.key(sword)),"hotbar ignores damage");
  Config kit=Core.config(KIT_CHECK);Core.capture(KIT_CHECK,kit);check(Core.missing(kit,true).isEmpty(),"kit baseline matches equipment and inventory");
  inv.setStack(1,ItemStack.EMPTY);check(!Core.missing(kit,true).isEmpty(),"kit detects missing golden apples");
  Config resupply=Core.config(RESUPPLY_LIST);Core.capture(RESUPPLY_LIST,resupply);inv.setStack(10,ItemStack.EMPTY);check(!Core.missing(resupply,false).isEmpty(),"resupply identifies missing reserve");inv.setStack(10,new ItemStack(Items.ENDER_PEARL,16));
  player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,100,1));
  Core.updateRows();check(!Core.list(EFFECT_ALARM).isEmpty(),"effect alarm near expiration");check(!Core.list(ARMOR_PANIC).isEmpty(),"armor alarm below threshold");check(!Core.list(REFILL_REMINDER).isEmpty(),"refill with existing reserve");check(!Core.list(OFFHAND_ALERT).isEmpty(),"offhand missing expected item");
  inv.setStack(0,ItemStack.EMPTY);Core.updateRows();check(!Core.list(REFILL_REMINDER).isEmpty(),"refill tracks fully depleted hotbar");
  inv.setStack(0,new ItemStack(Items.ENDER_PEARL,4));player.getItemCooldownManager().set(inv.getStack(0),40);Core.updateRows();check(!Core.cooldowns.isEmpty(),"cooldown captured");player.getItemCooldownManager().remove(player.getItemCooldownManager().getGroup(inv.getStack(0)));Core.updateRows();check(!Core.list(READY_AGAIN).isEmpty(),"ready alert after cooldown removal");
  Core.status(new EntityStatusS2CPacket(player,(byte)35));Core.updateRows();check(!Core.list(TOTEM_PULSE).isEmpty(),"totem pulse uses packet event");
  player.equipStack(EquipmentSlot.OFFHAND,new ItemStack(Items.TOTEM_OF_UNDYING));Core.updateRows();check(Core.list(OFFHAND_ALERT).isEmpty(),"offhand clears when restored");
  check(Core.matchesCategory(heal,0)&&!Core.matchesCategory(speed,0),"search healing filters potion effect");check(Core.matchesCategory(armor,1),"search armor");check(Core.matchesCategory(inv.getStack(0),2),"search pearl");
  Core.config(HUD_ON_DEMAND).enabled=false;check(!Core.hideVanilla("scoreboard"),"vanilla scoreboard visible outside combat");
  Core.startFight("QA opponent");check(Core.hideVanilla("scoreboard"),"focus hides vanilla scoreboard in combat");check(Core.visible(ARMOR_PANIC,new Core.Row("armor","low",true)),"critical alerts survive focus");Core.used(new ItemStack(Items.GOLDEN_APPLE),false);check(Core.used.isEmpty(),"starting food use is not consumption");Core.used(new ItemStack(Items.GOLDEN_APPLE),true);Core.used(heal,false);Core.status(new EntityStatusS2CPacket(player,(byte)35));
  check(Core.used.values().stream().mapToInt(Integer::intValue).sum()==2,"fight counts consumption and throw");check(Core.timeline.size()>=4,"timeline tracks own combat events");Core.finishFight();check(Core.lastTotems==1,"recap resets totems per fight");check(!Core.lastTimeline.isEmpty(),"timeline survives fight finish");
  Core.config(DUEL_NOTES).notes.merge("QA note",1,Integer::sum);Core.save(DUEL_NOTES,Core.config(DUEL_NOTES));check(Config.load(DUEL_NOTES).profiles.containsKey(Core.context),"server profiles persisted");
  Config before=Core.config(ARMOR_PANIC),draft=before.copy();draft.color=0;check(Core.config(ARMOR_PANIC).color!=0,"cancelled appearance draft isolated");
  String context=Core.context;Core.context="server:qa.example";Core.config(ARMOR_PANIC).threshold=7;Core.context=context;check(Core.config(ARMOR_PANIC).threshold!=7,"independent settings per server");
  Config malformed=Config.defaults(READY_AGAIN);malformed.x=Double.NaN;malformed.scale=Double.POSITIVE_INFINITY;malformed.threshold=-4;malformed.sanitize(READY_AGAIN);check(Double.isFinite(malformed.x)&&Double.isFinite(malformed.scale)&&malformed.threshold==1,"invalid settings sanitized");
  Config cfg=Core.config(INVENTORY_SEARCH_FLASH);cfg.mode=0;Core.searchUntil=Core.now()+20_000;
  for(Feature f:Feature.values()){Core.config(f).x=(f.ordinal()%3)*0.48;Core.config(f).y=(f.ordinal()%5)*0.2;}
  mc.getServer().execute(()->{
   var serverPlayer=mc.getServer().getPlayerManager().getPlayer(player.getUuid());var world=serverPlayer.getEntityWorld();
   EnderPearlEntity pearl=new EnderPearlEntity(world,serverPlayer,new ItemStack(Items.ENDER_PEARL));pearl.setPosition(serverPlayer.getX(),serverPlayer.getY()+4,serverPlayer.getZ());pearl.setVelocity(new Vec3d(0,-1,0));world.spawnEntity(pearl);
  });
 }
}
