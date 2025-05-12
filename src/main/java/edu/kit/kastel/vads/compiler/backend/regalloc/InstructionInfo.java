package edu.kit.kastel.vads.compiler.backend.regalloc;

import java.util.List;
import java.util.stream.Collectors;

import edu.kit.kastel.vads.compiler.ir.node.Node;

public record InstructionInfo(Node source, Register defines, List<Register> uses) {
    
    @Override
    public final String toString() {
        return source.toString() + ": " + defines.toString() + " <- " + uses.stream().map(r -> r.toString()).collect(Collectors.joining(" "));
    }
}
