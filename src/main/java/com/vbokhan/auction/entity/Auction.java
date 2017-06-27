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
    private static final Integer MAX_LOTS_TRADING_AT_ONE_MOMENT = 1;
    private static List<Lot> tradingLots;
    private static List<Client> registeredClients;
    private Semaphore semaphoreForLotTrade;
    private static AtomicBoolean isAuctionCreated = new AtomicBoolean(false);
    private static Auction instance;
    private static ReentrantLock lock = new ReentrantLock();

    private Auction() {
        this.tradingLots = new ArrayList<>();
        this.registeredClients = new ArrayList<>();
        this.semaphoreForLotTrade = new Semaphore(MAX_LOTS_TRADING_AT_ONE_MOMENT);
    }

    public static Auction getInstance() {
        if (!isAuctionCreated.get()) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new Auction();
                    isAuctionCreated.set(true);
                    LOGGER.log(Level.INFO, "Instance of auction is created");
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public void beginAuction() {
        try {
            if (isLotsNotRegistered()) {
                throw new AuctionException("No lots were exhibited for trade");
            }
            if (isClientsNotRegisteredInAuction()) {
                throw new AuctionException("No client were registered for participating in auction");
            }
            for (Lot lot : tradingLots) {
                semaphoreForLotTrade.acquire();
                prepareLotForTrading(lot);
                lot.tradeLot();
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.ERROR, e.getMessage());
        } catch (AuctionException e) {
            LOGGER.log(Level.ERROR, e.getMessage());
        }
    }

    public void setTradingLots(List<Lot> tradingLots) {
        lock.lock();
        try {
            this.tradingLots.clear();
            this.tradingLots = tradingLots;
            for (Lot lot : tradingLots) {
                lot.setSemaphoreForTradingLot(semaphoreForLotTrade);
            }
        } finally {
            lock.unlock();
        }
    }

    public void setParticipatingClients(List<Client> participatingClients) {
        lock.lock();
        try {
            this.registeredClients.clear();
            this.registeredClients = participatingClients;
        } finally {
            lock.unlock();
        }
    }

    private void prepareLotForTrading(Lot lot) {
        for (Client client : registeredClients) {
            if (isParticipating()) {
                lot.addClient(client);
                setDefaultTimeForTradingLot(lot);
            }
        }
    }

    private boolean isLotsNotRegistered() {
        return tradingLots == null || tradingLots.isEmpty();
    }

    private boolean isClientsNotRegisteredInAuction() {
        return registeredClients == null || registeredClients.isEmpty();
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

    private void setDefaultTimeForTradingLot(Lot lot) {
        int i = new Random().nextInt(10);
        Long timeForTrading = Long.valueOf(i * 1000);
        lot.setTimeForTrading(timeForTrading);
    }
}
