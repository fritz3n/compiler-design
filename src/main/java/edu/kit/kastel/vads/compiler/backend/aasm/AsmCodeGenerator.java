package edu.kit.kastel.vads.compiler.backend.aasm;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.vads.compiler.backend.regalloc.InstructionInfo;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;

public class AsmCodeGenerator {

    private static final String EAX = "%eax";
    private static final String EDX = "%edx";
    private static final String ASM_PREFIX = """
            .global main\r
            .global _main\r
            .text\r
            main:\r
            call _main\r
            # move the return value into the first argument for the syscall\r
            movq %rax, %rdi\r
            # move the exit syscall number into rax\r
            movq $0x3C, %rax\r
            syscall\r
            _main:\r
            # your generated code here\r""";

    private final List<AsmInstruction> asmInstructions = new ArrayList<>();

    public String generateCode(List<InstructionInfo> instructions,
            Map<edu.kit.kastel.vads.compiler.backend.regalloc.Reference, Register> registerMap) {
        SpillHandler spillHandler = new SpillHandler(registerMap, this);

        for (InstructionInfo instructionInfo : instructions) {
            emitComment(instructionInfo.toString());
            RegisterInstruction instruction = spillHandler.spillForInstruction(instructionInfo);

            emitComment(instruction.toString());

            switch (instruction.source()) {
                case AddNode add -> handleAddition(instruction);
                case SubNode _ -> handleSubtraction(instruction);
                case MulNode mul -> handleMultiply(instruction, mul);
                case DivNode div -> handleDividingBinary(instruction, div);
                case ModNode mod -> handleDividingBinary(instruction, mod);
                case ReturnNode r -> handleReturn(instruction, r);
                case ConstIntNode c -> handleConst(instruction, c);
                case Phi _ -> throw new UnsupportedOperationException("phi");
                case Block _,ProjNode _,StartNode _ -> {
                    // do nothing, skip line break
                }
            }

            emitComment("--");
        }
        StringBuilder builder = new StringBuilder();

        for (AsmInstruction asmInstruction : asmInstructions) {
            asmInstruction.emit(builder);
            builder.append("\n");
        }

        return ASM_PREFIX + builder.toString();
    }

    private void handleSubtraction(RegisterInstruction instruction) {
        emit(AsmType.MOV, instruction.uses().getFirst().toString(), EAX);
        emit(AsmType.MOV, instruction.uses().getLast().toString(), EDX);
        emit(AsmType.SUB, EDX, EAX);
        emit(AsmType.MOV, EAX, instruction.defines().toString());
    }

    private void handleAddition(RegisterInstruction instruction) {
        if (instruction.defines().equals(instruction.uses().getFirst()))
            emit(AsmType.ADD, instruction.uses().getLast().toString(), instruction.defines().toString());
        else if (instruction.defines().equals(instruction.uses().getLast()))
            emit(AsmType.ADD, instruction.uses().getFirst().toString(), instruction.defines().toString());
        else {
            emit(AsmType.MOV, instruction.uses().getLast().toString(), instruction.defines().toString());
            emit(AsmType.ADD, instruction.uses().getFirst().toString(), instruction.defines().toString());
        }
    }

    private void handleDividingBinary(RegisterInstruction instruction, BinaryOperationNode node) {
        emit(AsmType.MOV, instruction.uses().getFirst().toString(), EAX);
        emit(AsmType.CLTD);
        emit(AsmType.IDIV, instruction.uses().getLast().toString());

        if (node instanceof DivNode) {
            emit(AsmType.MOV, EAX, instruction.defines().toString());
        } else {
            emit(AsmType.MOV, EDX, instruction.defines().toString());
        }
    }

    private void handleMultiply(RegisterInstruction instruction, BinaryOperationNode node) {
        emit(AsmType.MOV, instruction.uses().getFirst().toString(), EAX);
        emit(AsmType.XOR, EDX, EDX);
        emit(AsmType.MUL, instruction.uses().getLast().toString());
        emit(AsmType.MOV, EAX, instruction.defines().toString());
    }

    private void handleReturn(RegisterInstruction instruction, ReturnNode node) {
        emit(AsmType.MOV, instruction.uses().getFirst().toString(), EAX);
        emit(AsmType.RET);
    }

    private void handleConst(RegisterInstruction instruction, ConstIntNode node) {
        emit(AsmType.MOV, "$" + node.value(), instruction.defines().toString());
    }

    public void emit(AsmType type, String... operands) {
        var stackTrace = Thread.currentThread().getStackTrace();
        String stackTraceString = String.join(" ",
                Arrays.stream(stackTrace).skip(2).map(st -> st.getMethodName()).toList());
        asmInstructions.add(new AsmInstruction(type, Arrays.asList(operands), stackTraceString));
    }

    public void emitMove(Register from, Register to) {
        emit(AsmType.MOV, from.toString(), to.toString());
    }

    public void emitComment(String comment) {
        asmInstructions.add(new AsmInstruction(AsmType.COMMENT, new ArrayList<String>(), comment));
    }
}
