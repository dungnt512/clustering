package bca.entity.solution;

import java.util.List;

public interface Solution {
    SolutionObjective getObjective();
    List<SolutionVehicle> getVehicles();
    Solution copy();
    SolutionVehicle getSolutionVehicleById(Long vehicle_id);
    SolutionCluster getSolutionClusterById(Long cluster_id);
    boolean isValid();
    boolean isConnected(Long cluster_id);
    void findBestCenter();
}
