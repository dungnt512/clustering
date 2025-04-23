package bca.entity.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BCAFamiliarity {
    private Long vehicle_id;
    private Long node_id;
    private int familiarity;
}
