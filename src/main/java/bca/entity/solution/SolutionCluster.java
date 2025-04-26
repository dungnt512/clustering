package bca.entity.solution;

import java.util.List;
import java.util.Set;

public interface SolutionCluster {
    Long getCluster_id();
    void setCluster_id(Long cluster_id);
    Long getCenter_id();
    void setCenter_id(Long center_id);
    Set<Long> getNode_ids();
    void setNode_ids(Set<Long> node_ids);
    SolutionCluster copy();
}
