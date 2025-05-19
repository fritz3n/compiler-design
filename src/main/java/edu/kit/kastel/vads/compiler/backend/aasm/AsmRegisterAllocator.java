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
            new AsmRegister("r8d"),
            new AsmRegister("r9d"),
            new AsmRegister("r10d"),
            new AsmRegister("r11d"),
            new AsmRegister("r12d"),
            new AsmRegister("r13d"),
            new AsmRegister("r14d"),
    };

    public Map<Reference, Register> allocateRegisters(List<InstructionInfo> instructions) {
        Map<Reference, Register> registerMapping = new HashMap<>();
        var liveness = LivenessAnalyzer.GetLiveness(instructions);
        var interferenceGraph = InterferenceGraphAlgorithms.GetInterferenceGraph(liveness);
        var ordering = InterferenceGraphAlgorithms.GetSEOrdering(interferenceGraph);
        var colors = InterferenceGraphAlgorithms.ColorGraph(ordering, interferenceGraph);

        for (InstructionInfo instruction : instructions) {
            Reference defines = instruction.defines();
            if (defines.isFixed()) {
                registerMapping.put(defines, new AsmRegister(""));
                continue;
            }
            Integer color = colors.get(defines);
            Register register = getRegisterForColor(color);
            registerMapping.put(instruction.defines(), register);
        }

        return registerMapping;
    }

    public static Map<Node, Register> getNodeMap(List<InstructionInfo> instructions,
            Map<Reference, Register> registerMap) {
        Map<Node, Register> nodeMap = new HashMap<>();
        for (InstructionInfo instruction : instructions) {
            var register = registerMap.get(instruction.defines());
            nodeMap.put(instruction.source(), register);
        }

        return nodeMap;
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
