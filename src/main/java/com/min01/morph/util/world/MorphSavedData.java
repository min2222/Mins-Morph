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
	private Map<String, List<String>> goalMap = new HashMap<>();
	private Map<String, List<String>> dataMap = new HashMap<>();
	
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
		ListTag animations = nbt.getList("MobAnimations", 10);
		ListTag goals = nbt.getList("MobGoals", 10);
		ListTag datas = nbt.getList("MobDatas", 10);
		for(int i = 0; i < animations.size(); ++i)
		{
			CompoundTag tag = animations.getCompound(i);
			String mobName = tag.getString("Mob");
			ListTag list = tag.getList("Animations", 10);
			List<String> list1 = new ArrayList<>();
			for(int i1 = 0; i1 < list.size(); ++i1)
			{
				CompoundTag tag1 = list.getCompound(i1);
				list1.add(tag1.getString("Animation"));
			}
			data.saveAnimation(mobName, list1);
		}
		for(int i = 0; i < goals.size(); ++i)
		{
			CompoundTag tag = goals.getCompound(i);
			String mobName = tag.getString("Mob");
			ListTag list = tag.getList("Goals", 10);
			List<String> list1 = new ArrayList<>();
			for(int i1 = 0; i1 < list.size(); ++i1)
			{
				CompoundTag tag1 = list.getCompound(i1);
				list1.add(tag1.getString("Goal"));
			}
			data.saveGoal(mobName, list1);
		}
		for(int i = 0; i < datas.size(); ++i)
		{
			CompoundTag tag = datas.getCompound(i);
			String mobName = tag.getString("Mob");
			ListTag list = tag.getList("Datas", 10);
			List<String> list1 = new ArrayList<>();
			for(int i1 = 0; i1 < list.size(); ++i1)
			{
				CompoundTag tag1 = list.getCompound(i1);
				list1.add(tag1.getString("Data"));
			}
			data.saveData(mobName, list1);
		}
        return data;
    }
	
	@Override
	public CompoundTag save(CompoundTag nbt)
	{
		ListTag animations = new ListTag();
		ListTag goals = new ListTag();
		ListTag datas = new ListTag();
		for(Entry<String, List<String>> entry : this.animationMap.entrySet())
		{
			String mobName = entry.getKey();
			List<String> names = entry.getValue();
			ListTag list = new ListTag();
			CompoundTag tag = new CompoundTag();
			names.forEach(t ->
			{
				CompoundTag tag1 = new CompoundTag();
				tag1.putString("Animation", t);
				list.add(tag1);
			});
			tag.putString("Mob", mobName);
			tag.put("Animations", list);
			animations.add(tag);
		}
		for(Entry<String, List<String>> entry : this.goalMap.entrySet())
		{
			String mobName = entry.getKey();
			List<String> names = entry.getValue();
			ListTag list = new ListTag();
			CompoundTag tag = new CompoundTag();
			names.forEach(t ->
			{
				CompoundTag tag1 = new CompoundTag();
				tag1.putString("Goal", t);
				list.add(tag1);
			});
			tag.putString("Mob", mobName);
			tag.put("Goals", list);
			goals.add(tag);
		}
		for(Entry<String, List<String>> entry : this.dataMap.entrySet())
		{
			String mobName = entry.getKey();
			List<String> names = entry.getValue();
			ListTag list = new ListTag();
			CompoundTag tag = new CompoundTag();
			names.forEach(t ->
			{
				CompoundTag tag1 = new CompoundTag();
				tag1.putString("Data", t);
				list.add(tag1);
			});
			tag.putString("Mob", mobName);
			tag.put("Datas", list);
			datas.add(tag);
		}
		nbt.put("MobAnimations", animations);
		nbt.put("MobGoals", goals);
		nbt.put("MobDatas", datas);
		return nbt;
	}
	
	public List<String> getDatas(String mobName)
	{
		return this.dataMap.get(mobName);
	}
	
	public List<String> getGoals(String mobName)
	{
		return this.goalMap.get(mobName);
	}
	
	public List<String> getAnimations(String mobName)
	{
		return this.animationMap.get(mobName);
	}
	
	public void saveData(String mobName, List<String> datas)
	{
		this.dataMap.put(mobName, datas);
		this.setDirty();
	}

	public void saveAnimation(String mobName, List<String> animations)
	{
		this.animationMap.put(mobName, animations);
		this.setDirty();
	}
	
	public void saveGoal(String mobName, List<String> goals)
	{
		this.goalMap.put(mobName, goals);
		this.setDirty();
	}
}
