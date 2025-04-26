package bca.algorithm.vlsn.process;

import bca.entity.data.Pair;
import bca.entity.input.*;
import bca.entity.solution.*;
import bca.process.EuclidianDistance;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class BCAGreedyAlgorithm implements GreedyAlgorithm {
    private int distance_iteration;
    private Solution solution;

    public BCAGreedyAlgorithm(int distance_iteration) {
        this.distance_iteration = distance_iteration;
    }

    public Solution solve(Input input) {
        solution = new BCASolution(input);

        List<Long> centers = new ArrayList<>(input.getNodeIds());
        Collections.shuffle(centers);
        centers = centers.subList(0, input.getNum_clusters());
        int num_clusters = input.getNum_clusters();
        List<Integer> cluster_size = new ArrayList<>();
        List<List<Long>> cluster_node_ids = new ArrayList<>();
        List<Pair<Double, Double>> sum_x = new ArrayList<>();
        List<Double> sum_y = new ArrayList<>();

        for (int i = 0; i < num_clusters; i++) {
            cluster_size.add(0);
            cluster_node_ids.add(new ArrayList<>());
            sum_x.add(new Pair<>(0.0, 0.0));
            sum_y.add(0.0);
        }

        for (int iter = 0; iter < distance_iteration; iter++) {
            dijkstraMultiSource(centers, cluster_size, cluster_node_ids, sum_x, sum_y, input);
//            shortestCenterCalculate(centers, cluster_size, cluster_node_ids, sum_x, sum_y, input);
            SolutionVehicle vehicle = new BCASolutionVehicle();
            weiszfeldGreedy(centers, cluster_size, cluster_node_ids, sum_x, sum_y, input);
        }

        return solution;
    }

    @SuppressWarnings("unused")
    private void shortestCenterCalculate(List<Long> centers, List<Integer> cluster_size, List<List<Long>> cluster_node_ids,
                                         List<Pair<Double, Double>> sum_x, List<Double> sum_y, Input input) {
        for (int i = 0; i < centers.size(); i++) {
            cluster_size.set(i, 0);
            cluster_node_ids.get(i).clear();
            sum_x.get(i).a = sum_x.get(i).b = 0.0;
            sum_y.set(i, 0.0);
        }

        List<Long> node_ids = input.getNodeIds();
        for (Long node_id : node_ids) {
            double min_distance = Double.MAX_VALUE;
            int min_node = -1;
            for (int i = 0; i < centers.size(); i++) {
                Long start_node = centers.get(i);
                double distance = input.getNodeDistance(start_node, node_id);
                if (Double.compare(distance, min_distance) < 0) {
                    min_distance = distance;
                    min_node = i;
                }
            }
            if (min_node >= 0) {
                BCANode node = (BCANode)input.getNodeById(node_id);
                cluster_size.set(min_node, cluster_size.get(min_node) + 1);
                cluster_node_ids.get(min_node).add(node_id);
                if (Double.compare(min_distance, 0.0) != 0) {
                    sum_x.get(min_node).a = sum_x.get(min_node).a + node.getLatitude() / min_distance;
                    sum_x.get(min_node).b = sum_x.get(min_node).b + 1. / min_distance;
                    sum_y.set(min_node, sum_y.get(min_node) + node.getLongitude() / min_distance);
                }
            }
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void weiszfeldGreedy(List<Long> centers, List<Integer> cluster_size, List<List<Long>> cluster_node_ids,
                                 List<Pair<Double, Double>> sum_x, List<Double> sum_y, Input input) {
        int num_clusters = input.getNum_clusters();
        for (int i = 0; i < num_clusters; i++) {
            if (cluster_size.get(i) > 0) {
                double median_x = 0.0;
                double median_y = 0.0;
                if (Double.compare(sum_x.get(i).b, 0.0) != 0) {
                    median_x = sum_x.get(i).a / sum_x.get(i).b;
                    median_y = sum_y.get(i) / sum_x.get(i).b;
                }

                double min_distance = Double.MAX_VALUE;
                Long min_node = -1L;
                for (Long node_id : cluster_node_ids.get(i)) {
                    BCANode node = (BCANode)input.getNodeById(node_id);
                    double distance = EuclidianDistance.euclideanDistance(median_x, median_y, node.getLatitude(), node.getLongitude());
                    if (Double.compare(distance, min_distance) < 0) {
                        min_distance = distance;
                        min_node = node_id;
                    }
                }

                centers.set(i, min_node);
            }
        }
    }

    private void bestVehicles(List<List<Long>> cluster_node_ids, List<Long> best_vehicles) {
        int num_clusters = cluster_node_ids.size();
        assert best_vehicles != null;
        for (int i = 0; i < num_clusters; i++) {
            List<Long> node_ids = cluster_node_ids.get(i);
        }
    }

    @SuppressWarnings("CommentedOutCode")
    private void dijkstraMultiSource(List<Long> centers, List<Integer> cluster_size, List<List<Long>> cluster_node_ids,
                                     List<Pair<Double, Double>> sum_x, List<Double> sum_y, Input input) {
        BCAInput bcaInput = (BCAInput)input;
        Map<Long, Pair<Double, Integer>> distances = new HashMap<>();
        PriorityQueue<Pair<Double, Long>> pq = new PriorityQueue<>(Comparator.comparingDouble(i -> i.a));
        for (int i = 0; i < centers.size(); i++) {
            cluster_size.set(i, 0);
            cluster_node_ids.get(i).clear();
            sum_x.set(i, new Pair<>(0.0, 0.0));
            sum_y.set(i, 0.0);

            Long center = centers.get(i);
            distances.put(center, new Pair<>(0.0, i));
            pq.add(new Pair<>(0.0, center));
        }

        int num_clusters = input.getNum_clusters();
        double customer_average = bcaInput.getCustomer_average() * (bcaInput.getCustomer_bias() + 1);
        double demand_average = bcaInput.getDemand_average() * (bcaInput.getDemand_bias() + 1);
        List<Double> customers = new ArrayList<>();
        List<Double> demands = new ArrayList<>();
        for (int i = 0; i < num_clusters; i++) {
            customers.add(0.0);
            demands.add(0.0);
        }

        double customer_demand_weight = ((BCASolutionObjective)solution.getObjective()).getCustomer_demand_weight();
        while (!pq.isEmpty()) {
            Pair<Double, Long> pair = pq.poll();
            double distance = pair.a;
            Long node_id = pair.b;

            Pair<Double, Integer> center = distances.getOrDefault(node_id, null);
            if (center == null || Double.compare(center.a, distance) > 0) {
                continue;
            }

            BCANode node = (BCANode)input.getNodeById(node_id);
            cluster_size.set(center.b, cluster_size.get(center.b) + 1);
            cluster_node_ids.get(center.b).add(node_id);
//            sum_x.set(center.b, sum_x.get(center.b) + ((BCANode)node).getLatitude());
            sum_x.get(center.b).a += node.getLatitude() / distance;
            sum_x.get(center.b).b += 1.0 / distance;
            sum_y.set(center.b, sum_y.get(center.b) + node.getLongitude() / distance);
            customers.set(center.b, customers.get(center.b) + node.getExpected_customers());
            demands.set(center.b, demands.get(center.b) + node.getExpected_demands());

            double current_customer = customers.get(center.b);
            double current_demand = demands.get(center.b);
            for (Long edge_id : node.getAdjacent_edges()) {
                Edge edge = input.getEdgeById(edge_id);
                Long to_node_id = edge.getRemaining(node_id);
                BCANode to_node = (BCANode)input.getNodeById(to_node_id);

                double to_node_customer = to_node.getExpected_customers();
                double to_node_demand = to_node.getExpected_demands();
//                if (Double.compare(current_customer + to_node_customer, customer_average) > 0 &&
//                        (Double.compare(current_customer, customer_average) > 0 ||
//                                Double.compare(current_customer + to_node_customer - customer_average, current_customer - customer_average) > 0)) {
//                    continue;
//                }
//                if (Double.compare(current_demand + to_node_demand, demand_average) > 0 &&
//                        (Double.compare(current_demand, demand_average) > 0 ||
//                                Double.compare(current_demand + to_node_demand - demand_average, current_demand - demand_average) > 0)) {
//                    continue;
//                }

                double new_distance = distance + edge.getDistance() + customer_demand_weight *
                        (current_customer + to_node_customer + Math.max(0, current_customer - customer_average) * 2 +
                                current_demand + to_node_demand + Math.max(0, current_demand - demand_average) * 2);
                if (!distances.containsKey(to_node_id) || Double.compare(distances.get(to_node_id).a, new_distance) > 0) {
                    distances.put(to_node_id, new Pair<>(new_distance, center.b));
                    pq.add(new Pair<>(new_distance, to_node_id));
                }
            }
        }
    }
}
