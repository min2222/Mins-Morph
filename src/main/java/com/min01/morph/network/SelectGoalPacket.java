package com.min01.morph.network;

import java.util.function.Supplier;

import com.min01.morph.command.MorphCommand;
import com.min01.morph.util.MorphUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SelectGoalPacket 
{
	private final String goalName;
	
	public SelectGoalPacket(String goalName) 
	{
		this.goalName = goalName;
	}

	public SelectGoalPacket(FriendlyByteBuf buf)
	{
		this.goalName = buf.readUtf();
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeUtf(this.goalName);
	}
	
	public static boolean handle(SelectGoalPacket message, Supplier<NetworkEvent.Context> ctx) 
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isServer())
			{
				if(MorphCommand.ANIMATION_SUGGESTIONS.contains(message.goalName))
				{
					MorphUtil.selectAnimation(ctx.get().getSender(), message.goalName);
				}
				else
				{
					MorphUtil.selectGoal(ctx.get().getSender(), message.goalName);
				}
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
