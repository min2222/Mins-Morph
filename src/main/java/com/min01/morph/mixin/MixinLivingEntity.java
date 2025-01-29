package com.min01.morph.mixin;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.morph.capabilities.IMorphCapability;
import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.util.MorphUtil;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mixin(LivingEntity.class)
public class MixinLivingEntity
{
	@Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
	private void tick(CallbackInfo ci)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		living.getCapability(MorphCapabilities.MORPH).ifPresent(IMorphCapability::tick);
		if(living.getId() < 0 && MorphUtil.getMorphOwner(living) == null)
		{
			living.discard();
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "isInvertedHealAndHarm", cancellable = true)
	private void isInvertedHealAndHarm(CallbackInfoReturnable<Boolean> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		living.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
    		LivingEntity morph = t.getMorph();
    		if(morph != null)
    		{
    			cir.setReturnValue(morph.isInvertedHealAndHarm());
    		}
		});
	}

	@Inject(at = @At(value = "HEAD"), method = "getHurtSound", cancellable = true)
	private void getHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) 
	{
		LivingEntity living = LivingEntity.class.cast(this);
		living.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
    		LivingEntity morph = t.getMorph();
    		if(morph != null)
    		{
    			Method m = ObfuscationReflectionHelper.findMethod(Mob.class, "m_7975_", DamageSource.class);
    			try 
    			{
    				SoundEvent sound = (SoundEvent) m.invoke(morph, source);
    				cir.setReturnValue(sound);
    			}
    			catch (Exception e)
    			{
    				
    			}
    		}
		});
	}

	@Inject(at = @At(value = "HEAD"), method = "getDeathSound", cancellable = true)
	private void getDeathSound(CallbackInfoReturnable<SoundEvent> cir) 
	{
		LivingEntity living = LivingEntity.class.cast(this);
		living.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
    		LivingEntity morph = t.getMorph();
    		if(morph != null)
    		{
    			Method m = ObfuscationReflectionHelper.findMethod(Mob.class, "m_5592_");
    			try 
    			{
    				SoundEvent sound = (SoundEvent) m.invoke(morph);
    				cir.setReturnValue(sound);
    			}
    			catch (Exception e)
    			{
    				
    			}
    		}
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "updateWalkAnimation", cancellable = true)
	private void updateWalkAnimation(float p_268283_, CallbackInfo ci)
	{
		if(LivingEntity.class.cast(this).level.isClientSide && LivingEntity.class.cast(this).getId() < 0)
		{
			ci.cancel();
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "getArmorValue", cancellable = true)
	private void getArmorValue(CallbackInfoReturnable<Integer> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		living.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
    		LivingEntity morph = t.getMorph();
    		if(morph != null)
    		{
				cir.setReturnValue(morph.getArmorValue());
    		}
		});
	}
	
	@Inject(at = @At(value = "HEAD"), method = "hurt", cancellable = true)
	private void hurt(DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		living.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
    		LivingEntity morph = t.getMorph();
    		if(morph != null)
    		{
    			if(source.getEntity() != null)
    			{
    				if(source.getEntity() == morph)
    				{
    					cir.setReturnValue(false);
    				}
    			}
    			if(morph instanceof Mob mob && source.getEntity() instanceof LivingEntity target)
    			{
    				for(WrappedGoal goal : mob.targetSelector.getAvailableGoals())
    				{
    					if(goal.getGoal() instanceof HurtByTargetGoal)
    					{
    						if(target != morph)
    						{
        						mob.setTarget(target);
    						}
    					}
    				}
    			}
    			morph.hurt(source, damage);
    		}
		});
		if(MorphUtil.getMorphOwner(living) != null)
		{
			if(source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
			{
				cir.setReturnValue(true);
			}
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "canBeAffected", cancellable = true)
	private void canBeAffected(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		living.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
    		LivingEntity morph = t.getMorph();
    		if(morph != null)
    		{
    			cir.setReturnValue(morph.canBeAffected(effect));
    		}
		});
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
}
