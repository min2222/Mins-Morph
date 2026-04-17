package com.min01.morph.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MorphClientState
{
	private static final Map<UUID, Snapshot> SNAPSHOTS = new ConcurrentHashMap<>();

	public static void putSnapshot(UUID ownerUuid, Snapshot snapshot)
	{
		SNAPSHOTS.put(ownerUuid, snapshot);
	}

	public static Snapshot getSnapshot(UUID ownerUuid)
	{
		return SNAPSHOTS.get(ownerUuid);
	}

	public static void removeSnapshot(UUID ownerUuid)
	{
		SNAPSHOTS.remove(ownerUuid);
	}

	public static void clear()
	{
		SNAPSHOTS.clear();
	}

	public static class Snapshot
	{
		private final int revision;
		private final List<String> goals;
		private final List<String> animations;
		private final List<String> datas;
		private final int selectedGoalId;
		private final int selectedAnimationId;

		public Snapshot(int revision, List<String> goals, List<String> animations, List<String> datas, int selectedGoalId, int selectedAnimationId)
		{
			this.revision = revision;
			this.goals = new ArrayList<>(goals);
			this.animations = new ArrayList<>(animations);
			this.datas = new ArrayList<>(datas);
			this.selectedGoalId = selectedGoalId;
			this.selectedAnimationId = selectedAnimationId;
		}

		public int getRevision()
		{
			return this.revision;
		}

		public List<String> getGoals()
		{
			return this.goals;
		}

		public List<String> getAnimations()
		{
			return this.animations;
		}

		public List<String> getDatas()
		{
			return this.datas;
		}

		public int getSelectedGoalId()
		{
			return this.selectedGoalId;
		}

		public int getSelectedAnimationId()
		{
			return this.selectedAnimationId;
		}
	}
}
