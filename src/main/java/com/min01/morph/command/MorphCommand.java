package com.min01.morph.command;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
	public static final SuggestionProvider<CommandSourceStack> TAG_VALUES = SuggestionProviders.register(new ResourceLocation("tag_values"), (p_258164_, p_258165_) -> 
	{
		for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
		{
			MorphSavedData data = MorphSavedData.get(player.level);
        	if(data != null)
        	{
        		IMorphCapability cap = player.getCapability(MorphCapabilities.MORPH).orElse(new MorphImpl());
        		if(cap.getMorph() != null)
        		{
        			List<Map<String, String>> list = data.getTags(cap.getMorph().getClass().getSimpleName());
            		return SharedSuggestionProvider.suggest(list.stream().flatMap(map -> map.values().stream()), p_258165_);
        		}
        	}
		}
		return SharedSuggestionProvider.suggest(Stream.empty(), p_258165_);
	});
	
	public static final SuggestionProvider<CommandSourceStack> TAGS = SuggestionProviders.register(new ResourceLocation("tags"), (p_258164_, p_258165_) -> 
	{
		for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
		{
			MorphSavedData data = MorphSavedData.get(player.level);
        	if(data != null)
        	{
        		IMorphCapability cap = player.getCapability(MorphCapabilities.MORPH).orElse(new MorphImpl());
        		if(cap.getMorph() != null)
        		{
        			List<Map<String, String>> list = data.getTags(cap.getMorph().getClass().getSimpleName());
            		return SharedSuggestionProvider.suggest(list.stream().flatMap(map -> map.keySet().stream()), p_258165_);
        		}
        	}
		}
		return SharedSuggestionProvider.suggest(Stream.empty(), p_258165_);
	});
	
	public static final SuggestionProvider<CommandSourceStack> PROCEDURES = SuggestionProviders.register(new ResourceLocation("procedure"), (p_258164_, p_258165_) -> 
	{
		for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
		{
			MorphSavedData data = MorphSavedData.get(player.level);
        	if(data != null)
        	{
        		IMorphCapability cap = player.getCapability(MorphCapabilities.MORPH).orElse(new MorphImpl());
        		if(cap.getMorph() != null)
        		{
            		return SharedSuggestionProvider.suggest(data.getProcedures(cap.getMorph().getClass().getSimpleName()).stream(), p_258165_);
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
		}))))).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("procedure").then(Commands.argument("procedureName", StringArgumentType.string()).suggests(PROCEDURES).executes(commandCtx -> 
		{
			return triggerProcedure(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "procedureName"), "");
		})))).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("procedure").then(Commands.argument("procedureName", StringArgumentType.string()).suggests(PROCEDURES).then(Commands.argument("animationName", StringArgumentType.string()).executes(commandCtx -> 
		{
			return triggerProcedure(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "procedureName"), StringArgumentType.getString(commandCtx, "animationName"));
		}))))).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("tag").then(Commands.argument("tagName", StringArgumentType.string()).suggests(TAGS).then(Commands.argument("tagValue", StringArgumentType.string()).suggests(TAG_VALUES).executes(commandCtx -> 
		{
			return setTag(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "tagName"), StringArgumentType.getString(commandCtx, "tagValue"));
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
	
	private static int triggerProcedure(CommandSourceStack sourceStack, Collection<ServerPlayer> players, String procedureName, String animationName)
	{
		for(ServerPlayer player : players)
		{
			player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
			{
				if(t.getMorph() != null)
				{
					Mob mob = (Mob) t.getMorph();
					MorphUtil.setTarget(player, mob, t.getFakeTarget());
					MorphUtil.triggerProcedure(mob, procedureName);
					MorphUtil.setAnimation(mob, animationName);
					sourceStack.sendSuccess(() -> Component.literal("Triggered procedure " + procedureName), true);
				}
				else
				{
					sourceStack.sendFailure(Component.literal("Player doesn't morphed to anything"));
				}
			});
		}
		return players.size();
	}
	
	private static int setTag(CommandSourceStack sourceStack, Collection<ServerPlayer> players, String tagName, String tagValue)
	{
		for(ServerPlayer player : players)
		{
			player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
			{
				if(t.getMorph() != null)
				{
					Mob mob = (Mob) t.getMorph();
					if(tagValue.equals("true") || tagValue.equals("false"))
					{
						mob.getPersistentData().putBoolean(tagName, Boolean.getBoolean(tagName));
					}
					else if(tagValue.contains("."))
					{
						mob.getPersistentData().putDouble(tagName, Double.parseDouble(tagName));
					}
					else
					{
						mob.getPersistentData().putString(tagName, tagValue);
					}
					sourceStack.sendSuccess(() -> Component.literal("Set tag " + tagName + " to " + tagValue), true);
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
