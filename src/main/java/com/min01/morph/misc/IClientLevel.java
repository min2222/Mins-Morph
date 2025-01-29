package com.min01.morph.misc;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.Entity;

public interface IClientLevel 
{
	public Int2ObjectMap<Entity> byId();
}
