package com.min01.morph.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Lists;
import com.min01.morph.capabilities.IMorphCapability;
import com.min01.morph.capabilities.MorphCapabilityImpl;
import com.min01.morph.entity.EntityFakeTarget;
import com.min01.morph.entity.MorphEntities;
import com.min01.morph.misc.ILevelEntityGetterAdapter;
import com.min01.morph.misc.IWrappedGoal;
import com.min01.morph.network.MorphNetwork;
import com.min01.morph.network.SyncMorphOptionsPacket;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class MorphUtil 
{
	public static final String MANUAL_TRIGGER_TICKS_KEY = "MorphManualTriggerTicks";
	private static final Pattern GOAL_INDEX_PATTERN = Pattern.compile("^(.*?)(\\d+)$");
	private static final List<MobActionAdapter> ACTION_ADAPTERS = new ArrayList<>();

	public static final Map<Integer, Entity> ENTITY_MAP = new HashMap<>();
	public static final Map<Integer, Entity> ENTITY_MAP2 = new HashMap<>();
	
	public static final List<Attribute> ATTRIBUTES = Lists.newArrayList(Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS, Attributes.ATTACK_DAMAGE, Attributes.ATTACK_KNOCKBACK, Attributes.FLYING_SPEED, Attributes.FOLLOW_RANGE, Attributes.JUMP_STRENGTH, Attributes.KNOCKBACK_RESISTANCE, Attributes.MAX_HEALTH);
	
	public static final Method GET_ENTITY = ObfuscationReflectionHelper.findMethod(Level.class, "m_142646_");

	static
	{
		ACTION_ADAPTERS.add(new IronsSpellbooksAdapter());
		ACTION_ADAPTERS.add(new MowzieAbilityAdapter());
		ACTION_ADAPTERS.add(new AquamiraePlayAdapter());
		ACTION_ADAPTERS.add(new MutantMethodAdapter());
		ACTION_ADAPTERS.add(new GenericFieldAnimationAdapter());
	}
	
	public static void selectAnimation(LivingEntity owner, String animationName)
	{
		owner.getCapability(MorphCapabilityImpl.MORPH).ifPresent(t -> 
		{
			t.selectAnimation(animationName);
		});
	}
	
	public static void selectGoal(LivingEntity owner, String goalName)
	{
		owner.getCapability(MorphCapabilityImpl.MORPH).ifPresent(t -> 
		{
			t.selectGoal(goalName);
		});
	}
	
	public static void selectOption(ServerPlayer sender, UUID ownerUuid, MorphSelectionType selectionType, int selectionId, int revision)
	{
		if(sender == null || !sender.getUUID().equals(ownerUuid))
		{
			sendClientWarning(sender, "minsmorph.sync.invalid_owner");
			return;
		}
		selectOption(sender, selectionType, selectionId, revision);
	}

	public static void selectOption(LivingEntity owner, MorphSelectionType selectionType, int selectionId, int revision)
	{
		owner.getCapability(MorphCapabilityImpl.MORPH).ifPresent(t ->
		{
			if(t.getOptionsRevision() != revision)
			{
				if(owner instanceof ServerPlayer serverPlayer)
				{
					sendClientWarning(serverPlayer, "minsmorph.sync.stale");
				}
				syncOptions(owner);
				return;
			}
			if(selectionType == MorphSelectionType.GOAL)
			{
				List<String> goals = t.getGoalOptions();
				if(selectionId >= 0 && selectionId < goals.size())
				{
					t.setSelectedGoalId(selectionId);
					t.selectGoal(goals.get(selectionId));
					syncOptions(owner);
				}
				else if(owner instanceof ServerPlayer serverPlayer)
				{
					sendClientWarning(serverPlayer, "minsmorph.sync.invalid_selection");
				}
			}
			else if(selectionType == MorphSelectionType.ANIMATION)
			{
				List<String> animations = t.getAnimationOptions();
				if(selectionId >= 0 && selectionId < animations.size())
				{
					t.setSelectedAnimationId(selectionId);
					t.selectAnimation(animations.get(selectionId));
					syncOptions(owner);
				}
				else if(owner instanceof ServerPlayer serverPlayer)
				{
					sendClientWarning(serverPlayer, "minsmorph.sync.invalid_selection");
				}
			}
		});
	}

	public static void triggerGoal(LivingEntity owner)
	{
		owner.getCapability(MorphCapabilityImpl.MORPH).ifPresent(t -> 
		{
			String goalName = t.getSelectedGoal();
			String animationName = t.getSelectedAnimation();
			if(t.getMorph() instanceof Mob mob && (goalName == null || goalName.isBlank()) && animationName != null && !animationName.isBlank())
			{
				if(!triggerAnimationOnly(owner, mob, animationName))
				{
					if(owner instanceof ServerPlayer serverPlayer)
					{
						sendClientWarning(serverPlayer, "minsmorph.sync.unsupported_action");
					}
				}
				return;
			}
			if(t.getMorph() instanceof Mob mob && goalName != null && !goalName.isBlank())
			{
				LivingEntity realTarget = findRealTarget(owner);
				LivingEntity fallbackTarget = realTarget != null ? null : createEphemeralFakeTarget(owner);
				if(realTarget == null && (fallbackTarget == null || !fallbackTarget.isAlive()))
				{
					if(owner instanceof ServerPlayer serverPlayer)
					{
						sendClientWarning(serverPlayer, "minsmorph.sync.missing_target");
					}
					syncOptions(owner);
					return;
				}
				Set<WrappedGoal> set = mob.goalSelector.getAvailableGoals();
				List<WrappedGoal> list = Lists.newArrayList(set);
				String normalizedGoalName = goalName.trim();
				int expectedIndex = getGoalIndexSuffix(normalizedGoalName);
				String baseGoalName = removeGoalIndexSuffix(normalizedGoalName);
				boolean triggeredGoal = false;
				for(WrappedGoal goal : set)
				{
					int index = list.indexOf(goal);
					String name = MorphUtil.getGoalName(goal.getGoal());
					boolean indexMatched = expectedIndex >= 0 && index == expectedIndex && name.equals(baseGoalName);
					if(indexMatched || name.equals(normalizedGoalName))
					{
						mob.getPersistentData().putInt(MANUAL_TRIGGER_TICKS_KEY, 20);
						((IWrappedGoal) goal).setCanUse();
						MorphUtil.setTarget(owner, mob, goal, fallbackTarget, realTarget);
						MorphUtil.setAnimation(mob, animationName);
						try
						{
							// Prefer normal GoalSelector path so goals that rely on selector-managed state can initialize properly.
							mob.goalSelector.tick();
							if(!goal.isRunning())
							{
								goal.start();
							}
							// Some custom goals (e.g. teleport-type goals) require at least one tick after start.
							if(goal.isRunning())
							{
								goal.tick();
							}
						}
						catch(Exception e)
						{
							goal.stop();
							mob.setTarget(null);
							if(owner instanceof ServerPlayer serverPlayer)
							{
								sendClientWarning(serverPlayer, "minsmorph.sync.goal_failed");
							}
						}
						triggeredGoal = true;
						break;
					}
				}
				if(!triggeredGoal && owner instanceof ServerPlayer serverPlayer)
				{
					sendClientWarning(serverPlayer, "minsmorph.sync.unsupported_action");
				}
			}
		});
	}

	private static LivingEntity createEphemeralFakeTarget(LivingEntity owner)
	{
		if(owner.level.isClientSide)
		{
			return null;
		}
		EntityFakeTarget fakeTarget = new EntityFakeTarget(MorphEntities.FAKE_TARGET.get(), owner.level);
		fakeTarget.setOwner(owner);
		fakeTarget.setLifeTicks(20);
		owner.level.addFreshEntity(fakeTarget);
		return fakeTarget;
	}

	private static LivingEntity findRealTarget(LivingEntity owner)
	{
		HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(owner, entity -> !entity.isAlliedTo(owner), 30.0F);
		if(hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living)
		{
			return living;
		}
		return null;
	}

	private static boolean triggerAnimationOnly(LivingEntity owner, Mob mob, String animationName)
	{
		boolean success = setAnimation(mob, animationName);
		if(!success && owner instanceof ServerPlayer serverPlayer)
		{
			sendClientWarning(serverPlayer, "minsmorph.sync.adapter_failed");
		}
		return success;
	}

	public static void triggerGoal(ServerPlayer sender, UUID ownerUuid, int revision)
	{
		if(sender == null || !sender.getUUID().equals(ownerUuid))
		{
			sendClientWarning(sender, "minsmorph.sync.invalid_owner");
			return;
		}
		sender.getCapability(MorphCapabilityImpl.MORPH).ifPresent(t ->
		{
			if(t.getOptionsRevision() != revision)
			{
				sendClientWarning(sender, "minsmorph.sync.stale");
				syncOptions(sender);
				return;
			}
			List<String> goals = t.getGoalOptions();
			int selectedGoalId = t.getSelectedGoalId();
			if(selectedGoalId < 0 || selectedGoalId >= goals.size())
			{
				sendClientWarning(sender, "minsmorph.sync.no_goal_selected");
				syncOptions(sender);
				return;
			}
			selectGoal(sender, goals.get(selectedGoalId));
			int selectedAnimationId = t.getSelectedAnimationId();
			List<String> animations = t.getAnimationOptions();
			if(selectedAnimationId >= 0 && selectedAnimationId < animations.size())
			{
				selectAnimation(sender, animations.get(selectedAnimationId));
			}
			triggerGoal((LivingEntity) sender);
		});
	}

	public static boolean selectGoalByName(LivingEntity owner, String goalName)
	{
		if(goalName == null || goalName.isBlank())
		{
			return false;
		}
		IMorphCapability cap = owner.getCapability(MorphCapabilityImpl.MORPH).orElse(new MorphCapabilityImpl(owner));
		int selectionId = cap.getGoalOptions().indexOf(goalName);
		if(selectionId < 0)
		{
			return false;
		}
		selectOption(owner, MorphSelectionType.GOAL, selectionId, cap.getOptionsRevision());
		return true;
	}

	public static boolean selectAnimationByName(LivingEntity owner, String animationName)
	{
		if(animationName == null || animationName.isBlank())
		{
			return false;
		}
		IMorphCapability cap = owner.getCapability(MorphCapabilityImpl.MORPH).orElse(new MorphCapabilityImpl(owner));
		int selectionId = cap.getAnimationOptions().indexOf(animationName);
		if(selectionId < 0)
		{
			return false;
		}
		selectOption(owner, MorphSelectionType.ANIMATION, selectionId, cap.getOptionsRevision());
		return true;
	}

	private static void sendClientWarning(ServerPlayer player, String translationKey)
	{
		if(player != null)
		{
			player.displayClientMessage(Component.translatable(translationKey), true);
		}
	}

	private static int getGoalIndexSuffix(String goalName)
	{
		Matcher matcher = GOAL_INDEX_PATTERN.matcher(goalName);
		if(matcher.matches())
		{
			try
			{
				return Integer.parseInt(matcher.group(2));
			}
			catch(NumberFormatException ignored)
			{
				
			}
		}
		return -1;
	}

	private static String removeGoalIndexSuffix(String goalName)
	{
		Matcher matcher = GOAL_INDEX_PATTERN.matcher(goalName);
		if(matcher.matches())
		{
			return matcher.group(1);
		}
		return goalName;
	}
	public static void setTarget(LivingEntity owner, Mob morph, LivingEntity fakeTarget)
	{
		setTarget(owner, morph, null, fakeTarget, null);
	}
	
	public static void setTarget(LivingEntity owner, Mob morph, WrappedGoal goal, LivingEntity fakeTarget)
	{
		setTarget(owner, morph, goal, fakeTarget, null);
	}

	public static void setTarget(LivingEntity owner, Mob morph, WrappedGoal goal, LivingEntity fakeTarget, LivingEntity preferredTarget)
	{
		if(goal != null && fakeTarget != null)
		{
			((IWrappedGoal) goal).setFakeTarget(fakeTarget);
		}
		if(preferredTarget != null)
		{
			if(goal != null)
			{
				((IWrappedGoal) goal).setTarget(preferredTarget);
			}
			morph.setTarget(preferredTarget);
			return;
		}
		HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(owner, entity -> !entity.isAlliedTo(owner), 30.0F);
		if(hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living)
		{
			if(goal != null)
			{
				((IWrappedGoal) goal).setTarget(living);
			}
			morph.setTarget(living);
			return;
		}
		if(fakeTarget != null)
		{
			morph.setTarget(fakeTarget);
		}
	}
	
	public static void getMorph(Entity entity, Consumer<LivingEntity> consumer)
	{
		entity.getCapability(MorphCapabilityImpl.MORPH).ifPresent(t -> 
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
		return entity.getId() < 0 && !(entity instanceof Player);
	}
	
	public static void setChangedDimension(LivingEntity living, boolean value)
	{
		IMorphCapability cap = living.getCapability(MorphCapabilityImpl.MORPH).orElse(new MorphCapabilityImpl(living));
		cap.setChangedDimension(value);
	}
	
	public static boolean isChangedDimension(LivingEntity living)
	{
		IMorphCapability cap = living.getCapability(MorphCapabilityImpl.MORPH).orElse(new MorphCapabilityImpl(living));
		return cap.isChangedDimension();
	}
	
	public static boolean isPersistent(LivingEntity living)
	{
		IMorphCapability cap = living.getCapability(MorphCapabilityImpl.MORPH).orElse(new MorphCapabilityImpl(living));
		return cap.isPersistent();
	}
	
	public static boolean hasMorph(LivingEntity living)
	{
		IMorphCapability cap = living.getCapability(MorphCapabilityImpl.MORPH).orElse(new MorphCapabilityImpl(living));
		return cap.getMorph() != null;
	}

	public static void syncOptions(LivingEntity owner)
	{
		if(owner.level.isClientSide)
		{
			return;
		}
		owner.getCapability(MorphCapabilityImpl.MORPH).ifPresent(t ->
		{
			if(owner instanceof ServerPlayer serverPlayer)
			{
				MorphNetwork.sendToPlayer(new SyncMorphOptionsPacket(owner.getUUID(), t.getOptionsRevision(), t.getGoalOptions(), t.getAnimationOptions(), t.getDataOptions(), t.getSelectedGoalId(), t.getSelectedAnimationId()), serverPlayer);
			}
		});
	}
	
	public static void removeMorph(LivingEntity living)
	{
		living.getCapability(MorphCapabilityImpl.MORPH).ifPresent(t -> 
		{
			if(hasMorph(living))
			{
				if(living instanceof Player player)
				{
					if(!player.getAbilities().instabuild)
					{
						player.getAbilities().flying = false;
						player.getAbilities().mayfly = false;
						player.setNoGravity(false);
						player.onUpdateAbilities();
					}
				}
				t.setMorph(null);
				t.setType(null);
				t.setPersistent(false);
				living.heal(living.getMaxHealth());
				living.refreshDimensions();
			}
		});
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
	public static boolean setData(Mob mob, String dataName, String dataValue)
	{
		try
		{
			Field f = findFieldInHierarchy(mob.getClass(), dataName);
			if(f == null)
			{
				return false;
			}
			f.setAccessible(true);
            if(dataValue.equalsIgnoreCase("true") || dataValue.equalsIgnoreCase("false")) 
            {
            	EntityDataAccessor<Boolean> accessor = (EntityDataAccessor<Boolean>) f.get(mob);
            	if(accessor.getSerializer() == EntityDataSerializers.BOOLEAN)
            	{
            		mob.getEntityData().set(accessor, Boolean.parseBoolean(dataValue));
            		return true;
            	}
            }
            else if(dataValue.contains("."))
            {
                try 
                {
                    float value = Float.parseFloat(dataValue);
                	EntityDataAccessor<Float> accessor = (EntityDataAccessor<Float>) f.get(mob);
                	if(accessor.getSerializer() == EntityDataSerializers.FLOAT)
                	{
                    	mob.getEntityData().set(accessor, value);
                    	return true;
                	}
                }
                catch (NumberFormatException e)
                {
                	
                }
            }
            else
            {
                try 
                {
                    int value = Integer.parseInt(dataValue);
                	EntityDataAccessor<Integer> accessor = (EntityDataAccessor<Integer>) f.get(mob);
                	if(accessor.getSerializer() == EntityDataSerializers.INT)
                	{
                    	mob.getEntityData().set(accessor, value);
                    	return true;
                	}
                }
                catch (NumberFormatException e)
                {
                	EntityDataAccessor<String> accessor = (EntityDataAccessor<String>) f.get(mob);
                	if(accessor.getSerializer() == EntityDataSerializers.STRING)
                	{
                    	mob.getEntityData().set(accessor, dataValue);
                    	return true;
                	}
                }
            }
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException e)
		{
			
		}
		return false;
	}
	
	public static boolean setAnimation(Mob mob, String animationName)
    {
		if(animationName == null || animationName.isBlank())
		{
			return false;
		}
		for(MobActionAdapter adapter : ACTION_ADAPTERS)
		{
			if(adapter.supports(mob))
			{
				boolean success = adapter.execute(mob, animationName);
				if(success)
				{
					return true;
				}
			}
		}
		return false;
    }

	private static Field findFieldInHierarchy(Class<?> clazz, String fieldName)
	{
		Class<?> current = clazz;
		while(current != null && current != Object.class)
		{
			try
			{
				return current.getDeclaredField(fieldName);
			}
			catch(NoSuchFieldException ignored)
			{
				current = current.getSuperclass();
			}
		}
		return null;
	}

	private static Method findMethodAssignable(Class<?> clazz, String methodName, Class<?> paramType)
	{
		for(Method method : clazz.getMethods())
		{
			if(method.getName().equals(methodName) && method.getParameterCount() == 1)
			{
				Class<?> expected = method.getParameterTypes()[0];
				if(expected.isAssignableFrom(paramType))
				{
					return method;
				}
			}
		}
		return null;
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
		for(MobActionAdapter adapter : ACTION_ADAPTERS)
		{
			for(String action : adapter.scan(clazz))
			{
				if(!list.contains(action))
				{
					list.add(action);
				}
			}
		}
		return list;
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
    
    public static void tick(LivingEntity player, LivingEntity morph)
    {
    	morph.getPersistentData().putUUID("MorphOwnerUUID", player.getUUID());
    	player.eyeHeight = morph.eyeHeight;
    	player.dimensions = morph.dimensions;
    	player.refreshDimensions();
		if(!player.isAlive())
		{
			morph.discard();
		}
		else
		{
    		if(morph instanceof Mob mob && mob.getPersistentData().contains(MANUAL_TRIGGER_TICKS_KEY))
    		{
    			int ticks = mob.getPersistentData().getInt(MANUAL_TRIGGER_TICKS_KEY);
    			if(ticks > 0)
    			{
    				mob.getPersistentData().putInt(MANUAL_TRIGGER_TICKS_KEY, ticks - 1);
    			}
    		}
	    	sync(morph, player);
			for(Entity entity : morph.getPassengers())
			{
				tickPassenger(morph, entity);
			}
			try
			{
				ILevelEntityGetterAdapter adapter = (ILevelEntityGetterAdapter) GET_ENTITY.invoke(player.level);
				adapter.byId().putIfAbsent(morph.getId(), morph);
				adapter.byId().values().removeIf(t -> !t.isAlive());
				adapter.byUuid().putIfAbsent(morph.getUUID(), morph);
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
        	try
        	{
            	mob.goalSelector.tickRunningGoals(false);
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        		mob.setTarget(null);
        	}
        	mob.level.getProfiler().pop();
        }
        else 
        {
        	mob.level.getProfiler().push("goalSelector");
        	try
        	{
        		mob.goalSelector.tick();
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        	}
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
			e.printStackTrace();
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

	public static boolean areFriendly(Entity attacker, Entity target)
	{
		if(attacker == null || target == null)
		{
			return false;
		}
		if(attacker == target)
		{
			return true;
		}
		Entity attackerOwner = MorphUtil.getMorphOwner(attacker);
		Entity targetOwner = MorphUtil.getMorphOwner(target);
		Entity attackerRoot = attackerOwner != null ? attackerOwner : attacker;
		Entity targetRoot = targetOwner != null ? targetOwner : target;
		if(attackerRoot == targetRoot)
		{
			return true;
		}
		// Do not call isAlliedTo() here: this method is used by our isAlliedTo mixin.
		if(attackerRoot.getTeam() != null && attackerRoot.getTeam() == targetRoot.getTeam())
		{
			return true;
		}
		return false;
	}

	public static boolean shouldCancelMorphTarget(Mob attacker, LivingEntity target)
	{
		if(target == null)
		{
			return false;
		}
		LivingEntity effectiveTarget = getEffectiveLivingTarget(target);
		if(areFriendly(attacker, target))
		{
			return true;
		}
		Entity attackerOwner = MorphUtil.getMorphOwner(attacker);
		Entity targetOwner = MorphUtil.getMorphOwner(effectiveTarget);
		boolean morphInvolved = attackerOwner != null || targetOwner != null;
		if(!morphInvolved)
		{
			return false;
		}
		EntityType<?> attackerType = getEffectiveType(attacker);
		EntityType<?> targetType = getEffectiveType(effectiveTarget);
		return attackerType != null && attackerType == targetType;
	}

	public static LivingEntity getEffectiveLivingTarget(LivingEntity living)
	{
		IMorphCapability cap = living.getCapability(MorphCapabilityImpl.MORPH).orElse(null);
		if(cap != null && cap.getMorph() != null)
		{
			return cap.getMorph();
		}
		return living;
	}

	private static EntityType<?> getEffectiveType(Entity entity)
	{
		if(entity instanceof LivingEntity living)
		{
			IMorphCapability cap = living.getCapability(MorphCapabilityImpl.MORPH).orElse(null);
			if(cap != null && cap.getMorph() != null)
			{
				return cap.getMorph().getType();
			}
		}
		return entity.getType();
	}
    
	@SuppressWarnings("unchecked")
	public static Entity getEntityByUUID(Level level, UUID uuid)
	{
		try 
		{
			LevelEntityGetter<Entity> entities = (LevelEntityGetter<Entity>) GET_ENTITY.invoke(level);
			return entities.get(uuid);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}

	private interface MobActionAdapter
	{
		boolean supports(Mob mob);
		boolean execute(Mob mob, String actionName);
		List<String> scan(Class<?> clazz);
	}

	private static class GenericFieldAnimationAdapter implements MobActionAdapter
	{
		@Override
		public boolean supports(Mob mob)
		{
			return true;
		}

		@Override
		public boolean execute(Mob mob, String actionName)
		{
			Field field = findFieldInHierarchy(mob.getClass(), actionName);
			if(field == null)
			{
				return false;
			}
			try
			{
				Method method = findMethodAssignable(mob.getClass(), "setAnimation", field.getType());
				if(method != null)
				{
					field.setAccessible(true);
					method.invoke(mob, field.get(Modifier.isStatic(field.getModifiers()) ? null : mob));
					return true;
				}
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				
			}
			return false;
		}

		@Override
		public List<String> scan(Class<?> clazz)
		{
			List<String> actions = new ArrayList<>();
			Class<?> current = clazz;
			while(current != null && current != Object.class)
			{
				for(Field field : current.getDeclaredFields())
				{
					String simple = field.getType().getSimpleName();
					if((simple.contains("Animation") || simple.contains("AbilityType")) && !field.getType().isArray() && !actions.contains(field.getName()))
					{
						actions.add(field.getName());
					}
				}
				current = current.getSuperclass();
			}
			return actions;
		}
	}

	private static class MowzieAbilityAdapter implements MobActionAdapter
	{
		@Override
		public boolean supports(Mob mob)
		{
			return mob.getClass().getName().toLowerCase().contains("mowz");
		}

		@Override
		public boolean execute(Mob mob, String actionName)
		{
			Field field = findFieldInHierarchy(mob.getClass(), actionName);
			if(field == null)
			{
				return false;
			}
			try
			{
				Method method = findMethodAssignable(mob.getClass(), "sendAbilityMessage", field.getType());
				if(method != null)
				{
					field.setAccessible(true);
					method.invoke(mob, field.get(Modifier.isStatic(field.getModifiers()) ? null : mob));
					return true;
				}
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				
			}
			return false;
		}

		@Override
		public List<String> scan(Class<?> clazz)
		{
			return Collections.emptyList();
		}
	}

	private static class AquamiraePlayAdapter implements MobActionAdapter
	{
		@Override
		public boolean supports(Mob mob)
		{
			return mob.getClass().getName().toLowerCase().contains("aquamirae");
		}

		@Override
		public boolean execute(Mob mob, String actionName)
		{
			Field field = findFieldInHierarchy(mob.getClass(), actionName);
			if(field == null)
			{
				return false;
			}
			try
			{
				field.setAccessible(true);
				Object animation = field.get(Modifier.isStatic(field.getModifiers()) ? null : mob);
				if(animation != null)
				{
					Method play = animation.getClass().getMethod("play", Entity.class, int.class);
					play.invoke(animation, mob, 20);
					return true;
				}
			}
			catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				
			}
			return false;
		}

		@Override
		public List<String> scan(Class<?> clazz)
		{
			return Collections.emptyList();
		}
	}

	private static class MutantMethodAdapter implements MobActionAdapter
	{
		@Override
		public boolean supports(Mob mob)
		{
			String className = mob.getClass().getName().toLowerCase();
			return className.contains("mutant") || className.contains("irons_spellbooks") || className.contains("spellbook");
		}

		@Override
		public boolean execute(Mob mob, String actionName)
		{
			String methodName = actionName.startsWith("M_") ? actionName.substring(2) : actionName;
			for(Method method : mob.getClass().getMethods())
			{
				if(method.getName().equals(methodName))
				{
					try
					{
						if(method.getParameterCount() == 0)
						{
							method.invoke(mob);
							return true;
						}
						if(method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(Entity.class))
						{
							method.invoke(mob, mob);
							return true;
						}
					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
						
					}
				}
			}
			return false;
		}

		@Override
		public List<String> scan(Class<?> clazz)
		{
			String className = clazz.getName().toLowerCase();
			if(!(className.contains("mutant") || className.contains("irons_spellbooks") || className.contains("spellbook")))
			{
				return Collections.emptyList();
			}
			List<String> methods = new ArrayList<>();
			for(Method method : clazz.getMethods())
			{
				String name = method.getName().toLowerCase();
				if(method.getParameterCount() <= 1 && (name.contains("spell") || name.contains("ability") || name.contains("animation")))
				{
					String key = "M_" + method.getName();
					if(!methods.contains(key))
					{
						methods.add(key);
					}
				}
			}
			return methods;
		}
	}

	private static class IronsSpellbooksAdapter implements MobActionAdapter
	{
		@Override
		public boolean supports(Mob mob)
		{
			String className = mob.getClass().getName().toLowerCase();
			return className.contains("irons_spellbooks") || className.contains("spellbook");
		}

		@Override
		public boolean execute(Mob mob, String actionName)
		{
			return new MutantMethodAdapter().execute(mob, actionName);
		}

		@Override
		public List<String> scan(Class<?> clazz)
		{
			return new MutantMethodAdapter().scan(clazz);
		}
	}
}
