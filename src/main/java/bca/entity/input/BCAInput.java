package bca.entity.input;

import bca.process.EuclidianDistance;
import bca.process.IdMapping;
import bca.process.Kattio;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BCAInput implements Input {
    private int num_nodes;
    private int num_edges;
    private List<Node> nodes;
    private List<Edge> edges;
    private int num_vehicles;
    private int num_clusters;
    private double customer_bias;
    private double demand_bias;
    private double area_bias;
    private double load_bias;
    private List<Vehicle> vehicles;
    private List<BCAFamiliarity> familiarities;

    private HashMap<Long, Node> node_map;
    private HashMap<Long, Vehicle> vehicle_map;

    @SuppressWarnings("resource")
    public BCAInput(String input_file) throws IOException {
        Kattio kattio = new Kattio(input_file);
        num_nodes = kattio.nextInt();
        nodes = new ArrayList<>();
        for (int i = 0; i < num_nodes; i++) {
            BCANode node = new BCANode(kattio.nextLong());
            node.setLatitude(kattio.nextDouble());
            node.setLongitude(kattio.nextDouble());
            node.setExpected_customers(kattio.nextDouble());
            node.setExpected_demands(kattio.nextDouble());
            node.setArea(kattio.nextDouble());
            node.setLoad(kattio.nextDouble());
            nodes.add(node);
        }

        num_edges = kattio.nextInt();
        edges = new ArrayList<>();
        for (int i = 0; i < num_edges; i++) {
            BCAEdge edge = new BCAEdge();
            edge.setEdge_id((long)i);
            edge.setFrom_node_id(kattio.nextLong());
            edge.setTo_node_id(kattio.nextLong());
            edges.add(edge);
        }

        num_vehicles = kattio.nextInt();
        num_clusters = kattio.nextInt();
        customer_bias = kattio.nextDouble();
        demand_bias = kattio.nextDouble();
        area_bias = kattio.nextDouble();
        load_bias = kattio.nextDouble();
        vehicles = new ArrayList<>();
        familiarities = new ArrayList<>();
        for (int i = 0; i < num_vehicles; i++) {
            BCAVehicle BCAVehicle = new BCAVehicle((long)i);
            for (int j = 0; j < num_nodes; j++) {
                BCAFamiliarity familiarity = new BCAFamiliarity();
                BCANode node = (BCANode)nodes.get(j);
                familiarity.setNode_id(node.getNode_id());
                familiarity.setVehicle_id(BCAVehicle.getVehicle_id());
                familiarity.setFamiliarity(kattio.nextInt());
                familiarities.add(familiarity);
                BCAVehicle.getFamiliarities().add(familiarity);
                node.getFamiliarities().add(familiarity);
            }
            vehicles.add(BCAVehicle);
        }

        IdMapping.process(this);
        EuclidianDistance.process(this);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Input: ").append(num_nodes).append(" nodes, ").append(num_edges).append(" edges, ")
                .append(num_vehicles).append(" vehicles, ").append(num_clusters).append(" clusters\n");
        sb.append("Nodes:\n");
        for (Node node: nodes) {
            sb.append(node).append("\n");
        }
        sb.append("Edges:\n");
        for (Edge edge: edges) {
            sb.append(edge).append("\n");
        }
        return sb.toString();
    }

    public Node getNodeById(Long node_id) {
        return node_map.get(node_id);
    }
    public Vehicle getVehicleById(Long vehicle_id) {
        return vehicle_map.get(vehicle_id);
    }
    public List<Long> getNodeIds() {
        return node_map.keySet().stream().toList();
    }
    public List<Long> getVehicleIds() {
        return vehicle_map.keySet().stream().toList();
    }
}
