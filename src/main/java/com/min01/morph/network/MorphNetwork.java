package com.min01.morph.network;

import com.min01.morph.MinsMorph;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public class MorphNetwork 
{
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath(MinsMorph.MODID, "minsmorph"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);
	
	public static int ID = 0;
	public static void registerMessages()
	{
		CHANNEL.registerMessage(ID++, UpdateMorphPacket.class, UpdateMorphPacket::write, UpdateMorphPacket::new, UpdateMorphPacket::handle);
		CHANNEL.registerMessage(ID++, UpdateMorphSuggestionsPacket.class, UpdateMorphSuggestionsPacket::write, UpdateMorphSuggestionsPacket::new, UpdateMorphSuggestionsPacket::handle);
		CHANNEL.registerMessage(ID++, SelectGoalPacket.class, SelectGoalPacket::write, SelectGoalPacket::new, SelectGoalPacket::handle);
		CHANNEL.registerMessage(ID++, TriggerGoalPacket.class, TriggerGoalPacket::write, TriggerGoalPacket::new, TriggerGoalPacket::handle);
	}
	
    public static <MSG> void sendToServer(MSG message) 
    {
    	CHANNEL.sendToServer(message);
    }
    
    public static <MSG> void sendToAll(MSG message)
    {
    	for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) 
    	{
    		CHANNEL.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    	}
    }
}
