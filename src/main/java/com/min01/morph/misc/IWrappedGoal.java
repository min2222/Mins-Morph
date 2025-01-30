package com.min01.morph.misc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public interface IWrappedGoal
{
	public void setEntity(Mob mob);
	
	public void setFakeTarget(LivingEntity target);
	
	public void setCanUse();
}
