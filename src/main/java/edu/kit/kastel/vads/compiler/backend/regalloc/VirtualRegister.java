package edu.kit.kastel.vads.compiler.backend.regalloc;

public record VirtualRegister(int id) implements Register {
    @Override
    public String toString() {
        return "%" + id();
    }
}
