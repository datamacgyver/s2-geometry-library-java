package org.proagrica.wallys2.gateways;

import py4j.GatewayServer;

public class GeometricEntryPoint {

    public GeomProcessor getGeom() {
        return new GeomProcessor();
    }

    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new GeometricEntryPoint());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }

}