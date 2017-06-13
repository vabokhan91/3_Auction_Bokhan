package com.vbokhan.auction.entity;

import com.vbokhan.auction.exception.AuctionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by vbokh on 11.06.2017.
 */
public class Auction {
    private List<Lot> lots;
    private List<Client> clients;
    private Semaphore semaphore;

    private static Auction instance = null;
    private static ReentrantLock lock = new ReentrantLock();

    private Auction() {
        this.lots = new ArrayList<>();
        this.clients = new ArrayList<>();
        semaphore = new Semaphore(1);
    }

    public static Auction getInstance() {
        lock.lock();
        try {
            if (instance == null) {
                instance = new Auction();
            }
        } finally {
            lock.unlock();
        }
        return instance;
    }

    public void startTrade() {
        for (Lot lot : lots) {
            try {
                if (semaphore.tryAcquire(5, TimeUnit.SECONDS)) {
                    for (Client client : clients) {
                        if (isParticipating()) {
                            lot.addClient(client);
                        }
                    }
                    lot.beginTrade();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (AuctionException e) {
                System.out.println(e.getMessage());
                semaphore.release();
            }
        }
    }

    public void setLots(List<Lot> lots) {
        this.lots.clear();
        this.lots = lots;
        for (Lot lot : lots) {
            lot.setSemaphore(semaphore);
        }
    }

    public void setClients(List<Client> clients) {
        this.clients.clear();
        this.clients = clients;
    }

    private boolean isParticipating() {
        boolean flag = true;
        int i = new Random().nextInt(2);
        if (i == 0) {
            flag = false;

        } else if (i == 1) {
            flag = true;
        }
        return flag;
    }
}
