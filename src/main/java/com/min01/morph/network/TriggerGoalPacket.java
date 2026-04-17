package com.min01.morph.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.morph.util.MorphUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class TriggerGoalPacket 
{
	private final UUID ownerUuid;
	private final int revision;
	
	public TriggerGoalPacket(UUID ownerUuid, int revision) 
	{
		this.ownerUuid = ownerUuid;
		this.revision = revision;
	}

	public TriggerGoalPacket(FriendlyByteBuf buf)
	{
		this.ownerUuid = buf.readUUID();
		this.revision = buf.readVarInt();
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.ownerUuid);
		buf.writeVarInt(this.revision);
	}
	
	public static boolean handle(TriggerGoalPacket message, Supplier<NetworkEvent.Context> ctx) 
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isServer() && ctx.get().getSender() != null)
			{
				MorphUtil.triggerGoal(ctx.get().getSender(), message.ownerUuid, message.revision);
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
