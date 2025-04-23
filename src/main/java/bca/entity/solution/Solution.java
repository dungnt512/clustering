package bca.entity.solution;

import java.util.List;

public interface Solution {
    SolutionObjective getObjective();
    List<SolutionVehicle> getVehicles();
}
