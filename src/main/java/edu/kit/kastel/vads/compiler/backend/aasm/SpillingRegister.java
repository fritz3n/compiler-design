package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public record SpillingRegister(int offset) implements Register {
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + offset;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpillingRegister other = (SpillingRegister) obj;
        if (offset != other.offset)
            return false;
        return true;
    }

    @Override
    public final String toString() {
        return "-" + (offset() * 4) + "(%" + AsmRegister.STACK_POINTER + ")";
    }
}
