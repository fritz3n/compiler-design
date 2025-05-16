package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public record AsmRegister(String name) implements Register {

    public static String STACK_POINTER = "rsp";

    @Override
    public final String toString() {
        return "%" + name();
    }
}
