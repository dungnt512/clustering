package bca.algorithm.vlsn.search;

import bca.entity.input.Input;
import bca.entity.solution.Solution;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class KExchangeSearch {
    final int K;
    public KExchangeSearch(int K) {
        this.K = K;
    }

    public Solution search(Input input) {
        int k = 1;
        List<Long> nodeIds = input.getNodeIds();
        List<List<Long>> pathSet = new ArrayList<>();
        for (Long nodeId : nodeIds) {
            List<Long> path = new ArrayList<>();
            path.add(nodeId);
            pathSet.add(path);
        }

        while (pathSet.size() > 0 && k < K) {
            k++;

        }

        return null;
    }
}
