package com.min01.morph.command;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.min01.morph.capabilities.IMorphCapability;
import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.capabilities.MorphImpl;
import com.min01.morph.misc.IWrappedGoal;
import com.min01.morph.util.MorphUtil;
import com.min01.morph.util.world.MorphSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraftforge.server.ServerLifecycleHooks;

public class GoalCommand 
{
	public static final SuggestionProvider<CommandSourceStack> GOALS = SuggestionProviders.register(new ResourceLocation("goals"), (p_258164_, p_258165_) -> 
	{
		for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
		{
			MorphSavedData data = MorphSavedData.get(player.level);
        	if(data != null)
        	{
        		return SharedSuggestionProvider.suggest(data.getGoalMap().keySet().stream().filter(t -> 
        		{
        			return !t.contains("$");
        		}), p_258165_);
        	}
		}
		return null;
	});
	
	public static final SuggestionProvider<CommandSourceStack> ANIMATIONS = SuggestionProviders.register(new ResourceLocation("animations"), (p_258164_, p_258165_) -> 
	{
		for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
		{
			MorphSavedData data = MorphSavedData.get(player.level);
        	if(data != null)
        	{
        		IMorphCapability cap = player.getCapability(MorphCapabilities.MORPH).orElse(new MorphImpl());
        		if(cap.getMorph() != null)
        		{
            		return SharedSuggestionProvider.suggest(data.getAnimations(cap.getMorph().getClass().getSimpleName()).stream().filter(t -> 
            		{
            			return !t.contains("$");
            		}), p_258165_);
        		}
        	}
		}
		return null;
	});
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("goal").requires((sourceStack) -> 
		{	
			return sourceStack.hasPermission(2);
		}).then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("goalName", StringArgumentType.string()).suggests(GOALS).then(Commands.argument("animationName", StringArgumentType.string()).suggests(ANIMATIONS).executes((commandCtx) ->
		{
			return triggerGoal(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "goalName"), StringArgumentType.getString(commandCtx, "animationName"));
		})))).then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("goalName", StringArgumentType.string()).suggests(GOALS).executes((commandCtx) ->
		{
			return triggerGoal(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "goalName"), StringArgumentType.getString(commandCtx, "goalName"));
		}))));
	}
	
	/*.then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("goalName", StringArgumentType.string()).suggests(GOALS).then(Commands.argument("goalIndex", IntegerArgumentType.integer(0))).executes((commandCtx) ->
	{
		return triggerGoalWithIndex(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "goalName"), IntegerArgumentType.getInteger(commandCtx, "goalIndex"));
	}))*/
	
	@SuppressWarnings("unused")
	private static int triggerGoalWithIndex(CommandSourceStack sourceStack, Collection<ServerPlayer> players, String goalName, int goalIndex)
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
						if(goal.getGoal().getClass().getSimpleName().equals(goalName))
						{
							((IWrappedGoal) goal).setCanUse();
							((IWrappedGoal) goal).setFakeTarget(t.getFakeTarget());
							mob.setTarget(t.getFakeTarget());
							goal.start();
							sourceStack.sendSuccess(() -> Component.literal("Triggered goal " + goal.getGoal().getClass().getSimpleName() + " in Index " + goalIndex), true);
						}
						else
						{
							sourceStack.sendFailure(Component.literal("Index at" + goalIndex + " is not a " + goalName + "!"));
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
	
	private static int triggerGoal(CommandSourceStack sourceStack, Collection<ServerPlayer> players, String goalName, String animationName)
	{
		for(ServerPlayer player : players)
		{
			player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
			{
				if(t.getMorph() != null)
				{
					Mob mob = (Mob) t.getMorph();
					for(WrappedGoal goal : mob.goalSelector.getAvailableGoals())
					{
						String name = goal.getGoal().getClass().getSimpleName();
						if(goal.getGoal().getClass().isAnonymousClass())
						{
							name = goal.getGoal().getClass().getSuperclass().getSimpleName();
						}
						if(name.equals(goalName))
						{
							((IWrappedGoal) goal).setCanUse();
							((IWrappedGoal) goal).setFakeTarget(t.getFakeTarget());
							mob.setTarget(t.getFakeTarget());
							MorphUtil.setAnimation(mob, animationName);
							goal.start();
							sourceStack.sendSuccess(() -> Component.literal("Triggered goal " + goalName), true);
							break;
						}
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
