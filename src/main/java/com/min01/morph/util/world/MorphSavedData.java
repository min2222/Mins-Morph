package com.min01.morph.util.world;

import java.util.HashMap;
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
	
	private Map<String, String> animationMap = new HashMap<>();
	
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
		ListTag list = nbt.getList("Animations", 10);
		for(int i = 0; i < list.size(); ++i)
		{
			CompoundTag tag = list.getCompound(i);
			String goalName = tag.getString("GoalName");
			String animationName = tag.getString("AnimationName");
			data.saveAnimation(goalName, animationName);
		}
        return data;
    }
	
	@Override
	public CompoundTag save(CompoundTag nbt)
	{
		ListTag list = new ListTag();
		for(Entry<String, String> entry : this.animationMap.entrySet())
		{
			String goalName = entry.getKey();
			String animationName = entry.getValue();
			CompoundTag tag = new CompoundTag();
			tag.putString("GoalName", goalName);
			tag.putString("AnimationName", animationName);
			list.add(tag);
		}
		nbt.put("Animations", list);
		return nbt;
	}
	
	public boolean hasAnimation(String animationName)
	{
		return this.animationMap.containsKey(animationName);
	}

	public String getAnimation(String animationName) 
	{
		if(this.animationMap.containsKey(animationName))
		{
			return this.animationMap.get(animationName);
		}
		return "";
	}

	public void saveAnimation(String goalName, String animationName)
	{
		this.animationMap.put(goalName, animationName);
		this.setDirty();
	}
}
