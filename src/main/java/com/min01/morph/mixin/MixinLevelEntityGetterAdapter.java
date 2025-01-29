package com.min01.morph.mixin;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Maps;
import com.min01.morph.misc.ILevelEntityGetterAdapter;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;

@Mixin(LevelEntityGetterAdapter.class)
public class MixinLevelEntityGetterAdapter implements ILevelEntityGetterAdapter
{
	@Unique
	private Map<UUID, Entity> byUuid = Maps.newHashMap();
	
	@Inject(at = @At(value = "HEAD"), method = "get(Ljava/util/UUID;)Lnet/minecraft/world/level/entity/EntityAccess;", cancellable = true)
	private void getEntity(UUID uuid, CallbackInfoReturnable<Entity> cir)
	{
		if(this.byUuid.containsKey(uuid))
		{
			cir.setReturnValue(this.byUuid.get(uuid));
		}
	}

	@Override
	public Map<UUID, Entity> byUuid()
	{
		return this.byUuid;
	}
}
