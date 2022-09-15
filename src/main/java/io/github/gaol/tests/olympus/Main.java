package io.github.gaol.tests.olympus;

import org.apache.activemq.artemis.core.server.NetworkHealthCheck;

import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        NetworkHealthCheck healthCheck = new NetworkHealthCheck();
        String address = System.getenv("CHECK_ADDRESS");
        if (address == null) {
            address = "127.0.0.1";
        }
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            inetAddress = InetAddress.getByName(inetAddress.getHostName());
            System.out.println("\n================== NETWORK REACHABLE CHECK =========================\n");
            System.out.println("Reachable check address: " + address + "\n");
            boolean reachable = inetAddress.isReachable(1000);
            System.out.println("Result: " + reachable);
            System.out.println("\n================== THE END =========================\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n================== NETWORK HEALTH CHECK =========================\n");
        System.out.println("Health check address: " + address + "\n");
        boolean result = healthCheck.check(address);
        System.out.println("Result: " + result);
        System.out.println("\n================== THE END =========================\n");
    }

}
