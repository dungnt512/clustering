package bca.entity.input;

import java.util.HashMap;
import java.util.List;

public interface Input {
    List<Node> getNodes();
    List<Edge> getEdges();
    List<Vehicle> getVehicles();
    Node getNodeById(Long id);
    List<Long> getNodeIds();
    Vehicle getVehicleById(Long id);
    List<Long> getVehicleIds();
    void setNode_map(HashMap<Long, Node> nodes);
    void setVehicle_map(HashMap<Long, Vehicle> vehicles);
}
