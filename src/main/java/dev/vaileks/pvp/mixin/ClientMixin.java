package dev.vaileks.pvp.mixin;
import dev.vaileks.pvp.Core;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MinecraftClient.class)
abstract class ClientMixin {
 @Inject(method="tick",at=@At("TAIL")) private void pvp$tick(CallbackInfo ci){Core.tick();}
}
