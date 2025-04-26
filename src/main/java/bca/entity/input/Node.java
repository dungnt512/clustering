package bca.entity.input;

import java.util.List;

public interface Node {
    Long getNode_id();
    List<Long> getAdjacent_edges();
}
