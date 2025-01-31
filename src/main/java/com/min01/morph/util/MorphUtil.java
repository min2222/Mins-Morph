package com.min01.morph.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.min01.morph.misc.IClientLevel;
import com.min01.morph.misc.ILevelEntityGetterAdapter;
import com.min01.morph.misc.IWrappedGoal;

import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class MorphUtil 
{
	public static final Map<Integer, Entity> ENTITY_MAP = new HashMap<>();
	public static final Map<Integer, Entity> ENTITY_MAP2 = new HashMap<>();
	
	public static final List<Attribute> ATTRIBUTES = Lists.newArrayList(Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS, Attributes.ATTACK_DAMAGE, Attributes.ATTACK_KNOCKBACK, Attributes.FLYING_SPEED, Attributes.FOLLOW_RANGE, Attributes.JUMP_STRENGTH, Attributes.KNOCKBACK_RESISTANCE, Attributes.MAX_HEALTH);
    
    public static void setAnimation(Mob mob, String animationName)
    {
		try
		{
			Field f = mob.getClass().getField(animationName);
			Method m = mob.getClass().getMethod("setAnimation", f.getType());
			f.setAccessible(true);
			m.invoke(mob, f.get(mob));
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e)
		{
			
		}
    }
    
    @SuppressWarnings("unchecked")
	public static <T> void setData(Mob mob, String dataName, int dataValue)
    {
		try
		{
			Field f = mob.getClass().getField(dataName);
			f.setAccessible(true);
			EntityDataAccessor<Integer> accessor = (EntityDataAccessor<Integer>) f.get(mob);
	    	mob.getEntityData().set(accessor, dataValue);
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException e)
		{
			
		}
    }
    
	public static List<String> getGoals(Mob mob)
	{
		List<String> list = new ArrayList<>();
		Set<WrappedGoal> set = mob.goalSelector.getAvailableGoals();
		List<WrappedGoal> goals = Lists.newArrayList(set);
		for(WrappedGoal goal : set)
		{
			((IWrappedGoal)goal).setEntity(mob);
			String goalName = goal.getGoal().getClass().getSimpleName();
			if(goal.getGoal().getClass().isAnonymousClass())
			{
    			goalName = goal.getGoal().getClass().getSuperclass().getSimpleName();
			}
			if(list.contains(goalName))
			{
				goalName = goalName + goals.indexOf(goal);
			}
			list.add(goalName);
		}
		return list;
	}
	
	public static List<String> getAnimations(Mob mob)
	{
		List<String> list = new ArrayList<>();
		for(Field f : mob.getClass().getDeclaredFields())
		{
			if(f.getType().getSimpleName().contains("Animation") && !f.getType().isArray())
			{
				list.add(f.getName());
			}
		}
		return list;
	}
	
	public static List<String> getDatas(Mob mob)
	{
		List<String> list = new ArrayList<>();
		for(Field f : mob.getClass().getDeclaredFields())
		{
			if(f.getType().getSimpleName().equals("EntityDataAccessor") && !f.getType().isArray())
			{
				list.add(f.getName());
			}
		}
		return list;
	}
    
    public static void tick(LivingEntity player, LivingEntity morph)
    {
    	morph.getPersistentData().putUUID("MorphOwnerUUID", player.getUUID());
    	player.eyeHeight = morph.getEyeHeight();
		if(!player.isAlive())
		{
			morph.discard();
		}
		else
		{
	    	sync(morph, player);
			for(Entity entity : morph.getPassengers())
			{
				tickPassenger(morph, entity);
			}
			Method m = ObfuscationReflectionHelper.findMethod(Level.class, "m_142646_");
			try
			{
				ILevelEntityGetterAdapter adapter = (ILevelEntityGetterAdapter) m.invoke(player.level);
				if(!adapter.byUuid().containsKey(morph.getUUID()))
				{
					adapter.byUuid().put(morph.getUUID(), morph);
				}
				adapter.byUuid().values().removeIf(t -> !t.isAlive());
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
	    	if(morph instanceof Mob mob)
	    	{
	    		if(!player.level.isClientSide)
	    		{
	        		serverAiStep(mob);
	    		}
	    		else
	    		{
    				IClientLevel clientLevel = (IClientLevel) player.level;
    				if(!clientLevel.byId().containsKey(morph.getId()))
    				{
    					clientLevel.byId().put(morph.getId(), morph);
    				}
    				clientLevel.byId().values().removeIf(t -> !t.isAlive());
	    		}
				if(mob.getTarget() != null)
				{
					if(!mob.getTarget().isAlive())
					{
						mob.setTarget(null);
					}
				}
	    	}
		}
    }
    
    public static void tickPassenger(Entity vehicle, Entity passenger)
    {
    	if(passenger.getVehicle() == vehicle)
    	{
        	passenger.rideTick();
            for(Entity entity : passenger.getPassengers())
            {
            	tickPassenger(passenger, entity);
            }
    	}
    }
    
	//from ichun's morph mod;
    public static void sync(LivingEntity morph, LivingEntity player)
    {
        morph.tickCount = player.tickCount;

        morph.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        
        morph.xOld = player.xOld;
        morph.yOld = player.yOld;
        morph.zOld = player.zOld;

        morph.xo = player.xo;
        morph.yo = player.yo;
        morph.zo = player.zo;

        morph.yRotO = player.yRotO;
        morph.xRotO = player.xRotO;

        morph.yHeadRot = player.yHeadRot;
        morph.yHeadRotO = player.yHeadRotO;

        morph.yBodyRot = player.yBodyRot;
        morph.yBodyRotO = player.yBodyRotO;
        
        morph.walkAnimation = player.walkAnimation;

        morph.setDeltaMovement(player.getDeltaMovement());

        morph.horizontalCollision = player.horizontalCollision;
        morph.verticalCollision = player.verticalCollision;
        
        morph.setOnGround(player.onGround());
        morph.setSwimming(player.isSwimming());
        morph.setSprinting(player.isSprinting());

        morph.hurtTime = player.hurtTime;
        morph.deathTime = player.deathTime;
        morph.fallDistance = player.fallDistance;

        morph.swingTime = player.swingTime;
        morph.swinging = player.swinging;
        morph.swingingArm = player.swingingArm;
        morph.attackAnim = player.attackAnim;
        morph.oAttackAnim = player.oAttackAnim;
    }
	
    public static void serverAiStep(Mob mob)
    {
        mob.setNoActionTime(mob.getNoActionTime() + 1);
        mob.level.getProfiler().push("sensing");
        mob.getSensing().tick();
        mob.level.getProfiler().pop();
        int i = mob.level.getServer().getTickCount() + mob.getId();
        if(i % 2 != 0 && mob.tickCount > 1)
        {
        	mob.level.getProfiler().push("goalSelector");
        	mob.goalSelector.tickRunningGoals(false);
        	mob.level.getProfiler().pop();
        }
        else 
        {
        	mob.level.getProfiler().push("goalSelector");
        	mob.goalSelector.tick();
        	mob.level.getProfiler().pop();
        }

        mob.level.getProfiler().push("navigation");
        mob.getNavigation().tick();
        mob.level.getProfiler().pop();
        mob.level.getProfiler().push("mob tick");
        customServerAiStep(mob);
        mob.level.getProfiler().pop();
        mob.level.getProfiler().push("controls");
        mob.level.getProfiler().push("move");
        mob.getMoveControl().tick();
        mob.level.getProfiler().popPush("look");
        mob.getLookControl().tick();
        mob.level.getProfiler().popPush("jump");
        mob.getJumpControl().tick();
        mob.level.getProfiler().pop();
        mob.level.getProfiler().pop();
        DebugPackets.sendGoalSelector(mob.level, mob, mob.goalSelector);
    }
    
    public static void customServerAiStep(Mob mob)
    {
		Method m = ObfuscationReflectionHelper.findMethod(Mob.class, "m_8024_");
		try 
		{
			m.invoke(mob);
		}
		catch (Exception e) 
		{
			
		}
    }
    
    public static Entity getMorphOwner(Entity entity)
    {
    	if(entity.getPersistentData().contains("MorphOwnerUUID"))
    	{
    		return getEntityByUUID(entity.level, entity.getPersistentData().getUUID("MorphOwnerUUID"));
    	}
    	return null;
    }
    
	@SuppressWarnings("unchecked")
	public static Entity getEntityByUUID(Level level, UUID uuid)
	{
		Method m = ObfuscationReflectionHelper.findMethod(Level.class, "m_142646_");
		try 
		{
			LevelEntityGetter<Entity> entities = (LevelEntityGetter<Entity>) m.invoke(level);
			return entities.get(uuid);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
}
