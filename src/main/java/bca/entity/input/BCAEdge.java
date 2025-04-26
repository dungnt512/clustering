package bca.entity.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BCAEdge implements Edge {
    private Long edge_id;
    private Long from_node_id;
    private Long to_node_id;
    private double distance;


    public Long getRemaining(Long current) {
        return current.equals(from_node_id) ? to_node_id : from_node_id;
    }
    public String toString() {
        return "Edge{" +
                "edge_id=" + edge_id +
                ", from_node_id=" + from_node_id +
                ", to_node_id=" + to_node_id +
                ", distance=" + distance +
                '}';
    }
}
