package com.min01.morph.network;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.min01.morph.util.MorphClientState;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.network.NetworkEvent;

public class SyncMorphOptionsPacket
{
	private final UUID ownerUuid;
	private final int revision;
	private final List<String> goals;
	private final List<String> animations;
	private final List<String> datas;
	private final int selectedGoalId;
	private final int selectedAnimationId;

	public SyncMorphOptionsPacket(UUID ownerUuid, int revision, List<String> goals, List<String> animations, List<String> datas, int selectedGoalId, int selectedAnimationId)
	{
		this.ownerUuid = ownerUuid;
		this.revision = revision;
		this.goals = goals;
		this.animations = animations;
		this.datas = datas;
		this.selectedGoalId = selectedGoalId;
		this.selectedAnimationId = selectedAnimationId;
	}

	public SyncMorphOptionsPacket(FriendlyByteBuf buf)
	{
		this.ownerUuid = buf.readUUID();
		this.revision = buf.readVarInt();
		this.goals = buf.readList(FriendlyByteBuf::readUtf);
		this.animations = buf.readList(FriendlyByteBuf::readUtf);
		this.datas = buf.readList(FriendlyByteBuf::readUtf);
		this.selectedGoalId = buf.readVarInt();
		this.selectedAnimationId = buf.readVarInt();
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.ownerUuid);
		buf.writeVarInt(this.revision);
		buf.writeCollection(this.goals, FriendlyByteBuf::writeUtf);
		buf.writeCollection(this.animations, FriendlyByteBuf::writeUtf);
		buf.writeCollection(this.datas, FriendlyByteBuf::writeUtf);
		buf.writeVarInt(this.selectedGoalId);
		buf.writeVarInt(this.selectedAnimationId);
	}

	public static boolean handle(SyncMorphOptionsPacket message, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isClient())
			{
				LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide()).filter(ClientLevel.class::isInstance).ifPresent(level ->
				{
					MorphClientState.putSnapshot(message.ownerUuid, new MorphClientState.Snapshot(message.revision, message.goals, message.animations, message.datas, message.selectedGoalId, message.selectedAnimationId));
				});
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
