package bca.algorithm.vlsn.generate;

import bca.entity.input.Input;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class PathValidExtend extends PathBasicExtend {
    public PathValidExtend(Input input) {
        super(input);
    }

    @Override
    public List<List<Long>> extend(List<List<Long>> paths) {
        List<List<Long>> extended_paths = new ArrayList<>();
        HashMap<Long, Boolean> visited = new HashMap<>();
        for (List<Long> path : paths) {
            visited.clear();
            for (Long node_id : path) {
                visited.put(node_id , true);
            }
            Long first_node_id = path.getFirst();
            for (Long node_id : input.getNodeIds()) {
                if (node_id.compareTo(first_node_id) > 0 && !visited.containsKey(node_id)) {
                    List<Long> extended_path = new ArrayList<>(path);
                    extended_path.add(node_id);
                    extended_paths.add(extended_path);
                }
            }
        }
        return extended_paths;
    }
}
