package com.min01.morph.event;

import com.min01.morph.MinsMorph;
import com.min01.morph.entity.MorphEntities;

import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MinsMorph.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandler 
{
    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) 
    {
    	event.put(MorphEntities.FAKE_TARGET.get(), Mob.createMobAttributes().build());
    }
}
