package com.min01.morph.command;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
        		IMorphCapability cap = player.getCapability(MorphCapabilities.MORPH).orElse(new MorphImpl());
        		if(cap.getMorph() != null)
        		{
            		return SharedSuggestionProvider.suggest(data.getGoals(cap.getMorph().getClass().getSimpleName()).stream(), p_258165_);
        		}
        	}
		}
		return SharedSuggestionProvider.suggest(Stream.empty(), p_258165_);
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
            		return SharedSuggestionProvider.suggest(data.getAnimations(cap.getMorph().getClass().getSimpleName()).stream(), p_258165_);
        		}
        	}
		}
		return SharedSuggestionProvider.suggest(Stream.empty(), p_258165_);
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
	
	private static int triggerGoal(CommandSourceStack sourceStack, Collection<ServerPlayer> players, String goalName, String animationName)
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
					for(WrappedGoal goal : set)
					{
						int index = list.indexOf(goal);
						char lastChar = goalName.charAt(goalName.length() - 1);
	        			String name = goal.getGoal().getClass().getSimpleName();
	        			if(goal.getGoal().getClass().isAnonymousClass())
	        			{
	        				name = goal.getGoal().getClass().getSuperclass().getSimpleName();
	        			}
						if(index == Character.getNumericValue(lastChar) || name.equals(goalName))
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
