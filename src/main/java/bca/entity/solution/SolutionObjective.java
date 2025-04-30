package bca.entity.solution;

public interface SolutionObjective {
    double getValue();
    @SuppressWarnings("UnusedReturnValue")
    double calculate();
    double update(Solution solution);
    SolutionObjective copy();
}
