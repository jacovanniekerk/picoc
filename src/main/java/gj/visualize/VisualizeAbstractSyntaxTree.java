package gj.visualize;

import gj.picoc.Node;
import gj.picoc.Parser;
import gj.picoc.Scanner;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.SpringBox;
import org.graphstream.ui.swingViewer.Viewer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class VisualizeAbstractSyntaxTree {

    private static void construct(Node ast, String id, Graph graph) {
        graph.addNode(id);
        if (ast == null) {
            graph.getNode(id).addAttribute("ui.class","empty");
            return;
        }

        if (ast.getType() == Node.NodeType.PROG) {
            graph.getNode(id).addAttribute("ui.class", "root");
        } else if (ast.isLeaf()) {
            graph.getNode(id).addAttribute("ui.class", "external");
        } else {
            graph.getNode(id).addAttribute("ui.class","internal");
        }

        String label = ast.getType().toString();
        if (ast.getValue() != null) {
            label = label + ":" + ast.getValue();
        }
        graph.getNode(id).addAttribute("ui.label", label);
        int index = 2;
        while (index >= 0 && ast.getChildren()[index] == null) index--;
        if (index < 0) return;
        for (int i = 0; i <= index; i++) {
            construct(ast.getChildren()[i], id+""+i, graph);
            graph.addEdge(id+"-"+i, id, id+""+i);
            graph.getEdge(id+"-"+i).setAttribute("ui.label", String.valueOf(i));
        }
    }




    public static void visualize(Node ast) throws IOException, URISyntaxException {
        Path file = Path.of(VisualizeAbstractSyntaxTree.class.getResource("graph.css").toURI());
        String css = Files.readString(file);
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Graph graph = new SingleGraph("AST");
        graph.addAttribute("ui.stylesheet", css);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        construct(ast, "0", graph);
        Viewer viewer = graph.display(false);
        SpringBox layout = new SpringBox(false,new Random(123));
        viewer.enableAutoLayout(layout);


    }


    public static void main(String args[]) throws URISyntaxException, IOException {

        String prog = Files.readString(Path.of("prog1.pc"));
        //System.out.println(prog);


        Scanner scanner = new Scanner(prog);
        Parser parser = new Parser(scanner);
        Node n = parser.program();

        System.out.println(n);

        visualize(n);


    }
}

