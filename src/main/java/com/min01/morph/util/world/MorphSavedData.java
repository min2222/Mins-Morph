package com.min01.morph.util.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class MorphSavedData extends SavedData
{
	public static final String NAME = "morph_data";
	
	private Map<String, List<String>> animationMap = new HashMap<>();
	private Map<String, Integer> goalMap = new HashMap<>();
	
    public static MorphSavedData get(Level level)
    {
        if(level instanceof ServerLevel serverLevel) 
        {
            DimensionDataStorage storage = serverLevel.getDataStorage();
            MorphSavedData data = storage.computeIfAbsent(MorphSavedData::load, MorphSavedData::new, NAME);
            return data;
        }
        return null;
    }

    public static MorphSavedData load(CompoundTag nbt) 
    {
    	MorphSavedData data = new MorphSavedData();
		ListTag list = nbt.getList("Mobs", 10);
		ListTag list1 = nbt.getList("Mobs", 10);
		for(int i = 0; i < list.size(); ++i)
		{
			CompoundTag tag = list.getCompound(i);
			String mobName = tag.getString("Mob");
			ListTag list2 = tag.getList("Animations", 10);
			List<String> list3 = new ArrayList<>();
			for(int i1 = 0; i1 < list2.size(); ++i1)
			{
				CompoundTag tag1 = list2.getCompound(i1);
				list3.add(tag1.getString("Animation"));
			}
			data.saveAnimation(mobName, list3);
		}
		for(int i = 0; i < list1.size(); ++i)
		{
			CompoundTag tag = list1.getCompound(i);
			String goalName = tag.getString("GoalName");
			int index = tag.getInt("GoalIndex");
			data.saveGoal(goalName, index);
		}
        return data;
    }
	
	@Override
	public CompoundTag save(CompoundTag nbt)
	{
		ListTag list = new ListTag();
		ListTag list1 = new ListTag();
		for(Entry<String, List<String>> entry : this.animationMap.entrySet())
		{
			String mobName = entry.getKey();
			List<String> names = entry.getValue();
			ListTag list2 = new ListTag();
			CompoundTag tag = new CompoundTag();
			names.forEach(t ->
			{
				CompoundTag tag1 = new CompoundTag();
				tag1.putString("Animation", t);
				list2.add(tag1);
			});
			tag.putString("Mob", mobName);
			tag.put("Animations", list2);
			list.add(tag);
		}
		for(Entry<String, Integer> entry : this.goalMap.entrySet())
		{
			String goalName = entry.getKey();
			int index = entry.getValue();
			CompoundTag tag = new CompoundTag();
			tag.putString("GoalName", goalName);
			tag.putInt("GoalIndex", index);
			list1.add(tag);
		}
		nbt.put("Mobs", list);
		nbt.put("Goals", list1);
		return nbt;
	}
	
	public Map<String, Integer> getGoalMap()
	{
		return this.goalMap;
	}
	
	public Map<String, List<String>> getAnimationMap()
	{
		return this.animationMap;
	}
	
	public Integer getGoalIndex(String goalName)
	{
		return this.goalMap.get(goalName);
	}
	
	public List<String> getAnimations(String mobName)
	{
		return this.animationMap.get(mobName);
	}

	public void saveAnimation(String mobName, List<String> animations)
	{
		this.animationMap.put(mobName, animations);
		this.setDirty();
	}
	
	public void saveGoal(String goalName, Integer goalIndex)
	{
		this.goalMap.put(goalName, goalIndex);
		this.setDirty();
	}
}
