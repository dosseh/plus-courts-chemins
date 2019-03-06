
import org.graphstream.algorithm.*;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static org.graphstream.algorithm.Dijkstra.Element.EDGE;

public class Main {


    public static void main(String args[]) {

        Graph graph = new SingleGraph("Le Havre");

        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        graph.addAttribute("ui.stylesheet", "url('data/jolie.css')");

        try {
            graph.read("data/lh.dgs");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (GraphParseException e) {
            e.printStackTrace();
        }


        System.out.println("Nombre de sommets = " + graph.getNodeCount());
        System.out.println("Nombre d'arètes = " + graph.getEdgeCount());
        System.out.println("-------- Distribution de dégrés --------");
        System.out.println(degreDeDistribution(graph));
        System.out.println("degré moyen = " + Toolkit.averageDegree(graph));


        System.out.println("coefficient de clustering : " + Toolkit.averageClusteringCoefficient(graph));


       // diametre_Dijkstra(graph);
       // diametre_FloydWarshall(graph);
        betweenness_centrality(graph);
         //centre(graph);
        //dikstraPlusCourt_chemin(graph, randomNode(graph), randomNode(graph));
        graph.display(false);

    }

    public static Node randomNode(Graph graph) {
        return Toolkit.randomNode(graph);
    }

    public static String degreDeDistribution(Graph graph) {
        String recuperer = "";
        int n = graph.getNodeCount();
        int[] dd = Toolkit.degreeDistribution(graph);

        for (int i = 0; i < dd.length; i++) {
            if (dd[i] > 0) {
                recuperer += " \n" + i + "     " + (double) dd[i] / n + " \n";
            }

        }

        return recuperer;
    }

    public static void dikstraPlusCourt_chemin(Graph graph, Node sommet1, Node sommet2) {

        Dijkstra dijkstra = new Dijkstra(EDGE, "result", "length");

        dijkstra.init(graph);
        dijkstra.setSource(graph.getNode(String.valueOf(sommet1)));
        dijkstra.compute();
        //System.out.println(dijkstra.getPath(graph.getNode(String.valueOf(sommet2))));

        System.out.printf("%s->%s:%6.2f%n", dijkstra.getSource(), sommet2, dijkstra.getPathLength(sommet2));
        for (Edge edge : dijkstra.getPathEdges(sommet2)) edge.addAttribute("ui.style", "fill-color: red;");


    }

    public static void diametre_Dijkstra(Graph graph) {

        long startTime = System.currentTimeMillis();

        ArrayList<Double> stockLongueur = new ArrayList<>();
        Dijkstra dijkstra = new Dijkstra(EDGE, "result", "length");
        dijkstra.init(graph);
        Node source = null;
        Node destination = null;
        double longSet = 0;
        double j = 0;

        for (Node n : graph.getEachNode()) {
            dijkstra.setSource(n);
            dijkstra.compute();


            for (Node n2 : graph.getEachNode()) {
                double longActu = dijkstra.getPathLength(n2);
                if (longActu > longSet) {
                    longSet = longActu;
                    source = n;
                    destination = n2;
                }
                stockLongueur.add(dijkstra.getPathLength(n2));

            }
            // determination de distance moyenne
        }

        System.out.println("\nDiametre de dijkstra: " + longSet);

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Le temps d'execution de dijkstra : " + elapsedTime);

        dijkstra.setSource(source);
        dijkstra.compute();
        for (Node node : dijkstra.getPathNodes(destination)) node.addAttribute("ui.style", "fill-color: blue;");
        for (Edge edge : dijkstra.getPathEdges(destination)) edge.addAttribute("ui.style", "fill-color: blue;");


        for (int k = 0; k < stockLongueur.size(); k++) {
            j += stockLongueur.get(k) / stockLongueur.size();
        }
        System.out.println("La distance moyenne :" + j);


    }


    public static void diametre_FloydWarshall(Graph g) {
        long startTime = System.currentTimeMillis();

        Path path = null;
        double longSet = 0;

        APSP apsp = new APSP();
        apsp.init(g);
        apsp.setDirected(false);
        apsp.setWeightAttributeName("length");
        apsp.compute();

        for (Node n : g.getEachNode()) {
            APSPInfo info = n.getAttribute(APSPInfo.ATTRIBUTE_NAME);

            for (Node n2 : g.getEachNode()) {
                Path p = info.getShortestPathTo(n2.getId());
                double longActu = p.getPathWeight("length");

                if (longActu > longSet) {
                    longSet = longActu;
                    path = p;
                }
            }
        }

        System.out.println("\nDiametre de Floyd Warshall: " + longSet);

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Le temps d'execution de Floyd Warshall : " + elapsedTime);

        for (Node node : path.getNodePath())
            node.addAttribute("ui.style", "fill-color: blue;");

        for (Edge edge : path.getEdgePath())
            edge.addAttribute("ui.style", "fill-color: blue;");

    }


    public static void centre(Graph graph) {
        APSP apsp = new APSP();
        apsp.init(graph);
        apsp.compute();

        Eccentricity eccentricity = new Eccentricity();
        eccentricity.init(graph);
        eccentricity.compute();

        for (Node n : graph.getEachNode()) {
            Boolean in = n.getAttribute("eccentricity");

            if (in) {
                System.out.printf("%s est en excentricité.\n", n.getId());
                n.setAttribute("ui.style", "fill-color: red;size: 13px;");


            }

        }
    }

    public static void betweenness_centrality(Graph graph) {

        ArrayList<Double> arrayList = new ArrayList<>();

        BetweennessCentrality bcb = new BetweennessCentrality();
        bcb.setWeightAttributeName("weight");
        bcb.init(graph);
        bcb.compute();

        // Le  gradient de couleur va du bleu (Intermediarité le moins chargé) vers le rouge (intermediarité le plus chargée)

        graph.addAttribute("ui.stylesheet", "edge {" +
                "fill-mode: dyn-plain; fill-color:#4137aa," +
                " #99089e, #d4007c, #f7004c, #ff0000;size : 3px;" +
                "}" +
                "node {\n" +
                "        size: 5px;\n" +
                "        fill-color: #777;\n" +
                "        z-index: 0;\n" +
                "    }");

        for (Node n : graph.getEachNode()) {
            Iterator<? extends Node> k = n.getNeighborNodeIterator();
            while (k.hasNext()) {
                Node next = k.next();
                Edge edge = n.getEdgeBetween(next);
                edge.addAttribute("ui.color", (((Double) edge.getAttribute("Cb") * 0.00001)));
                 // System.out.println(((Double) edge.getAttribute("Cb") * 0.00001));

                arrayList.add((Double) edge.getAttribute("Cb"));

            }
        }
    }

}






