package dev.vaileks.pvp.mixin;
import dev.vaileks.pvp.Core;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ClientPlayerInteractionManager.class)
abstract class UseMixin {
 @Unique private ItemStack pvp$before=ItemStack.EMPTY;
 @Inject(method="interactItem",at=@At("HEAD")) private void pvp$before(PlayerEntity p,Hand h,CallbackInfoReturnable<ActionResult> ci){pvp$before=p.getStackInHand(h).copy();}
 @Inject(method="interactItem",at=@At("RETURN")) private void pvp$after(PlayerEntity p,Hand h,CallbackInfoReturnable<ActionResult> ci){if(ci.getReturnValue().isAccepted())Core.used(pvp$before,false);}
}
