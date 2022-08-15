package io.github.gaol.tests.netty;

import io.netty.channel.epoll.Epoll;

public class EPollMain {
    public static void main(String[] args) throws Exception {
        if (Epoll.isAvailable()) {
            System.out.println("ALL GOOD: epoll is Available");
          } else {
            System.out.println("NOT GOOD: epoll is NOT Available");
            Epoll.unavailabilityCause().printStackTrace();
          }
    }

}
