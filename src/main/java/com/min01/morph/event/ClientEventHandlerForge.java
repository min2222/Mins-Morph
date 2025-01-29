package com.min01.morph.event;

import com.min01.morph.MinsMorph;
import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.util.MorphUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MinsMorph.MODID)
public class ClientEventHandlerForge
{
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static <T extends LivingEntity & GeoAnimatable, E extends LivingEntity> void onRenderPlayerPre(RenderPlayerEvent.Pre event)
	{
    	Player player = event.getEntity();
    	player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
    	{
    		LivingEntity living = t.getMorph();
    		if(living != null)
    		{
            	event.setCanceled(true);
        		EntityRenderer<? super E> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(living);
        		float yaw = Mth.lerp(event.getPartialTick(), living.yRotO, living.getYRot());
        		MorphUtil.tick(player, living);
	    		if(ModList.get().isLoaded("geckolib") && renderer instanceof GeoEntityRenderer)
	    		{
					GeoEntityRenderer<T> geoRenderer = (GeoEntityRenderer<T>) renderer;
					T animatable = (T) living;
					geoRenderer.render(animatable, yaw, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
	    		}
        		if(renderer instanceof LivingEntityRenderer)
        		{
        			LivingEntityRenderer<? super LivingEntity, ?> livingrenderer = (LivingEntityRenderer<? super LivingEntity, ?>) renderer;
        			livingrenderer.render(living, yaw, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        		}
    		}
    	});
	}
}
