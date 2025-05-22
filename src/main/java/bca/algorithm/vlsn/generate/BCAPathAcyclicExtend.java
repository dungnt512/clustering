package bca.algorithm.vlsn.generate;

import bca.entity.input.Input;
import bca.entity.solution.Solution;
import bca.entity.solution.SolutionCluster;
import bca.entity.solution.SolutionVehicle;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BCAPathAcyclicExtend extends PathBasicExtend implements PathExtend {
    private Map<Long, Long> node_cluster_map;
    private Solution solution;

    public BCAPathAcyclicExtend(Input input) {
        super(input);
    }
    public BCAPathAcyclicExtend(Input input, Solution solution) {
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


    public BCAPathAcyclicExtend(Input input, Solution solution, Map<Long, Long> node_cluster_map) {
        super(input);
        this.solution = solution;
        this.node_cluster_map = node_cluster_map;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<List<Long>> extend(List<List<Long>> paths, List<Double> deltas) {
        List<List<Long>> extendedPaths = new ArrayList<>();
        Map<Long, Boolean> visited = new HashMap<>();

        for (List<Long> path : paths) {
            visited.clear();
            for (Long nodeId : path) {
                visited.put(nodeId, Boolean.TRUE);
            }

            Long lastNodeId = path.getLast();
            Long lastClusterId = node_cluster_map.getOrDefault(lastNodeId, null );
            SolutionCluster lastCluster = solution.getSolutionClusterById(lastClusterId);
            for (Long nodeId : input.getNodeIds()) {
                Long nextClusterId = node_cluster_map.getOrDefault(nodeId, null);
                if (lastClusterId.longValue() != nextClusterId.longValue() && !visited.containsKey(nodeId)) {
                    SolutionCluster cluster = solution.getSolutionClusterById(nextClusterId);
                    lastCluster.getNode_ids().remove(lastNodeId);
                    cluster.getNode_ids().add(lastNodeId);
                    if (solution.isConnected(nextClusterId) && solution.isConnected(lastClusterId)) {
                        List<Long> extendedPath = new ArrayList<>(path);
                        extendedPath.add(nodeId);
                        extendedPaths.add(extendedPath);
                    }
                    else {
                        List<Long> extendedPath = new ArrayList<>(path);
                        extendedPath.add(nodeId);
                        extendedPath.add(-1L);
                        extendedPaths.add(extendedPath);
                    }
                    cluster.getNode_ids().remove(lastNodeId);
                    lastCluster.getNode_ids().add(lastNodeId);
                }
            }
        }
        return extendedPaths;
    }

}
