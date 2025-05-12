package edu.kit.kastel.vads.compiler.backend.regalloc;

import java.util.Set;
import java.util.stream.Collectors;

public record LivenessInfo(InstructionInfo instruction, Set<Register> liveRegisters) {
    @Override
    public final String toString() {
        return instruction.toString() + " live: " + liveRegisters.stream().map(r -> r.toString()).collect(Collectors.joining(" "));
    }
}
