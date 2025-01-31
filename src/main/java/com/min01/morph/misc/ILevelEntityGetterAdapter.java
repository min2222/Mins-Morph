package com.min01.morph.misc;

import java.util.Map;
import java.util.UUID;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.Entity;

public interface ILevelEntityGetterAdapter 
{
	public Int2ObjectMap<Entity> byId();
	
	public Map<UUID, Entity> byUuid();
}
