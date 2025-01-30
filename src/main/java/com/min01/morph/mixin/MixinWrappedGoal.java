package com.min01.morph.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.morph.misc.IWrappedGoal;
import com.min01.morph.util.world.MorphSavedData;

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
	private int index;
	
	@Inject(at = @At(value = "HEAD"), method = "start", cancellable = true)
	private void start(CallbackInfo ci)
	{
		if(this.mob != null && this.mob.getId() > 0)
		{
			try
			{
				Method m = this.mob.getClass().getMethod("getAnimation");
				Object obj = m.invoke(this.mob);
				for(Field f : this.mob.getClass().getDeclaredFields())
				{
					if(f.getType().getSimpleName().equals("Animation"))
					{
						f.setAccessible(true);
						if(obj == f.get(this.mob))
						{
							MorphSavedData data = MorphSavedData.get(this.mob.level);
				        	if(data != null)
				        	{
				        		data.saveAnimation(this.index, f.getName());
				        	}
						}
					}
				}
			}
			catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				
			}
		}
	}
	
	@Inject(at = @At(value = "RETURN"), method = "canUse", cancellable = true)
	private void canUse(CallbackInfoReturnable<Boolean> cir)
	{
		if(this.mob != null && this.mob.getId() < 0)
		{
			cir.setReturnValue(cir.getReturnValue() || this.canUse);
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
	private void tick(CallbackInfo ci)
	{
		if(this.mob != null && this.mob.getId() < 0)
		{
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
	public void setIndex(int index) 
	{
		this.index = index;
	}
}
