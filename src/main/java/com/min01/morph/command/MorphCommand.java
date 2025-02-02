package com.min01.morph.command;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.min01.morph.capabilities.IMorphCapability;
import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.capabilities.MorphImpl;
import com.min01.morph.entity.MorphEntities;
import com.min01.morph.misc.IWrappedGoal;
import com.min01.morph.util.MorphUtil;
import com.min01.morph.util.world.MorphSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

public class MorphCommand 
{
	public static final SuggestionProvider<CommandSourceStack> DATAS = SuggestionProviders.register(new ResourceLocation("datas"), (p_258164_, p_258165_) -> 
	{
		for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
		{
			MorphSavedData data = MorphSavedData.get(player.level);
        	if(data != null)
        	{
        		IMorphCapability cap = player.getCapability(MorphCapabilities.MORPH).orElse(new MorphImpl());
        		if(cap.getMorph() != null)
        		{
            		return SharedSuggestionProvider.suggest(data.getDatas(cap.getMorph().getClass().getSimpleName()).stream(), p_258165_);
        		}
        	}
		}
		return SharedSuggestionProvider.suggest(Stream.empty(), p_258165_);
	});
	
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
	
	public static final SuggestionProvider<CommandSourceStack> ENTITIES = SuggestionProviders.register(new ResourceLocation("entities"), (p_258164_, p_258165_) -> 
	{
		return SharedSuggestionProvider.suggestResource(ForgeRegistries.ENTITY_TYPES.getValues().stream().filter((p_247987_) -> 
		{
			return p_247987_.isEnabled(p_258164_.getSource().enabledFeatures()) && p_247987_ != MorphEntities.FAKE_TARGET.get();
		}), p_258165_, EntityType::getKey, (p_212436_) -> 
		{
			return Component.translatable(Util.makeDescriptionId("entity", EntityType.getKey(p_212436_)));
		});
	});
	   
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx)
	{
		dispatcher.register(Commands.literal("morph").requires((sourceStack) -> 
		{
			return sourceStack.hasPermission(2);
		}).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("set").then(Commands.argument("morphTarget", ResourceArgument.resource(ctx, Registries.ENTITY_TYPE)).suggests(ENTITIES).then(Commands.argument("persistent", BoolArgumentType.bool()).executes((commandCtx) -> 
		{
			return morph(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), ResourceArgument.getEntityType(commandCtx, "morphTarget"), BoolArgumentType.getBool(commandCtx, "persistent"));
		}))))).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("set").then(Commands.argument("morphTarget", ResourceArgument.resource(ctx, Registries.ENTITY_TYPE)).suggests(ENTITIES).executes((commandCtx) -> 
		{
			return morph(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), ResourceArgument.getEntityType(commandCtx, "morphTarget"), false);
		})))).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("trigger").then(Commands.argument("goalName", StringArgumentType.string()).suggests(GOALS).executes(commandCtx -> 
		{
			return triggerGoal(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "goalName"));
		})))).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("trigger").then(Commands.argument("goalName", StringArgumentType.string()).suggests(GOALS).then(Commands.argument("animationName", StringArgumentType.string()).suggests(ANIMATIONS).executes(commandCtx -> 
		{
			return triggerGoalWithAnimation(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "goalName"), StringArgumentType.getString(commandCtx, "animationName"));
		}))))).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("data").then(Commands.argument("dataName", StringArgumentType.string()).suggests(DATAS).then(Commands.argument("dataValue", StringArgumentType.string()).executes(commandCtx -> 
		{
			return setData(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "dataName"), StringArgumentType.getString(commandCtx, "dataValue"));
		}))))));
	}
	
	private static int triggerGoal(CommandSourceStack sourceStack, Collection<ServerPlayer> players, String goalName)
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
						String name = MorphUtil.getGoalName(goal.getGoal());
						if(index == Character.getNumericValue(lastChar) || name.equals(goalName))
						{
							((IWrappedGoal) goal).setCanUse();
							MorphUtil.setTarget(player, mob, goal, t.getFakeTarget());
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
	
	private static int triggerGoalWithAnimation(CommandSourceStack sourceStack, Collection<ServerPlayer> players, String goalName, String animationName)
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
						String name = MorphUtil.getGoalName(goal.getGoal());
						if(index == Character.getNumericValue(lastChar) || name.equals(goalName))
						{
							((IWrappedGoal) goal).setCanUse();
							MorphUtil.setTarget(player, mob, goal, t.getFakeTarget());
							MorphUtil.setAnimation(mob, animationName);
							goal.start();
							sourceStack.sendSuccess(() -> Component.literal("Triggered goal " + goalName + " with animation " + animationName), true);
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
	
	private static int setData(CommandSourceStack sourceStack, Collection<ServerPlayer> players, String dataName, String dataValue)
	{
		for(ServerPlayer player : players)
		{
			player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
			{
				if(t.getMorph() != null)
				{
					Mob mob = (Mob) t.getMorph();
					MorphUtil.setData(mob, dataName, dataValue);
					sourceStack.sendSuccess(() -> Component.literal("Changed entity data " + dataName + " to " + dataValue), true);
				}
			});
		}
		return players.size();
	}
	
	private static int morph(CommandSourceStack sourceStack, Collection<ServerPlayer> players, Reference<EntityType<?>> entityType, boolean isPersistent)
	{
		for(ServerPlayer player : players)
		{
			player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
			{
				Entity entity = entityType.get().create(player.level);
				if(entity instanceof LivingEntity living)
				{
					t.setPersistent(isPersistent);
					t.setMorph(living);
					sourceStack.sendSuccess(() -> Component.literal("Changed morph entity of " + player.getDisplayName().getString() + " to " + living.getDisplayName().getString()), true);
				}
				else
				{
					sourceStack.sendFailure(Component.literal("Can't morph to none living entity"));
				}
			});
		}
		return players.size();
	}
}
