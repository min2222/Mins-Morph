package com.min01.morph.command;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

public class GoalCopyCommand 
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("goalcopy").requires((sourceStack) -> 
		{	
			return sourceStack.hasPermission(2);
		}).then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("goalIndex", IntegerArgumentType.integer(0)).then(Commands.argument("target", EntityArgument.entity()).executes((commandCtx) ->
		{
			return copyGoalTick(commandCtx.getSource(), EntityArgument.getPlayer(commandCtx, "player"), IntegerArgumentType.getInteger(commandCtx, "goalIndex"), EntityArgument.getEntity(commandCtx, "target"));
		})))));
	}
	
	private static int copyGoalTick(CommandSourceStack sourceStack, ServerPlayer player, int goalIndex, Entity entity)
	{
		player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
			if(t.getMorph() != null)
			{
				if(entity instanceof Mob mob)
				{
					Mob morph = ((Mob) t.getMorph());
					Set<WrappedGoal> set = morph.goalSelector.getAvailableGoals();
					Set<WrappedGoal> set1 = mob.goalSelector.getAvailableGoals();
					List<WrappedGoal> list = Lists.newArrayList(set);
					List<WrappedGoal> list1 = Lists.newArrayList(set1);
					if(set.size() > goalIndex && set1.size() > goalIndex && morph.getType() == mob.getType())
					{
						WrappedGoal goal = list.get(goalIndex);
						WrappedGoal goal1 = list1.get(goalIndex);
						if(((IWrappedGoal) goal1).getLastTick() != 0)
						{
							((IWrappedGoal) goal).setLastTick(((IWrappedGoal) goal1).getLastTick());
							sourceStack.sendSuccess(() -> Component.literal("Copied Goal Tick " + ((IWrappedGoal) goal1).getLastTick() + " from " + goal.getGoal().getClass().getSimpleName() + " at Index " + goalIndex + " of " + mob.getDisplayName().getString()), true);
							MorphSavedData data = MorphSavedData.get(player.level, player.level.dimension());
				        	if(data != null)
				        	{
				        		data.saveGoalTicks(mob.getType(), goalIndex, ((IWrappedGoal) goal1).getLastTick());
				        	}
						}
						else
						{
							sourceStack.sendFailure(Component.literal("Tick is zero, make sure mob already performed a goal"));
						}
					}
					else
					{
						sourceStack.sendFailure(Component.literal("Target is invalid or Index is too large"));
					}
				}
				else
				{
					sourceStack.sendFailure(Component.literal("Target is invalid"));
				}
			}
			else
			{
				sourceStack.sendFailure(Component.literal("Player doesn't morphed to anything"));
			}
		});
		return 1;
	}
}
