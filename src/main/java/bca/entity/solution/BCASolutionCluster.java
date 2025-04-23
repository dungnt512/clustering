package bca.entity.solution;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BCASolutionCluster implements SolutionCluster {
    private Long cluster_id;
    private List<Long> node_ids;
}
