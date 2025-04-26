package bca.entity.solution;

import java.util.List;

public interface Solution {
    SolutionObjective getObjective();
    List<SolutionVehicle> getVehicles();
    Solution copy();
    SolutionVehicle getSolutionVehicleById(Long vehicle_id);
    SolutionCluster getSolutionClusterById(Long cluster_id);
}
