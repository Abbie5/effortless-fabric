package dev.huskcasaca.effortless.core.event.api;

import java.util.*;

public class PhaseSorting {

	public static boolean ENABLE_CYCLE_WARNING = true;

	static <T> void sortPhases(List<EventPhaseData<T>> sortedPhases) {
		var topoSort = new ArrayList<EventPhaseData<T>>(sortedPhases.size());

		for (var phase : sortedPhases) {
			forwardVisit(phase, null, topoSort);
		}

		clearStatus(topoSort);
		Collections.reverse(topoSort);

		var phaseToScc = new IdentityHashMap<EventPhaseData<T>, PhaseScc<T>>();

		for (var phase : topoSort) {
			if (phase.visitStatus == 0) {
				var sccPhases = new ArrayList<EventPhaseData<T>>();
				backwardVisit(phase, sccPhases);
				sccPhases.sort(Comparator.comparing(p -> p.id));
				var scc = new PhaseScc<>(sccPhases);

				for (var phaseInScc : sccPhases) {
					phaseToScc.put(phaseInScc, scc);
				}
			}
		}

		clearStatus(topoSort);

		for (var scc : phaseToScc.values()) {
			for (var phase : scc.phases) {
				for (var subsequentPhase : phase.subsequentPhases) {
					var subsequentScc = phaseToScc.get(subsequentPhase);

					if (subsequentScc != scc) {
						scc.subsequentSccs.add(subsequentScc);
						subsequentScc.inDegree++;
					}
				}
			}
		}

		var pq = new PriorityQueue<PhaseScc<T>>(Comparator.comparing(scc -> scc.phases.get(0).id));
		sortedPhases.clear();

		for (var scc : phaseToScc.values()) {
			if (scc.inDegree == 0) {
				pq.add(scc);
				scc.inDegree = -1;
			}
		}

		while (!pq.isEmpty()) {
			var scc = pq.poll();
			sortedPhases.addAll(scc.phases);

			for (var subsequentScc : scc.subsequentSccs) {
				subsequentScc.inDegree--;

				if (subsequentScc.inDegree == 0) {
					pq.add(subsequentScc);
				}
			}
		}
	}

	private static <T> void forwardVisit(EventPhaseData<T> phase, EventPhaseData<T> parent, List<EventPhaseData<T>> toposort) {
		if (phase.visitStatus == 0) {
			// Not yet visited.
			phase.visitStatus = 1;

			for (var data : phase.subsequentPhases) {
				forwardVisit(data, phase, toposort);
			}

			toposort.add(phase);
			phase.visitStatus = 2;
		} else if (phase.visitStatus == 1 && ENABLE_CYCLE_WARNING) {
		}
	}

	private static <T> void clearStatus(List<EventPhaseData<T>> phases) {
		for (var phase : phases) {
			phase.visitStatus = 0;
		}
	}

	private static <T> void backwardVisit(EventPhaseData<T> phase, List<EventPhaseData<T>> sccPhases) {
		if (phase.visitStatus == 0) {
			phase.visitStatus = 1;
			sccPhases.add(phase);

			for (var data : phase.previousPhases) {
				backwardVisit(data, sccPhases);
			}
		}
	}

	private static class PhaseScc<T> {
		final List<EventPhaseData<T>> phases;
		final List<PhaseScc<T>> subsequentSccs = new ArrayList<>();
		int inDegree = 0;

		private PhaseScc(List<EventPhaseData<T>> phases) {
			this.phases = phases;
		}
	}
}
