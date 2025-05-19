package edu.kit.kastel.vads.compiler.backend.aasm;

import java.util.List;
import java.util.stream.Collectors;

import edu.kit.kastel.vads.compiler.ir.node.Node;

public record RegisterInstruction(Node source, AsmRegister defines, List<AsmRegister> uses) {
    @Override
    public final String toString() {
        return source.toString() + ": " + defines.toString() + " <- "
                + uses.stream().map(r -> r.toString()).collect(Collectors.joining(" "));
    }
}
