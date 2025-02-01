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
import com.mojang.brigadier.arguments.DoubleArgumentType;
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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

public class MorphCommand 
{
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
            		return SharedSuggestionProvider.suggest(data.getTags(cap.getMorph().getClass().getSimpleName()).stream(), p_258165_);
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
			return morphTo(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), ResourceArgument.getEntityType(commandCtx, "morphTarget"), BoolArgumentType.getBool(commandCtx, "persistent"));
		}))))).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("set").then(Commands.argument("morphTarget", ResourceArgument.resource(ctx, Registries.ENTITY_TYPE)).suggests(ENTITIES).executes((commandCtx) -> 
		{
			return morphTo(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), ResourceArgument.getEntityType(commandCtx, "morphTarget"), false);
		})))).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("trigger").then(Commands.argument("goalName", StringArgumentType.string()).suggests(GOALS).executes(commandCtx -> 
		{
			return triggerGoal(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "goalName"));
		})))).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("trigger").then(Commands.argument("goalName", StringArgumentType.string()).suggests(GOALS).then(Commands.argument("animationName", StringArgumentType.string()).suggests(ANIMATIONS).executes(commandCtx -> 
		{
			return triggerGoalWithAnimation(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "goalName"), StringArgumentType.getString(commandCtx, "animationName"));
		})))).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("trigger").then(Commands.argument("tagName", StringArgumentType.string()).suggests(TAGS).then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes(commandCtx -> 
		{
			return addTag(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), StringArgumentType.getString(commandCtx, "tagName"), DoubleArgumentType.getDouble(commandCtx, "value"));
		})))))));
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
							((IWrappedGoal) goal).setFakeTarget(t.getFakeTarget());
							HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(player, entity -> !entity.isAlliedTo(player), 30.0F);
							if(hitResult instanceof EntityHitResult entityHit)
							{
								if(entityHit.getEntity() instanceof LivingEntity living)
								{
									((IWrappedGoal) goal).setTarget(living);
									mob.setTarget(living);
								}
							}
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
							((IWrappedGoal) goal).setFakeTarget(t.getFakeTarget());
							HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(player, entity -> !entity.isAlliedTo(player), 30.0F);
							if(hitResult instanceof EntityHitResult entityHit)
							{
								if(entityHit.getEntity() instanceof LivingEntity living)
								{
									((IWrappedGoal) goal).setTarget(living);
									mob.setTarget(living);
								}
							}
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
	
	private static int addTag(CommandSourceStack sourceStack, Collection<ServerPlayer> players, String tagName, double value)
	{
		for(ServerPlayer player : players)
		{
			player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
			{
				if(t.getMorph() != null)
				{
					Mob mob = (Mob) t.getMorph();
					mob.getPersistentData().putDouble(tagName, value);
					sourceStack.sendSuccess(() -> Component.literal("Added tag " + tagName + " with value " + value), true);
				}
			});
		}
		return players.size();
	}
	
	private static int morphTo(CommandSourceStack sourceStack, Collection<ServerPlayer> players, Reference<EntityType<?>> entityType, boolean isPersistent)
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
