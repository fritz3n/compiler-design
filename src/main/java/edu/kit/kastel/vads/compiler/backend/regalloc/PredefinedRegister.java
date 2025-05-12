package edu.kit.kastel.vads.compiler.backend.regalloc;

public record PredefinedRegister(String name) implements Register {
    @Override
    public final String toString() {
        return name;
    }
}
