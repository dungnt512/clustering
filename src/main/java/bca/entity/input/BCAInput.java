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

    @SuppressWarnings("resource")
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

        customer_average /= num_clusters;
        demand_average /= num_clusters;
        area_average /= num_clusters;
        load_average /= num_clusters;

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

        if (flag) {
            if ((long) ((num_edges + num_nodes) * Math.log(num_nodes) / Math.log(2)) < (long) num_nodes * num_nodes * num_nodes) {
                Dijkstra();
            } else {
                Floyd();
            }
        }
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
