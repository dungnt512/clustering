package bca.algorithm.vlsn.process;

import bca.entity.data.DoubleLinkedList;
import bca.entity.data.LinkedNode;
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
    private int greedy_iteration;
    private Solution solution;

    public BCAGreedyAlgorithm(int greedy_iteration) {
        this.greedy_iteration = greedy_iteration;
    }

    private double customer_average;
    private double demand_average;
    private double customer_low;
    private double demand_low;
    private double customer_high;
    private double demand_high;

    @SuppressWarnings({"CommentedOutCode", "SpellCheckingInspection"})
    public Solution solve(Input input) {
        solution = new BCASolution(input);
        customer_average = ((BCAInput)input).getCustomer_average();
        demand_average = ((BCAInput)input).getDemand_average();
        customer_low = customer_average * (1 - ((BCAInput)input).getCustomer_bias());
        demand_low = demand_average * (1 - ((BCAInput)input).getDemand_bias());
        customer_high = customer_average * (1 + ((BCAInput)input).getCustomer_bias());
        demand_high = demand_average * (1 + ((BCAInput)input).getDemand_bias());

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

        for (int iter = 0; iter < greedy_iteration; iter++) {
//            dijkstraMultiSource(centers, cluster_size, cluster_node_ids, sum_x, sum_y, input);
            List<Long> best_vehicles = new ArrayList<>();
//            greedyGrouping(centers, cluster_size, cluster_node_ids, sum_x, sum_y, best_vehicles, input);
            greedyGroupingLinkedList(centers, cluster_size, cluster_node_ids, sum_x, sum_y, best_vehicles, input);
//            shortestCenterCalculate(centers, cluster_size, cluster_node_ids, sum_x, sum_y, input);

//            weiszfeldGreedy(centers, cluster_size, cluster_node_ids, sum_x, sum_y, input);
//            medianGreedy(centers, cluster_size, cluster_node_ids, input);
            findBestCenter(centers, cluster_size, cluster_node_ids, input);

//            for (int i = 0; i < num_clusters; i++) {
//                System.err.print(cluster_size.get(i) + ": ");
//                System.err.print(centers.get(i) + " - ");
//                for (Long node_id : cluster_node_ids.get(i)) {
//                    System.err.print(node_id + " ");
//                }
//                System.err.println(sum_x.get(i).a + " " + sum_x.get(i).b + " " + sum_y.get(i));
//            }
//            System.err.println();

            double current_objective = solution.getObjective().getValue();
            List<Long> current_center_ids = new ArrayList<>();
            List<Set<Long>> current_node_ids = new ArrayList<>();
            for (int i = 0; i < num_clusters; i++) {
                Long vehicle_id = best_vehicles.get(i);
                Long center_id = centers.get(i);
                List<Long> node_ids = cluster_node_ids.get(i);

                SolutionCluster cluster = solution.getSolutionClusterById(vehicle_id);
                current_center_ids.add(cluster.getCenter_id());
                cluster.setCenter_id(center_id);

                current_node_ids.add(new HashSet<>(cluster.getNode_ids()));
                cluster.getNode_ids().clear();
                cluster.getNode_ids().addAll(node_ids);
            }

//            solution.getObjective().update(solution);
//            System.err.println(solution);
            if (Double.compare(current_objective, solution.getObjective().update(solution)) < 0) {
                for (int i = 0; i < num_clusters; i++) {
                    Long vehicle_id = best_vehicles.get(i);
                    SolutionCluster cluster = solution.getSolutionClusterById(vehicle_id);
                    cluster.setCenter_id(current_center_ids.get(i));
                    cluster.setNode_ids(current_node_ids.get(i));
                }
                solution.getObjective().update(solution);
            }

//            System.err.println(current_objective + " " + solution.getObjective().getValue() + "\n");
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

    @SuppressWarnings({"SpellCheckingInspection", "CommentedOutCode", "DuplicatedCode", "unused"})
    private void weiszfeldGreedy(List<Long> centers, List<Integer> cluster_size, List<List<Long>> cluster_node_ids,
                                 List<Pair<Double, Double>> sum_x, List<Double> sum_y, Input input) {
        int num_clusters = input.getNum_clusters();
        for (int i = 0; i < num_clusters; i++) {
            if (cluster_size.get(i) > 0) {
                double median_x = 0.0;
                double median_y = 0.0;
//                System.err.print("(" + cluster_size.get(i) + " " + cluster_node_ids.get(i).size() + " " +
//                        sum_x.get(i).a + " " + sum_y.get(i) + " " + sum_x.get(i).b + " " + median_x + " " + median_y + ") ");
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
//        System.err.println();
    }

    @SuppressWarnings({"CommentedOutCode", "DuplicatedCode", "unused"})
    private void medianGreedy(List<Long> centers, List<Integer> cluster_size, List<List<Long>> cluster_node_ids, Input input) {
        for (int i = 0; i < centers.size(); i++) {
            if (cluster_size.get(i) > 0) {
                double median_x = 0.0;
                double median_y = 0.0;
//                System.err.print("(" + cluster_size.get(i) + " " + cluster_node_ids.get(i).size() + " " +
//                        sum_x.get(i).a + " " + sum_y.get(i) + " " + sum_x.get(i).b + " " + median_x + " " + median_y + ") ");
                for (Long node_id : cluster_node_ids.get(i)) {
                    BCANode node = (BCANode)input.getNodeById(node_id);
                    median_x += node.getLatitude();
                    median_y += node.getLongitude();
                }
                if (Double.compare(cluster_size.get(i), 0.0) != 0) {
                    median_x /= cluster_size.get(i);
                    median_y /= cluster_size.get(i);
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
//        System.err.println();
    }

    private void findBestCenter(List<Long> centers, List<Integer> cluster_size, List<List<Long>> cluster_node_ids, Input input) {
        for (int i = 0; i < centers.size(); i++) {
            if (cluster_size.get(i) > 0) {
                double min_distance = Double.MAX_VALUE;
                Long min_node = -1L;
                for (Long node_id : cluster_node_ids.get(i)) {
                    double distance = 0.0;
                    for (Long node_id2 : cluster_node_ids.get(i)) {
                        if (node_id.equals(node_id2)) {
                            continue;
                        }
                        distance += input.getNodeDistance(node_id, node_id2);
                    }
                    if (Double.compare(distance, min_distance) < 0) {
                        min_distance = distance;
                        min_node = node_id;
                    }
                }
                centers.set(i, min_node);
            }
        }
//        System.err.println();
    }


    private void bestVehicleFamiliarities(List<List<Long>> cluster_node_ids, List<Long> best_vehicles, Input input) {
        int num_clusters = cluster_node_ids.size();
        assert best_vehicles != null;
        List<Long> vehicle_ids = new ArrayList<>(input.getVehicleIds());
        Collections.shuffle(vehicle_ids);
        int num_vehicles = vehicle_ids.size();
        boolean[] marked = new boolean[num_vehicles];

        for (int i = 0; i < num_clusters; i++) {
            List<Long> node_ids = cluster_node_ids.get(i);
            int min_familiarity = Integer.MAX_VALUE;
            int best_vehicle = -1;

            for (int j = 0; j < num_vehicles; j++) {
                if (marked[j]) {
                    continue;
                }
                Long vehicle_id = vehicle_ids.get(j);
                BCAVehicle vehicle = (BCAVehicle)input.getVehicleById(vehicle_id);
                int familiarity = 0;
                for (Long node_id : node_ids) {
                    familiarity += vehicle.getFamiliarity(node_id) - 1;
                }
                if (Double.compare(familiarity, min_familiarity) < 0) {
                    min_familiarity = familiarity;
                    best_vehicle = j;
                }
            }
            if (best_vehicle >= 0) {
                best_vehicles.add(vehicle_ids.get(best_vehicle));
                marked[best_vehicle] = true;
            } else {
                best_vehicles.set(i, -1L);
            }
        }
    }

    private double calculateCustomerVariance(double current) {
        if (current < customer_low) {
            return customer_low - current;
        } else if (current > customer_high) {
            return current - customer_high;
        }
        return 0;
    }
    private double calculateDemandVariance(double current) {
        if (current < demand_low) {
            return demand_low - current;
        } else if (current > demand_high) {
            return current - demand_high;
        }
        return 0;
    }

    private void greedyGroupingLinkedList(List<Long> centers, List<Integer> cluster_size, List<List<Long>> cluster_node_ids,
                                List<Pair<Double, Double>> sum_x, List<Double> sum_y, List<Long> vehicle_ids, Input input) {
        int num_clusters = input.getNum_clusters();
        Map<Long, Integer> visited = new HashMap<>();

        List<DoubleLinkedList<Long>> pq = new ArrayList<>();
        List<Long> best_vehicles = new ArrayList<>();
        bestVehicleFamiliarities(cluster_node_ids, best_vehicles, input);
        vehicle_ids.addAll(best_vehicles);

        double customer_demand_weight = ((BCASolutionObjective)solution.getObjective()).getCustomer_demand_weight();
        double familiarity_weight = ((BCASolutionObjective)solution.getObjective()).getFamiliarity_weight();
        int num_remaining_nodes = input.getNum_nodes() - num_clusters;
        for (int i = 0; i < num_clusters; i++) {
            Long center_id = centers.get(i);
            pq.add(new DoubleLinkedList<>(center_id));
            visited.put(center_id, i);
        }

        List<Double> customer_totals = new ArrayList<>();
        List<Double> demand_totals = new ArrayList<>();

        for (int i = 0; i < centers.size(); i++) {
            cluster_node_ids.get(i).clear();
            cluster_size.set(i, 1);

            Long center_id = centers.get(i);
            cluster_node_ids.get(i).add(center_id);
            BCANode center = (BCANode)input.getNodeById(center_id);
            customer_totals.add(center.getExpected_customers());
            demand_totals.add(center.getExpected_demands());
            for (Long edge_id : center.getAdjacent_edges()) {
                Edge edge = input.getEdgeById(edge_id);
                Long to_node_id = edge.getRemaining(center_id);
                if (!visited.containsKey(to_node_id)) {
                    pq.get(i).add(to_node_id);
                }
            }
        }

        while (num_remaining_nodes > 0) {
            int min_cluster_it = -1;
            LinkedNode<Long> min_linked_node = null;
            BCANode min_node = null;
            double min_cost = Double.MAX_VALUE;

            for (int i = 0; i < num_clusters; i++) {
                if (pq.get(i).isEmpty()) {
                    continue;
                }
                LinkedNode<Long> current = pq.get(i).getFirst();
                double customer_total = customer_totals.get(i);
                double customer_variance = calculateCustomerVariance(customer_total);
                double demand_total = demand_totals.get(i);
                double demand_variance = calculateDemandVariance(demand_total);
                while (current != null) {
                    Long node_id = current.value;
//                    System.err.print(current.value + " ");
                    if (visited.containsKey(node_id)) {
                        pq.get(i).remove(current);
                        current = current.next;
//                        System.err.print("-" + current.value + " ");
                        continue;
                    }
                    BCANode node = (BCANode)input.getNodeById(node_id);
                    double cost = customer_demand_weight *
                            (calculateCustomerVariance(customer_total + node.getExpected_customers()) - customer_variance +
                            calculateDemandVariance(demand_total + node.getExpected_demands()) - demand_variance) +
                            familiarity_weight * (node.getFamiliarity(best_vehicles.get(i)) - 1) +
                            input.getNodeDistance(centers.get(i), node_id);
                    if (Double.compare(cost, min_cost) < 0) {
                        min_cost = cost;
                        min_node = node;
                        min_linked_node = current;
                        min_cluster_it = i;
                    }
                    current = current.next;
                }
            }

            num_remaining_nodes--;
            assert min_node != null;
//            System.err.print("(" + min_cluster_it + " " + min_node.getNode_id() + " " + min_cost + " " + min_linked_node.value + ") ");
            pq.get(min_cluster_it).remove(min_linked_node);
            visited.put(min_node.getNode_id(), min_cluster_it);
            customer_totals.set(min_cluster_it, customer_totals.get(min_cluster_it) + min_node.getExpected_customers());
            demand_totals.set(min_cluster_it, demand_totals.get(min_cluster_it) + min_node.getExpected_demands());

            cluster_size.set(min_cluster_it, cluster_size.get(min_cluster_it) + 1);
            cluster_node_ids.get(min_cluster_it).add(min_node.getNode_id());

            for (Long edge_id : min_node.getAdjacent_edges()) {
                Edge edge = input.getEdgeById(edge_id);
                Long to_node_id = edge.getRemaining(min_node.getNode_id());
                if (visited.containsKey(to_node_id)) {
                    continue;
                }
                pq.get(min_cluster_it).add(to_node_id);
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void greedyGrouping(List<Long> centers, List<Integer> cluster_size, List<List<Long>> cluster_node_ids,
                                List<Pair<Double, Double>> sum_x, List<Double> sum_y, List<Long> vehicle_ids, Input input) {
        Map<Long, Integer> visited = new HashMap<>();
        int num_clusters = input.getNum_clusters();

        List<Long> best_vehicles = new ArrayList<>();
        bestVehicleFamiliarities(cluster_node_ids, best_vehicles, input);
        vehicle_ids.addAll(best_vehicles);

        PriorityQueue<Pair<Double, Integer>> min_cost = new PriorityQueue<>(Comparator.comparingDouble(i -> i.a));
        List<PriorityQueue<Pair<Double, Long>>> pq = new ArrayList<>();
        for (int i = 0; i < num_clusters; i++) {
            pq.add(new PriorityQueue<>(Comparator.comparingDouble(j -> +j.a)));
        }

        double customer_demand_weight = ((BCASolutionObjective)solution.getObjective()).getCustomer_demand_weight();
        double familiarity_weight = ((BCASolutionObjective)solution.getObjective()).getFamiliarity_weight();
        int num_remaining_nodes = input.getNum_nodes() - num_clusters;
        for (int i = 0; i < centers.size(); i++) {
            Long center_id = centers.get(i);
            visited.put(center_id, i);
        }
        for (int i = 0; i < centers.size(); i++) {
            cluster_node_ids.get(i).clear();
            cluster_size.set(i, 1);
            sum_x.set(i, new Pair<>(0.0, 0.0));
            sum_y.set(i, 0.0);

            Long center_id = centers.get(i);
            cluster_node_ids.get(i).add(center_id);
            BCANode center = (BCANode)input.getNodeById(center_id);
//            pq.get(i).add(new Pair<>(0.0, center));
            min_cost.add(new Pair<>(customer_demand_weight * (center.getExpected_customers() + center.getExpected_demands()) +
                    center.getFamiliarity(best_vehicles.get(i)), i));
            for (Long edge_id : center.getAdjacent_edges()) {
                Edge edge = input.getEdgeById(edge_id);
                Long to_node_id = edge.getRemaining(center_id);
                BCANode to_node = (BCANode)input.getNodeById(to_node_id);
                double cost = customer_demand_weight * (to_node.getExpected_customers() + to_node.getExpected_demands()) +
                        familiarity_weight * (to_node.getFamiliarity(best_vehicles.get(i)) - 1) +
                        input.getNodeDistance(center_id, to_node_id);
                pq.get(i).add(new Pair<>(cost, to_node_id));
            }
        }

        while (num_remaining_nodes > 0) {
            Pair<Double, Integer> pair = min_cost.poll();
            assert pair != null;
            double cluster_cost = pair.a;
            int cluster_id = pair.b;
            Long center_id = centers.get(cluster_id);
            boolean check = false;
            while (!pq.get(cluster_id).isEmpty()) {
                Pair<Double, Long> node_pair = pq.get(cluster_id).poll();
                assert node_pair != null;
                double cost = node_pair.a;
                Long node_id = node_pair.b;
                if (visited.containsKey(node_id)) {
                    continue;
                }
                visited.put(node_id, cluster_id);
                BCANode node = (BCANode)input.getNodeById(node_id);
                cluster_size.set(cluster_id, cluster_size.get(cluster_id) + 1);
                cluster_node_ids.get(cluster_id).add(node_id);
                cluster_cost += cost;
                if (Double.compare(cost, 0.0) != 0) {
                    sum_x.get(cluster_id).a += node.getLatitude() / cost;
                    sum_x.get(cluster_id).b += 1.0 / cost;
                    sum_y.set(cluster_id, sum_y.get(cluster_id) + node.getLongitude() / cost);
                }
                num_remaining_nodes--;
                check = true;

                for (Long edge_id : node.getAdjacent_edges()) {
                    Edge edge = input.getEdgeById(edge_id);
                    Long to_node_id = edge.getRemaining(node_id);
                    if (visited.containsKey(to_node_id)) {
                        continue;
                    }
                    BCANode to_node = (BCANode)input.getNodeById(to_node_id);
                    double new_cost = customer_demand_weight * (to_node.getExpected_customers() + to_node.getExpected_demands()) +
                            familiarity_weight * (to_node.getFamiliarity(best_vehicles.get(cluster_id)) - 1) +
                            input.getNodeDistance(center_id, to_node_id);
                    pq.get(cluster_id).add(new Pair<>(new_cost, to_node_id));
                }

                break;
            }

            if (!check) {
                continue;
            }
            min_cost.add(new Pair<>(cluster_cost, cluster_id));
        }

    }

    @SuppressWarnings({"CommentedOutCode", "unused", "GrazieInspection"})
    private void dijkstraMultiSource(List<Long> centers, List<Integer> cluster_size, List<List<Long>> cluster_node_ids,
                                     List<Pair<Double, Double>> sum_x, List<Double> sum_y, Input input) {
        BCAInput bcaInput = (BCAInput)input;
        Map<Long, Pair<Double, Integer>> distances = new HashMap<>();
        PriorityQueue<Pair<Double, Long>> pq = new PriorityQueue<>(Comparator.comparingDouble(i -> i.a));

        int num_clusters = input.getNum_clusters();
        double customer_average = bcaInput.getCustomer_average() * (bcaInput.getCustomer_bias() + 1);
        double demand_average = bcaInput.getDemand_average() * (bcaInput.getDemand_bias() + 1);
        List<Double> customers = new ArrayList<>();
        List<Double> demands = new ArrayList<>();
//        for (int i = 0; i < num_clusters; i++) {
//            customers.add(0.0);
//            demands.add(0.0);
//        }

        for (int i = 0; i < centers.size(); i++) {
            cluster_size.set(i, 0);
            cluster_node_ids.get(i).clear();
            sum_x.set(i, new Pair<>(0.0, 0.0));
            sum_y.set(i, 0.0);

            Long center = centers.get(i);
            distances.put(center, new Pair<>(0.0, i));
//            customers.add(((BCANode)input.getNodeById(center)).getExpected_customers());
//            demands.add(((BCANode)input.getNodeById(center)).getExpected_demands());
            customers.add(0.0);
            demands.add(0.0);
            pq.add(new Pair<>(0.0, center));
        }


        double customer_demand_weight = ((BCASolutionObjective)solution.getObjective()).getCustomer_demand_weight();
        while (!pq.isEmpty()) {
            Pair<Double, Long> pair = pq.poll();
            double distance = pair.a;
            Long node_id = pair.b;

            Pair<Double, Integer> center = distances.getOrDefault(node_id, null);
            BCANode node = (BCANode)input.getNodeById(node_id);
            if (center == null || Double.compare(center.a, distance) < 0) {
//                if (center != null) {
//                    customers.set(center.b, customers.get(center.b) - node.getExpected_customers());
//                    demands.set(center.b, demands.get(center.b) - node.getExpected_demands());
//                }
                continue;
            }
//            System.err.print(node_id + " ");
            cluster_size.set(center.b, cluster_size.get(center.b) + 1);
            cluster_node_ids.get(center.b).add(node_id);
//            sum_x.set(center.b, sum_x.get(center.b) + ((BCANode)node).getLatitude());
            if (Double.compare(distance, 0.0) != 0) {
                sum_x.get(center.b).a += node.getLatitude() / distance;
                sum_x.get(center.b).b += 1.0 / distance;
                sum_y.set(center.b, sum_y.get(center.b) + node.getLongitude() / distance);
            }

            customers.set(center.b, customers.get(center.b) + node.getExpected_customers());
            demands.set(center.b, demands.get(center.b) + node.getExpected_demands());

            double current_customer = customers.get(center.b);
            double current_demand = demands.get(center.b);
//            for (Long edge_id : node.getAdjacent_edges()) {
//                Edge edge = input.getEdgeById(edge_id);
//                Long to_node_id = edge.getRemaining(node_id);
//                BCANode to_node = (BCANode) input.getNodeById(to_node_id);
//
//                double to_node_customer = to_node.getExpected_customers();
//                double to_node_demand = to_node.getExpected_demands();
//                current_customer += to_node_customer;
//                current_demand += to_node_demand;
//            }

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

//                double new_distance = distance + edge.getDistance() + customer_demand_weight *
//                        (current_customer + to_node_customer + Math.max(0, current_customer + to_node_customer - customer_average) * 10 +
//                                current_demand + to_node_demand + Math.max(0, current_demand + to_node_demand - demand_average) * 10);
                double new_distance = distance + edge.getDistance() + customer_demand_weight *
                        (current_customer + Math.max(0, current_customer - customer_average) * 10 +
                                current_demand + Math.max(0, current_demand - demand_average) * 10);
//                double new_distance = distance + edge.getDistance() + customer_demand_weight * (to_node_customer + to_node_demand);
//                new_distance = distance + edge.getDistance();
//                System.err.print(to_node_id + " ");
                if (!distances.containsKey(to_node_id) || Double.compare(distances.get(to_node_id).a, new_distance) > 0) {

                    distances.put(to_node_id, new Pair<>(new_distance, center.b));
//                    customers.set(center.b, customers.get(center.b) + to_node_customer);
//                    demands.set(center.b, demands.get(center.b) + to_node_demand);
                    pq.add(new Pair<>(new_distance, to_node_id));
                }
            }

//            customers.set(center.b, current_customer);
//            demands.set(center.b, current_demand);

        }
        System.err.println(bcaInput.getCustomer_average() + " " + bcaInput.getDemand_average() + " " +
                bcaInput.getCustomer_bias() + " " + bcaInput.getDemand_bias());
        for (int i = 0; i < num_clusters; i++) {
            System.err.println("{" + customers.get(i) + " " + demands.get(i) + "} ");
        }
        System.err.println();
    }
}
