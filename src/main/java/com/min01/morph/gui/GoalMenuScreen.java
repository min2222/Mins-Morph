package com.min01.morph.gui;

import com.min01.morph.command.MorphCommand;
import com.min01.morph.network.MorphNetwork;
import com.min01.morph.network.SelectGoalPacket;
import com.min01.morph.util.MorphClientUtil;
import com.min01.morph.util.MorphUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class GoalMenuScreen extends Screen {
    public static final GoalMenuScreen INSTANCE = new GoalMenuScreen();
    public static boolean active = false;

    public GoalMenuScreen() {
        super(Component.translatable("minsmorph.goalmenu.title"));
    }

    public static void activate() {
        if (Minecraft.getInstance().screen == null && MorphUtil.hasMorph(MorphClientUtil.MC.player)) {
            active = true;
            Minecraft.getInstance().setScreen(INSTANCE);
        }
    }

    public static void deactivate() {
        active = false;
        if (Minecraft.getInstance().screen == INSTANCE) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (active) {
            double mouseAngle = MorphClientUtil.getMouseAngle();
            int goalSize = MorphCommand.GOAL_SUGGESTIONS.size();
            int animationSize = MorphCommand.ANIMATION_SUGGESTIONS.size();
            int totalSize = goalSize + animationSize;
            float angle = 360 / totalSize;
            mouseAngle -= angle / 2;
            mouseAngle = 360 - mouseAngle;
            mouseAngle = MorphClientUtil.correctAngle(mouseAngle);

            if (!this.getMinecraft().options.hideGui) {
                for (int i = 0; i < totalSize; i++) {
                    double currAngle = angle * i;
                    double nextAngle = currAngle + angle;
                    currAngle = MorphClientUtil.correctAngle(currAngle);
                    nextAngle = MorphClientUtil.correctAngle(nextAngle);

                    boolean mouseIn = mouseAngle > currAngle && mouseAngle < nextAngle;

                    if (mouseIn) {
                        if (button == 0) {
                        	int index = i - goalSize;
                        	String name = "";
                        	if(i < goalSize) {
                            	name = MorphCommand.GOAL_SUGGESTIONS.get(i);
                        	}
                        	else if(index < animationSize && index > 0) {
                            	name = MorphCommand.ANIMATION_SUGGESTIONS.get(index);
                    		}
                        	MorphNetwork.sendToServer(new SelectGoalPacket(name));
                        	this.getMinecraft().player.playSound(SoundEvents.UI_BUTTON_CLICK.get());
                            deactivate();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        active = false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}