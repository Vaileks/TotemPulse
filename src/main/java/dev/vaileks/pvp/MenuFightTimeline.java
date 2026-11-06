package dev.vaileks.pvp;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
public final class MenuFightTimeline implements ModMenuApi {
 public ConfigScreenFactory<?> getModConfigScreenFactory() { return parent -> new SettingsScreen(parent,Feature.FIGHT_TIMELINE); }
}
