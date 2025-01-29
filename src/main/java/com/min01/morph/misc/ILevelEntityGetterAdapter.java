package com.min01.morph.misc;

import java.util.Map;
import java.util.UUID;

import net.minecraft.world.entity.Entity;

public interface ILevelEntityGetterAdapter 
{
	public Map<UUID, Entity> byUuid();
}
