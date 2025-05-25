package com.min01.morph.event;

import com.min01.morph.MinsMorph;
import com.min01.morph.config.MorphConfig;
import com.min01.morph.util.MorphClientUtil;
import com.min01.morph.util.MorphUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MinsMorph.MODID)
public class ClientEventHandlerForge
{
	@SubscribeEvent
    public static void onRenderGuiOverlayEvent(RenderGuiOverlayEvent event)
    {
    	Player player = MorphClientUtil.MC.player;
    	MorphUtil.getMorph(player, t -> 
    	{
        	if(event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type() && !player.getAbilities().instabuild && !player.isSpectator() && t.getMaxHealth() >= MorphConfig.healthThreshold.get())
        	{
        		GuiGraphics guiGraphics = event.getGuiGraphics();
        		Component component = Component.literal(t.getHealth() + " / " + t.getMaxHealth());
        		PoseStack poseStack = guiGraphics.pose();
        		event.setCanceled(true);
        		poseStack.pushPose();
                int screenWidth = MorphClientUtil.MC.getWindow().getGuiScaledWidth();
                int screenHeight = MorphClientUtil.MC.getWindow().getGuiScaledHeight();
        		int left = screenWidth / 2 - 91;
        		int top = screenHeight - 50;
        		guiGraphics.drawString(MorphClientUtil.MC.font, component, left, top, 16777215);
        		poseStack.popPose();
        	}
    	});
    }
    
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static <T extends LivingEntity & GeoAnimatable, E extends LivingEntity> void onRenderPlayerPre(RenderPlayerEvent.Pre event)
	{
    	Player player = event.getEntity();
    	MorphUtil.getMorph(player, t -> 
    	{
    		if(!player.isSpectator())
    		{
            	event.setCanceled(true);
        		EntityRenderer<? super E> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(t);
        		float yaw = Mth.lerp(event.getPartialTick(), t.yRotO, t.getYRot());
        		MorphUtil.tick(player, t);
	    		if(ModList.get().isLoaded("geckolib") && renderer instanceof GeoEntityRenderer)
	    		{
					GeoEntityRenderer<T> geoRenderer = (GeoEntityRenderer<T>) renderer;
					T animatable = (T) t;
					geoRenderer.render(animatable, yaw, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
					return;
	    		}
    			renderer.render((E) t, yaw, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    		}
    	});
	}
}
