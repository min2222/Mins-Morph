package com.min01.morph.capabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.min01.morph.entity.EntityFakeTarget;
import com.min01.morph.entity.MorphEntities;
import com.min01.morph.network.MorphNetwork;
import com.min01.morph.network.UpdateMorphPacket;
import com.min01.morph.util.MorphUtil;
import com.min01.morph.util.world.MorphSavedData;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphCapabilityImpl implements IMorphCapability
{
	public static final Capability<IMorphCapability> MORPH = CapabilityManager.get(new CapabilityToken<>() {});
	
	private LivingEntity entity;
	private LivingEntity morph;
	private LivingEntity target;
	private EntityType<?> type;
	private boolean isPersistent;
	private boolean changedDimension;
	private List<Map<String, String>> dataList = new ArrayList<>();
	private String selectedGoalName = "";
	private String selectedAnimationName = "";
	
	@Override
	public CompoundTag serializeNBT() 
	{
		CompoundTag nbt = new CompoundTag();
		ListTag datas = new ListTag();
		this.dataList.forEach(t -> 
		{
			for(Entry<String, String> entry : t.entrySet())
			{
				String dataName = entry.getKey();
				String dataValue = entry.getValue();
				CompoundTag tag = new CompoundTag();
				tag.putString("DataName", dataName);
				tag.putString("DataValue", dataValue);
				datas.add(tag);
			}
		});
		if(this.type != null)
		{
			nbt.putString("MorphType", ForgeRegistries.ENTITY_TYPES.getKey(this.type).toString());
		}
		nbt.putBoolean("isPersistent", this.isPersistent);
		nbt.putBoolean("ChangedDimension", this.changedDimension);
		nbt.put("MobDatas", datas);
		nbt.putString("SelectedGoal", this.selectedGoalName);
		nbt.putString("SelectedAnimation", this.selectedAnimationName);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		if(nbt.contains("MorphType"))
		{
			this.setType(ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(nbt.getString("MorphType"))));
		}
		ListTag datas = nbt.getList("MobDatas", 11);
		for(int i = 0; i < datas.size(); ++i)
		{
			CompoundTag tag = datas.getCompound(i);
			String dataName = tag.getString("DataName");
			String dataValue = tag.getString("DataValue");
			this.setData(dataName, dataValue);
		}
		this.setPersistent(nbt.getBoolean("isPersistent"));
		this.setChangedDimension(nbt.getBoolean("ChangedDimension"));
		this.selectGoal(nbt.getString("SelectedGoal"));
		this.selectAnimation(nbt.getString("SelectedAnimation"));
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
			if(this.morph instanceof AbstractPiglin piglin)
			{
				piglin.setImmuneToZombification(true);
			}
			if(this.morph instanceof Hoglin hoglin)
			{
				hoglin.setImmuneToZombification(true);
			}
			if(this.entity instanceof Player player)
			{
				player.setNoGravity(this.morph.isNoGravity());
				if(!player.getAbilities().instabuild && !player.isSpectator())
				{
					MoveControl control = ((Mob) this.morph).getMoveControl();
					PathNavigation navigation = ((Mob) this.morph).getNavigation();
					if(control instanceof FlyingMoveControl || navigation instanceof FlyingPathNavigation || control.getClass().getSimpleName().contains("Flight") || navigation.getClass().getSimpleName().contains("Flight"))
					{
						if(!player.getAbilities().mayfly)
						{
							player.getAbilities().mayfly = true;
							player.onUpdateAbilities();
						}
					}
					else if(player.getAbilities().mayfly)
					{
						player.getAbilities().flying = false;
						player.getAbilities().mayfly = false;
						player.onUpdateAbilities();
					}
				}
			}
			if(!this.entity.level.isClientSide)
			{
				if(this.target == null || !this.target.isAlive())
				{
					EntityFakeTarget target = new EntityFakeTarget(MorphEntities.FAKE_TARGET.get(), this.entity.level);
					target.setOwner(this.entity);
					this.entity.level.addFreshEntity(target);
					this.target = target;
				}
				if(!this.morph.isAlive() && !this.morph.getRemovalReason().equals(RemovalReason.CHANGED_DIMENSION) && !this.isPersistent)
				{
					MorphUtil.removeMorph(this.entity);
				}
			}
		}
		else if(this.type != null)
		{
			this.setMorph((LivingEntity) this.type.create(this.entity.level));
		}
	}

	@Override
	public void selectGoal(String selectedGoal) 
	{
		this.selectedGoalName = selectedGoal;
	}
	
	@Override
	public String getSelectedGoal()
	{
		return this.selectedGoalName;
	}
	
	@Override
	public void selectAnimation(String selectedAnimation) 
	{
		this.selectedAnimationName = selectedAnimation;
	}
	
	@Override
	public String getSelectedAnimation()
	{
		return this.selectedAnimationName;
	}
	
	@Override
	public void setPersistent(boolean isPersistent) 
	{
		this.isPersistent = isPersistent;
		this.sendUpdataPacket();
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
		if(morph != null)
		{
			if(morph.getId() > 0)
			{
	    		int number = this.entity.level.random.nextInt(1, 1000);
	        	morph.setId(-number);
			}
			this.setType(morph.getType());
			this.setup((Mob) morph);
			MorphUtil.ENTITY_MAP.put(morph.getClass().hashCode(), morph);
			MorphUtil.ENTITY_MAP2.put(morph.getClass().getSuperclass().hashCode(), morph);
		}
		this.morph = morph;
		this.sendUpdataPacket();
	}
	
	public void setup(Mob mob)
	{
		if(!mob.level.isClientSide)
		{
			if(this.isPersistent)
			{
				this.dataList.forEach(t -> 
				{
					for(Entry<String, String> entry : t.entrySet())
					{
						String dataName = entry.getKey();
						String dataValue = entry.getValue();
						MorphUtil.setData(mob, dataName, dataValue);
					}
				});
			}
			MorphSavedData data = MorphSavedData.get(mob.level);
        	if(data != null)
        	{
    			data.saveData(mob.getClass().getSimpleName(), MorphUtil.getDatas(mob.getClass()));
    			data.saveGoal(mob.getClass().getSimpleName(), MorphUtil.getGoals(mob));
    			data.saveAnimation(mob.getClass().getSimpleName(), MorphUtil.getAnimations(mob.getClass()));
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
		this.sendUpdataPacket();
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
	
	@Override
	public void setData(String dataName, String dataValue) 
	{
		Map<String, String> map = new HashMap<>();
		map.put(dataName, dataValue);
		this.dataList.add(map);
		this.sendUpdataPacket();
	}
	
	@Override
	public void setChangedDimension(boolean isChangedDimension)
	{
		this.changedDimension = isChangedDimension;
		this.sendUpdataPacket();
	}
	
	@Override
	public boolean isChangedDimension() 
	{
		return this.changedDimension;
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
	
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) 
	{
		return MORPH.orEmpty(cap, LazyOptional.of(() -> this));
	}
}
