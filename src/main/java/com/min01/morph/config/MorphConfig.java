package com.min01.morph.config;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphConfig 
{
	public static final MorphConfig CONFIG;
	public static final ForgeConfigSpec CONFIG_SPEC;

	public static ForgeConfigSpec.DoubleValue healthThreshold;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> flightMorphOverrides;
    
    static 
    {
    	Pair<MorphConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(MorphConfig::new);
    	CONFIG = pair.getLeft();
    	CONFIG_SPEC = pair.getRight();
    }
	
    public MorphConfig(ForgeConfigSpec.Builder config) 
    {
    	config.push("Settings");
    	MorphConfig.healthThreshold = config.comment("if health of morphed mob is higher than threshold, it will displayed as number").defineInRange("healthThreshold", 100.0D, 0.0D, Double.MAX_VALUE);
    	MorphConfig.flightMorphOverrides = config.comment("Entity type ids that should be treated as flying morphs").defineListAllowEmpty(List.of("flightMorphOverrides"), List::of, value -> value instanceof String);
        config.pop();
    }

    public static boolean isFlightOverride(EntityType<?> entityType)
    {
    	if(entityType == null)
    	{
    		return false;
    	}
    	String key = ForgeRegistries.ENTITY_TYPES.getKey(entityType) == null ? "" : ForgeRegistries.ENTITY_TYPES.getKey(entityType).toString();
    	return !key.isEmpty() && MorphConfig.flightMorphOverrides.get().contains(key);
    }
}
