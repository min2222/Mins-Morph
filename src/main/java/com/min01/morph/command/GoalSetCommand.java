package com.min01.morph.command;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.misc.IWrappedGoal;
import com.min01.morph.world.MorphSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

public class GoalSetCommand 
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("goalset").requires((sourceStack) -> 
		{	
			return sourceStack.hasPermission(2);
		}).then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("goalIndex", IntegerArgumentType.integer(0)).then(Commands.argument("goalTick", IntegerArgumentType.integer()).executes((commandCtx) ->
		{
			return setGoalTick(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), IntegerArgumentType.getInteger(commandCtx, "goalIndex"), IntegerArgumentType.getInteger(commandCtx, "goalTick"));
		})))));
	}
	
	private static int setGoalTick(CommandSourceStack sourceStack, Collection<ServerPlayer> players, int goalIndex, int goalTick)
	{
		for(ServerPlayer player : players)
		{
			player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
			{
				if(t.getMorph() != null)
				{
					Mob morph = ((Mob) t.getMorph());
					Set<WrappedGoal> set = morph.goalSelector.getAvailableGoals();
					List<WrappedGoal> list = Lists.newArrayList(set);
					if(set.size() > goalIndex)
					{
						WrappedGoal goal = list.get(goalIndex);
						((IWrappedGoal) goal).setLastTick(goalTick);
						sourceStack.sendSuccess(() -> Component.literal("Set Goal Tick of " + goal.getGoal().getClass().getSimpleName() + " to " + goalTick), true);
						MorphSavedData data = MorphSavedData.get(player.level, player.level.dimension());
			        	if(data != null)
			        	{
			        		data.saveGoalTicks(morph.getType(), goalIndex, goalTick);
			        	}
					}
					else
					{
						sourceStack.sendFailure(Component.literal("Index is too large!"));
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
