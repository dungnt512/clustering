package bca.algorithm.vlsn.operator;

import bca.entity.input.Input;
import bca.entity.solution.Solution;
import bca.process.Kattio;

import java.util.ArrayList;
import java.util.List;

public class QAPNeighborhoodSearch {
    private Kattio kattio;
    public QAPNeighborhoodSearch(Kattio kattio) {
        this.kattio = kattio;
    }

    public void solve(Solution solution /*initial greedy solution*/, Input input) {
        KExchangeSearch kExchangeSearch = new KExchangeSearch(5, input, solution);
        List<Long> kExchanges = new ArrayList<>();
//        System.err.println(solution);
//        solution.getObjective().update(solution);
        kattio.println(solution.getObjective());
        System.err.println(solution.getObjective());
        long startTime = System.currentTimeMillis();
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
        }
    }
}
