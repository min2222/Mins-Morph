package com.min01.morph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.util.MorphUtil;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public class MixinEntity 
{
	@Inject(at = @At(value = "HEAD"), method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", cancellable = true)
	private void isAlliedTo(Entity entity, CallbackInfoReturnable<Boolean> cir)
	{
		if(MorphUtil.getMorphOwner(Entity.class.cast(this)) != null)
		{
			if(entity == MorphUtil.getMorphOwner(Entity.class.cast(this)))
			{
				cir.setReturnValue(true);
			}
		}
		entity.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
    		LivingEntity morph = t.getMorph();
    		if(morph != null)
    		{
    			if(morph.isAlliedTo(Entity.class.cast(this)) || Entity.class.cast(this).isAlliedTo(morph))
    			{
        			cir.setReturnValue(true);
    			}
    		}
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "causeFallDamage", cancellable = true)
	private void causeFallDamage(float distance, float damagae, DamageSource source, CallbackInfoReturnable<Boolean> cir)
	{
		Entity entity = Entity.class.cast(this);
		entity.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
    		LivingEntity morph = t.getMorph();
    		if(morph != null)
    		{
    			cir.setReturnValue(morph.causeFallDamage(distance, damagae, source));
    		}
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "isInvulnerableTo", cancellable = true)
	private void isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir)
	{
		Entity entity = Entity.class.cast(this);
		entity.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
    		LivingEntity morph = t.getMorph();
    		if(morph != null)
    		{
    			cir.setReturnValue(morph.isInvulnerableTo(source));
    		}
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "setPos(DDD)V", cancellable = true)
	private void setPos(double x, double y, double z, CallbackInfo ci)
	{
		if(MorphUtil.getMorphOwner(Entity.class.cast(this)) != null)
		{
			MorphUtil.getMorphOwner(Entity.class.cast(this)).setPos(x, y, z);
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", cancellable = true)
	private void setDeltaMovement(Vec3 vec3, CallbackInfo ci)
	{
		if(MorphUtil.getMorphOwner(Entity.class.cast(this)) != null)
		{
			MorphUtil.getMorphOwner(Entity.class.cast(this)).setDeltaMovement(vec3);
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "getDeltaMovement", cancellable = true)
	private void getDeltaMovement(CallbackInfoReturnable<Vec3> cir)
	{
		if(MorphUtil.getMorphOwner(Entity.class.cast(this)) != null)
		{
			cir.setReturnValue(MorphUtil.getMorphOwner(Entity.class.cast(this)).getDeltaMovement());
		}
	}
}
