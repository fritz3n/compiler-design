package edu.kit.kastel.vads.compiler.backend.regalloc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LivenessAnalyzer {
    public static List<LivenessInfo> GetLiveness(List<InstructionInfo> instructions) {
        var list = new ArrayList<LivenessInfo>();
        var set = new HashSet<Register>();
        for (InstructionInfo instruction : instructions.reversed()) {
            updateSet(set, instruction);
            @SuppressWarnings("unchecked")
            var copy = (Set<Register>)set.clone();
            list.add(new LivenessInfo(instruction, copy));
        }
        return list.reversed();
    }
    
    private static void updateSet(HashSet<Register> set, InstructionInfo instruction) {
        set.remove(instruction.defines());
        for (Register register : instruction.uses()) {
            set.add(register);
        }
    }
}
