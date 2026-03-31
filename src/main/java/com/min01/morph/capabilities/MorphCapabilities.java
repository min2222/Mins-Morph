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
    		MorphCapabilityImpl cap = new MorphCapabilityImpl();
        	cap.setEntity(living);
    		event.addCapability(MorphCapabilityImpl.ID, cap);
    	}
	}
}
