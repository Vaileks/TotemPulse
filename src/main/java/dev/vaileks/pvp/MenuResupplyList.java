package dev.vaileks.pvp;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
public final class MenuResupplyList implements ModMenuApi {
 public ConfigScreenFactory<?> getModConfigScreenFactory() { return parent -> new SettingsScreen(parent,Feature.RESUPPLY_LIST); }
}
