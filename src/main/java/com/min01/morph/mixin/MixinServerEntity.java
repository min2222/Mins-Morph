package com.min01.morph.mixin;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.min01.morph.util.MorphUtil;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@Mixin(ServerEntity.class)
public class MixinServerEntity
{
    @Shadow
    @Final
    private Entity entity;
    
    @Redirect(method = "sendPairingData", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    private void warn(Logger instance, String string, Object arg)
    {
		if(MorphUtil.isMorph(this.entity))
		{
			
		}
		else
		{
            instance.warn(string, arg);
		}
    }
    
    @Redirect(method = "sendPairingData", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0))
    private <T> void accept(Consumer<T> instance, T packet)
    {
		if(MorphUtil.isMorph(this.entity))
		{
			
		}
		else
		{
            instance.accept(packet);
		}
    }
    
    @Redirect(method = "addPairing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;startSeenByPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void startSeenByPlayer(Entity instance, ServerPlayer player)
    {
		if(MorphUtil.isMorph(this.entity))
		{
			
		}
		else
		{
            instance.startSeenByPlayer(player);
		}
    }
}
