package com.min01.morph.capabilities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class MorphCapabilities
{
	public static void onAttachEntityCapability(AttachCapabilitiesEvent<Entity> event)
	{
    	Entity entity = event.getObject();
    	if(entity instanceof LivingEntity living)
    	{
    		event.addCapability(MorphCapabilityImpl.ID, new MorphCapabilityImpl(living));
    	}
	}
}
