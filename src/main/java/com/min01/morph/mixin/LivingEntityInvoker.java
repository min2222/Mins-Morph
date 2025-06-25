package com.min01.morph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker 
{
	@Invoker("isAffectedByFluids")
	public boolean invokeIsAffectedByFluids();
}
