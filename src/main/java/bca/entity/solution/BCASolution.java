package bca.entity.solution;

import bca.entity.input.Edge;
import bca.entity.input.Input;

import bca.entity.input.Node;
import bca.entity.input.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BCASolution implements Solution {
    private Input input;
    private SolutionObjective objective;
    private List<SolutionVehicle> vehicles;
    private HashMap<Long, SolutionVehicle> vehicle_map;
    private HashMap<Long, SolutionCluster> cluster_ids;

    public BCASolution(Input input) {
        this.input = input;
        this.objective = new BCASolutionObjective(input, 1000, 500, 1);
        this.vehicles = new ArrayList<>();
        this.vehicle_map = new HashMap<>();
        for (Vehicle vehicle : input.getVehicles()) {
            SolutionVehicle solutionVehicle = new BCASolutionVehicle(vehicle.getVehicle_id());
            this.vehicles.add(solutionVehicle);
            this.vehicle_map.put(vehicle.getVehicle_id(), solutionVehicle);
        }
        this.cluster_ids = new HashMap<>();
        for (SolutionVehicle vehicle : this.vehicles) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                this.cluster_ids.put(cluster.getCluster_id(), cluster);
            }
        }
    }

    private void DFS(Long node_id, HashMap<Long, Integer> visited) {
        Node node = input.getNodeById(node_id);
        visited.put(node_id, 2);
        for (Long edge_id : node.getAdjacent_edges()) {
            Edge edge = input.getEdgeById(edge_id);
            Long next_node = edge.getRemaining(node.getNode_id());
            int visit = visited.getOrDefault(next_node, 0);
            if (visit == 1) {
                DFS(next_node, visited);
            }
        }
    }

    private void BFS(Long node_id, HashMap<Long, Integer> visited) {
        Queue<Long> queue = new LinkedList<>();
        queue.add(node_id);
        visited.put(node_id, 2);
        while (!queue.isEmpty()) {
            Long current_node = queue.poll();
            Node node = input.getNodeById(current_node);
            for (Long edge_id : node.getAdjacent_edges()) {
                Edge edge = input.getEdgeById(edge_id);
                Long next_node = edge.getRemaining(node.getNode_id());
                int visit = visited.getOrDefault(next_node, 0);
                if (visit == 1) {
                    visited.put(next_node, 2);
                    queue.add(next_node);
                }
            }
        }
    }

    public boolean isValid() {
        int num_nodes = input.getNum_nodes();
        HashMap<Long, Integer> visited = new HashMap<>();
        for (SolutionVehicle vehicle : vehicles) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                for (Long node_id : cluster.getNode_ids()) {
                    if (visited.containsKey(node_id)) {
                        return false;
                    }
                }

                for (Long node_id : cluster.getNode_ids()) {
                    BFS(node_id, visited);
                    break;
                }

                for (Long node_id : cluster.getNode_ids()) {
                    if (!visited.containsKey(node_id)) {
                        return false;
                    }
                }
                num_nodes -= visited.size();
                if (num_nodes < 0) {
                    return false;
                }
            }
        }
        return num_nodes == 0;
    }

    public boolean isConnected(Long cluster_id) {
        return isConnected(getSolutionClusterById(cluster_id));
    }
    public boolean isConnected(SolutionCluster cluster) {
        if (cluster == null) {
            return false;
        }
        HashMap<Long, Integer> visited = new HashMap<>();
        for (Long node_id : cluster.getNode_ids()) {
            visited.put(node_id, 1);
        }
        BFS(cluster.getCenter_id(), visited);
        for (Long node_id : cluster.getNode_ids()) {
            int visit = visited.getOrDefault(node_id, 0);
            if (visit != 2) {
                return false;
            }
        }
        return true;
    }

    public Solution copy() {
        BCASolution clone = new BCASolution();
        clone.input = this.input;
        clone.objective = this.objective.copy();
        clone.vehicles = this.vehicles.stream().map(SolutionVehicle::copy).toList();
        clone.vehicle_map = new HashMap<>();
        for (SolutionVehicle vehicle : this.vehicles) {
            clone.vehicle_map.put(vehicle.getVehicle_id(), vehicle.copy());
        }
        clone.cluster_ids = new HashMap<>();
        for (SolutionVehicle vehicle : this.vehicles) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                clone.cluster_ids.put(cluster.getCluster_id(), cluster);
            }
        }
        return clone;
    }

    public SolutionVehicle getSolutionVehicleById(Long vehicle_id) {
        return vehicle_map.get(vehicle_id);
    }
    public SolutionCluster getSolutionClusterById(Long cluster_id) {
        return cluster_ids.get(cluster_id);
    }

    public String toString() {
        StringBuilder str = new StringBuilder("solution : {\n");
        str.append("\tobjective : {\n" + "\t\tvalue : ").
                append(objective.getValue()).append(",\n").
                append("\t\tvariance : ").append(((BCASolutionObjective) objective).getVariance_total()).append(",\n").
                append("\t\tfamiliarity : ").append(((BCASolutionObjective) objective).getFamiliarity_total()).append(",\n").
                append("\t\tdistance : ").append(((BCASolutionObjective) objective).getDistance_total()).append(",\n");
        str.append("\t},\n");

        str.append("\tvehicles : [\n");
        for (SolutionVehicle vehicle : vehicles) {
            str.append("\t\t{\n");
            str.append("\t\t\tvehicle_id : ").append(vehicle.getVehicle_id()).append(",\n");
            str.append("\t\t\tclusters : [\n");
            for (SolutionCluster cluster : vehicle.getClusters()) {
                str.append("\t\t\t\t{\n");
                str.append("\t\t\t\t\tcluster_id : ").append(cluster.getCluster_id()).append(",\n");
                str.append("\t\t\t\t\tcenter_id : ").append(cluster.getCenter_id()).append(",\n");
                str.append("\t\t\t\t\tnode_ids : [");
                for (Long node_id : cluster.getNode_ids()) {
                    str.append(node_id).append(", ");
                }
                if (!cluster.getNode_ids().isEmpty())
                    str.delete(str.length() - 2, str.length());
                str.append("]\n");
                str.append("\t\t\t\t}\n");
            }
            str.append("\t\t\t]\n");
            str.append("\t\t}\n");
        }
        str.append("\t]\n");
        str.append("}\n");
        return str.toString();
    }
}
