package edu.kit.kastel.vads.compiler.backend.aasm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.vads.compiler.backend.regalloc.InstructionInfo;
import edu.kit.kastel.vads.compiler.backend.regalloc.InterferenceGraphAlgorithms;
import edu.kit.kastel.vads.compiler.backend.regalloc.LivenessAnalyzer;
import edu.kit.kastel.vads.compiler.backend.regalloc.Reference;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.node.Node;

public class AsmRegisterAllocator {
    private static final AsmRegister[] registers = new AsmRegister[] {
            new AsmRegister("r8"),
            new AsmRegister("r9"),
            new AsmRegister("r10"),
            new AsmRegister("r11"),
            new AsmRegister("r12"),
            new AsmRegister("r13"),
            new AsmRegister("r14"),
            new AsmRegister("r15"),
            new AsmRegister("rcx"),
            new AsmRegister("rdx"),
    };

    public Map<Node, Register> allocateRegisters(List<InstructionInfo> instructions) {
        Map<Node, Register> registerMapping = new HashMap<>();
        var liveness = LivenessAnalyzer.GetLiveness(instructions);
        var interferenceGraph = InterferenceGraphAlgorithms.GetInterferenceGraph(liveness);
        var ordering = InterferenceGraphAlgorithms.GetSEOrdering(interferenceGraph);
        var colors = InterferenceGraphAlgorithms.ColorGraph(ordering, interferenceGraph);

        for (InstructionInfo instruction : instructions) {
            Reference defines = instruction.defines();
            if (defines.isFixed())
                continue;
            Integer color = colors.get(defines);
            Register register = getRegisterForColor(color);
            registerMapping.put(instruction.source(), register);
        }

        return registerMapping;
    }

    private Register getRegisterForColor(int color) {
        if (color < registers.length) {
            return registers[color];
        } else {
            var spillingRegister = new SpillingRegister(color - registers.length + 1);
            return spillingRegister;
        }
    }

}
