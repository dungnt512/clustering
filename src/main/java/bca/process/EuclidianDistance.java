package bca.process;

import bca.entity.input.*;

public class EuclidianDistance {
    public static double euclideanDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public static void process(Input input) {
        for (Edge edge : input.getEdges()) {
            Node fromNode = input.getNodeById(edge.getFrom_node_id());
            Node toNode = input.getNodeById(edge.getTo_node_id());

            if (fromNode != null && toNode != null) {
                double distance = euclideanDistance(((BCANode)fromNode).getLatitude(), ((BCANode)fromNode).getLongitude(),
                        ((BCANode)toNode).getLatitude(), ((BCANode)toNode).getLongitude());
                edge.setDistance(distance);
            } else {
                System.err.println("Edge references non-existent nodes: " + edge +
                        " fromNode: " + fromNode + " toNode: " + toNode);
            }
        }
    }
}
