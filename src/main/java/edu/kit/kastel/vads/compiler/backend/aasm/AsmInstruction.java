package edu.kit.kastel.vads.compiler.backend.aasm;

import java.util.List;

public record AsmInstruction(AsmType type, List<String> operands, String comment) {

    public void emit(StringBuilder builder) {
        if (type != AsmType.COMMENT) {
            builder.append(type.toString().toLowerCase()).append(" ").append(String.join(", ", operands)).append("\t");
        }
        builder.append("# ").append(comment);
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        emit(builder);
        return builder.toString();
    }
}
