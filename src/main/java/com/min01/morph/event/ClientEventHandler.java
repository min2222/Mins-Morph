package com.min01.morph.event;

import com.min01.morph.MinsMorph;
import com.min01.morph.entity.MorphEntities;
import com.min01.morph.entity.renderer.NoneRenderer;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MinsMorph.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler
{
	public static final KeyMapping OPEN_MENU = new KeyMapping("key.minsmorph.open_menu", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_Y, "key.categories.minsmorph");
	public static final KeyMapping TRIGGER = new KeyMapping("key.minsmorph.trigger", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_V, "key.categories.minsmorph");
	
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event)
    {
    	event.register(OPEN_MENU);
    	event.register(TRIGGER);
    }
    
    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
    	event.registerEntityRenderer(MorphEntities.FAKE_TARGET.get(), NoneRenderer::new);
    }
}
