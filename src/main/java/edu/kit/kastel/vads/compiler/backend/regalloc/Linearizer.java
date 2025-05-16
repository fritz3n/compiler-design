package edu.kit.kastel.vads.compiler.backend.regalloc;


import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;

public class Linearizer {

    private final Map<Node, Reference> defineMapping = new HashMap<>();

    int id = 0;

    public List<InstructionInfo> Linearize(IrGraph graph) {
        var instructions = new ArrayList<InstructionInfo>();
        scan(graph.endBlock(), new HashSet<>(), instructions);
        return instructions;
    }
    
    public void scan(Node node, Set<Node> visited, List<InstructionInfo> instructions) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited, instructions);
            }
        }

        if (!needsRegister(node))
            return;

        switch (node) {
            case BinaryOperationNode b -> binary(b, instructions);
            case ReturnNode r -> returnNode(r, instructions);
            case ConstIntNode c -> constNode(c, instructions);
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _,ProjNode _,StartNode _ -> {
                // do nothing, skip line break
            }
        }
    }
    
    private void constNode(ConstIntNode node, List<InstructionInfo> instructions) {
        List<Reference> uses = new ArrayList<>();
        Reference defines = new Reference(id++);
        defineMapping.put(node, defines);
        
        instructions.add(new InstructionInfo(node, defines, uses));
    }

    private void returnNode(ReturnNode node, List<InstructionInfo> instructions) {
        List<Reference> uses = new ArrayList<>();
        uses.add(defineMapping.get(predecessorSkipProj(node, ReturnNode.RESULT)));
        var defines = new Reference(id++, true);

        instructions.add(new InstructionInfo(node, defines, uses));
    }

    private void binary(BinaryOperationNode node, List<InstructionInfo> instructions) {
        List<Reference> uses = new ArrayList<>();
        uses.add(defineMapping.get(predecessorSkipProj(node, BinaryOperationNode.LEFT)));
        uses.add(defineMapping.get(predecessorSkipProj(node, BinaryOperationNode.RIGHT)));

        Reference defines = new Reference(id++);
        defineMapping.put(node, defines);

        instructions.add(new InstructionInfo(node, defines, uses));
    }
    
    private static boolean needsRegister(Node node) {
        return !(node instanceof ProjNode || node instanceof StartNode || node instanceof Block);
    }
}
