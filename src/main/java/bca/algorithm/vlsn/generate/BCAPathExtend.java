package bca.algorithm.vlsn.generate;

import bca.entity.input.Input;
import bca.entity.solution.Solution;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BCAPathExtend extends PathBasicExtend implements PathExtend {
    private Solution solution;

    public BCAPathExtend(Input input) {
        super(input);
    }
    public BCAPathExtend(Input input, Solution solution) {
        super(input);
        this.solution = solution;
    }

    @Override
    public List<List<Long>> extend(List<List<Long>> path) {

    }
}
