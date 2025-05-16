package edu.kit.kastel.vads.compiler.backend.regalloc;

public record Reference(int id, boolean isFixed) {
 
    public Reference(int id) {
        this(id, false);
    }
    @Override
    public final String toString() {
        return "#" + id() + (isFixed() ? "!" : "");
    }
}
