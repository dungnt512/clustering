package bca.algorithm.vlsn.generate;

import java.util.List;

public interface PathExtend {
    List<List<Long>> extend(List<List<Long>> path, List<Double> deltas);
}
