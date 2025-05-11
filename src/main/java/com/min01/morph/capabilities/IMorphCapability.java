package com.min01.morph.capabilities;

import com.min01.morph.MinsMorph;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

@AutoRegisterCapability
public interface IMorphCapability extends INBTSerializable<CompoundTag>
{
	ResourceLocation ID = new ResourceLocation(MinsMorph.MODID, "morph");

	void setEntity(LivingEntity entity);
	
	void tick();
	
	void setPersistent(boolean isPersistent);
	
	boolean isPersistent();
	
	void setMorph(LivingEntity morph);
	
	LivingEntity getMorph();
	
	void setType(EntityType<?> type);
	
	EntityType<?> getType();
	
	LivingEntity getFakeTarget();
	
	void setData(LivingEntity living, String dataName, String dataValue);
	
	void setChangedDimension(boolean isChangedDimension);
	
	boolean isChangedDimension();
}
