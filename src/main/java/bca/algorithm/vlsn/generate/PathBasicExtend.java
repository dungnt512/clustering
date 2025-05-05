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

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<List<Long>> extend(List<List<Long>> paths, List<Double> deltas) {
        List<List<Long>> extended_paths = new ArrayList<>();
        HashMap<Long, Boolean> visited = new HashMap<>();

        for (List<Long> path : paths) {
            visited.clear();
            for (Long node_id: path) {
                visited.put(node_id, true);
            }

            for (Long node_id : input.getNodeIds()) {
                if (!visited.containsKey(node_id)) {
                    List<Long> extended_path = new ArrayList<>(path);
                    extended_path.add(node_id);
                    extended_paths.add(extended_path);
                }
            }
        }
        return extended_paths;
    }

}
