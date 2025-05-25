package com.min01.morph.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class MorphConfig 
{
	public static final MorphConfig CONFIG;
	public static final ForgeConfigSpec CONFIG_SPEC;

	public static ForgeConfigSpec.DoubleValue healthThreshold;
    
    static 
    {
    	Pair<MorphConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(MorphConfig::new);
    	CONFIG = pair.getLeft();
    	CONFIG_SPEC = pair.getRight();
    }
	
    public MorphConfig(ForgeConfigSpec.Builder config) 
    {
    	config.push("Settings");
    	MorphConfig.healthThreshold = config.comment("minimum value of health for show it as number instead of icon").defineInRange("healthThreshold", 100.0D, 0.0D, Double.MAX_VALUE);
        config.pop();
    }
}
