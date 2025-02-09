package com.min01.morph.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.util.MorphUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class UpdateMorphPacket 
{
	private final UUID entityUUID;
	private final EntityType<?> type;
	private final int entityId;
	private final boolean reset;
	
	public UpdateMorphPacket(LivingEntity entity, EntityType<?> type, int entityId, boolean reset) 
	{
		this.entityUUID = entity.getUUID();
		this.type = type;
		this.entityId = entityId;
		this.reset = reset;
	}

	public UpdateMorphPacket(FriendlyByteBuf buf)
	{
		this.entityUUID = buf.readUUID();
		this.type = ForgeRegistries.ENTITY_TYPES.getValue(buf.readResourceLocation());
		this.entityId = buf.readInt();
		this.reset = buf.readBoolean();
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.entityUUID);
		buf.writeResourceLocation(ForgeRegistries.ENTITY_TYPES.getKey(this.type));
		buf.writeInt(this.entityId);
		buf.writeBoolean(this.reset);
	}
	
	public static class Handler 
	{
		public static boolean onMessage(UpdateMorphPacket message, Supplier<NetworkEvent.Context> ctx) 
		{
			ctx.get().enqueueWork(() ->
			{
				if(ctx.get().getDirection().getReceptionSide().isClient())
				{
					LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide()).filter(ClientLevel.class::isInstance).ifPresent(level -> 
					{
						LivingEntity entity = (LivingEntity) MorphUtil.getEntityByUUID(level, message.entityUUID);
						if(entity != null)
						{
							entity.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
							{
								if(!message.reset)
								{
									LivingEntity living = (LivingEntity) message.type.create(level);
									living.setId(message.entityId);
									t.setMorph(living);
								}
								else
								{
									MorphUtil.removeMorph(entity);
								}
							});
						}
					});
				}
			});
			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
