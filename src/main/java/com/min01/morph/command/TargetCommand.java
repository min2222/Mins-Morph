package com.min01.morph.command;

import java.util.Collection;

import com.min01.morph.capabilities.MorphCapabilities;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class TargetCommand 
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("target").requires((sourceStack) -> 
		{	
			return sourceStack.hasPermission(2);
		}).then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("target", EntityArgument.entity()).executes((commandCtx) ->
		{
			return setTarget(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), EntityArgument.getEntity(commandCtx, "target"));
		}))));
	}
	
	private static int setTarget(CommandSourceStack sourceStack, Collection<ServerPlayer> players, Entity entity)
	{
		for(ServerPlayer player : players)
		{
			player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
			{
				if(t.getMorph() != null)
				{
					Mob mob = (Mob) t.getMorph();
					if(entity instanceof LivingEntity living)
					{
						mob.setTarget(living);
					}
					else
					{
						sourceStack.sendFailure(Component.literal("Target is not a living entity"));
					}
				}
				else
				{
					sourceStack.sendFailure(Component.literal("Player doesn't morphed to anything"));
				}
			});
		}
		return players.size();
	}
}
