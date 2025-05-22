package bca.algorithm.vlsn.operator;

import bca.algorithm.vlsn.generate.BCAPathAcyclicExtend;
import bca.algorithm.vlsn.generate.BCAPathExtend;
import bca.algorithm.vlsn.generate.PathExtend;
import bca.entity.input.Input;
import bca.entity.solution.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class KExchangeAcyclicSearch {
    private final int K;
    private final Input input;
    private Solution solution;
    private PathExtend pathExtend;
    private Map<Long, Long> node_cluster_map;

    public KExchangeAcyclicSearch(int K, Input input, Solution solution) {
        this.K = K;
        this.input = input;
        this.solution = solution;
        node_cluster_map = new HashMap<>();
        for (SolutionVehicle vehicle : solution.getVehicles()) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                for (Long nodeId : cluster.getNode_ids()) {
                    node_cluster_map.put(nodeId, cluster.getCluster_id());
                }
            }
        }
        this.pathExtend = new BCAPathAcyclicExtend(input, solution, node_cluster_map);
    }
    public KExchangeAcyclicSearch(int K, Input input, Solution solution, Map<Long, Long> node_cluster_map) {
        this.K = K;
        this.input = input;
        this.solution = solution;
        this.node_cluster_map = node_cluster_map;

        this.pathExtend = new BCAPathAcyclicExtend(input, solution, node_cluster_map);
    }

    public List<Long> search() {
        int k = 1;
        int num_node = input.getNum_nodes();
        List<Long> nodeIds = input.getNodeIds();
        List<List<Long>> pathSet = new ArrayList<>();
        for (Long nodeId : nodeIds) {
            List<Long> path = new ArrayList<>();
            path.add(nodeId);
            pathSet.add(path);
        }

        List<Long> exchange = null;
        double weight_delta = 0;
        while (!pathSet.isEmpty() && k < K && Double.compare(weight_delta, 0.0) >= 0) {
            k++;
            List<List<Long>> extendedPaths = pathExtend.extend(pathSet, null);
            List<Double> deltas = new ArrayList<>();
            System.err.println("Iteration " + k + " extendedAcyclicPaths size: " + extendedPaths.size() + "\n");
            {
                for (List<Long> path : extendedPaths) {
                    if (path.getLast() == -1) {
                        deltas.add(Double.POSITIVE_INFINITY);
                        path.removeLast();
                        continue;
                    }
                    List<Long> cluster_id = new ArrayList<>();
                    for (Long nodeId : path) {
                        cluster_id.add(node_cluster_map.getOrDefault(nodeId, null));
                    }
                    double delta = ((BCASolutionObjective)solution.getObjective()).updateClusterAcyclic(solution, cluster_id, path);
                    deltas.add(delta);
                    if (Double.compare(delta, weight_delta) < 0) {
                        weight_delta = delta;
                        exchange = path;
                    }
                }
            }

            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < extendedPaths.size(); i++) {
                indices.add(i);
            }
            indices.sort(Comparator.comparingDouble(deltas::get));
            if (indices.size() > num_node * 10) {
                if (indices.size() > num_node * 15) {
                    indices.subList(num_node * 15, indices.size()).clear();
                }
                Collections.shuffle(indices);
                indices.subList(num_node * 10, indices.size()).clear();
            }
//            if (indices.size() > num_node * num_node) {
//                indices.subList(num_node * num_node, indices.size()).clear();
//            }
            pathSet.clear();
            for (int index : indices) {
                pathSet.add(extendedPaths.get(index));
            }

        }
        System.err.println(weight_delta);
        return exchange;
    }

    public void move(List<Long> path) {
        for (int i = 1; i < path.size(); i++) {
            Long node_id = path.get(i);
            Long prev_node_id = path.get(i - 1);
            Long cluster_id = node_cluster_map.getOrDefault(node_id, null);
            Long prev_cluster_id = node_cluster_map.getOrDefault(prev_node_id, null);
            SolutionCluster cluster = solution.getSolutionClusterById(cluster_id);
            SolutionCluster prev_cluster = solution.getSolutionClusterById(prev_cluster_id);
            cluster.getNode_ids().add(prev_node_id);
            prev_cluster.getNode_ids().remove(prev_node_id);
            node_cluster_map.put(prev_node_id, cluster_id);
        }

        solution.findBestCenter();
        solution.getObjective().update(solution);
    }
}
