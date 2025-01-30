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
	
	private Map<Integer, String> animationMap = new HashMap<>();
	
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
			int goalIndex = tag.getInt("Index");
			String name = tag.getString("AnimationName");
			data.saveAnimation(goalIndex, name);
		}
        return data;
    }
	
	@Override
	public CompoundTag save(CompoundTag nbt)
	{
		ListTag list = new ListTag();
		for(Entry<Integer, String> entry : this.animationMap.entrySet())
		{
			int index = entry.getKey();
			String name = entry.getValue();
			CompoundTag tag = new CompoundTag();
			tag.putInt("Index", index);
			tag.putString("AnimationName", name);
			list.add(tag);
		}
		nbt.put("Animations", list);
		return nbt;
	}

	public String getAnimation(int goalIndex) 
	{
		if(this.animationMap.containsKey(goalIndex))
		{
			return this.animationMap.get(goalIndex);
		}
		return "";
	}

	public void saveAnimation(int goalIndex, String name)
	{
		this.animationMap.put(goalIndex, name);
		this.setDirty();
	}
}
