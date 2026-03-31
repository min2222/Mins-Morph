package com.min01.morph.event;

import org.lwjgl.opengl.GL11;

import com.min01.morph.MinsMorph;
import com.min01.morph.command.MorphCommand;
import com.min01.morph.config.MorphConfig;
import com.min01.morph.gui.GoalMenuScreen;
import com.min01.morph.network.MorphNetwork;
import com.min01.morph.network.TriggerGoalPacket;
import com.min01.morph.util.MorphClientUtil;
import com.min01.morph.util.MorphUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MinsMorph.MODID)
public class ClientEventHandlerForge
{
    private static final double OUTER_RADIUS = 80;
    private static final double INNER_RADIUS = 60;
    private static boolean lastWheelState = false;
    
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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) 
    {
        if(event.phase == TickEvent.Phase.END)
        {
            if((MorphClientUtil.MC.level == null || MorphClientUtil.MC.isPaused()) && GoalMenuScreen.active)
            {
                GoalMenuScreen.deactivate();
            }
        }
        
        if(event.phase == TickEvent.Phase.END || event.type != TickEvent.Type.CLIENT) 
        {
            return;
        }
        
        if(MorphClientUtil.MC.level == null) 
        {
            return;
        }

        long handle = MorphClientUtil.MC.getWindow().getWindow();
        boolean wheelKeyPressed = ClientEventHandler.OPEN_MENU.getKey().getValue() >= 0 ? InputConstants.isKeyDown(handle, ClientEventHandler.OPEN_MENU.getKey().getValue()) : InputConstants.isKeyDown(handle, ClientEventHandler.OPEN_MENU.getKey().getValue() + 100);
        if(wheelKeyPressed != lastWheelState) 
        {
            if(wheelKeyPressed != GoalMenuScreen.active) 
            {
                if(wheelKeyPressed) 
                {
                    if(MorphClientUtil.MC.screen == null || MorphClientUtil.MC.screen instanceof GoalMenuScreen) 
                    {
                        GoalMenuScreen.activate();
                    }
                } 
                else 
                {
                    GoalMenuScreen.deactivate();
                }
            }
        }
        lastWheelState = wheelKeyPressed;
        
        if(ClientEventHandler.TRIGGER.consumeClick())
        {
        	MorphNetwork.sendToServer(new TriggerGoalPacket());
        }
    }

    @SubscribeEvent
    public static void onRenderOverlayPost(RenderGuiOverlayEvent.Post event) 
    {
        if(!(event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_LIST.id()))) 
        {
            return;
        }

        if(MorphClientUtil.MC.level != null && !MorphClientUtil.MC.options.hideGui && !MorphClientUtil.MC.isPaused() && GoalMenuScreen.active)
        {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            renderButtonBackgrounds();
            renderText(guiGraphics);
        }
    }
    
    private static void renderText(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        Font fontRenderer = mc.font;
        double mouseAngle = MorphClientUtil.getMouseAngle();
        int goalSize = MorphCommand.GOAL_SUGGESTIONS.size();
        int animationSize = MorphCommand.ANIMATION_SUGGESTIONS.size();
        int totalSize = goalSize + animationSize;
        float angle = 360 / totalSize;
        mouseAngle -= angle / 2;
        mouseAngle = 360 - mouseAngle;
        mouseAngle = MorphClientUtil.correctAngle(mouseAngle);

        for (int i = 0; i < totalSize; i++) {
            double currAngle = angle * i;
            double nextAngle = currAngle + angle;
            currAngle = MorphClientUtil.correctAngle(currAngle);
            nextAngle = MorphClientUtil.correctAngle(nextAngle);

            boolean mouseIn = mouseAngle > currAngle && mouseAngle < nextAngle;

            if (mouseIn) {
            	int index = i - goalSize;
            	String name = "";
            	if(i < goalSize) {
                	name = MorphCommand.GOAL_SUGGESTIONS.get(i);
            	}
            	else if(index < animationSize && index > 0) {
                	name = MorphCommand.ANIMATION_SUGGESTIONS.get(index);
        		}

                int drawX = window.getGuiScaledWidth() / 2 - fontRenderer.width(name) / 2;
                int drawY = window.getGuiScaledHeight() / 2;

                int drawWidth = mc.font.width(name);
                int drawHeight = mc.font.lineHeight;

                float padding = 5F;

                // Background
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShader(GameRenderer::getPositionColorShader);

                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

                float r = 0;
                float g = 0;
                float b = 0;
                float alpha = 153 / 255;

                bufferBuilder.vertex(drawX - padding, drawY + drawHeight + padding, 0).color(r, g, b, alpha).endVertex();
                bufferBuilder.vertex(drawX + drawWidth + padding, drawY + drawHeight + padding, 0).color(r, g, b, alpha).endVertex();
                bufferBuilder.vertex(drawX + drawWidth + padding, drawY - padding, 0).color(r, g, b, alpha).endVertex();
                bufferBuilder.vertex(drawX - padding, drawY - padding, 0).color(r, g, b, alpha).endVertex();

                tessellator.end();
                RenderSystem.disableBlend();

                // Text
                guiGraphics.drawString(Minecraft.getInstance().font, name, drawX, drawY, 0xFFFFFF, false);
            }
        }
    }

    private static void renderButtonBackgrounds() {
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(mc.getWindow().getGuiScaledWidth() * 0.5D, mc.getWindow().getGuiScaledHeight() * 0.5D, 0);
        RenderSystem.applyModelViewMatrix();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        int goalSize = MorphCommand.GOAL_SUGGESTIONS.size();
        int animationSize = MorphCommand.ANIMATION_SUGGESTIONS.size();
        int totalSize = goalSize + animationSize;
        float angle = 360 / totalSize;
        double mouseAngle = MorphClientUtil.getMouseAngle();
        mouseAngle -= (angle / 2);
        mouseAngle = MorphClientUtil.correctAngle(mouseAngle);
        for (int i = 0; i < totalSize; i++) {
            double currAngle = (angle * i) + 90 + (angle / 2);
            double nextAngle = (currAngle + angle);
            currAngle = MorphClientUtil.correctAngle(currAngle);
            nextAngle = MorphClientUtil.correctAngle(nextAngle);
            double truecurrAngle = (angle * i);
            double truenextAngle = (truecurrAngle + angle);
            currAngle = MorphClientUtil.correctAngle(currAngle);
            nextAngle = MorphClientUtil.correctAngle(nextAngle);

            boolean mouseIn = (mouseAngle > truecurrAngle && mouseAngle < truenextAngle);

            currAngle = Math.toRadians(currAngle);
            nextAngle = Math.toRadians(nextAngle);

            double innerRadius = ((INNER_RADIUS - 0 - (mouseIn ? 2 : 0)) / 100F) * (130F);
            double outerRadius = ((OUTER_RADIUS - 0 + (mouseIn ? 2 : 0)) / 100F) * (130F);

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float r, g, b, alpha;

            if (mouseIn) {
                r = 1;
                g = 0;
                b = 0;
                alpha = 153 / (float) 255;
            } else {
                r = 0;
                g = 0;
                b = 0;
                alpha = (float) 153 / (float) 255;
            }

            double x1 = Math.cos(currAngle) * innerRadius;
            double x2 = Math.cos(currAngle) * outerRadius;
            double x3 = Math.cos(nextAngle) * outerRadius;
            double x4 = Math.cos(nextAngle) * innerRadius;

            double y1 = Math.sin(currAngle) * innerRadius;
            double y2 = Math.sin(currAngle) * outerRadius;
            double y3 = Math.sin(nextAngle) * outerRadius;
            double y4 = Math.sin(nextAngle) * innerRadius;

            bufferBuilder.vertex(x1, y1, 0).color(r, g, b, alpha).endVertex();
            bufferBuilder.vertex(x2, y2, 0).color(r, g, b, alpha).endVertex();
            bufferBuilder.vertex(x3, y3, 0).color(r, g, b, alpha).endVertex();
            bufferBuilder.vertex(x4, y4, 0).color(r, g, b, alpha).endVertex();

            tessellator.end();
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
