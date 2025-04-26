package bca;

import bca.entity.input.BCAInput;
import bca.entity.input.Input;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException {
        File folder = new File("DP-FWD/input/L2");
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".dat")) {
//                System.out.println("Processing file: " + file.getName() + " " + file.getParent() + " " + file.getAbsolutePath() + " " + file.getPath());
                System.out.println("Processing file: " + file.getPath());
                Input input = new BCAInput(file.getPath(), false);
            }
        }
    }
}