package com.min01.morph.mixin;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.min01.morph.misc.ITrackedEntity;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(ChunkMap.TrackedEntity.class)
public class MixinTrackedEntity implements ITrackedEntity
{
	@Shadow
	@Final
	private ServerEntity serverEntity;
	
	@Shadow
	@Final
	private Entity entity;

	@Shadow
	@Final
	private Set<ServerPlayerConnection> seenBy;
	
	@Unique
	int dist;

	@Override
	public void updatePlayersCustom(List<ServerPlayer> players)
	{
		for(ServerPlayer player : players)
		{
			if(player != this.entity) 
			{
				Vec3 vec3 = player.position().subtract(this.entity.position());
				double d0 = (double) Math.min(this.getEffectiveRange(), this.dist * 16);
				double d1 = vec3.x * vec3.x + vec3.z * vec3.z;
				double d2 = d0 * d0;
				boolean flag = d1 <= d2 && this.entity.broadcastToPlayer(player);
				if(flag)
				{
					if(this.seenBy.add(player.connection)) 
					{
						
					}
				} 
				else if(this.seenBy.remove(player.connection))
				{
					
				}
			}
		}
	}
	
	@Override
	public void setViewDist(int dist)
	{
		this.dist = dist;
	}

	@Shadow
	private int getEffectiveRange()
	{
		throw new IllegalStateException();
	}
}
