package com.min01.morph.misc;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;

public interface ITrackedEntity
{
	public void updatePlayersCustom(List<ServerPlayer> players);
}
