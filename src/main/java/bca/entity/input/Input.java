package bca.entity.input;

import java.util.HashMap;
import java.util.List;

public interface Input {
    int getNum_nodes();
    int getNum_edges();
    int getNum_vehicles();
    int getNum_clusters();
    List<Node> getNodes();
    List<Edge> getEdges();
    List<Vehicle> getVehicles();
    Node getNodeById(Long id);
    Edge getEdgeById(Long id);
    List<Long> getNodeIds();
    Vehicle getVehicleById(Long id);
    List<Long> getVehicleIds();
    void setNode_map(HashMap<Long, Node> nodes);
    void setVehicle_map(HashMap<Long, Vehicle> vehicles);
    double getNodeDistance(Long from_node_id, Long to_node_id);
}
