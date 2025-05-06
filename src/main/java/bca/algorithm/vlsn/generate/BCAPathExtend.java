package bca.algorithm.vlsn.generate;

import bca.entity.input.Input;
import bca.entity.solution.Solution;
import bca.entity.solution.SolutionCluster;
import bca.entity.solution.SolutionVehicle;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class BCAPathExtend extends PathBasicExtend implements PathExtend {
    private Map<Long, Long> node_cluster_map;
    private Solution solution;

    public BCAPathExtend(Input input) {
        super(input);
    }
    public BCAPathExtend(Input input, Solution solution) {
        super(input);
        this.solution = solution;
        node_cluster_map = new HashMap<>();
        for (SolutionVehicle vehicle : solution.getVehicles()) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                for (Long nodeId : cluster.getNode_ids()) {
                    node_cluster_map.put(nodeId, cluster.getCluster_id());
                }
            }
        }
    }


    public BCAPathExtend(Input input, Solution solution, Map<Long, Long> node_cluster_map) {
        super(input);
        this.solution = solution;
        this.node_cluster_map = node_cluster_map;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<List<Long>> extend(List<List<Long>> paths, List<Double> deltas) {
        List<List<Long>> extendedPaths = new ArrayList<>();
        HashMap<Long, Boolean> visited = new HashMap<>();
//        int i = 0;
//        List<Double> old_deltas = new ArrayList<>(deltas);
//        deltas.clear();
        for (List<Long> path : paths) {
            visited.clear();
            for (Long nodeId : path) {
                visited.put(nodeId, true);
            }
            Long firstClusterId = node_cluster_map.getOrDefault(path.getFirst(), null);
            SolutionCluster firstCluster = solution.getSolutionClusterById(firstClusterId);
            Long lastClusterId = node_cluster_map.getOrDefault(path.getLast(), null);
            for (Long nodeId : input.getNodeIds()) {
                Long nextClusterId = node_cluster_map.getOrDefault(nodeId, null);
                if (nodeId.compareTo(path.getFirst()) > 0 && lastClusterId.longValue() != nextClusterId.longValue() && !visited.containsKey(nodeId)) {
                    SolutionCluster cluster = solution.getSolutionClusterById(nextClusterId);

                    if (nextClusterId.longValue() != firstClusterId.longValue()) {
                        cluster.getNode_ids().remove(nodeId);
                        cluster.getNode_ids().add(path.getLast());
                        firstCluster.getNode_ids().remove(path.getFirst());
                        firstCluster.getNode_ids().add(nodeId);
                    }

                    if (nextClusterId.longValue() == firstClusterId.longValue() || solution.isConnected(nextClusterId) && solution.isConnected(firstClusterId)) {
                        List<Long> extendedPath = new ArrayList<>(path);
                        extendedPath.add(nodeId);
                        extendedPaths.add(extendedPath);
//                        deltas.add(old_deltas.get(i));
                    }

                    if (firstClusterId.longValue() != nextClusterId.longValue()) {
                        cluster.getNode_ids().add(nodeId);
                        cluster.getNode_ids().remove(path.getLast());
                        firstCluster.getNode_ids().add(path.getFirst());
                        firstCluster.getNode_ids().remove(nodeId);
                    }
                }
            }
//            i++;
        }
        return extendedPaths;
    }

}
