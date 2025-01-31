package com.min01.morph.mixin;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.collect.Sets;
import com.min01.morph.misc.ITrackedEntity;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

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
	private Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

	@Override
	public void updatePlayersCustom(List<ServerPlayer> players)
	{
		for(ServerPlayer player : players)
		{
			if(player != this.entity) 
			{
				Vec3 vec3 = player.position().subtract(this.entity.position());
				int i = Mth.clamp(ServerLifecycleHooks.getCurrentServer().getPlayerList().getSimulationDistance(), 2, 32);
				double d0 = (double) Math.min(this.getEffectiveRange(), i * 16);
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

	@Shadow
	private int getEffectiveRange()
	{
		throw new IllegalStateException();
	}
}
