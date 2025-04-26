package bca.entity.solution;

import bca.entity.input.Input;

import bca.entity.input.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        for (Vehicle vehicle : input.getVehicles()) {
            SolutionVehicle solutionVehicle = new BCASolutionVehicle(vehicle.getVehicle_id());
            this.vehicles.add(solutionVehicle);
            this.vehicle_map.put(vehicle.getVehicle_id(), solutionVehicle);
        }
        this.cluster_ids = new HashMap<>();
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
}
