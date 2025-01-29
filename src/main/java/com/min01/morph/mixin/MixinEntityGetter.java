package com.min01.morph.mixin;

import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;

import com.min01.morph.event.EventHandlerForge;
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
		Entity entity1 = null;
		Entity entity2 = null;
		Class<?>[] ctx = manager.getContext();
		int i = 0;
		int i2 = 0;
		do
		{
			entity1 = EventHandlerForge.ENTITY_MAP.get(ctx[i].hashCode());
			i++;
		}
		while(entity1 == null && i < ctx.length);
		do
		{
			entity2 = EventHandlerForge.ENTITY_MAP2.get(ctx[i2].hashCode());
			i2++;
		}
		while(entity2 == null && i2 < ctx.length);
		Entity entity = entity1 != null ? entity1 : entity2;
		if(entity != null)
		{
			if(MorphUtil.getMorphOwner(entity) != null)
			{
				list.removeIf(t -> t == MorphUtil.getMorphOwner(entity));
			}
		}
		return list;
	}
	
	@Override
	default List<Entity> getEntities(Entity p_45934_, AABB p_45935_)
	{
		List<Entity> list = this.getEntities(p_45934_, p_45935_, EntitySelector.NO_SPECTATORS);
		MySecurityManager manager = new MySecurityManager();
		Entity entity1 = null;
		Entity entity2 = null;
		Class<?>[] ctx = manager.getContext();
		int i = 0;
		int i2 = 0;
		do
		{
			entity1 = EventHandlerForge.ENTITY_MAP.get(ctx[i].hashCode());
			i++;
		}
		while(entity1 == null && i < ctx.length);
		do
		{
			entity2 = EventHandlerForge.ENTITY_MAP2.get(ctx[i2].hashCode());
			i2++;
		}
		while(entity2 == null && i2 < ctx.length);
		Entity entity = entity1 != null ? entity1 : entity2;
		if(entity != null)
		{
			if(MorphUtil.getMorphOwner(entity) != null)
			{
				list.removeIf(t -> t == MorphUtil.getMorphOwner(entity));
			}
		}
		return list;
	}
	
	@SuppressWarnings({ "removal"})
	static class MySecurityManager extends SecurityManager
	{
		public Class<?>[] getContext()
		{
			return this.getClassContext();
		}
	}
}
