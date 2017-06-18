package com.vbokhan.auction.entity;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by vbokh on 11.06.2017.
 */
public class Auction {
    private List<Lot> lots;
    private List<Client> clients;
    private Semaphore semaphore;
    private static AtomicBoolean isCreated = new AtomicBoolean(false);
    private static final Logger LOGGER = LogManager.getLogger();

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
            if (!isCreated.get()) {
                LOGGER.log(Level.INFO, "Instance of auction is created");
                instance = new Auction();
                isCreated = new AtomicBoolean(true);
            }
        } finally {
            lock.unlock();
        }
        return instance;
    }

    public void startTrade() {
        for (Lot lot : lots) {
            try {
                if (semaphore.tryAcquire(20, TimeUnit.SECONDS)) {
                    for (Client client : clients) {
                        if (isParticipating()) {
                            lot.addClient(client);
                        }
                    }
                    lot.initiateTrading();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
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

   public static class AuctionFiller {
        public static void fillAuctionWithLots(List<Lot> lots) {
            getInstance().setLots(lots);
        }

        public static void fillAuctionWithClients(List<Client> clients) {
            getInstance().setClients(clients);
        }
    }
}
