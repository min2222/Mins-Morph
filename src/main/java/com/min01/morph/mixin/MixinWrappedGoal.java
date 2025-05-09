package com.min01.morph.mixin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.morph.misc.IWrappedGoal;
import com.min01.morph.util.MorphUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

@Mixin(WrappedGoal.class)
public class MixinWrappedGoal implements IWrappedGoal
{
	@Unique
	private Mob mob;
	
	@Unique
	private LivingEntity target;
	
	@Unique
	private LivingEntity fakeTarget;
	
	@Unique
	private boolean canUse;
	
	@Shadow
	private Goal goal;
	
	@Inject(at = @At(value = "RETURN"), method = "canUse", cancellable = true)
	private void canUse(CallbackInfoReturnable<Boolean> cir)
	{
		if(this.mob != null && MorphUtil.isMorph(this.mob))
		{
			if(this.mob.getClass().getSimpleName().equals("EntityCorpseWarlock"))
			{
				cir.setReturnValue(cir.getReturnValue() || this.canUse);
			}
			else
			{
				cir.setReturnValue(this.canUse);
			}
		}
	}
	
	@Inject(at = @At(value = "RETURN"), method = "canContinueToUse", cancellable = true)
	private void canContinueToUse(CallbackInfoReturnable<Boolean> cir)
	{
		if(this.mob != null && MorphUtil.isMorph(this.mob))
		{
			try 
			{
				Method m = this.mob.getClass().getMethod("getAnimationTick");
				int animationTick = (int) m.invoke(this.mob);
				cir.setReturnValue(animationTick > 0);
			}
			catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				
			}
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
	private void tick(CallbackInfo ci)
	{
		if(this.mob != null && MorphUtil.isMorph(this.mob))
		{
			if(this.target != null)
			{
				if(this.target.isAlive())
				{
					this.mob.setTarget(this.target);
				}
				else
				{
					this.target = null;
				}
			}
			else if(this.fakeTarget != null)
			{
				this.mob.setTarget(this.fakeTarget);
			}
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "stop", cancellable = true)
	private void stop(CallbackInfo ci)
	{
		if(this.mob != null && MorphUtil.isMorph(this.mob))
		{
			this.mob.setTarget(null);
			this.canUse = false;
			this.target = null;
			this.fakeTarget = null;
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
		this.fakeTarget = target;
	}
	
	@Override
	public void setTarget(LivingEntity target)
	{
		this.target = target;
	}

	@Override
	public void setCanUse() 
	{
		this.canUse = true;
	}
}
