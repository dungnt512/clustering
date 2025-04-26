package bca.entity.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BCANode implements Node {
    private Long node_id;
    private double latitude;
    private double longitude;
    private double expected_customers;
    private double expected_demands;
    private double area;
    private double load;
    private List<Long> adjacent_edges;
    private List<BCAFamiliarity> familiarities;

    public BCANode(Long node_id) {
        this.node_id = node_id;
        this.adjacent_edges = new ArrayList<>();
        this.familiarities = new ArrayList<>();
    }

    public int getFamiliarity(Long vehicle_id) {
        if (vehicle_id >= familiarities.size() || vehicle_id < 0) {
            return Integer.MAX_VALUE;
        }
        BCAFamiliarity familiarity = familiarities.get(vehicle_id.intValue());
        assert familiarity != null;
        assert Objects.equals(familiarity.getVehicle_id(), vehicle_id);
        return familiarity.getFamiliarity();
    }

    public String toString() {
        return "Node{" +
                "node_id=" + node_id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", expected_customers=" + expected_customers +
                ", expected_demands=" + expected_demands +
                ", area=" + area +
                ", load=" + load +
                ", adjacent_edges=" + adjacent_edges +
                ", familiarities=" + familiarities +
                '}';
    }
}
