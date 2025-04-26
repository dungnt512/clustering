package bca.entity.solution;

public interface SolutionObjective {
    double getObjective();
    void calculate();
    void update(Solution solution);
    SolutionObjective copy();
}
