package bca.algorithm.vlsn.generate;

import bca.entity.input.Input;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class PathBasicExtend implements PathExtend {
    protected final Input input;
    public PathBasicExtend(Input input) {
        this.input = input;
    }

    @Override
    public List<List<Long>> extend(List<List<Long>> paths) {
        List<List<Long>> extendedPaths = new ArrayList<>();
        HashMap<Long, Boolean> visited = new HashMap<>();
        for (List<Long> path : paths) {
            visited.clear();
            for (Long nodeId : path) {
                visited.put(nodeId, true);
            }

            for (Long nodeId : input.getNodeIds()) {
                if (!visited.containsKey(nodeId)) {
                    List<Long> extendedPath = new ArrayList<>(path);
                    extendedPath.add(nodeId);
                    extendedPaths.add(extendedPath);
                }
            }
        }
        return extendedPaths;
    }
}
