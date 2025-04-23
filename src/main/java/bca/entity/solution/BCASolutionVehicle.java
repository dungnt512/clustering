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
public class BCASolutionVehicle implements SolutionVehicle {
    private Long vehicle_id;
    private List<SolutionCluster> clusters;
}
