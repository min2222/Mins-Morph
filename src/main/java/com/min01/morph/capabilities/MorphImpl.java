package com.min01.morph.capabilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.min01.morph.entity.EntityFakeTarget;
import com.min01.morph.entity.MorphEntities;
import com.min01.morph.misc.IWrappedGoal;
import com.min01.morph.network.MorphNetwork;
import com.min01.morph.network.UpdateMorphPacket;
import com.min01.morph.util.MorphUtil;
import com.min01.morph.util.world.MorphSavedData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphImpl implements IMorphCapability
{
	private LivingEntity entity;
	private LivingEntity morph;
	private LivingEntity target;
	private EntityType<?> type;
	private boolean isPersistent;
	
	@Override
	public CompoundTag serializeNBT() 
	{
		CompoundTag nbt = new CompoundTag();
		if(this.type != null)
		{
			nbt.putString("MorphType", ForgeRegistries.ENTITY_TYPES.getKey(this.type).toString());
		}
		nbt.putBoolean("isPersistent", this.isPersistent);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		if(nbt.contains("MorphType"))
		{
			this.type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(nbt.getString("MorphType")));
		}
		this.isPersistent = nbt.getBoolean("isPersistent");
	}

	@Override
	public void setEntity(LivingEntity entity) 
	{
		this.entity = entity;
	}
	
	@Override
	public void tick()
	{
		if(this.morph != null)
		{
			MorphUtil.tick(this.entity, this.morph);
			this.morph.tick();
			if(!this.entity.level.isClientSide)
			{
				boolean flag = this.target != null && !this.target.isAlive();
				if(this.target == null || flag)
				{
					EntityFakeTarget target = new EntityFakeTarget(MorphEntities.FAKE_TARGET.get(), this.entity.level);
					target.setOwner(this.entity);
					this.entity.level.addFreshEntity(target);
					this.target = target;
				}
				if(!this.morph.isAlive() && !this.isPersistent)
				{
					this.setMorph(null);
					this.setType(null);
					this.setPersistent(false);
					if(this.target != null)
					{
						this.target.discard();
					}
				}
			}
		}
		else if(this.type != null)
		{
			this.setMorph((LivingEntity) this.type.create(this.entity.level));
		}
	}
	
	@Override
	public void setPersistent(boolean isPersistent) 
	{
		this.isPersistent = isPersistent;
	}
	
	@Override
	public boolean isPersistent() 
	{
		return this.isPersistent;
	}

	@Override
	public void setMorph(LivingEntity morph) 
	{
		if(this.morph != null)
		{
			this.morph.discard();
		}
		this.morph = morph;
		if(morph != null)
		{
			if(morph.getId() > 0)
			{
	    		int number = this.entity.level.random.nextInt(1, 1000);
	        	morph.setId(-number);
			}
			this.entity.setHealth(morph.getMaxHealth());
			this.setType(morph.getType());
			this.setup((Mob) morph);
			MorphUtil.ENTITY_MAP.put(morph.getClass().hashCode(), morph);
			MorphUtil.ENTITY_MAP2.put(morph.getClass().getSuperclass().hashCode(), morph);
		}
		this.sendUpdataPacket();
	}
	
	public void setup(Mob mob)
	{
		if(!mob.level.isClientSide)
		{
			MorphSavedData data = MorphSavedData.get(mob.level);
        	if(data != null)
        	{
        		Set<WrappedGoal> set = mob.goalSelector.getAvailableGoals();
        		List<String> list = new ArrayList<>();
        		List<String> list1 = new ArrayList<>();
        		List<WrappedGoal> goals = Lists.newArrayList(set);
        		for(WrappedGoal goal : set)
        		{
        			((IWrappedGoal)goal).setEntity(mob);
        			String goalName = goal.getGoal().getClass().getSimpleName();
        			if(goal.getGoal().getClass().isAnonymousClass())
        			{
            			goalName = goal.getGoal().getClass().getSuperclass().getSimpleName();
        			}
        			list1.add(goalName + goals.indexOf(goal));
        		}
    			for(Field f : mob.getClass().getDeclaredFields())
    			{
    				if(f.getType().getSimpleName().contains("Animation") && !f.getType().isArray())
    				{
    					list.add(f.getName());
    				}
    			}
    			data.saveGoal(mob.getClass().getSimpleName(), list1);
    			data.saveAnimation(mob.getClass().getSimpleName(), list);
        	}
			ForgeEventFactory.onFinalizeSpawn((Mob) mob, (ServerLevelAccessor) this.entity.level, this.entity.level.getCurrentDifficultyAt(this.entity.blockPosition()), MobSpawnType.COMMAND, null, null);
		}
	}

	@Override
	public LivingEntity getMorph()
	{
		return this.morph;
	}
	
	@Override
	public void setType(EntityType<?> type) 
	{
		this.type = type;
	}
	
	@Override
	public EntityType<?> getType() 
	{
		return this.type;
	}

	@Override
	public LivingEntity getFakeTarget() 
	{
		return this.target;
	}
	
	public void sendUpdataPacket()
	{
		if(!this.entity.level.isClientSide)
		{
			if(this.morph != null && this.type != null)
			{
				MorphNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.entity), new UpdateMorphPacket(this.entity, this.type, this.morph.getId(), false));
			}
			else
			{
				MorphNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.entity), new UpdateMorphPacket(this.entity, EntityType.PIG, 0, true));
			}
		}
	}
}
