package com.vbokhan.auction.entity;

import com.vbokhan.auction.exception.AuctionException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class Auction {
    private static final Logger LOGGER = LogManager.getLogger();
    private static List<Lot> tradingLots;
    private static List<Client> registeredClients;
    private Semaphore semaphore = new Semaphore(1);
    private static AtomicBoolean isCreated = new AtomicBoolean(false);
    private static Auction instance = null;
    private static ReentrantLock lock = new ReentrantLock();

    private Auction() {
        this.tradingLots = new ArrayList<>();
        this.registeredClients = new ArrayList<>();
    }

    public static Auction getInstance() {
        try {
            if (!isCreated.get()) {
                lock.lock();
                LOGGER.log(Level.INFO, "Instance of auction is created");
                instance = new Auction();
                isCreated = new AtomicBoolean(true);
            }
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
        return instance;
    }

    public void startTrade() {
        for (Lot lot : tradingLots) {
            try {
                if (tradingLots.isEmpty()) {
                    throw new AuctionException("No lots were exhibited for trading");
                }
                semaphore.acquire();
                prepareLotForTrading(lot);
                lot.startTrading();
            } catch (InterruptedException e) {
                LOGGER.log(Level.INFO, e.getMessage());
            } catch (AuctionException e) {
                LOGGER.log(Level.INFO, e.getMessage());
            }
        }
    }

    private void prepareLotForTrading(Lot lot) {
        for (Client client : registeredClients) {
            if (isParticipating()) {
                lot.addClient(client);
            }
        }
    }

    public void setTradingLots(List<Lot> tradingLots) {
        this.tradingLots.clear();
        this.tradingLots = tradingLots;
        for (Lot lot : tradingLots) {
            lot.setSemaphore(semaphore);
        }
    }

    public void setParticipatingClients(List<Client> participatingClients) {
        this.registeredClients.clear();
        this.registeredClients = participatingClients;
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
            getInstance().setTradingLots(lots);
        }

        public static void fillAuctionWithClients(List<Client> clients) {
            getInstance().setParticipatingClients(clients);
        }
    }
}
