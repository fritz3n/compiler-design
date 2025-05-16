package edu.kit.kastel.vads.compiler.backend.regalloc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class InterferenceGraphAlgorithms {
    public static Map<Reference, Set<Reference>> GetInterferenceGraph(List<LivenessInfo> liveness) {
        Map<Reference, Set<Reference>> neighbourMap = new HashMap<>();
        for (int i = 0; i < liveness.size() - 1; i++) {
            var currentInst = liveness.get(i);
            Reference currentRef = currentInst.instruction().defines();
            if (currentRef.isFixed())
                continue;
            neighbourMap.put(currentRef, new HashSet<>());

            var nextInst = liveness.get(i + 1);

            for (Reference reference : nextInst.liveReferences()) {
                if (reference.isFixed())
                    continue;

                if (reference == currentRef)
                    continue;

                neighbourMap.get(currentRef).add(reference);
                neighbourMap.get(reference).add(currentRef);
            }

        }
        return neighbourMap;
    }

    public static List<Reference> GetSEOrdering(Map<Reference, Set<Reference>> interference) {
        var interferenceGraph = new HashMap<>(interference);
        Map<Reference, Integer> weights = new HashMap<>();

        List<Reference> ordering = new ArrayList<>();

        for (var reference : interferenceGraph.keySet()) {
            weights.put(reference, 0);
        }

        while (true) {
            Optional<Reference> maximalReferenceMaybe = weights.keySet().stream()
                    .max(Comparator.comparing(r -> weights.get(r)));
            if (maximalReferenceMaybe.isEmpty())
                return ordering;

            var maximalReference = maximalReferenceMaybe.get();

            ordering.add(maximalReference);

            for (Reference reference : interferenceGraph.keySet()) {
                if (!weights.containsKey(reference) || interferenceGraph.get(maximalReference).contains(reference))
                    continue;
                weights.put(reference, weights.get(reference) + 1);
            }

            weights.remove(maximalReference);
        }
    }

    public static Map<Reference, Integer> ColorGraph(List<Reference> ordering,
            Map<Reference, Set<Reference>> interferenceGraph) {

        Map<Reference, Integer> colors = new HashMap<>();

        for (Reference reference : ordering) {
            int referenceColor = 0;
            for (int i = 0;; i++) {
                boolean used = false;
                for (Reference neighbour : interferenceGraph.get(reference)) {
                    if (colors.containsKey(neighbour) && colors.get(neighbour) == i) {
                        used = true;
                        break;
                    }
                }

                if (!used) {
                    referenceColor = i;
                    break;
                }
            }
            colors.put(reference, referenceColor);
        }

        return colors;
    }
}
