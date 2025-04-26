package bca.algorithm.vlsn.operator;

import bca.algorithm.vlsn.generate.PathExtend;
import bca.algorithm.vlsn.generate.PathValidExtend;
import bca.entity.input.Input;
import bca.entity.solution.Solution;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class KExchangeSearch {
    private final int K;
    private final Input input;
    private PathExtend pathExtend;

    public KExchangeSearch(int K, Input input) {
        this.K = K;
        this.input = input;
        this.pathExtend = new PathValidExtend(input);
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

        List<Long> exchange = new ArrayList<>();
        double weight_delta = 0;
        while (!pathSet.isEmpty() && k < K && Double.compare(weight_delta, 0.0) >= 0) {
            k++;
            List<List<Long>> extendedPaths = pathExtend.extend(pathSet);
            pathSet = extendedPaths;
        }

        return null;
    }
}
