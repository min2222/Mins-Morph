package com.min01.morph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.morph.util.MorphUtil;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public class MixinEntity 
{
	@Inject(at = @At(value = "RETURN"), method = "makeBoundingBox", cancellable = true)
	private void makeBoundingBox(CallbackInfoReturnable<AABB> cir) 
	{
		Entity entity = Entity.class.cast(this);
		MorphUtil.getMorph(entity, t -> 
		{
			entity.eyeHeight = t.eyeHeight;
			cir.setReturnValue(t.dimensions.makeBoundingBox(t.position()));
		});
	}
	
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
	
	//ChatGPT ahh;
	@Inject(at = @At("RETURN"), method = "getDeltaMovement", cancellable = true)
	private void getDeltaMovement(CallbackInfoReturnable<Vec3> cir)
	{
	    Entity entity = (Entity)(Object)this;
	    MorphUtil.getMorph(entity, t -> 
	    {
            Vec3 playerMove = cir.getReturnValue();
            Vec3 morphMove = t.getDeltaMovement();
            double epsilon = 1.0E-4;
            double x = Math.abs(morphMove.x) > epsilon ? morphMove.x : playerMove.x;
            double y = Math.abs(morphMove.y) > epsilon ? morphMove.y : playerMove.y;
            double z = Math.abs(morphMove.z) > epsilon ? morphMove.z : playerMove.z;
            Vec3 merged = new Vec3(x, y, z);
            cir.setReturnValue(merged);
            if(morphMove.lengthSqr() > epsilon * epsilon) 
            {
                t.setDeltaMovement(Vec3.ZERO);
            }
	    });
	}
}
