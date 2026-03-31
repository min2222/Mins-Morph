package com.min01.morph.network;

import java.util.function.Supplier;

import com.min01.morph.util.MorphUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class TriggerGoalPacket 
{
	public TriggerGoalPacket() 
	{
		
	}

	public TriggerGoalPacket(FriendlyByteBuf buf)
	{
		
	}

	public void write(FriendlyByteBuf buf)
	{
		
	}
	
	public static boolean handle(TriggerGoalPacket message, Supplier<NetworkEvent.Context> ctx) 
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isServer())
			{
				MorphUtil.triggerGoal(ctx.get().getSender());
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
