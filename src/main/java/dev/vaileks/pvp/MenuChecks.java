package dev.vaileks.pvp;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
final class MenuChecks {
 static void check(){
  int matched=0;
  for(var entry:FabricLoader.getInstance().getEntrypointContainers("modmenu",ModMenuApi.class)){
   String id=entry.getProvider().getMetadata().getId();
   for(Feature f:Core.roots.keySet())if(f.id.equals(id)){
    var screen=entry.getEntrypoint().getModConfigScreenFactory().create(null);
    Smoke.check(screen instanceof SettingsScreen,"Mod Menu factory: "+id);matched++;
   }
  }
  Smoke.check(matched==Core.roots.size(),"all installed features have independent Mod Menu entries");
 }
}
