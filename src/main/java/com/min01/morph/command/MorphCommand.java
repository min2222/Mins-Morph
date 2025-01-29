package com.min01.morph.command;

import java.util.Collection;

import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.entity.MorphEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphCommand 
{
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
		}).then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("morphTarget", ResourceArgument.resource(ctx, Registries.ENTITY_TYPE)).suggests(ENTITIES).then(Commands.argument("persistent", BoolArgumentType.bool()).executes((commandCtx) ->
		{
			return morphTo(commandCtx.getSource(), EntityArgument.getPlayers(commandCtx, "players"), ResourceArgument.getEntityType(commandCtx, "morphTarget"), BoolArgumentType.getBool(commandCtx, "persistent"));
		})))));
	}
	
	private static int morphTo(CommandSourceStack sourceStack, Collection<ServerPlayer> players, Reference<EntityType<?>> entityType, boolean isPersistent)
	{
		for(ServerPlayer player : players)
		{
			player.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
			{
				Entity entity = entityType.get().create(player.level);
				if(t.getMorph() != null)
				{
					t.getMorph().discard();
				}
				if(entity instanceof LivingEntity living)
				{
					t.setPersistent(isPersistent);
					t.setMorph(living);
					ForgeEventFactory.onFinalizeSpawn((Mob) living, sourceStack.getLevel(), sourceStack.getLevel().getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.COMMAND, null, null);
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
