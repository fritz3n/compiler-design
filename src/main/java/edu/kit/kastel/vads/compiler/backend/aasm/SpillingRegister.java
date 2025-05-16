package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public record SpillingRegister(int offset) implements Register {
    @Override
    public final String toString() {
        return "-" + offset() + "(%" + AsmRegister.STACK_POINTER + ")";
    }
}
