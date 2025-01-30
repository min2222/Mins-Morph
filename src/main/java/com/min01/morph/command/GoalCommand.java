package com.min01.morph.command;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.misc.IWrappedGoal;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.StrollThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.PatrollingMonster.LongDistancePatrolGoal;
import net.minecraft.world.entity.raid.Raider.ObtainRaidLeaderBannerGoal;

public class GoalCommand 
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("goal").requires((sourceStack) -> 
		{	
			return sourceStack.hasPermission(2);
		}).then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("goalIndex", IntegerArgumentType.integer(0)).executes((commandCtx) ->
		{
			return triggerGoal(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), IntegerArgumentType.getInteger(commandCtx, "goalIndex"));
		}))));
	}
	
	private static int triggerGoal(CommandSourceStack sourceStack, Collection<ServerPlayer> players, int goalIndex)
	{
		for(ServerPlayer player : players)
		{
			player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
			{
				if(t.getMorph() != null)
				{
					Mob mob = (Mob) t.getMorph();
					Set<WrappedGoal> set = mob.goalSelector.getAvailableGoals();
					List<WrappedGoal> list = Lists.newArrayList(set);
					if(set.size() > goalIndex)
					{
						WrappedGoal goal = list.get(goalIndex);
						if(!(goal.getGoal() instanceof LookAtPlayerGoal) && !(goal.getGoal() instanceof LongDistancePatrolGoal) && !(goal.getGoal() instanceof ObtainRaidLeaderBannerGoal) && !(goal.getGoal() instanceof StrollThroughVillageGoal))
						{
							((IWrappedGoal) goal).setCanUse();
							((IWrappedGoal) goal).setFakeTarget(t.getFakeTarget());
							mob.setTarget(t.getFakeTarget());
							goal.start();
							sourceStack.sendSuccess(() -> Component.literal("Triggered goal " + goal.getGoal().getClass().getSimpleName() + " in Index " + goalIndex), true);
						}
						else
						{
							sourceStack.sendFailure(Component.literal("This goal will cause crash!"));
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
