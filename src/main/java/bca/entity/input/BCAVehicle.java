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
public class BCAVehicle implements Vehicle {
    private Long vehicle_id;
    private List<BCAFamiliarity> familiarities;
    public BCAVehicle(Long vehicle_id) {
        this.vehicle_id = vehicle_id;
        this.familiarities = new ArrayList<>();
    }

    public int getFamiliarity(Long node_id) {
        if (node_id >= familiarities.size() || node_id < 0) {
            return Integer.MAX_VALUE;
        }
        BCAFamiliarity familiarity = familiarities.get(node_id.intValue());
        assert familiarity != null;
        assert Objects.equals(familiarity.getNode_id(), node_id);
        return familiarity.getFamiliarity();
    }

    public String toString() {
        return "Vehicle{" +
                "vehicle_id=" + vehicle_id +
                ", familiarities=" + familiarities +
                '}';
    }
}
