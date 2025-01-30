package com.min01.morph.mixin;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.entity.EntityFakeTarget;
import com.min01.morph.misc.IWrappedGoal;
import com.min01.morph.util.MorphUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

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
	
	@Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
	private void tick(CallbackInfo ci)
	{
		Set<WrappedGoal> set = Mob.class.cast(this).goalSelector.getAvailableGoals();
		List<WrappedGoal> list = Lists.newArrayList(set);
		for(WrappedGoal goal : set)
		{
			((IWrappedGoal)goal).setEntity(Mob.class.cast(this));
			((IWrappedGoal)goal).setIndex(list.indexOf(goal));
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
