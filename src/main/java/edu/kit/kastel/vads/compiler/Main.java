package edu.kit.kastel.vads.compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.vads.compiler.backend.aasm.AsmCodeGenerator;
import edu.kit.kastel.vads.compiler.backend.aasm.AsmRegisterAllocator;
import edu.kit.kastel.vads.compiler.backend.regalloc.InstructionInfo;
import edu.kit.kastel.vads.compiler.backend.regalloc.Linearizer;
import edu.kit.kastel.vads.compiler.backend.regalloc.LivenessAnalyzer;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.SsaTranslation;
import edu.kit.kastel.vads.compiler.ir.optimize.LocalValueNumbering;
import edu.kit.kastel.vads.compiler.ir.util.GraphVizPrinter;
import edu.kit.kastel.vads.compiler.ir.util.YCompPrinter;
import edu.kit.kastel.vads.compiler.lexer.Lexer;
import edu.kit.kastel.vads.compiler.parser.ParseException;
import edu.kit.kastel.vads.compiler.parser.Parser;
import edu.kit.kastel.vads.compiler.parser.TokenSource;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.semantic.SemanticAnalysis;
import edu.kit.kastel.vads.compiler.semantic.SemanticException;

public class Main {

    private static final String TEMP_PATH = "temp.s";

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            System.err.println("Invalid arguments: Expected one input file and one output file");
            System.exit(3);
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        ProgramTree program = lexAndParse(input);
        try {
            new SemanticAnalysis(program).analyze();
        } catch (SemanticException e) {
            e.printStackTrace();
            System.exit(7);
            return;
        }
        List<IrGraph> graphs = new ArrayList<>();
        for (FunctionTree function : program.topLevelTrees()) {
            SsaTranslation translation = new SsaTranslation(function, new LocalValueNumbering());
            graphs.add(translation.translate());
        }

        if ("vcg".equals(System.getenv("DUMP_GRAPHS")) || "vcg".equals(System.getProperty("dumpGraphs"))) {
            Path tmp = output.toAbsolutePath().resolveSibling("graphs");
            Files.createDirectory(tmp);
            for (IrGraph graph : graphs) {
                dumpGraph(graph, tmp, "before-codegen");
            }
        }

        boolean foundMain = false;

        for (IrGraph irGraph : graphs) {
            if (!irGraph.name().equals("main"))
                continue;
            foundMain = true;

            System.out.println(GraphVizPrinter.print(irGraph));
            Linearizer linearizer = new Linearizer();
            var linear = linearizer.Linearize(irGraph);

            System.out.println("Linear:");
            for (InstructionInfo inst : linear) {
                System.out.println(inst.toString());
            }

            System.out.println("Liveness:");
            var live = new LivenessAnalyzer().GetLiveness(linear);
            for (var liveness : live) {
                System.out.println(liveness.toString());
            }

            AsmRegisterAllocator allocator = new AsmRegisterAllocator();
            var registerMap = allocator.allocateRegisters(linear);

            System.out.println("Assembly:");
            var asmGenerator = new AsmCodeGenerator();
            String assembly = asmGenerator.generateCode(linear, registerMap);

            System.out.print(assembly);
            Files.writeString(Paths.get(TEMP_PATH), assembly);

            ProcessBuilder builder = new ProcessBuilder("gcc", "-ggdb", TEMP_PATH, "-o", output.toString());
            builder.inheritIO();
            builder.start().waitFor();
        }
        if (!foundMain) {
            System.exit(42);
        }

    }

    private static ProgramTree lexAndParse(Path input) throws IOException {
        try {
            Lexer lexer = Lexer.forString(Files.readString(input));
            TokenSource tokenSource = new TokenSource(lexer);
            Parser parser = new Parser(tokenSource);
            return parser.parseProgram();
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(42);
            throw new AssertionError("unreachable");
        }
    }

    private static void dumpGraph(IrGraph graph, Path path, String key) throws IOException {
        Files.writeString(
                path.resolve(graph.name() + "-" + key + ".vcg"),
                YCompPrinter.print(graph));
    }
}
