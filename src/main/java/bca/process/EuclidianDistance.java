package bca.process;

import bca.entity.input.*;

public class EuclidianDistance {
    public static double euclideanDistance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    public static void process(Input input) {
        for (Edge edge : input.getEdges()) {
            Node from_node = input.getNodeById(edge.getFrom_node_id());
            Node to_node = input.getNodeById(edge.getTo_node_id());

            if (from_node != null && to_node != null) {
                double distance = euclideanDistance(((BCANode)from_node).getLatitude(), ((BCANode)from_node).getLongitude(),
                        ((BCANode)to_node).getLatitude(), ((BCANode)to_node).getLongitude());
                edge.setDistance(distance);
            } else {
                System.err.println("Edge references non-existent nodes: " + edge +
                        " from_node: " + from_node + " to_node: " + to_node);
            }
        }
    }
}
