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

	@Unique
	private boolean manualActive;

	@Unique
	private int manualGraceTicks;
	
	@Shadow
	private Goal goal;
	
	@Inject(at = @At(value = "RETURN"), method = "canUse", cancellable = true)
	private void canUse(CallbackInfoReturnable<Boolean> cir)
	{
		if(this.mob != null && MorphUtil.isMorph(this.mob))
		{
			boolean inManualWindow = this.mob.getPersistentData().getInt(MorphUtil.MANUAL_TRIGGER_TICKS_KEY) > 0;
			boolean forced = this.canUse;
			// Consume one-shot arm flag so unrelated future ticks cannot auto-retrigger.
			if(forced)
			{
				this.canUse = false;
			}
			// Some goals still require original canUse path when manually activated.
			boolean allowOriginalPath = (this.manualActive || inManualWindow) && cir.getReturnValue();
			cir.setReturnValue(forced || allowOriginalPath);
		}
	}
	
	@Inject(at = @At(value = "RETURN"), method = "canContinueToUse", cancellable = true)
	private void canContinueToUse(CallbackInfoReturnable<Boolean> cir)
	{
		if(this.mob != null && MorphUtil.isMorph(this.mob))
		{
			boolean inManualWindow = this.mob.getPersistentData().getInt(MorphUtil.MANUAL_TRIGGER_TICKS_KEY) > 0;
			if(!this.manualActive && !inManualWindow)
			{
				cir.setReturnValue(false);
				return;
			}
			if(this.manualGraceTicks > 0)
			{
				cir.setReturnValue(true);
				return;
			}
			try 
			{
				Method m = this.mob.getClass().getMethod("getAnimationTick");
				int animationTick = (int) m.invoke(this.mob);
				// Keep goal active only while an animation is actually progressing.
				cir.setReturnValue(cir.getReturnValue() || animationTick > 0);
			}
			catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				// For non-animated goals, defer to original continuation logic.
				cir.setReturnValue(cir.getReturnValue());
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
			if(this.manualGraceTicks > 0)
			{
				this.manualGraceTicks--;
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
			this.manualActive = false;
			this.manualGraceTicks = 0;
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
		this.manualActive = true;
		this.manualGraceTicks = 10;
	}
}
