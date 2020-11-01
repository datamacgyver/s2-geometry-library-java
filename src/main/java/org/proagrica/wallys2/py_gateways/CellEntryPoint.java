package org.proagrica.wallys2.gateways;

import py4j.GatewayServer;

public class CellEntryPoint {

    public CellProcessor getCell() {
        return new CellProcessor();
    }

    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new CellEntryPoint());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }

}