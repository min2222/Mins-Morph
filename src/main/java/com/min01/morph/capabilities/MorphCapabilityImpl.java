package com.min01.morph.capabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.min01.morph.network.MorphNetwork;
import com.min01.morph.network.UpdateMorphPacket;
import com.min01.morph.config.MorphConfig;
import com.min01.morph.util.MorphUtil;
import com.min01.morph.util.world.MorphSavedData;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphCapabilityImpl implements IMorphCapability
{
	public static final Capability<IMorphCapability> MORPH = CapabilityManager.get(new CapabilityToken<>() {});
	private static final AtomicInteger NEXT_MORPH_ID = new AtomicInteger(-1000);
	
	private final LivingEntity entity;
	private LivingEntity morph;
	private LivingEntity target;
	private EntityType<?> type;
	private boolean isPersistent;
	private boolean changedDimension;
	private List<Map<String, String>> dataList = new ArrayList<>();
	private String selectedGoalName = "";
	private String selectedAnimationName = "";
	private int selectedGoalId = -1;
	private int selectedAnimationId = -1;
	private List<String> goalOptions = new ArrayList<>();
	private List<String> animationOptions = new ArrayList<>();
	private List<String> dataOptions = new ArrayList<>();
	private int optionsRevision = 0;
	
	public MorphCapabilityImpl(LivingEntity entity)
	{
		this.entity = entity;
	}
	
	@Override
	public CompoundTag serializeNBT() 
	{
		CompoundTag nbt = new CompoundTag();
		ListTag datas = new ListTag();
		this.dataList.forEach(t -> 
		{
			for(Entry<String, String> entry : t.entrySet())
			{
				String dataName = entry.getKey();
				String dataValue = entry.getValue();
				CompoundTag tag = new CompoundTag();
				tag.putString("DataName", dataName);
				tag.putString("DataValue", dataValue);
				datas.add(tag);
			}
		});
		if(this.type != null)
		{
			nbt.putString("MorphType", ForgeRegistries.ENTITY_TYPES.getKey(this.type).toString());
		}
		nbt.putBoolean("isPersistent", this.isPersistent);
		nbt.putBoolean("ChangedDimension", this.changedDimension);
		nbt.put("MobDatas", datas);
		nbt.putString("SelectedGoal", this.selectedGoalName);
		nbt.putString("SelectedAnimation", this.selectedAnimationName);
		nbt.putInt("SelectedGoalId", this.selectedGoalId);
		nbt.putInt("SelectedAnimationId", this.selectedAnimationId);
		nbt.putInt("OptionsRevision", this.optionsRevision);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		if(nbt.contains("MorphType"))
		{
			this.setType(ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(nbt.getString("MorphType"))));
		}
		ListTag datas = nbt.getList("MobDatas", 10);
		for(int i = 0; i < datas.size(); ++i)
		{
			CompoundTag tag = datas.getCompound(i);
			String dataName = tag.getString("DataName");
			String dataValue = tag.getString("DataValue");
			this.setData(dataName, dataValue);
		}
		this.setPersistent(nbt.getBoolean("isPersistent"));
		this.setChangedDimension(nbt.getBoolean("ChangedDimension"));
		this.selectGoal(nbt.getString("SelectedGoal"));
		this.selectAnimation(nbt.getString("SelectedAnimation"));
		this.setSelectedGoalId(nbt.contains("SelectedGoalId") ? nbt.getInt("SelectedGoalId") : -1);
		this.setSelectedAnimationId(nbt.contains("SelectedAnimationId") ? nbt.getInt("SelectedAnimationId") : -1);
		if(nbt.contains("OptionsRevision"))
		{
			this.setOptionsRevision(nbt.getInt("OptionsRevision"));
		}
	}
	
	@Override
	public void tick()
	{
		if(this.morph != null)
		{
			this.tickMorphState();
			this.tickOwnerState();
			if(!this.entity.level.isClientSide)
			{
				this.removeInvalidMorph();
			}
		}
		else if(this.type != null)
		{
			EntityType<?> morphType = this.type;
			if(morphType.create(this.entity.level) instanceof LivingEntity livingMorph)
			{
				this.setMorph(livingMorph);
			}
			else
			{
				this.setType(null);
			}
		}
	}

	@Override
	public void selectGoal(String selectedGoal) 
	{
		this.selectedGoalName = selectedGoal;
		this.selectedGoalId = this.goalOptions.indexOf(selectedGoal);
		this.sendUpdataPacket();
	}
	
	@Override
	public String getSelectedGoal()
	{
		return this.selectedGoalName;
	}
	
	@Override
	public void selectAnimation(String selectedAnimation) 
	{
		this.selectedAnimationName = selectedAnimation;
		this.selectedAnimationId = this.animationOptions.indexOf(selectedAnimation);
		this.sendUpdataPacket();
	}
	
	@Override
	public String getSelectedAnimation()
	{
		return this.selectedAnimationName;
	}
	
	@Override
	public void setPersistent(boolean isPersistent) 
	{
		this.isPersistent = isPersistent;
		this.sendUpdataPacket();
	}
	
	@Override
	public boolean isPersistent() 
	{
		return this.isPersistent;
	}

	@Override
	public void setMorph(LivingEntity morph) 
	{
		float healthRatio = 1.0F;
		float ownerMaxHealth = Math.max(this.entity.getMaxHealth(), 1.0F);
		if(ownerMaxHealth > 0.0F)
		{
			healthRatio = this.entity.getHealth() / ownerMaxHealth;
		}
		if(this.morph != null)
		{
			this.morph.discard();
		}
		if(morph != null)
		{
			if(morph.getId() > 0)
			{
	        	morph.setId(NEXT_MORPH_ID.getAndDecrement());
			}
			this.setType(morph.getType());
			this.setup(morph);
			MorphUtil.ENTITY_MAP.put(morph.getClass().hashCode(), morph);
			MorphUtil.ENTITY_MAP2.put(morph.getClass().getSuperclass().hashCode(), morph);
			this.refreshOptions(morph);
		}
		else
		{
			this.setGoalOptions(new ArrayList<>());
			this.setAnimationOptions(new ArrayList<>());
			this.setDataOptions(new ArrayList<>());
			this.setSelectedGoalId(-1);
			this.setSelectedAnimationId(-1);
		}
		this.morph = morph;
		this.applyOwnerHealthRatio(healthRatio);
		this.sendUpdataPacket();
		MorphUtil.syncOptions(this.entity);
	}

	private void applyOwnerHealthRatio(float ratio)
	{
		if(this.entity.level.isClientSide || this.morph == null)
		{
			return;
		}
		float clampedRatio = Math.max(0.0F, Math.min(1.0F, ratio));
		float maxHealth = this.entity.getMaxHealth();
		if(maxHealth <= 0.0F)
		{
			return;
		}
		float targetHealth = Math.max(1.0F, maxHealth * clampedRatio);
		this.entity.setHealth(Math.min(targetHealth, maxHealth));
	}
	
	public void setup(LivingEntity morph)
	{
		if(!morph.level.isClientSide)
		{
			if(this.isPersistent)
			{
				this.dataList.forEach(t -> 
				{
					for(Entry<String, String> entry : t.entrySet())
					{
						String dataName = entry.getKey();
						String dataValue = entry.getValue();
						if(morph instanceof Mob mob)
						{
							MorphUtil.setData(mob, dataName, dataValue);
						}
					}
				});
			}
			MorphSavedData data = MorphSavedData.get(morph.level);
        	if(data != null)
        	{
    			String morphKey = MorphSavedData.getMorphKey(morph.getType());
    			data.saveData(morphKey, MorphUtil.getDatas(morph.getClass()));
    			if(morph instanceof Mob mob)
    			{
    				data.saveGoal(morphKey, MorphUtil.getGoals(mob));
    			}
    			data.saveAnimation(morphKey, MorphUtil.getAnimations(morph.getClass()));
        	}
			if(morph instanceof Mob mob && this.entity.level instanceof ServerLevelAccessor accessor)
			{
				ForgeEventFactory.onFinalizeSpawn(mob, accessor, this.entity.level.getCurrentDifficultyAt(this.entity.blockPosition()), MobSpawnType.COMMAND, null, null);
			}
		}
	}

	private void refreshOptions(LivingEntity morph)
	{
		this.goalOptions = morph instanceof Mob mob ? MorphUtil.getGoals(mob) : new ArrayList<>();
		this.animationOptions = MorphUtil.getAnimations(morph.getClass());
		this.dataOptions = MorphUtil.getDatas(morph.getClass());
		this.selectedGoalId = this.goalOptions.indexOf(this.selectedGoalName);
		this.selectedAnimationId = this.animationOptions.indexOf(this.selectedAnimationName);
		if(this.selectedGoalId < 0)
		{
			this.selectedGoalName = "";
		}
		if(this.selectedAnimationId < 0)
		{
			this.selectedAnimationName = "";
		}
		this.optionsRevision++;
	}

	private void tickMorphState()
	{
		MorphUtil.tick(this.entity, this.morph);
		this.morph.tick();
		if(this.morph instanceof AbstractPiglin piglin)
		{
			piglin.setImmuneToZombification(true);
		}
		if(this.morph instanceof Hoglin hoglin)
		{
			hoglin.setImmuneToZombification(true);
		}
	}

	private void tickOwnerState()
	{
		if(this.entity instanceof Player player)
		{
			player.setNoGravity(this.morph.isNoGravity());
			this.updatePlayerFlight(player);
		}
	}

	private void updatePlayerFlight(Player player)
	{
		if(player.getAbilities().instabuild || player.isSpectator() || !(this.morph instanceof Mob mob))
		{
			return;
		}
		MoveControl control = mob.getMoveControl();
		PathNavigation navigation = mob.getNavigation();
		boolean canFly = control instanceof FlyingMoveControl || navigation instanceof FlyingPathNavigation || hasFlightLikeClass(control) || hasFlightLikeClass(navigation) || MorphConfig.isFlightOverride(this.morph.getType());
		if(canFly)
		{
			if(!player.getAbilities().mayfly)
			{
				player.getAbilities().mayfly = true;
				player.onUpdateAbilities();
			}
		}
		else if(player.getAbilities().mayfly)
		{
			player.getAbilities().flying = false;
			player.getAbilities().mayfly = false;
			player.onUpdateAbilities();
		}
	}

	private boolean hasFlightLikeClass(Object object)
	{
		if(object == null)
		{
			return false;
		}
		Class<?> current = object.getClass();
		while(current != null && current != Object.class)
		{
			String name = current.getSimpleName().toLowerCase();
			if(name.contains("flight") || name.contains("flying"))
			{
				return true;
			}
			current = current.getSuperclass();
		}
		return false;
	}

	private void removeInvalidMorph()
	{
		RemovalReason removalReason = this.morph.getRemovalReason();
		if(!this.morph.isAlive() && removalReason != RemovalReason.CHANGED_DIMENSION && !this.isPersistent)
		{
			MorphUtil.removeMorph(this.entity);
		}
	}

	@Override
	public LivingEntity getMorph()
	{
		return this.morph;
	}
	
	@Override
	public void setType(EntityType<?> type) 
	{
		this.type = type;
		this.sendUpdataPacket();
	}
	
	@Override
	public EntityType<?> getType() 
	{
		return this.type;
	}

	@Override
	public LivingEntity getFakeTarget() 
	{
		return this.target;
	}
	
	@Override
	public void setData(String dataName, String dataValue) 
	{
		Map<String, String> map = new HashMap<>();
		map.put(dataName, dataValue);
		this.dataList.add(map);
		this.sendUpdataPacket();
	}
	
	@Override
	public void setChangedDimension(boolean isChangedDimension)
	{
		this.changedDimension = isChangedDimension;
		this.sendUpdataPacket();
	}
	
	@Override
	public boolean isChangedDimension() 
	{
		return this.changedDimension;
	}
	
	public void sendUpdataPacket()
	{
		if(!this.entity.level.isClientSide)
		{
			if(this.morph != null && this.type != null)
			{
				MorphNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.entity), new UpdateMorphPacket(this.entity, this.type, this.morph.getId(), false));
			}
			else
			{
				MorphNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.entity), new UpdateMorphPacket(this.entity, EntityType.PIG, 0, true));
			}
		}
	}

	@Override
	public void setSelectedGoalId(int selectedGoalId)
	{
		this.selectedGoalId = selectedGoalId;
	}

	@Override
	public int getSelectedGoalId()
	{
		return this.selectedGoalId;
	}

	@Override
	public void setSelectedAnimationId(int selectedAnimationId)
	{
		this.selectedAnimationId = selectedAnimationId;
	}

	@Override
	public int getSelectedAnimationId()
	{
		return this.selectedAnimationId;
	}

	@Override
	public void setGoalOptions(List<String> options)
	{
		this.goalOptions = options;
	}

	@Override
	public List<String> getGoalOptions()
	{
		return this.goalOptions;
	}

	@Override
	public void setAnimationOptions(List<String> options)
	{
		this.animationOptions = options;
	}

	@Override
	public List<String> getAnimationOptions()
	{
		return this.animationOptions;
	}

	@Override
	public void setDataOptions(List<String> options)
	{
		this.dataOptions = options;
	}

	@Override
	public List<String> getDataOptions()
	{
		return this.dataOptions;
	}

	@Override
	public void setOptionsRevision(int revision)
	{
		this.optionsRevision = revision;
	}

	@Override
	public int getOptionsRevision()
	{
		return this.optionsRevision;
	}
	
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) 
	{
		return MORPH.orEmpty(cap, LazyOptional.of(() -> this));
	}
}
