package com.min01.morph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.morph.util.MorphUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

@Mixin(Projectile.class)
public class MixinProjectile 
{
	@Inject(at = @At(value = "RETURN"), method = "getOwner", cancellable = true)
	private void getOwner(CallbackInfoReturnable<Entity> cir)
	{
		if(cir.getReturnValue() != null)
		{
			if(MorphUtil.getMorphOwner(cir.getReturnValue()) != null)
			{
				cir.setReturnValue(MorphUtil.getMorphOwner(cir.getReturnValue()));
			}
		}
	}
}
