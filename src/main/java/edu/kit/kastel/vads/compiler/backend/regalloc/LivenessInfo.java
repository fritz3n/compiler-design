package edu.kit.kastel.vads.compiler.backend.regalloc;

import java.util.Set;
import java.util.stream.Collectors;

public record LivenessInfo(InstructionInfo instruction, Set<Reference> liveReferences) {
    @Override
    public final String toString() {
        return instruction.toString() + " live: " + liveReferences.stream().map(r -> r.toString()).collect(Collectors.joining(" "));
    }
}
