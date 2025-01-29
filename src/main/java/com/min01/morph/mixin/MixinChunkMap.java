package com.min01.morph.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
	private void broadcastAndSend(Entity p_140334_, Packet<?> p_140335_, CallbackInfo ci)
	{
		if(p_140334_.getId() < 0)
		{
			ci.cancel();
			EntityType<?> type = p_140334_.getType();
			int i = type.clientTrackingRange() * 16;
			if(i != 0)
			{
	            int j = type.updateInterval();
	            ChunkMap.TrackedEntity chunkmap$trackedentity = ChunkMap.class.cast(this).new TrackedEntity(p_140334_, i, j, type.trackDeltas());
	            chunkmap$trackedentity.updatePlayers(this.level.players());
	            chunkmap$trackedentity.broadcastAndSend(p_140335_);
			}
		}
	}
}
