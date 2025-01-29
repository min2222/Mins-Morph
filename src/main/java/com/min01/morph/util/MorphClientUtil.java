package com.min01.morph.util;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MorphClientUtil 
{
	public static final Minecraft MC = Minecraft.getInstance();
}
