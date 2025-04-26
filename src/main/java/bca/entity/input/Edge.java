package bca.entity.input;

public interface Edge {
    Long getEdge_id();
    Long getFrom_node_id();
    Long getTo_node_id();
    Long getRemaining(Long current);
    double getDistance();
    void setDistance(double distance);
}
