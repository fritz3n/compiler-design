package edu.kit.kastel.vads.compiler.backend.aasm;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.vads.compiler.backend.regalloc.InstructionInfo;
import edu.kit.kastel.vads.compiler.backend.regalloc.Reference;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class SpillHandler {
    private static final AsmRegister[] spillRegisters = new AsmRegister[] {
            new AsmRegister("r15"),
            new AsmRegister("rbx"),
            new AsmRegister("rcx"),
    };

    private final Map<AsmRegister, SpillingRegister> spillMap = new HashMap<>();

    private final Map<Reference, Register> registerMap;

    private final AsmCodeGenerator generator;

    public SpillHandler(Map<Reference, Register> registerMap, AsmCodeGenerator generator) {
        this.registerMap = registerMap;
        this.generator = generator;

    }

    public RegisterInstruction spillForInstruction(InstructionInfo instruction) {
        Set<AsmRegister> blockedRegisters = new HashSet<>();

        AsmRegister defines = spillMaybe(registerMap.get(instruction.defines()), blockedRegisters);
        List<AsmRegister> uses = instruction.uses().stream().map(r -> spillMaybe(registerMap.get(r), blockedRegisters))
                .toList();

        return new RegisterInstruction(instruction.source(), defines, uses);
    }

    private AsmRegister spillMaybe(Register register, Set<AsmRegister> blockedRegisters) {
        if (register instanceof AsmRegister asmRegister)
            return asmRegister;

        if (!(register instanceof SpillingRegister spillRegister))
            throw new InvalidParameterException("can only spill Asm- or SpillingRegisters");

        if (spillMap.containsValue(spillRegister)) {
            AsmRegister usedRegister = spillMap.keySet().stream().filter(r -> spillMap.get(r).equals(spillRegister))
                    .findFirst().get();
            blockedRegisters.add(usedRegister);
            return usedRegister;
        }

        AsmRegister spillToRegister = Arrays.stream(spillRegisters)
                .filter(r -> !blockedRegisters.contains(r)).findFirst().get();

        spillMap.put(spillToRegister, spillRegister);
        generator.emitMove(spillRegister, spillToRegister);
        return spillToRegister;
    }
}
