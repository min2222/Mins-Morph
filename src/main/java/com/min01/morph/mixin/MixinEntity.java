package com.min01.morph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.morph.util.MorphUtil;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
		MorphUtil.getMorph(Entity.class.cast(this), t -> 
		{
			if(t.isAlliedTo(Entity.class.cast(this)))
			{
    			cir.setReturnValue(true);
			}
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "getAirSupply", cancellable = true)
	private void getAirSupply(CallbackInfoReturnable<Integer> cir)
	{
		MorphUtil.getMorph(Entity.class.cast(this), t -> 
		{
			cir.setReturnValue(t.getAirSupply());
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "setSecondsOnFire", cancellable = true)
	private void setSecondsOnFire(int second, CallbackInfo ci)
	{
		if(MorphUtil.getMorphOwner(Entity.class.cast(this)) != null)
		{
			ci.cancel();
			MorphUtil.getMorphOwner(Entity.class.cast(this)).setSecondsOnFire(second);
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "fireImmune", cancellable = true)
	private void fireImmune(CallbackInfoReturnable<Boolean> cir)
	{
		Entity entity = Entity.class.cast(this);
		MorphUtil.getMorph(entity, t -> 
		{
			cir.setReturnValue(t.fireImmune());
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "causeFallDamage", cancellable = true)
	private void causeFallDamage(float distance, float damage, DamageSource source, CallbackInfoReturnable<Boolean> cir)
	{
		Entity entity = Entity.class.cast(this);
		MorphUtil.getMorph(entity, t -> 
		{
			cir.setReturnValue(t.causeFallDamage(distance, damage, source));
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "isInvulnerableTo", cancellable = true)
	private void isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir)
	{
		Entity entity = Entity.class.cast(this);
		MorphUtil.getMorph(entity, t -> 
		{
			cir.setReturnValue(t.isInvulnerableTo(source));
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
