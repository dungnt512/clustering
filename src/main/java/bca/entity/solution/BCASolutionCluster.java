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
public class BCASolutionCluster implements SolutionCluster {
    private Long cluster_id;
    private Long center_id;
    private Set<Long> node_ids;

    public BCASolutionCluster(Long cluster_id) {
        this.cluster_id = cluster_id;
        this.node_ids = new HashSet<>();
    }
    public BCASolutionCluster(Long cluster_id, Long center_id, Set<Long> node_ids) {
        this.cluster_id = cluster_id;
        this.center_id = center_id;
        this.node_ids = node_ids;
    }

    public SolutionCluster copy() {
        BCASolutionCluster clone = new BCASolutionCluster();
        clone.cluster_id = this.cluster_id;
        clone.center_id = this.center_id;
        clone.node_ids = new HashSet<>(this.node_ids);
        return clone;
    }
}
