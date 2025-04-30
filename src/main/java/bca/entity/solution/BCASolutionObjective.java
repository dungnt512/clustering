package bca.entity.solution;

import bca.entity.input.BCAInput;
import bca.entity.input.BCANode;
import bca.entity.input.Input;
import bca.entity.input.Node;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public BCASolutionObjective(Input input, double customer_demand_weight, double familiarity_weight, double distance_weight) {
        this.input = input;
        this.value = Double.MAX_VALUE;

        this.customer_demand_weight = customer_demand_weight;
        this.familiarity_weight = familiarity_weight;
        this.distance_weight = distance_weight;

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
    }

    public double calculate() {
        value = distance_total * distance_weight + variance_total * customer_demand_weight + familiarity_total * familiarity_weight;
        return value;
    }

    public void updateVariance(Solution solution) {
        BCASolution bcaSolution = (BCASolution)solution;
        BCAInput bcaInput = (BCAInput)bcaSolution.getInput();
        variance_total = 0;
        double customer_average = bcaInput.getCustomer_average();
        double demand_average = bcaInput.getDemand_average();

        double customer_low = customer_average * (1 - bcaInput.getCustomer_bias());
        double customer_high = customer_average * (1 + bcaInput.getCustomer_bias());
        double demand_low = demand_average * (1 - bcaInput.getDemand_bias());
        double demand_high = demand_average * (1 + bcaInput.getDemand_bias());
        for (SolutionVehicle vehicle : bcaSolution.getVehicles()) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                for (Long node_id : cluster.getNode_ids()) {
                    BCANode node = (BCANode)input.getNodeById(node_id);
                    double customer = node.getExpected_customers();
                    double demand = node.getExpected_demands();
                    if (customer < customer_low) {
                        variance_total += customer_low - customer;
                    } else if (customer > customer_high) {
                        variance_total += customer - customer_high;
                    }
                    if (demand < demand_low) {
                        variance_total += demand_low - demand;
                    } else if (demand > demand_high) {
                        variance_total += demand - demand_high;
                    }
                }
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

    public double update(Solution solution) {
        BCASolution bcaSolution = (BCASolution)solution;
        BCAInput bcaInput = (BCAInput)bcaSolution.getInput();
        variance_total = 0;
        familiarity_total = 0;
        distance_total = 0;


        double customer_average = bcaInput.getCustomer_average();
        double demand_average = bcaInput.getDemand_average();

        double customer_low = customer_average * (1 - bcaInput.getCustomer_bias());
        double customer_high = customer_average * (1 + bcaInput.getCustomer_bias());
        double demand_low = demand_average * (1 - bcaInput.getDemand_bias());
        double demand_high = demand_average * (1 + bcaInput.getDemand_bias());
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

        return clone;
    }
}
