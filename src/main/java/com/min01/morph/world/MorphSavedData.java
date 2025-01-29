package com.min01.morph.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphSavedData extends SavedData
{
	public static final String NAME = "morph_data";
	
	private Map<EntityType<?>, Map<Integer, Integer>> goalTicksMap = new HashMap<>();
	
    public static MorphSavedData get(Level level, ResourceKey<Level> dimension)
    {
        if(level instanceof ServerLevel) 
        {
            ServerLevel serverLevel = level.getServer().getLevel(dimension);
            DimensionDataStorage storage = serverLevel.getDataStorage();
            MorphSavedData data = storage.computeIfAbsent(MorphSavedData::load, MorphSavedData::new, NAME);
            return data;
        }
        return null;
    }

    public static MorphSavedData load(CompoundTag nbt) 
    {
    	MorphSavedData data = new MorphSavedData();
		ListTag list = nbt.getList("GoalData", 10);
		for(int i = 0; i < list.size(); ++i)
		{
			CompoundTag tag = list.getCompound(i);
    		ListTag list1 = tag.getList("GoalTicks", 10);
    		for(int i1 = 0; i1 < list1.size(); ++i1)
    		{
    			CompoundTag tag1 = list1.getCompound(i1);
    			int goalIndex = tag1.getInt("GoalIndex");
    			int goalTick = tag1.getInt("GoalTick");
    			data.saveGoalTicks(ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(tag.getString("Entity"))), goalIndex, goalTick);
    		}
		}
        return data;
    }
	
	@Override
	public CompoundTag save(CompoundTag nbt)
	{
		ListTag list = new ListTag();
		for(Entry<EntityType<?>, Map<Integer, Integer>> entry : this.goalTicksMap.entrySet())
		{
			EntityType<?> type = entry.getKey();
			Map<Integer, Integer> map = entry.getValue();
			String typeName = ForgeRegistries.ENTITY_TYPES.getKey(type).toString();
			CompoundTag tag = new CompoundTag();
			ListTag list1 = new ListTag();
			for(Entry<Integer, Integer> entry1 : map.entrySet()) 
			{
				int goalIndex = entry1.getKey();
				int goalTick = entry1.getValue();
				CompoundTag tag1 = new CompoundTag();
				tag1.putInt("GoalIndex", goalIndex);
				tag1.putInt("GoalTick", goalTick);
				list1.add(tag1);
			}
			tag.putString("Entity", typeName);
			tag.put("GoalTicks", list1);
			list.add(tag);
		}
		nbt.put("GoalData", list);
		return nbt;
	}

	public int getGoalTick(EntityType<?> type, int goalIndex) 
	{
		if(this.goalTicksMap.containsKey(type))
		{
			Map<Integer, Integer> map = this.goalTicksMap.get(type);
			if(map.containsKey(goalIndex))
			{
				return map.get(goalIndex);
			}
		}
		return 0;
	}

	public void saveGoalTicks(EntityType<?> type, int goalIndex, int goalTick)
	{
		if(!this.goalTicksMap.containsKey(type))
		{
			Map<Integer, Integer> map = new HashMap<>();
			map.put(goalIndex, goalTick);
			this.goalTicksMap.put(type, map);
		}
		else
		{
			Map<Integer, Integer> map = this.goalTicksMap.get(type);
			map.put(goalIndex, goalTick);
			this.goalTicksMap.put(type, map);
		}
		this.setDirty();
	}
}
