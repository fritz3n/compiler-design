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
            new AsmRegister("r15d"),
            new AsmRegister("ebx"),
            new AsmRegister("ecx"),
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

        AsmRegister defines = spillMaybe(instruction.defines(), blockedRegisters);
        List<AsmRegister> uses = instruction.uses().stream().map(r -> spillMaybe(r, blockedRegisters))
                .toList();

        return new RegisterInstruction(instruction.source(), defines, uses);
    }

    private AsmRegister spillMaybe(Reference reference, Set<AsmRegister> blockedRegisters) {
        Register register = registerMap.get(reference);

        if (register instanceof AsmRegister asmRegister) {
            generator.emitComment(reference.toString() + " is AsmRegister " + register.toString());
            return asmRegister;
        }

        if (!(register instanceof SpillingRegister spillRegister))
            throw new InvalidParameterException("can only spill Asm- or SpillingRegisters");

        if (spillMap.containsValue(spillRegister)) {
            AsmRegister usedRegister = spillMap.keySet().stream().filter(r -> spillMap.get(r).equals(spillRegister))
                    .findFirst().get();
            blockedRegisters.add(usedRegister);

            generator.emitComment(reference.toString() + " with offset " + (spillRegister.offset() * 4)
                    + " is already spilled into " + usedRegister.toString());
            return usedRegister;
        }

        AsmRegister spillToRegister = Arrays.stream(spillRegisters)
                .filter(r -> !blockedRegisters.contains(r)).findFirst().get();

        generator.emitComment(reference.toString() + " with offset " + (spillRegister.offset() * 4)
                + " is being spilled into " + spillToRegister.toString());

        if (spillMap.containsKey(spillToRegister)) {
            generator.emitComment(spillToRegister + " needs to save " + spillMap.get(spillToRegister));
            generator.emitMove(spillToRegister, spillMap.get(spillToRegister));
        }

        spillMap.put(spillToRegister, spillRegister);
        generator.emitMove(spillRegister, spillToRegister);
        blockedRegisters.add(spillToRegister);
        return spillToRegister;
    }
}
