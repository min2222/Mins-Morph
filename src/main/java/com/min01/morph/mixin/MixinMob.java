package com.min01.morph.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.entity.EntityFakeTarget;
import com.min01.morph.util.MorphUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

@Mixin(Mob.class)
public class MixinMob 
{
	@Inject(at = @At(value = "HEAD"), method = "serverAiStep", cancellable = true)
	private final void serverAiStep(CallbackInfo ci)
	{
		if(Mob.class.cast(this).getId() < 0)
		{
			ci.cancel();
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "setTarget", cancellable = true)
	private void setTarget(@Nullable LivingEntity target, CallbackInfo ci)
	{
		if(MorphUtil.getMorphOwner(Mob.class.cast(this)) != null)
		{
			if(MorphUtil.getMorphOwner(Mob.class.cast(this)) == target)
			{
				ci.cancel();
			}
		}
		if(target != null)
		{
			if(MorphUtil.getMorphOwner(target) != null)
			{
				ci.cancel();
				Mob.class.cast(this).setTarget((LivingEntity) MorphUtil.getMorphOwner(target));
			}
			target.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
			{
	    		LivingEntity morph = t.getMorph();
	    		if(morph != null)
	    		{
	    			if(morph.getType() == Mob.class.cast(this).getType())
	    			{
	    				ci.cancel();
	    			}
	    		}
			});
			if(MorphUtil.getMorphOwner(Mob.class.cast(this)) == null && target instanceof EntityFakeTarget)
			{
				ci.cancel();
			}
		}
	}
}
