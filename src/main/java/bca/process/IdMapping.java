package bca.process;

import bca.entity.input.*;

import java.util.HashMap;

public class IdMapping {
    public static void process(Input input) {
        HashMap<Long, Node> node_map = new HashMap<>();
        for (Node node : input.getNodes()) {
            if (node.getNode_id() != null) {
                if (node_map.containsKey(node.getNode_id())) {
                    System.err.println("Duplicate node ID found: " + node.getNode_id());
                    continue;
                }
                node_map.put(node.getNode_id(), node);
            }
            else {
                System.err.println("Node ID is null for node: " + node);
            }
        }
        input.setNode_map(node_map);

        HashMap<Long, Vehicle> vehicle_map = new HashMap<>();
        for (Vehicle vehicle : input.getVehicles()) {
            if (vehicle.getVehicle_id() != null) {
                if (vehicle_map.containsKey(vehicle.getVehicle_id())) {
                    System.err.println("Duplicate vehicle ID found: " + vehicle.getVehicle_id());
                    continue;
                }
                vehicle_map.put(vehicle.getVehicle_id(), vehicle);
            }
            else {
                System.err.println("Vehicle ID is null for vehicle: " + vehicle);
            }
        }
        input.setVehicle_map(vehicle_map);
    }
}
