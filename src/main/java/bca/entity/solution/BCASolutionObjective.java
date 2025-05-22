package bca.entity.solution;

import bca.entity.input.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"CommentedOutCode", "DuplicatedCode"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BCASolutionObjective implements SolutionObjective {
    private Input input;
    private double value;

    private double customer_demand_weight;
    private double familiarity_weight;
    private double distance_weight;

//    private double customer_bias;
//    private double demand_bias;

//    private double customer_average;
//    private double demand_average;
//    private double area_average;
//    private double load_average;

    private double variance_total;
    private double distance_total;
    private int familiarity_total;
    private List<Double> customer_totals;
    private List<Double> demand_totals;
    private double customer_average;
    private double demand_average;
    private double customer_low;
    private double customer_high;
    private double demand_low;
    private double demand_high;

    private List<SolutionCluster> clusters;

    public BCASolutionObjective(Input input, double customer_demand_weight, double familiarity_weight, double distance_weight) {
        this.input = input;
        this.value = Double.MAX_VALUE;

        this.customer_demand_weight = customer_demand_weight;
        this.familiarity_weight = familiarity_weight;
        this.distance_weight = distance_weight;

        BCAInput bcaInput = (BCAInput)input;
        customer_average = bcaInput.getCustomer_average();
        demand_average = bcaInput.getDemand_average();

        customer_low = customer_average * (1 - bcaInput.getCustomer_bias());
        customer_high = customer_average * (1 + bcaInput.getCustomer_bias());
        demand_low = demand_average * (1 - bcaInput.getDemand_bias());
        demand_high = demand_average * (1 + bcaInput.getDemand_bias());
//        this.customer_bias = ((BCAInput)input).getCustomer_bias();
//        this.demand_bias = ((BCAInput)input).getDemand_bias();
//        this.customer_average = 0;
//        this.demand_average = 0;
//        for (int i = 0; i < input.getNum_nodes(); i++) {
//            this.customer_average += ((BCANode)input.getNodes().get(i)).getExpected_customers();
//            this.demand_average += ((BCANode)input.getNodes().get(i)).getExpected_demands();
//        }
//        this.customer_average /= input.getNum_clusters();
//        this.demand_average /= input.getNum_clusters();
//        this.area_average = 0;
//        this.load_average = 0;

        this.variance_total = 0;
        this.distance_total = 0;
        this.familiarity_total = 0;
        int num_clusters = input.getNum_clusters();
        this.customer_totals = new ArrayList<>();
        this.demand_totals = new ArrayList<>();
        for (int i = 0; i < num_clusters; i++) {
            customer_totals.add(0.0);
            demand_totals.add(0.0);
        }
    }

    public double calculate() {
        value = distance_total * distance_weight + variance_total * customer_demand_weight + familiarity_total * familiarity_weight;
        return value;
    }

    public void updateVariance(Solution solution) {
        BCASolution bcaSolution = (BCASolution)solution;
//        BCAInput bcaInput = (BCAInput)bcaSolution.getInput();
        variance_total = 0;
//        double customer_average = bcaInput.getCustomer_average();
//        double demand_average = bcaInput.getDemand_average();
//
//        double customer_low = customer_average * (1 - bcaInput.getCustomer_bias());
//        double customer_high = customer_average * (1 + bcaInput.getCustomer_bias());
//        double demand_low = demand_average * (1 - bcaInput.getDemand_bias());
//        double demand_high = demand_average * (1 + bcaInput.getDemand_bias());
        for (SolutionVehicle vehicle : bcaSolution.getVehicles()) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                Long cluster_id = cluster.getCluster_id();
                double cluster_customer_total = 0.0;
                double cluster_demand_total = 0.0;
                for (Long node_id : cluster.getNode_ids()) {
                    BCANode node = (BCANode)input.getNodeById(node_id);
                    double customer = node.getExpected_customers();
                    double demand = node.getExpected_demands();
                    cluster_customer_total += customer;
                    cluster_demand_total += demand;

                }
                if (cluster_customer_total < customer_low) {
                    variance_total += customer_low - cluster_customer_total;
                } else if (cluster_customer_total > customer_high) {
                    variance_total += cluster_customer_total - customer_high;
                }
                if (cluster_demand_total < demand_low) {
                    variance_total += demand_low - cluster_demand_total;
                } else if (cluster_customer_total > demand_high) {
                    variance_total += cluster_demand_total - demand_high;
                }
                customer_totals.set(cluster_id.intValue(), cluster_customer_total);
                demand_totals.set(cluster_id.intValue(), cluster_demand_total);
            }
        }
    }

    public void updateVarianceAndCalculate(Solution solution) {
        updateVariance(solution);
        calculate();
    }

    public void updateFamiliarity(Solution solution) {
        BCASolution bcaSolution = (BCASolution)solution;
        familiarity_total = 0;
        for (SolutionVehicle vehicle : bcaSolution.getVehicles()) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                for (Long node_id : cluster.getNode_ids()) {
                    BCANode node = (BCANode)input.getNodeById(node_id);
                    familiarity_total += node.getFamiliarity(vehicle.getVehicle_id()) - 1;
                }
            }
        }
    }

    public void updateFamiliarityAndCalculate(Solution solution) {
        updateFamiliarity(solution);
        calculate();
    }

    public void updateDistance(Solution solution) {
        BCASolution bcaSolution = (BCASolution)solution;
        distance_total = 0;
        for (SolutionVehicle vehicle : bcaSolution.getVehicles()) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                Long center_id = cluster.getCenter_id();
                for (Long node_id : cluster.getNode_ids()) {
                    double distance = input.getNodeDistance(center_id, node_id);
                    distance_total += distance;
                }
            }
        }
    }

    public void updateDistanceAndCalculate(Solution solution) {
        updateDistance(solution);
        calculate();
    }

    private double calcCustomerVariance(double customer_total) {
        if (customer_total < customer_low) {
            return customer_low - customer_total;
        } else if (customer_total > customer_high) {
            return customer_total - customer_high;
        }
        return 0;
    }

    private double calcDemandVariance(double demand_total) {
        if (demand_total < demand_low) {
            return demand_low - demand_total;
        } else if (demand_total > demand_high) {
            return demand_total - demand_high;
        }
        return 0;
    }

    public double updateCluster(Solution solution, Long vehicle_id, List<Long> inserted, List<Long> removed) {
//        BCASolution bcaSolution = (BCASolution)solution;

        double customer_total = customer_totals.get(vehicle_id.intValue());
        double demand_total = demand_totals.get(vehicle_id.intValue());
        SolutionCluster solution_cluster = solution.getSolutionClusterById(vehicle_id);
        variance_total -= calcCustomerVariance(customer_total) + calcDemandVariance(demand_total);
        for (Long node_id : inserted) {
            double expected_customers = ((BCANode) input.getNodeById(node_id)).getExpected_customers();
            double expected_demands = ((BCANode) input.getNodeById(node_id)).getExpected_demands();
            customer_total += expected_customers;
            demand_total += expected_demands;
            distance_total += input.getNodeDistance(solution_cluster.getCenter_id(), node_id);
            familiarity_total += ((BCAVehicle) input.getVehicleById(vehicle_id)).getFamiliarity(node_id) - 1;
        }
        for (Long node_id : removed) {
            double expected_customers = ((BCANode) input.getNodeById(node_id)).getExpected_customers();
            double expected_demands = ((BCANode) input.getNodeById(node_id)).getExpected_demands();
            customer_total -= expected_customers;
            demand_total -= expected_demands;
            distance_total -= input.getNodeDistance(solution_cluster.getCenter_id(), node_id);
            familiarity_total -= ((BCAVehicle) input.getVehicleById(vehicle_id)).getFamiliarity(node_id) - 1;
        }
        customer_totals.set(vehicle_id.intValue(), customer_total);
        demand_totals.set(vehicle_id.intValue(), demand_total);
        variance_total += calcCustomerVariance(customer_total) + calcDemandVariance(demand_total);


        return calculate();
    }
    public double updateClusterAcyclic(Solution solution, List<Long> vehicle_ids, List<Long> path) {
        List<SolutionCluster> solution_clusters = new ArrayList<>();
        Set<Long> vehicle_set = new HashSet<>();
        for (Long vehicle_id : vehicle_ids) {
            if (vehicle_set.contains(vehicle_id)) {
                continue;
            }
            vehicle_set.add(vehicle_id);
        }
        for (Long vehicle_id : vehicle_ids) {
            solution_clusters.add(solution.getSolutionClusterById(vehicle_id));
        }

        double obj = 0;
        for (Long vehicle_id : vehicle_set) {
            obj -= customer_demand_weight * (calcCustomerVariance(customer_totals.get(vehicle_id.intValue())) +
                    calcDemandVariance(demand_totals.get(vehicle_id.intValue())));
        }

        for (int i = 0; i < path.size(); i++) {
            Long node_id = path.get(i);
            Long vehicle_id = vehicle_ids.get(i);
            SolutionCluster solution_cluster = solution_clusters.get(i);

            if (i + 1 < path.size()) {
                Long next_vehicle_id = vehicle_ids.get(i + 1);
                SolutionCluster next_solution_cluster = solution_clusters.get(i + 1);
                obj += distance_weight * (input.getNodeDistance(next_solution_cluster.getCenter_id(), node_id) -
                        input.getNodeDistance(solution_cluster.getCenter_id(), node_id));
                obj += familiarity_weight * (((BCAVehicle) input.getVehicleById(next_vehicle_id)).getFamiliarity(node_id) -
                        ((BCAVehicle) input.getVehicleById(vehicle_id)).getFamiliarity(node_id));
            }

            double customer_total = customer_totals.get(vehicle_id.intValue());
            double demand_total = demand_totals.get(vehicle_id.intValue());
            if (i + 1 < path.size()) {
                customer_total -= ((BCANode) input.getNodeById(node_id)).getExpected_customers();
                demand_total -= ((BCANode) input.getNodeById(node_id)).getExpected_demands();
            }
            if (i > 0) {
                Long prev_node_id = path.get(i - 1);
                customer_total += ((BCANode) input.getNodeById(prev_node_id)).getExpected_customers();
                demand_total += ((BCANode) input.getNodeById(prev_node_id)).getExpected_demands();
            }
            customer_totals.set(vehicle_id.intValue(), customer_total);
            demand_totals.set(vehicle_id.intValue(), demand_total);
        }

        for (Long vehicle_id : vehicle_set) {
            obj += customer_demand_weight * (calcCustomerVariance(customer_totals.get(vehicle_id.intValue())) +
                    calcDemandVariance(demand_totals.get(vehicle_id.intValue())));
        }
        for (int i = 0; i < path.size(); i++) {
            Long node_id = path.get(i);
            Long vehicle_id = vehicle_ids.get(i);
            double customer_total = customer_totals.get(vehicle_id.intValue());
            double demand_total = demand_totals.get(vehicle_id.intValue());
            if (i + 1 < path.size()) {
                customer_total += ((BCANode) input.getNodeById(node_id)).getExpected_customers();
                demand_total += ((BCANode) input.getNodeById(node_id)).getExpected_demands();
            }
            if (i > 0) {
                Long prev_node_id = path.get(i - 1);
                customer_total -= ((BCANode) input.getNodeById(prev_node_id)).getExpected_customers();
                demand_total -= ((BCANode) input.getNodeById(prev_node_id)).getExpected_demands();
            }
            customer_totals.set(vehicle_id.intValue(), customer_total);
            demand_totals.set(vehicle_id.intValue(), demand_total);
        }

        return obj;
    }

    public double updateCluster(Solution solution, List<Long> vehicle_ids, List<Long> path) {
//        BCASolution bcaSolution = (BCASolution)solution;

        List<SolutionCluster> solution_clusters = new ArrayList<>();
        Set<Long> vehicle_set = new HashSet<>();
        for (Long vehicle_id : vehicle_ids) {
            if (vehicle_set.contains(vehicle_id)) {
                continue;
            }
            vehicle_set.add(vehicle_id);
        }
        for (Long vehicle_id : vehicle_ids) {
            solution_clusters.add(solution.getSolutionClusterById(vehicle_id));
        }

        double obj = 0;
        for (Long vehicle_id : vehicle_set) {
//            variance_total -= calcCustomerVariance(customer_totals.get(vehicle_id.intValue())) +
//                    calcDemandVariance(demand_totals.get(vehicle_id.intValue()));
            obj -= customer_demand_weight * (calcCustomerVariance(customer_totals.get(vehicle_id.intValue())) +
                    calcDemandVariance(demand_totals.get(vehicle_id.intValue())));
        }

        for (int i = 0; i < path.size(); i++) {
            int j = i + 1 < path.size() ? i + 1 : 0;
            int k = i > 0 ? i - 1 : path.size() - 1;

            Long node_id = path.get(i);
            Long prev_node_id = path.get(k);
            Long vehicle_id = vehicle_ids.get(i);
            Long next_vehicle_id = vehicle_ids.get(j);
            SolutionCluster solution_cluster = solution_clusters.get(i);
            SolutionCluster next_solution_cluster = solution_clusters.get(j);

            double customer_total = customer_totals.get(vehicle_id.intValue());
            double demand_total = demand_totals.get(vehicle_id.intValue());

//            distance_total -= input.getNodeDistance(solution_cluster.getCenter_id(), node_id);
//            familiarity_total -= ((BCAVehicle) input.getVehicleById(vehicle_id)).getFamiliarity(node_id) - 1;
//            distance_total += input.getNodeDistance(next_solution_cluster.getCenter_id(), node_id);
//            familiarity_total += ((BCAVehicle) input.getVehicleById(next_vehicle_id)).getFamiliarity(node_id) - 1;
            obj += distance_weight * (input.getNodeDistance(next_solution_cluster.getCenter_id(), node_id) -
                    input.getNodeDistance(solution_cluster.getCenter_id(), node_id));
            obj += familiarity_weight * (((BCAVehicle) input.getVehicleById(next_vehicle_id)).getFamiliarity(node_id) -
                    ((BCAVehicle) input.getVehicleById(vehicle_id)).getFamiliarity(node_id));

            customer_total -= ((BCANode) input.getNodeById(node_id)).getExpected_customers();
            demand_total -= ((BCANode) input.getNodeById(node_id)).getExpected_demands();
            customer_total += ((BCANode) input.getNodeById(prev_node_id)).getExpected_customers();
            demand_total += ((BCANode) input.getNodeById(prev_node_id)).getExpected_demands();

            customer_totals.set(vehicle_id.intValue(), customer_total);
            demand_totals.set(vehicle_id.intValue(), demand_total);
        }

        for (Long vehicle_id : vehicle_set) {
//            variance_total += calcCustomerVariance(customer_totals.get(vehicle_id.intValue())) +
//                    calcDemandVariance(demand_totals.get(vehicle_id.intValue()));
            obj += customer_demand_weight * (calcCustomerVariance(customer_totals.get(vehicle_id.intValue())) +
                    calcDemandVariance(demand_totals.get(vehicle_id.intValue())));
        }
        for (int i = 0; i < path.size(); i++) {
            int k = i > 0 ? i - 1 : path.size() - 1;

            Long node_id = path.get(i);
            Long prev_node_id = path.get(k);
            Long vehicle_id = vehicle_ids.get(i);

            double customer_total = customer_totals.get(vehicle_id.intValue());
            double demand_total = demand_totals.get(vehicle_id.intValue());

            customer_total += ((BCANode) input.getNodeById(node_id)).getExpected_customers();
            demand_total += ((BCANode) input.getNodeById(node_id)).getExpected_demands();
            customer_total -= ((BCANode) input.getNodeById(prev_node_id)).getExpected_customers();
            demand_total -= ((BCANode) input.getNodeById(prev_node_id)).getExpected_demands();

            customer_totals.set(vehicle_id.intValue(), customer_total);
            demand_totals.set(vehicle_id.intValue(), demand_total);
        }
//        for (int i = 0; i < path.size(); i++) {
//            Long vehicle_id = vehicle_ids.get(i);
//            variance_total += calcCustomerVariance(customer_totals.get(vehicle_id.intValue())) +
//                    calcDemandVariance(demand_totals.get(vehicle_id.intValue()));
//        }

//        return calculate();
        return obj;
    }
    public double update(Solution solution) {
        BCASolution bcaSolution = (BCASolution)solution;
        variance_total = 0;
        familiarity_total = 0;
        distance_total = 0;


//        BCAInput bcaInput = (BCAInput)bcaSolution.getInput();
//        double customer_average = bcaInput.getCustomer_average();
//        double demand_average = bcaInput.getDemand_average();
//
//        double customer_low = customer_average * (1 - bcaInput.getCustomer_bias());
//        double customer_high = customer_average * (1 + bcaInput.getCustomer_bias());
//        double demand_low = demand_average * (1 - bcaInput.getDemand_bias());
//        double demand_high = demand_average * (1 + bcaInput.getDemand_bias());
//        System.err.print(customer_low + " " + customer_high + " " + demand_low + " " + demand_high + " ");
        for (SolutionVehicle vehicle : bcaSolution.getVehicles()) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                Long center_id = cluster.getCenter_id();
                double cluster_customer_total = 0.0;
                double cluster_demand_total = 0.0;
                for (Long node_id : cluster.getNode_ids()) {
                    BCANode node = (BCANode)input.getNodeById(node_id);

                    double customer = node.getExpected_customers();
                    double demand = node.getExpected_demands();
                    cluster_customer_total += customer;
                    cluster_demand_total += demand;


                    familiarity_total += node.getFamiliarity(vehicle.getVehicle_id()) - 1;
                    double distance = input.getNodeDistance(center_id, node_id);
                    distance_total += distance;
                }
                if (cluster_customer_total < customer_low) {
                    variance_total += customer_low - cluster_customer_total;
                } else if (cluster_customer_total > customer_high) {
                    variance_total += cluster_customer_total - customer_high;
                }

                if (cluster_demand_total < demand_low) {
                    variance_total += demand_low - cluster_demand_total;
                } else if (cluster_demand_total > demand_high) {
                    variance_total += cluster_demand_total - demand_high;
                }

                int cluster_id = cluster.getCluster_id().intValue();
                customer_totals.set(cluster_id, cluster_customer_total);
                demand_totals.set(cluster_id, cluster_demand_total);
            }
        }
//        System.err.println(" " + variance_total + " " + familiarity_total + " " + distance_total);
        return calculate();
    }


    public SolutionObjective copy() {
        BCASolutionObjective clone = new BCASolutionObjective();
        clone.input = this.input;
        clone.value = this.value;

        clone.customer_demand_weight = this.customer_demand_weight;
        clone.familiarity_weight = this.familiarity_weight;
        clone.distance_weight = this.distance_weight;

//        clone.customer_bias = this.customer_bias;
//        clone.demand_bias = this.demand_bias;
//        clone.customer_average = this.customer_average;
//        clone.demand_average = this.demand_average;
//        clone.area_average = this.area_average;
//        clone.load_average = this.load_average;

        clone.variance_total = this.variance_total;
        clone.distance_total = this.distance_total;
        clone.familiarity_total = this.familiarity_total;

        clone.customer_totals = new ArrayList<>(this.customer_totals);
        clone.demand_totals = new ArrayList<>(this.demand_totals);
        clone.customer_average = this.customer_average;
        clone.demand_average = this.demand_average;
        clone.customer_low = this.customer_low;
        clone.customer_high = this.customer_high;

        return clone;
    }

    public String toString() {
        StringBuilder str = new StringBuilder("solution objective : {\n");
        str.append("\tvalue : ").append(value).append(",\n");
        str.append("\tvariance_total : ").append(variance_total).append(",\n");
        str.append("\tdistance_total : ").append(distance_total).append(",\n");
        str.append("\tfamiliarity_total : ").append(familiarity_total).append(",\n");
        str.append("\tcustomer_totals : ").append(customer_totals).append(",\n");
        str.append("\tdemand_totals : ").append(demand_totals).append("\n");
        str.append("}\n");
        return str.toString();
    }
}
