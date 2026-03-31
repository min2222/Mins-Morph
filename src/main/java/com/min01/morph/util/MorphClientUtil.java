package com.min01.morph.util;

import net.minecraft.client.Minecraft;

public class MorphClientUtil 
{
	public static final Minecraft MC = Minecraft.getInstance();
	
    public static double getMouseAngle() {
        return getRelativeAngle(MC.getWindow().getScreenWidth() * 0.5D, MC.getWindow().getScreenHeight() * 0.5D, MC.mouseHandler.xpos(), MC.mouseHandler.ypos());
    }

    private static double getRelativeAngle(double originX, double originY, double x, double y) {
        double angle = -Math.toDegrees(Math.atan2(x - originX, y - originY));

        return correctAngle(angle);
    }

    public static double correctAngle(double angle) {
        if (angle < 0) {
            angle += 360;
        } else if (angle > 360) {
            angle -= 360;
        }
        return angle;
    }
}
