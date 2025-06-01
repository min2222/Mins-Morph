package com.min01.morph.network;

import java.util.List;
import java.util.function.Supplier;

import com.min01.morph.command.MorphCommand;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.network.NetworkEvent;

public class UpdateMorphSuggestionsPacket 
{
	private final String suggestionType;
	private final List<String> suggestions;
	
	public UpdateMorphSuggestionsPacket(String suggestionType, List<String> suggestions) 
	{
		this.suggestionType = suggestionType;
		this.suggestions = suggestions;
	}

	public UpdateMorphSuggestionsPacket(FriendlyByteBuf buf)
	{
		this.suggestionType = buf.readUtf();
		this.suggestions = buf.readList(FriendlyByteBuf::readUtf);
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeUtf(this.suggestionType);
		buf.writeCollection(this.suggestions, FriendlyByteBuf::writeUtf);
	}
	
	public static class Handler 
	{
		public static boolean onMessage(UpdateMorphSuggestionsPacket message, Supplier<NetworkEvent.Context> ctx) 
		{
			ctx.get().enqueueWork(() ->
			{
				if(ctx.get().getDirection().getReceptionSide().isClient())
				{
					LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide()).filter(ClientLevel.class::isInstance).ifPresent(level -> 
					{
						List<String> suggestions = message.suggestions;
						String type = message.suggestionType;
						if(type.equals("Data"))
						{
							MorphCommand.DATA_SUGGESTIONS = suggestions;
						}
						else if(type.equals("Goal"))
						{
							MorphCommand.GOAL_SUGGESTIONS = suggestions;
						}
						else if(type.equals("Animation"))
						{
							MorphCommand.ANIMATION_SUGGESTIONS = suggestions;
						}
					});
				}
			});
			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
