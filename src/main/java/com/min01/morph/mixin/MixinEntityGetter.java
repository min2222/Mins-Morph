package com.min01.morph.mixin;

import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;

import com.min01.morph.util.MorphUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

@Mixin(EntityGetter.class)
public interface MixinEntityGetter extends EntityGetter
{
	@Override
	default <T extends Entity> List<T> getEntitiesOfClass(Class<T> p_45979_, AABB p_45980_, Predicate<? super T> p_45981_)
	{
		List<T> list = this.getEntities(EntityTypeTest.forClass(p_45979_), p_45980_, p_45981_);
		MySecurityManager manager = new MySecurityManager();
		Class<?>[] ctx = manager.getContext();
		for(Class<?> clazz : ctx)
		{
			if(MorphUtil.ENTITY_MAP.containsKey(clazz.hashCode()))
			{
				Entity entity = MorphUtil.ENTITY_MAP.get(clazz.hashCode());
				if(entity != null)
				{
					if(MorphUtil.getMorphOwner(entity) != null)
					{
						list.removeIf(t -> t == MorphUtil.getMorphOwner(entity));
					}
				}
			}
			else if(MorphUtil.ENTITY_MAP2.containsKey(clazz.hashCode()))
			{
				Entity entity = MorphUtil.ENTITY_MAP2.get(clazz.hashCode());
				if(entity != null)
				{
					if(MorphUtil.getMorphOwner(entity) != null)
					{
						list.removeIf(t -> t == MorphUtil.getMorphOwner(entity));
					}
				}
			}
		}
		return list;
	}
	
	@Override
	default List<Entity> getEntities(Entity p_45934_, AABB p_45935_)
	{
		List<Entity> list = this.getEntities(p_45934_, p_45935_, EntitySelector.NO_SPECTATORS);
		MySecurityManager manager = new MySecurityManager();
		Class<?>[] ctx = manager.getContext();
		for(Class<?> clazz : ctx)
		{
			if(MorphUtil.ENTITY_MAP.containsKey(clazz.hashCode()))
			{
				Entity entity = MorphUtil.ENTITY_MAP.get(clazz.hashCode());
				if(entity != null)
				{
					if(MorphUtil.getMorphOwner(entity) != null)
					{
						list.removeIf(t -> t == MorphUtil.getMorphOwner(entity));
					}
				}
			}
			else if(MorphUtil.ENTITY_MAP2.containsKey(clazz.hashCode()))
			{
				Entity entity = MorphUtil.ENTITY_MAP2.get(clazz.hashCode());
				if(entity != null)
				{
					if(MorphUtil.getMorphOwner(entity) != null)
					{
						list.removeIf(t -> t == MorphUtil.getMorphOwner(entity));
					}
				}
			}
		}
		return list;
	}

	@SuppressWarnings("removal")
	static class MySecurityManager extends SecurityManager
	{
		public Class<?>[] getContext()
		{
			return this.getClassContext();
		}
	}
}
