package com.min01.morph.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.util.MorphClientUtil;
import com.min01.morph.util.MorphUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
					DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> new Runnable() 
					{
						@Override
						public void run() 
						{
							LivingEntity entity = (LivingEntity) MorphUtil.getEntityByUUID(MorphClientUtil.MC.level, message.entityUUID);
							if(entity != null)
							{
								entity.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
								{
									if(!message.reset)
									{
										LivingEntity living = (LivingEntity) message.type.create(MorphClientUtil.MC.level);
										living.setId(message.entityId);
										t.setMorph(living);
									}
									else
									{
										MorphUtil.removeMorph(entity);
									}
								});
							}
						}
					});
				}
			});
			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
