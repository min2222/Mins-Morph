package com.min01.morph.entity;

import com.min01.morph.MinsMorph;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MorphEntities
{
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MinsMorph.MODID);

	public static final RegistryObject<EntityType<EntityFakeTarget>> FAKE_TARGET = registerEntity("fake_target", createBuilder(EntityFakeTarget::new, MobCategory.MISC).sized(0.0F, 0.0F).fireImmune().noSave().noSummon().setShouldReceiveVelocityUpdates(false).setTrackingRange(0).updateInterval(0));
	
	public static <T extends Entity> EntityType.Builder<T> createBuilder(EntityType.EntityFactory<T> factory, MobCategory category)
	{
		return EntityType.Builder.<T>of(factory, category);
	}
	
	public static <T extends Entity> RegistryObject<EntityType<T>> registerEntity(String name, EntityType.Builder<T> builder) 
	{
		return ENTITY_TYPES.register(name, () -> builder.build(new ResourceLocation(MinsMorph.MODID, name).toString()));
	}
}
