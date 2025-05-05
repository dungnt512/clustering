package bca.entity.solution;

import java.util.List;

public interface SolutionObjective {
    double getValue();
    @SuppressWarnings("UnusedReturnValue")
    double calculate();
    double update(Solution solution);
    double updateCluster(Solution solution, Long vehicle_id, List<Long> inserted, List<Long> removed);
    double updateCluster(Solution solution, List<Long> vehicle_ids, List<Long> path);
    SolutionObjective copy();
}
