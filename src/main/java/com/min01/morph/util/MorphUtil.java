package com.min01.morph.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.util.TriConsumer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Lists;
import com.min01.morph.capabilities.IMorphCapability;
import com.min01.morph.capabilities.MorphCapabilities;
import com.min01.morph.capabilities.MorphImpl;
import com.min01.morph.misc.ILevelEntityGetterAdapter;
import com.min01.morph.misc.IWrappedGoal;

import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class MorphUtil 
{
	public static final Map<Integer, Entity> ENTITY_MAP = new HashMap<>();
	public static final Map<Integer, Entity> ENTITY_MAP2 = new HashMap<>();
	
	public static final List<Attribute> ATTRIBUTES = Lists.newArrayList(Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS, Attributes.ATTACK_DAMAGE, Attributes.ATTACK_KNOCKBACK, Attributes.FLYING_SPEED, Attributes.FOLLOW_RANGE, Attributes.JUMP_STRENGTH, Attributes.KNOCKBACK_RESISTANCE, Attributes.MAX_HEALTH);
	
	public static void resetTarget(Mob mob)
	{
		try
		{
			Method m = mob.getClass().getMethod("getSyncedAnimation");
			String animation = (String) m.invoke(mob);
			if(animation.equals("empty") && mob.getTarget() != null)
			{
				mob.setTarget(null);
			}
		}
		catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
		{
			
		}
	}
	
	public static void setTarget(LivingEntity owner, Mob morph, LivingEntity fakeTarget)
	{
		morph.setTarget(fakeTarget);
		HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(owner, entity -> !entity.isAlliedTo(owner), 30.0F);
		if(hitResult instanceof EntityHitResult entityHit)
		{
			if(entityHit.getEntity() instanceof LivingEntity living)
			{
				morph.setTarget(living);
			}
		}
	}
	
	public static void setTarget(LivingEntity owner, Mob morph, WrappedGoal goal, LivingEntity fakeTarget)
	{
		((IWrappedGoal) goal).setFakeTarget(fakeTarget);
		morph.setTarget(fakeTarget);
		HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(owner, entity -> !entity.isAlliedTo(owner), 30.0F);
		if(hitResult instanceof EntityHitResult entityHit)
		{
			if(entityHit.getEntity() instanceof LivingEntity living)
			{
				((IWrappedGoal) goal).setTarget(living);
				morph.setTarget(living);
			}
		}
	}
	
	public static void getMorph(Entity entity, Consumer<LivingEntity> consumer)
	{
		entity.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
    		LivingEntity morph = t.getMorph();
    		if(morph != null)
    		{
    			consumer.accept(morph);
    		}
		});
	}
	
	public static boolean isMorph(Entity entity)
	{
		return entity.getId() < 0;
	}
	
	public static boolean hasMorph(LivingEntity living)
	{
		IMorphCapability cap = living.getCapability(MorphCapabilities.MORPH).orElse(new MorphImpl());
		return cap.getMorph() != null;	
	}
	
	public static void removeMorph(LivingEntity living)
	{
		living.getCapability(MorphCapabilities.MORPH).ifPresent(t -> 
		{
			t.setMorph(null);
			t.setType(null);
			t.setPersistent(false);
		});
	}	
	
	public static void getMethodCalls(Class<?> clazz, Consumer<MethodInsnNode> consumer)
	{
        try
        {
    		ClassNode classNode = getClassNode(clazz);
    		for(MethodNode method : classNode.methods)
    		{
    		    for(AbstractInsnNode ain : method.instructions.toArray())
    		    {
    		        if(ain.getType() == AbstractInsnNode.METHOD_INSN) 
    		        {
    		        	MethodInsnNode min = (MethodInsnNode) ain;
    		            consumer.accept(min);
    		        }
    		    }
    		}
        }
        catch (IOException | SecurityException e) 
        {
        	
		}
	}
	
	public static void getFieldCalls(Class<?> clazz, Consumer<FieldInsnNode> consumer)
	{
        try
        {
    		ClassNode classNode = getClassNode(clazz);
    		for(MethodNode method : classNode.methods)
    		{
    		    for(AbstractInsnNode ain : method.instructions.toArray())
    		    {
    		        if(ain.getType() == AbstractInsnNode.FIELD_INSN) 
    		        {
    		        	FieldInsnNode fin = (FieldInsnNode) ain;
    		            consumer.accept(fin);
    		        }
    		    }
    		}
        }
        catch (IOException | SecurityException e) 
        {
        	
		}
	}

    //ChatGPT ahh;
	public static void getLoadConstants(Class<?> clazz, TriConsumer<String, String, MethodInsnNode> consumer) 
	{
	    try 
	    {
	        ClassNode classNode = getClassNode(clazz);
	        for(MethodNode method : classNode.methods) 
	        {
	            AbstractInsnNode[] instructions = method.instructions.toArray();
	            for(int i = 0; i < instructions.length - 2; i++) 
	            {
	                if(instructions[i] instanceof MethodInsnNode getPersistentDataNode) 
	                {
	                    if(instructions[i + 1] instanceof LdcInsnNode keyNode) 
	                    {
	                        if(keyNode.cst instanceof String key) 
	                        {
	                            if(!key.isBlank()) 
	                            {
	    	                    	if(instructions[i + 2] instanceof LdcInsnNode valueNode)
	    	                    	{
	                                    consumer.accept(key, valueNode.cst.toString(), getPersistentDataNode);
	    	                    	}
	    	                    	else if(instructions[i + 2] instanceof InsnNode)
	    	                    	{
	    	                    		consumer.accept(key, Double.toString(0.0d), getPersistentDataNode);
	    	                    	}
	                            }
	                        }
	                    }
	                }
	            }
	        }
	    } 
	    catch (IOException | SecurityException e) 
	    {
	    	
	    }
	}

    //ChatGPT ahh;
    public static ClassNode getClassNode(Class<?> clazz) throws IOException
    {
        String className = clazz.getName().replace('.', '/') + ".class";
        ClassLoader classLoader = clazz.getClassLoader();
        if(classLoader == null)
        {
        	classLoader = ClassLoader.getSystemClassLoader();
        }
        try(InputStream classStream = classLoader.getResourceAsStream(className))
        {
            if(classStream == null)
            {
                throw new IOException("Class not found: " + clazz.getName());
            }
            ClassReader classReader = new ClassReader(classStream);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            return classNode;
        }
    }
    
	public static String getGoalName(Goal goal)
	{
		String name = goal.getClass().getSimpleName();
		if(goal.getClass().isAnonymousClass())
		{
			name = goal.getClass().getSuperclass().getSimpleName();
		}
		return name;
	}
	
	@SuppressWarnings("unchecked")
	public static void setData(Mob mob, String dataName, String dataValue)
	{
		try
		{
			Field f = mob.getClass().getField(dataName);
			f.setAccessible(true);
            if(dataValue.equalsIgnoreCase("true") || dataValue.equalsIgnoreCase("false")) 
            {
            	EntityDataAccessor<Boolean> accessor = (EntityDataAccessor<Boolean>) f.get(mob);
            	mob.getEntityData().set(accessor, Boolean.parseBoolean(dataValue));
            }
            else
            {
                try 
                {
                    int value = Integer.parseInt(dataValue);
                	EntityDataAccessor<Integer> accessor = (EntityDataAccessor<Integer>) f.get(mob);
                	mob.getEntityData().set(accessor, value);
                }
                catch (NumberFormatException e)
                {
                	EntityDataAccessor<String> accessor = (EntityDataAccessor<String>) f.get(mob);
                	mob.getEntityData().set(accessor, dataValue);
                }
            }
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException e)
		{
			
		}
	}
	
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
	
	public static List<String> getDatas(Class<?> clazz)
	{
		List<String> list = new ArrayList<>();
		for(Field f : clazz.getDeclaredFields())
		{
			if(f.getType().getSimpleName().equals("EntityDataAccessor"))
			{
				list.add(f.getName());
			}
		}
		for(Field f : clazz.getSuperclass().getDeclaredFields())
		{
			if(f.getType().getSimpleName().equals("EntityDataAccessor"))
			{
				list.add(f.getName());
			}
		}
		return list;
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
	
	public static List<String> getAnimations(Class<?> clazz)
	{
		List<String> list = new ArrayList<>();
		for(Field f : clazz.getDeclaredFields())
		{
			if(f.getType().getSimpleName().contains("Animation") && !f.getType().isArray())
			{
				list.add(f.getName());
			}
		}
		for(Field f : clazz.getSuperclass().getDeclaredFields())
		{
			if(f.getType().getSimpleName().contains("Animation") && !f.getType().isArray())
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
				adapter.byId().putIfAbsent(morph.getId(), morph);
				adapter.byId().values().removeIf(t -> !t.isAlive());
				adapter.byUuid().putIfAbsent(morph.getUUID(), morph);
				adapter.byUuid().values().removeIf(t -> !t.isAlive());
			}
			catch (Exception e) 
			{
				
			}
	    	if(morph instanceof Mob mob)
	    	{
	    		if(!player.level.isClientSide)
	    		{
	        		serverAiStep(mob);
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
