package bca.algorithm.vlsn.operator;

import bca.algorithm.vlsn.generate.BCAPathExtend;
import bca.algorithm.vlsn.generate.PathBasicExtend;
import bca.algorithm.vlsn.generate.PathExtend;
import bca.algorithm.vlsn.generate.PathValidExtend;
import bca.entity.input.Input;
import bca.entity.solution.Solution;
import bca.entity.solution.SolutionCluster;
import bca.entity.solution.SolutionVehicle;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class KExchangeSearch {
    private final int K;
    private final Input input;
    private Solution solution;
    private PathExtend pathExtend;
    private Map<Long, Long> node_cluster_map;

    public KExchangeSearch(int K, Input input, Solution solution) {
        this.K = K;
        this.input = input;
        this.solution = solution;
//        this.pathExtend = new PathValidExtend(input);
        node_cluster_map = new HashMap<>();
        for (SolutionVehicle vehicle : solution.getVehicles()) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                for (Long nodeId : cluster.getNode_ids()) {
                    node_cluster_map.put(nodeId, cluster.getCluster_id());
                }
            }
        }
        this.pathExtend = new BCAPathExtend(input, solution, node_cluster_map);
    }
    public KExchangeSearch(int K, Input input, Solution solution, Map<Long, Long> node_cluster_map) {
        this.K = K;
        this.input = input;
        this.solution = solution;
//        this.pathExtend = new PathValidExtend(input);
        this.node_cluster_map = node_cluster_map;
        this.pathExtend = new BCAPathExtend(input, solution, node_cluster_map);
    }
    public List<Long> search() {
        int k = 1;
        int num_node = input.getNum_nodes();
        List<Long> nodeIds = input.getNodeIds();
        List<List<Long>> pathSet = new ArrayList<>();
//        List<Double> deltas = new ArrayList<>();
        for (Long nodeId : nodeIds) {
            List<Long> path = new ArrayList<>();
//            deltas.add(0.);
            path.add(nodeId);
            pathSet.add(path);
        }

        List<Long> exchange = null;
        double weight_delta = 0;
//        List<Long> inserted = new ArrayList<>();
//        List<Long> removed = new ArrayList<>();
//        double currentObj = solution.getObjective().getValue();
//        System.err.println(solution.getObjective());
        while (!pathSet.isEmpty() && k < K && Double.compare(weight_delta, 0.0) >= 0) {
            k++;
            List<Double> deltas = new ArrayList<>();
            List<List<Long>> extendedPaths = pathExtend.extend(pathSet, deltas);
            System.err.println("Iteration " + k + " extendedPaths size: " + extendedPaths.size() + "\n");
//            if (k > 2)
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
//                double delta = solution.getObjective().updateCluster(solution, cluster_id, path) - currentObj;
                    double delta = solution.getObjective().updateCluster(solution, cluster_id, path);
//                    double delta = -1;

//                System.err.print("[" + i + ": ");
//                for (Long nodeId : path)
//                    System.err.print(nodeId + " ");
//                System.err.print(delta);
//                System.err.print("]");

//                Long first = path.getFirst();
//                Long firstClusterId = node_cluster_map.getOrDefault(first, null);
//                Long last = path.getLast();
//                Long lastClusterId = node_cluster_map.getOrDefault(last, null);

//                if (k == 2) {
//                    inserted.clear();
//                    removed.clear();
//                    inserted.add(last);
//                    removed.add(first);
//                    solution.getObjective().updateCluster(solution, firstClusterId, inserted, removed);
//                    delta += solution.getObjective().updateCluster(solution, lastClusterId, removed, inserted) - currentObj;
//                    solution.getObjective().updateCluster(solution, firstClusterId, removed, inserted);
//                    solution.getObjective().updateCluster(solution, lastClusterId, inserted, removed);
//                }
//                else {
//                    Long preLast = path.get(path.size() - 2);
//
//                    inserted.clear();
//                    removed.clear();
//                    inserted.add(preLast);
//                    removed.add(first);
//                    delta -= solution.getObjective().updateCluster(solution, firstClusterId, inserted, removed);
//                    removed.clear();
//                    removed.add(last);
//                    solution.getObjective().updateCluster(solution, firstClusterId, removed, inserted);
//                    delta += solution.getObjective().updateCluster(solution, lastClusterId, inserted, removed);
//                    solution.getObjective().updateCluster(solution, lastClusterId, removed, inserted);
//                    inserted.clear();
//                    inserted.add(first);
//                    solution.getObjective().updateCluster(solution, firstClusterId, inserted, removed);
//                }

                    deltas.add(delta);
                    if (Double.compare(delta, weight_delta) < 0) {
                        weight_delta = delta;
                        exchange = path;
                    }
                }
            }
//            else {
//                for (List<Long> path : extendedPaths) {
//                    deltas.add(0.);
//                }
//            }
//            System.err.println();


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
//                deltas.add(deltas.get(index));
            }
//            deltas.subList(0, indices.size() - 1).clear();
//            assert(deltas.size() == pathSet.size());
        }
        System.err.println(weight_delta);
        return exchange;
    }

    public void move(List<Long> path) {
//        for (Long node_id : path) {
        Long last_cluster_id = node_cluster_map.getOrDefault(path.getLast(), null);
//        for (Long node_id : path) {
//            Long cluster_id = node_cluster_map.getOrDefault(node_id, null);
//            SolutionCluster cluster = solution.getSolutionClusterById(cluster_id);
//            System.err.print("[");
//            for (Long id : cluster.getNode_ids()) {
//                System.err.print(id + " ");
//            }
//            System.err.println("]");
//        }
//        System.err.println();
        boolean isSame = false;
        for (int i = 0; i < path.size(); i++) {
            Long node_id = path.get(i);
            Long prev_node_id = path.get(i > 0 ? i - 1 : path.size() - 1);
            Long cluster_id = i + 1 < path.size() ? node_cluster_map.getOrDefault(node_id, null) : last_cluster_id;

//            System.err.print(cluster_id + " ");
            SolutionCluster cluster = solution.getSolutionClusterById(cluster_id);

            if (i + 1 < path.size() || !isSame) {
                cluster.getNode_ids().remove(node_id);
            }
            if (!cluster.getNode_ids().contains(prev_node_id)) {
               cluster.getNode_ids().add(prev_node_id);
            }
            else {
                isSame = true;
            }
            node_cluster_map.put(prev_node_id, cluster_id);
        }
//        System.err.println();

//        for (Long node_id : path) System.err.print(node_cluster_map.getOrDefault(node_id, null) + " ");
//        System.err.println();
//        for (Long node_id : path) {
//            Long cluster_id = node_cluster_map.getOrDefault(node_id, null);
//            SolutionCluster cluster = solution.getSolutionClusterById(cluster_id);
//            System.err.print(cluster.getNode_ids().contains(node_id) + " ");
//        }
//        System.err.println();
//        for (Long node_id : path) {
//            Long cluster_id = node_cluster_map.getOrDefault(node_id, null);
//            SolutionCluster cluster = solution.getSolutionClusterById(cluster_id);
//            System.err.print("[");
//            for (Long id : cluster.getNode_ids()) {
//                System.err.print(id + " ");
//            }
//            System.err.println("]");
//        }
//        System.err.println();
        solution.findBestCenter();
        solution.getObjective().update(solution);
    }
}
