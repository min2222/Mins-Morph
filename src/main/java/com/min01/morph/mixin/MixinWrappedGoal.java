package com.min01.morph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.morph.misc.IWrappedGoal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

@Mixin(WrappedGoal.class)
public class MixinWrappedGoal implements IWrappedGoal
{
	@Unique
	private Mob mob;
	
	@Unique
	private LivingEntity target;
	
	@Unique
	private boolean canUse;
	
	@Unique
	private int tick;
	
	@Unique
	private int lastTick;
	
	@Inject(at = @At(value = "RETURN"), method = "canUse", cancellable = true)
	private void canUse(CallbackInfoReturnable<Boolean> cir)
	{
		if(this.mob != null && this.mob.getId() < 0)
		{
			cir.setReturnValue(cir.getReturnValue() || this.canUse);
		}
	}
	
	@Inject(at = @At(value = "RETURN"), method = "canContinueToUse", cancellable = true)
	private void canContinueToUse(CallbackInfoReturnable<Boolean> cir)
	{
		if(this.mob != null && this.mob.getId() < 0)
		{
			cir.setReturnValue(cir.getReturnValue() || this.canUse);
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
	private void tick(CallbackInfo ci)
	{
		this.tick++;
		if(this.mob != null && this.mob.getId() < 0 && this.lastTick != 0)
		{
			if(this.tick == this.lastTick)
			{
				WrappedGoal.class.cast(this).stop();
			}
			if(this.target != null)
			{
				this.mob.setTarget(this.target);
			}
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "stop", cancellable = true)
	private void stop(CallbackInfo ci)
	{
		if(this.mob != null && this.mob.getId() < 0)
		{
			this.mob.setTarget(null);
			this.canUse = false;
			this.target = null;
			this.tick = 0;
		}
		if(this.tick != 0)
		{
			this.lastTick = this.tick;
			this.tick = 0;
		}
	}
	
	@Override
	public void setEntity(Mob mob)
	{
		this.mob = mob;
	}
	
	@Override
	public void setFakeTarget(LivingEntity target)
	{
		this.target = target;
	}

	@Override
	public void setCanUse() 
	{
		this.canUse = true;
	}
	
	@Override
	public void setLastTick(int tick) 
	{
		this.lastTick = tick;
	}
	
	@Override
	public int getLastTick() 
	{
		return this.lastTick;
	}
}
