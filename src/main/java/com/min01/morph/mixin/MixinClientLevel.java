package com.min01.morph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.morph.misc.IClientLevel;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

@Mixin(ClientLevel.class)
public class MixinClientLevel implements IClientLevel
{
	@Unique
	private Int2ObjectMap<Entity> byId = new Int2ObjectLinkedOpenHashMap<>();
	   
	@Inject(at = @At(value = "HEAD"), method = "getEntity", cancellable = true)
	private void getEntity(int id, CallbackInfoReturnable<Entity> cir)
	{
		if(id < 0)
		{
			cir.setReturnValue(this.byId.get(id));
		}
	}

	@Override
	public Int2ObjectMap<Entity> byId()
	{
		return this.byId;
	}
}
