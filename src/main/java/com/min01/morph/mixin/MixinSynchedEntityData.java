package com.min01.morph.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SynchedEntityData.class)
public class MixinSynchedEntityData 
{
	@Shadow
	@Final
	private Entity entity;

	@Inject(at = @At(value = "HEAD"), method = "set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V", cancellable = true)
	private <T> void set(EntityDataAccessor<T> accessor, T value, CallbackInfo ci)
	{
		if(this.entity.getId() < 0)
		{
			for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) 
			{
				List<SynchedEntityData.DataValue<?>> list = this.entity.getEntityData().packDirty();
				if(list != null)
				{
					player.connection.connection.send(new ClientboundSetEntityDataPacket(this.entity.getId(), list));
				}
			}
		}
	}
}
