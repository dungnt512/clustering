package bca.entity.solution;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BCASolutionVehicle implements SolutionVehicle {
    private Long vehicle_id;
    private List<SolutionCluster> clusters;

    public BCASolutionVehicle(Long vehicle_id) {
        this.vehicle_id = vehicle_id;
        clusters = new ArrayList<>();
        clusters.add(new BCASolutionCluster(vehicle_id));
    }

    public SolutionVehicle copy() {
        BCASolutionVehicle clone = new BCASolutionVehicle();
        clone.vehicle_id = this.vehicle_id;
        clone.clusters = new ArrayList<>();
        for (SolutionCluster cluster : this.clusters) {
            clone.clusters.add(cluster.copy());
        }
        return clone;
    }
}
