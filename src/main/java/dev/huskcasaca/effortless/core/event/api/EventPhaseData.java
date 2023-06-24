package dev.huskcasaca.effortless.core.event.api;

import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class EventPhaseData<T> {
	final ResourceLocation id;
	final List<EventPhaseData<T>> subsequentPhases = new ArrayList<>();
	final List<EventPhaseData<T>> previousPhases = new ArrayList<>();
	T[] listeners;
	int visitStatus = 0; // 0: not visited, 1: visiting, 2: visited

	@SuppressWarnings("unchecked")
	EventPhaseData(ResourceLocation id, Class<?> listenerClass) {
		this.id = id;
		this.listeners = (T[]) Array.newInstance(listenerClass, 0);
	}

	void addListener(T listener) {
		int oldLength = listeners.length;
		listeners = Arrays.copyOf(listeners, oldLength + 1);
		listeners[oldLength] = listener;
	}
}
