package bca.entity.input;

import bca.entity.data.Pair;
import bca.process.EuclidianDistance;
import bca.process.IdMapping;
import bca.process.Kattio;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.*;

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

    private double customer_average;
    private double demand_average;
    private double area_average;
    private double load_average;

    private List<Vehicle> vehicles;
    private List<BCAFamiliarity> familiarities;

    private List<Long> node_ids;
    private List<Long> vehicle_ids;

    private HashMap<Long, Node> node_map;
    private HashMap<Long, Vehicle> vehicle_map;
    private HashMap<Long, HashMap<Long, Double>> distances;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("input : {\n");
        sb.append("\tnum_nodes: ").append(num_nodes).append(",\n");
        sb.append("\tnum_edges: ").append(num_edges).append(",\n");
        sb.append("\tnodes: [");
        for (Node node : nodes) {
            sb.append(node.getNode_id()).append(", ");
        }
        sb.deleteCharAt(sb.length() - 2);
        sb.append("],\n");
        if (edges != null) {
            sb.append("\tedges: [\n");
            for (Edge edge : edges) {
                sb.append("\t\t{").append(edge.getEdge_id()).append(", ")
                        .append(edge.getFrom_node_id()).append(", ")
                        .append(edge.getTo_node_id()).append(", ")
                        .append(edge.getDistance()).append("},\n");
            }
            sb.delete(sb.length() - 2, sb.length() - 1);
            sb.append("\t],\n");
        }
        else {
            sb.append("\tedges: []\n");
        }

        sb.append("\tnum_vehicles: ").append(num_vehicles).append(",\n");
        sb.append("\tnum_clusters: ").append(num_clusters).append(",\n");
        sb.append("\tcustomer_bias: ").append(customer_bias).append(",\n");
        sb.append("\tdemand_bias: ").append(demand_bias).append(",\n");
        sb.append("\tarea_bias: ").append(area_bias).append(",\n");
        sb.append("\tload_bias: ").append(load_bias).append(",\n");

        sb.append("\tcustomer_average: ").append(customer_average).append(",\n");
        sb.append("\tdemand_average: ").append(demand_average).append(",\n");
        sb.append("\tarea_average: ").append(area_average).append(",\n");
        sb.append("\tload_average: ").append(load_average).append(",\n");

        if (vehicles != null) {
            sb.append("\tvehicles: [\n");
            for (Vehicle vehicle : vehicles) {
                sb.append("\t\t{").append(vehicle.getVehicle_id()).append(", ");
                sb.append("familiarities: [");
                for (BCAFamiliarity familiarity : ((BCAVehicle) vehicle).getFamiliarities()) {
                    sb.append(familiarity.getFamiliarity()).append(", ");
                }
                sb.delete(sb.length() - 2, sb.length() - 1);
                sb.append("]},\n");
            }
            if (!vehicles.isEmpty()) {
                sb.delete(sb.length() - 2, sb.length() - 1);
            }
            sb.append("\t],\n");
        }
        else {
            sb.append("\tvehicles: []\n");
        }
        sb.append("}");

        return sb.toString();
    }


    public BCAInput(String input_file, boolean flag) throws IOException {
        Kattio kattio = new Kattio(input_file);
        num_nodes = kattio.nextInt();
        nodes = new ArrayList<>();

        customer_average = 0;
        demand_average = 0;
        area_average = 0;
        load_average = 0;
        for (int i = 0; i < num_nodes; i++) {
            BCANode node = new BCANode(kattio.nextLong());
            node.setLatitude(kattio.nextDouble());
            node.setLongitude(kattio.nextDouble());
            node.setExpected_customers(kattio.nextDouble());
            node.setExpected_demands(kattio.nextDouble());
            node.setArea(kattio.nextDouble());
            node.setLoad(kattio.nextDouble());
            customer_average += node.getExpected_customers();
            demand_average += node.getExpected_demands();
            area_average += node.getArea();
            load_average += node.getLoad();

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
        customer_average /= num_clusters;
        demand_average /= num_clusters;
        area_average /= num_clusters;
        load_average /= num_clusters;


        customer_bias = kattio.nextDouble();
        demand_bias = kattio.nextDouble();
        area_bias = kattio.nextDouble();
        load_bias = kattio.nextDouble();
        vehicles = new ArrayList<>();
        familiarities = new ArrayList<>();
        for (int i = 0; i < num_vehicles; i++) {
            BCAVehicle vehicle = new BCAVehicle((long)i);
            for (int j = 0; j < num_nodes; j++) {
                BCAFamiliarity familiarity = new BCAFamiliarity();
                BCANode node = (BCANode)nodes.get(j);
                familiarity.setNode_id(node.getNode_id());
                familiarity.setVehicle_id(vehicle.getVehicle_id());
                familiarity.setFamiliarity(kattio.nextInt());
                familiarities.add(familiarity);
                vehicle.getFamiliarities().add(familiarity);
                node.getFamiliarities().add(familiarity);
            }
            vehicles.add(vehicle);
        }

        IdMapping.process(this);
        EuclidianDistance.process(this);

        node_ids = node_map.keySet().stream().toList();
        vehicle_ids = vehicle_map.keySet().stream().toList();

        for (Edge edge : edges) {
            Node from_node = getNodeById(edge.getFrom_node_id());
            Node to_node = getNodeById(edge.getTo_node_id());
            from_node.getAdjacent_edges().add(edge.getEdge_id());
            to_node.getAdjacent_edges().add(edge.getEdge_id());
        }

        if (flag) {
            if ((long) ((num_edges + num_nodes) * Math.log(num_nodes) / Math.log(2)) < (long) num_nodes * num_nodes * num_nodes) {
                Dijkstra();
            } else {
                Floyd();
            }
        }
        kattio.close();
    }

    private void initDistance() {
        distances = new HashMap<>();
        for (Node node : nodes) {
            distances.put(node.getNode_id(), new HashMap<>());
            for (Node other_node : nodes) {
                if (node.getNode_id().equals(other_node.getNode_id())) {
                    distances.get(node.getNode_id()).put(other_node.getNode_id(), 0.0);
                } else {
                    distances.get(node.getNode_id()).put(other_node.getNode_id(), Double.MAX_VALUE);
                }
            }
        }
    }

    private void Floyd() {
        initDistance();
        for (Edge edge: edges) {
            Node from_node = getNodeById(edge.getFrom_node_id());
            Node to_node = getNodeById(edge.getTo_node_id());
            distances.get(from_node.getNode_id()).put(to_node.getNode_id(), edge.getDistance());
        }

        for (Long k: node_map.keySet()) {
            for (Long i: node_map.keySet()) {
                for (Long j: node_map.keySet()) {
                    if (distances.get(i).get(k) != Integer.MAX_VALUE && distances.get(k).get(j) != Integer.MAX_VALUE) {
                        distances.get(i).put(j, Math.min(distances.get(i).get(j), distances.get(i).get(k) + distances.get(k).get(j)));
                    }
                }
            }
        }
    }

    private void Dijkstra() {
        initDistance();
        PriorityQueue<Pair<Double, Long>> pq = new PriorityQueue<>(Comparator.comparingDouble(i -> i.a));
        for (Node start : nodes) {
            pq.add(new Pair<>(0.0, start.getNode_id()));
            HashMap<Long, Double> distance = distances.getOrDefault(start.getNode_id(), null);
            if (distance == null) {
                System.err.println("Distance map is null for node: " + start.getNode_id());
                return ;
            }
            while (!pq.isEmpty()) {
                Pair<Double, Long> pair = pq.poll();
                Long from_node_id = pair.b;
                Node from_node = getNodeById(from_node_id);
                Double fromNodeDistance = pair.a;
                if (Double.compare(fromNodeDistance, distance.get(from_node_id)) > 0) {
                    continue;
                }

                for (Long edge_id : from_node.getAdjacent_edges()) {
                    Edge edge = getEdgeById(edge_id);
                    Long to_node_id = edge.getRemaining(from_node_id);
                    double newDistance = fromNodeDistance + edge.getDistance();
                    if (Double.compare(newDistance, distance.get(to_node_id)) < 0) {
                        distance.put(to_node_id, newDistance);
                        pq.add(new Pair<>(newDistance, to_node_id));
                    }
                }
            }
        }
    }

    public double getNodeDistance(Long from_node_id, Long to_node_id) {
        if (distances == null || !distances.containsKey(from_node_id)) {
            return Double.MAX_VALUE;
        }
        return distances.get(from_node_id).getOrDefault(to_node_id, Double.MAX_VALUE);
    }


    public Node getNodeById(Long node_id) {
        if (node_map == null) {
            System.err.println("Node map is null");
            return null;
        }
        if (!node_map.containsKey(node_id)) {
            System.err.println("Node map does not contain node id: " + node_id);
            return null;
        }
        return node_map.get(node_id);
    }
    public Vehicle getVehicleById(Long vehicle_id) {
        return vehicle_map.get(vehicle_id);
    }
    public List<Long> getNodeIds() {
//        return node_map.keySet().stream().toList();
        return node_ids;
    }
    public List<Long> getVehicleIds() {
//        return vehicle_map.keySet().stream().toList();
        return vehicle_ids;
    }
    public Edge getEdgeById(Long edge_id) {
        return edges.get(edge_id.intValue());
    }

}
