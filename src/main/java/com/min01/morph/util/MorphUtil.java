package com.min01.morph.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.min01.morph.misc.IClientLevel;
import com.min01.morph.misc.ILevelEntityGetterAdapter;
import com.min01.morph.util.world.MorphSavedData;

import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class MorphUtil 
{
	public static final Map<Integer, Entity> ENTITY_MAP = new HashMap<>();
	public static final Map<Integer, Entity> ENTITY_MAP2 = new HashMap<>();
    
    public static void setAnimation(Mob mob, String animationName)
    {
		try
		{
			MorphSavedData data = MorphSavedData.get(mob.level);
        	if(data != null)
        	{
    			Field f = mob.getClass().getField(animationName);
    			Method m = mob.getClass().getMethod("setAnimation", f.getType());
    			f.setAccessible(true);
    			m.invoke(mob, f.get(mob));
        	}
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e)
		{
			
		}
    }
    
	public static void getMethodsInsideClass(Class<?> clazz, Consumer<MethodInsnNode> consumer)
	{
        try
        {
    		ClassNode classNode = MorphUtil.getClassNode(clazz);
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
	
	public static void getFieldsInsideClass(Class<?> clazz, Consumer<FieldInsnNode> consumer)
	{
        try
        {
    		ClassNode classNode = MorphUtil.getClassNode(clazz);
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
	
	//ChatGPT ahhh;
    public static ClassNode getClassNode(Class<?> clazz) throws IOException
    {
        String className = clazz.getName().replace('.', '/') + ".class";
        try(InputStream classStream = clazz.getClassLoader().getResourceAsStream(className))
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
