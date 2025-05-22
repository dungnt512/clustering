package bca.algorithm.vlsn.operator;

import bca.entity.input.Input;
import bca.entity.solution.Solution;
import bca.entity.solution.SolutionCluster;
import bca.entity.solution.SolutionVehicle;
import bca.process.Kattio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QAPNeighborhoodSearch {
    private Kattio kattio;
    public QAPNeighborhoodSearch(Kattio kattio) {
        this.kattio = kattio;
    }

    public void solve(Solution solution /*initial greedy solution*/, Input input) {
        Map<Long, Long> node_cluster_map = new HashMap<>();
        for (SolutionVehicle vehicle : solution.getVehicles()) {
            for (SolutionCluster cluster : vehicle.getClusters()) {
                for (Long nodeId : cluster.getNode_ids()) {
                    node_cluster_map.put(nodeId, cluster.getCluster_id());
                }
            }
        }
        KExchangeSearch kExchangeSearch = new KExchangeSearch(5, input, solution, node_cluster_map);
        KExchangeAcyclicSearch kEXchangeAcyclicSearch = new KExchangeAcyclicSearch(6, input, solution, node_cluster_map);
        List<Long> kExchanges;
        List<Long> kEXchangeAcyclic;
//        System.err.println(solution);
//        solution.getObjective().update(solution);
        kattio.println(solution.getObjective());
        System.err.println(solution.getObjective());
        long startTime = System.currentTimeMillis();

//        while ((kExchanges = kExchangeSearch.search()) != null) {
        boolean ok;
        do {
            ok = false;
            while ((kExchanges = kExchangeSearch.search()) != null) {
                kExchangeSearch.move(kExchanges);
                System.err.print("\nkExchanges : [");
                for (Long node_id : kExchanges) System.err.print(node_id + ",");
                System.err.println("]");
                System.err.println(solution.getObjective());
                kattio.println(solution.getObjective());
                long currentTime = System.currentTimeMillis();
                kattio.println("Running: " + (currentTime - startTime) + "ms");
                startTime = currentTime;
                ok = true;
            }

//            System.err.println(solution);
            while ((kEXchangeAcyclic = kEXchangeAcyclicSearch.search()) != null) {
                System.err.print("\nkEXchangeAcyclic : [");
//                System.err.println(solution);
                kEXchangeAcyclicSearch.move(kEXchangeAcyclic);
                for (Long node_id : kEXchangeAcyclic) System.err.print(node_id + ",");
                System.err.println("]");
                System.err.println(solution.getObjective());
                kattio.println(solution.getObjective());
                long currentTime = System.currentTimeMillis();
                kattio.println("Running: " + (currentTime - startTime) + "ms");
//                System.err.println(solution);
                startTime = currentTime;
                ok = true;
            }
//            System.err.println(solution);

        } while (ok);
    }
}
