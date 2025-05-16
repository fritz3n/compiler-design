package edu.kit.kastel.vads.compiler.backend.regalloc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LivenessAnalyzer {
    public static List<LivenessInfo> GetLiveness(List<InstructionInfo> instructions) {
        var list = new ArrayList<LivenessInfo>();
        var set = new HashSet<Reference>();
        for (InstructionInfo instruction : instructions.reversed()) {
            updateSet(set, instruction);
            @SuppressWarnings("unchecked")
            var copy = (Set<Reference>)set.clone();
            list.add(new LivenessInfo(instruction, copy));
        }
        return list.reversed();
    }
    
    private static void updateSet(HashSet<Reference> set, InstructionInfo instruction) {
        set.remove(instruction.defines());
        for (Reference register : instruction.uses()) {
            set.add(register);
        }
    }
}
