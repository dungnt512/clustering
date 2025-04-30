package bca;

import bca.algorithm.vlsn.process.BCAGreedyAlgorithm;
import bca.algorithm.vlsn.process.GreedyAlgorithm;
import bca.entity.input.BCAInput;
import bca.entity.input.Input;
import bca.entity.solution.Solution;
import bca.process.Kattio;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException {
        File folder = new File("DP-FWD/input/L2");
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".dat")) {
//                System.out.println("Processing file: " + file.getName() + " " + file.getParent() + " " + file.getAbsolutePath() + " " + file.getPath());
                System.err.println("Processing file: " + file.getPath());
                Input input = new BCAInput(file.getPath(), true);
//                System.err.println(input);
                GreedyAlgorithm greedyAlgorithm = new BCAGreedyAlgorithm(100);
                Solution solution = greedyAlgorithm.solve(input);

                Kattio kattio = new Kattio(null,
                        file.getPath().replace(".dat", ".out"));
                kattio.println(solution);
//                System.out.println(input);
                System.err.println(solution);
                kattio.close();
                break;
            }
        }
    }
}