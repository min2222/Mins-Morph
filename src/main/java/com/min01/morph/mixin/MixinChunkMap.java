package com.min01.morph.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.morph.misc.ITrackedEntity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Mixin(ChunkMap.class)
public class MixinChunkMap
{
	@Shadow
	@Final
	private ServerLevel level;
	
	@Inject(at = @At(value = "HEAD"), method = "broadcastAndSend", cancellable = true)
	private void broadcastAndSend(Entity entity, Packet<?> packet, CallbackInfo ci)
	{
		if(entity.getId() < 0)
		{
			EntityType<?> type = entity.getType();
			int i = type.clientTrackingRange() * 16;
			if(i != 0)
			{
	            int j = type.updateInterval();
	            ChunkMap.TrackedEntity chunkmap$trackedentity = ChunkMap.class.cast(this).new TrackedEntity(entity, i, j, type.trackDeltas());
	            ((ITrackedEntity)chunkmap$trackedentity).updatePlayersCustom(this.level.players());
	            chunkmap$trackedentity.broadcastAndSend(packet);
			}
		}
	}
}
