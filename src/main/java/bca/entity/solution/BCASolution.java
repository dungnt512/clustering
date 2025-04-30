package bca.entity.solution;

import bca.entity.input.Input;

import bca.entity.input.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
                append(objective.getValue()).append("\n").
                append("\t\tvariance : ").append(((BCASolutionObjective) objective).getVariance_total()).append("\n").
                append("\t\tfamiliarity : ").append(((BCASolutionObjective) objective).getFamiliarity_total()).append("\n").
                append("\t\tdistance : ").append(((BCASolutionObjective) objective).getDistance_total()).append("\n");
        str.append("\t}\n");

        str.append("\tvehicles : [\n");
        for (SolutionVehicle vehicle : vehicles) {
            str.append("\t\t{\n");
            str.append("\t\t\tvehicle_id : ").append(vehicle.getVehicle_id()).append("\n");
            str.append("\t\t\tclusters : [\n");
            for (SolutionCluster cluster : vehicle.getClusters()) {
                str.append("\t\t\t\t{\n");
                str.append("\t\t\t\t\tcluster_id : ").append(cluster.getCluster_id()).append("\n");
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
