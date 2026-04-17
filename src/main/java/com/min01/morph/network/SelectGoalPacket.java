package com.min01.morph.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.morph.util.MorphUtil;
import com.min01.morph.util.MorphSelectionType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SelectGoalPacket 
{
	private final UUID ownerUuid;
	private final MorphSelectionType selectionType;
	private final int selectionId;
	private final int revision;
	
	public SelectGoalPacket(UUID ownerUuid, MorphSelectionType selectionType, int selectionId, int revision) 
	{
		this.ownerUuid = ownerUuid;
		this.selectionType = selectionType;
		this.selectionId = selectionId;
		this.revision = revision;
	}

	public SelectGoalPacket(FriendlyByteBuf buf)
	{
		this.ownerUuid = buf.readUUID();
		int ordinal = buf.readVarInt();
		MorphSelectionType[] values = MorphSelectionType.values();
		this.selectionType = ordinal >= 0 && ordinal < values.length ? values[ordinal] : MorphSelectionType.GOAL;
		this.selectionId = buf.readVarInt();
		this.revision = buf.readVarInt();
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.ownerUuid);
		buf.writeVarInt(this.selectionType.ordinal());
		buf.writeVarInt(this.selectionId);
		buf.writeVarInt(this.revision);
	}
	
	public static boolean handle(SelectGoalPacket message, Supplier<NetworkEvent.Context> ctx) 
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isServer())
			{
				if(ctx.get().getSender() == null || message.selectionId < 0)
				{
					return;
				}
				MorphUtil.selectOption(ctx.get().getSender(), message.ownerUuid, message.selectionType, message.selectionId, message.revision);
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
