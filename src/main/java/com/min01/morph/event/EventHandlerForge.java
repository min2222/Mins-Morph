package com.min01.morph.event;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.min01.morph.MinsMorph;
import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.command.GoalCommand;
import com.min01.morph.command.MorphCommand;
import com.min01.morph.command.TargetCommand;
import com.min01.morph.util.MorphUtil;
import com.min01.morph.util.world.MorphSavedData;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent.ImpactResult;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MinsMorph.MODID)
public class EventHandlerForge
{
	public static final Map<Integer, Entity> ENTITY_MAP = new HashMap<>();
	public static final Map<Integer, Entity> ENTITY_MAP2 = new HashMap<>();
	
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
    	MorphCommand.register(event.getDispatcher(), event.getBuildContext());
    	GoalCommand.register(event.getDispatcher());
    	TargetCommand.register(event.getDispatcher());
    }
    
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event)
    {
    	if(event.getRayTraceResult() instanceof EntityHitResult entityHit)
    	{
    		if(entityHit.getEntity().getId() < 0)
    		{
            	event.setImpactResult(ImpactResult.SKIP_ENTITY);
    		}
    	}
    }
    
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event)
    {
    	if(event.getEntity() instanceof Mob mob)
    	{
    		if(!mob.level.isClientSide)
    		{
        		Set<WrappedGoal> set = mob.goalSelector.getAvailableGoals();
        		for(WrappedGoal goal : set)
        		{
        	    	List<String> names = MorphUtil.getUsedAnimations(goal.getGoal());
        	    	for(String name : names)
        	    	{
        				MorphSavedData data = MorphSavedData.get(mob.level);
        	        	if(data != null)
        	        	{
        	        		data.saveAnimation(goal.getGoal().getClass().getSimpleName(), name);
        	        	}
        	    	}
        		}
    			for(Field f : mob.getClass().getDeclaredFields())
    			{
    				if(f.getType().getSimpleName().contains("Animation") && !f.getType().isArray())
    				{
        				MorphSavedData data = MorphSavedData.get(mob.level);
        	        	if(data != null)
        	        	{
        	        		if(!data.hasAnimation(f.getName()))
        	        		{
        	        			data.saveAnimation(f.getName(), f.getName());
        	        		}
        	        	}
    				}
    			}
    		}
    	}
    }
    
    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event)
    {
    	Explosion explosion = event.getExplosion();
    	if(explosion.getIndirectSourceEntity() != null)
    	{
    		LivingEntity source = explosion.getIndirectSourceEntity();
    		if(MorphUtil.getMorphOwner(source) != null)
    		{
    			Entity owner = MorphUtil.getMorphOwner(source);
    			List<Entity> list = event.getAffectedEntities();
    			if(list.contains(owner) && !list.isEmpty())
    			{
    				list.removeIf(t -> t == owner);
    			}
    		}
    	}
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerRespawnEvent event)
    {
    	for(ServerPlayer serverPlayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
    	{
    		serverPlayer.getCapability(MorphCapabilities.MORPH).ifPresent(t ->
        	{
        		if(t.getMorph() != null)
        		{
        			t.setMorph(t.getMorph());
        		}
        	});
    	}
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event)
    {
    	for(ServerPlayer serverPlayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
    	{
    		serverPlayer.getCapability(MorphCapabilities.MORPH).ifPresent(t ->
        	{
        		if(t.getMorph() != null)
        		{
        			t.setMorph(t.getMorph());
        		}
        	});
    	}
    }
    
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event)
    {
    	if(event.isWasDeath())
    	{
        	Player original = event.getOriginal();
        	original.revive();
        	Player player = event.getEntity();
        	original.getCapability(MorphCapabilities.MORPH).ifPresent(t ->
        	{
        		if(t.getMorph() != null && t.isPersistent())
        		{
        			player.getCapability(MorphCapabilities.MORPH).ifPresent(t2 ->
                	{
                		t2.setType(t.getMorph().getType());
                		t2.setPersistent(true);
                	});
        		}
        	});
    	}
    }
}
