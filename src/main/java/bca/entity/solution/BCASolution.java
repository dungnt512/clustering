package bca.entity.solution;

import bca.entity.input.Input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BCASolution implements Solution {
    private Input input;
    private SolutionObjective objective;
    private List<SolutionVehicle> vehicles;
}
