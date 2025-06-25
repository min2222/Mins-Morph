package com.min01.morph.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.morph.capabilities.IMorphCapability;
import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.capabilities.MorphCapabilityImpl;
import com.min01.morph.util.MorphUtil;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import net.minecraftforge.fluids.FluidType;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements IForgeLivingEntity
{
	@Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
	private void tick(CallbackInfo ci)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		living.getCapability(MorphCapabilities.MORPH).ifPresent(IMorphCapability::tick);
		if(MorphUtil.isMorph(living) && MorphUtil.getMorphOwner(living) == null)
		{
			living.discard();
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "onClimbable", cancellable = true)
	private void onClimbable(CallbackInfoReturnable<Boolean> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			if(t instanceof Spider)
			{
				cir.setReturnValue(living.horizontalCollision);
			}
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "getMaxHealth", cancellable = true)
	private void getMaxHealth(CallbackInfoReturnable<Float> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			cir.setReturnValue(t.getMaxHealth());
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "getHealth", cancellable = true)
	private void getHealth(CallbackInfoReturnable<Float> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		if(MorphUtil.getMorphOwner(living) != null)
		{
			cir.setReturnValue(((LivingEntity) MorphUtil.getMorphOwner(living)).getHealth());
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "getAttribute", cancellable = true)
	private void getAttribute(Attribute attribute, CallbackInfoReturnable<AttributeInstance> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			if(MorphUtil.ATTRIBUTES.contains(attribute) && t.getAttribute(attribute) != null)
			{
    			cir.setReturnValue(t.getAttribute(attribute));
			}
			if(attribute == ForgeMod.SWIM_SPEED.get() && t.getAttribute(Attributes.MOVEMENT_SPEED) != null)
			{
				cir.setReturnValue(t.getAttribute(Attributes.MOVEMENT_SPEED));
			}
		});
	}
	
	@Inject(at = @At(value = "RETURN"), method = "getAttributeValue(Lnet/minecraft/core/Holder;)D", cancellable = true)
	private void getAttributeValue(Holder<Attribute> holder, CallbackInfoReturnable<Double> cir) 
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			if(MorphUtil.ATTRIBUTES.contains(holder.value()) && t.getAttribute(holder.value()) != null)
			{
    			cir.setReturnValue(cir.getReturnValue() + t.getAttributeValue(holder.value()));
			}
			if(holder.value() == ForgeMod.SWIM_SPEED.get() && t.getAttribute(Attributes.MOVEMENT_SPEED) != null)
			{
				cir.setReturnValue(cir.getReturnValue() + t.getAttributeValue(Attributes.MOVEMENT_SPEED));
			}
		});
	}

	@Inject(at = @At(value = "RETURN"), method = "getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D", cancellable = true)
	private void getAttributeValue(Attribute attribute, CallbackInfoReturnable<Double> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			if(MorphUtil.ATTRIBUTES.contains(attribute) && t.getAttribute(attribute) != null)
			{
    			cir.setReturnValue(cir.getReturnValue() + t.getAttributeValue(attribute));
			}
			if(attribute == ForgeMod.SWIM_SPEED.get() && t.getAttribute(Attributes.MOVEMENT_SPEED) != null)
			{
				cir.setReturnValue(cir.getReturnValue() + t.getAttributeValue(Attributes.MOVEMENT_SPEED));
			}
		});
	}

	@Inject(at = @At(value = "HEAD"), method = "getAttributeBaseValue(Lnet/minecraft/core/Holder;)D", cancellable = true)
	private void getAttributeBaseValue(Holder<Attribute> holder, CallbackInfoReturnable<Double> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			if(MorphUtil.ATTRIBUTES.contains(holder.value()) && t.getAttribute(holder.value()) != null)
			{
    			cir.setReturnValue(t.getAttributeBaseValue(holder.value()));
			}
			if(holder.value() == ForgeMod.SWIM_SPEED.get() && t.getAttribute(Attributes.MOVEMENT_SPEED) != null)
			{
				cir.setReturnValue(t.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
			}
		});
	}

	@Inject(at = @At(value = "HEAD"), method = "getAttributeBaseValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D", cancellable = true)
	private void getAttributeBaseValue(Attribute attribute, CallbackInfoReturnable<Double> cir) 
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			if(MorphUtil.ATTRIBUTES.contains(attribute) && t.getAttribute(attribute) != null)
			{
    			cir.setReturnValue(t.getAttributeBaseValue(attribute));
			}
			if(attribute == ForgeMod.SWIM_SPEED.get() && t.getAttribute(Attributes.MOVEMENT_SPEED) != null)
			{
				cir.setReturnValue(t.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
			}
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "isInvertedHealAndHarm", cancellable = true)
	private void isInvertedHealAndHarm(CallbackInfoReturnable<Boolean> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			cir.setReturnValue(t.isInvertedHealAndHarm());
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "updateWalkAnimation", cancellable = true)
	private void updateWalkAnimation(float p_268283_, CallbackInfo ci)
	{
		if(LivingEntity.class.cast(this).level.isClientSide && MorphUtil.isMorph(LivingEntity.class.cast(this)))
		{
			ci.cancel();
		}
	}
	
	@Inject(at = @At(value = "RETURN"), method = "hurt", cancellable = true)
	private void hurt(DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			boolean flag = t.hurt(source, damage);
			if(!flag)
			{
				cir.setReturnValue(flag);
			}
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "canBeAffected", cancellable = true)
	private void canBeAffected(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			cir.setReturnValue(t.canBeAffected(effect));
		});
	}

	@Inject(at = @At(value = "HEAD"), method = "isSensitiveToWater", cancellable = true)
	private void isSensitiveToWater(CallbackInfoReturnable<Boolean> cir) 
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			cir.setReturnValue(t.isSensitiveToWater());
		});
	}

	@Inject(at = @At(value = "HEAD"), method = "doPush", cancellable = true)
	protected void doPush(Entity entity, CallbackInfo ci) 
	{
		LivingEntity living = LivingEntity.class.cast(this);
		if(MorphUtil.getMorphOwner(living) != null)
		{
			if(entity == MorphUtil.getMorphOwner(living))
			{
				ci.cancel();
			}
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", cancellable = true)
	private void addEffect(MobEffectInstance effect, @Nullable Entity entity, CallbackInfoReturnable<Boolean> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		if(MorphUtil.getMorphOwner(living) != null)
		{
			((LivingEntity) MorphUtil.getMorphOwner(living)).addEffect(effect, entity);
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "heal")
	private void heal(float amount, CallbackInfo ci)
	{
		if(MorphUtil.getMorphOwner(LivingEntity.class.cast(this)) != null)
		{
			((LivingEntity) MorphUtil.getMorphOwner(LivingEntity.class.cast(this))).heal(amount);
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "randomTeleport", cancellable = true)
	private void randomTeleport(double x, double y, double z, boolean flag, CallbackInfoReturnable<Boolean> cir)
	{
		if(MorphUtil.getMorphOwner(Entity.class.cast(this)) != null)
		{
			((LivingEntity) MorphUtil.getMorphOwner(Entity.class.cast(this))).randomTeleport(x, y, z, flag);
		}
	}
	

	@Inject(at = @At(value = "HEAD"), method = "isAffectedByFluids", cancellable = true)
	protected void isAffectedByFluids(CallbackInfoReturnable<Boolean> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			cir.setReturnValue(((LivingEntityInvoker)t).invokeIsAffectedByFluids());
		});
	}
	
	@SuppressWarnings("deprecation")
	@Inject(at = @At(value = "HEAD"), method = "isPushedByFluid", cancellable = true)
	private void isPushedByFluid(CallbackInfoReturnable<Boolean> cir) 
	{
		LivingEntity living = LivingEntity.class.cast(this);
		MorphUtil.getMorph(living, t -> 
		{
			cir.setReturnValue(t.isPushedByFluid());
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isPushedByFluid(FluidType type) 
	{
		LivingEntity living = LivingEntity.class.cast(this);
		if(living.getCapability(MorphCapabilities.MORPH).isPresent())
		{
			IMorphCapability cap = living.getCapability(MorphCapabilities.MORPH).orElse(new MorphCapabilityImpl());
			if(MorphUtil.hasMorph(living))
			{
				return cap.getMorph().isPushedByFluid(type);
			}
		}
        return this.self().isPushedByFluid() && type.canPushEntity(self());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean canDrownInFluidType(FluidType type)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		if(living.getCapability(MorphCapabilities.MORPH).isPresent())
		{
			IMorphCapability cap = living.getCapability(MorphCapabilities.MORPH).orElse(new MorphCapabilityImpl());
			if(MorphUtil.hasMorph(living))
			{
				return cap.getMorph().canDrownInFluidType(type);
			}
		}
        if(type == ForgeMod.WATER_TYPE.get())
        {
        	return !this.self().canBreatheUnderwater();
        }
        return type.canDrownIn(this.self());
	}
}
