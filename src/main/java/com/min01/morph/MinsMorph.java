package com.min01.morph;

import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.entity.MorphEntities;
import com.min01.morph.item.MorphItems;
import com.min01.morph.network.MorphNetwork;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MinsMorph.MODID)
public class MinsMorph 
{
	public static final String MODID = "minsmorph";
	
	public MinsMorph() 
	{
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		MorphItems.ITEMS.register(bus);
		MorphEntities.ENTITY_TYPES.register(bus);
		MorphNetwork.registerMessages();
		MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, MorphCapabilities::attachEntityCapability);
	}
}
