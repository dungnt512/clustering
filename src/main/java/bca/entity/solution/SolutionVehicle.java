package bca.entity.solution;

import java.util.List;

public interface SolutionVehicle {
    Long getVehicle_id();
    void setVehicle_id(Long vehicle_id);
    List<SolutionCluster> getClusters();
    SolutionVehicle copy();
}
