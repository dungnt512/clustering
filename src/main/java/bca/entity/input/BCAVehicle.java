package bca.entity.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    public String toString() {
        return "Vehicle{" +
                "vehicle_id=" + vehicle_id +
                ", familiarities=" + familiarities +
                '}';
    }
}
