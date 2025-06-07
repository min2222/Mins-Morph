package com.min01.morph.entity;

import com.min01.morph.util.MorphUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class EntityFakeTarget extends AbstractOwnableMob<LivingEntity>
{
	public EntityFakeTarget(EntityType<? extends AbstractOwnableMob<?>> p_21368_, Level p_21369_)
	{
		super(p_21368_, p_21369_);
		this.setNoAi(true);
		this.setInvulnerable(true);
		this.setSilent(true);
		this.noPhysics = true;
	}
	
	@Override
	public void tick() 
	{
		super.tick();
		if(this.getOwner() == null)
		{
			this.discard();
		}
		else
		{
			LivingEntity living = this.getOwner();
			HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(living, entity -> !entity.isAlliedTo(living), 30.0F);
			this.setPos(hitResult.getLocation());
			if(!MorphUtil.hasMorph(living))
			{
				this.discard();
			}
		}
	}
	
	@Override
	public boolean isAlliedTo(Entity p_20355_)
	{
		return false;
	}
	
	@Override
	public boolean isAlliedTo(Team p_20032_)
	{
		return false;
	}
	
	@Override
	public boolean addEffect(MobEffectInstance p_147208_, Entity p_147209_)
	{
		return false;
	}
	
	@Override
	public boolean canBeAffected(MobEffectInstance p_21197_)
	{
		return false;
	}
	
	@Override
	public boolean hurt(DamageSource p_21016_, float p_21017_)
	{
		if(p_21016_.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
		{
			this.discard();
		}
		return false;
	}
	
	@Override
	public void setDeltaMovement(Vec3 p_20257_) 
	{

	}
	
	@Override
	public Vec3 getDeltaMovement() 
	{
		return Vec3.ZERO;
	}
	
	@Override
	public boolean isPickable() 
	{
		return false;
	}
	
	@Override
	public boolean displayFireAnimation() 
	{
		return false;
	}
	
	@Override
	protected void playHurtSound(DamageSource p_21493_) 
	{
		
	}
	
	@Override
	protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) 
	{
		
	}
}
