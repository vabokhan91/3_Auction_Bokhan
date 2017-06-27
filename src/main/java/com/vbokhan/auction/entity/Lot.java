package com.vbokhan.auction.entity;

import com.vbokhan.auction.exception.AuctionException;
import com.vbokhan.auction.generator.IdGenerator;
import com.vbokhan.auction.state.IState;
import com.vbokhan.auction.state.StartState;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by vbokh on 11.06.2017.
 */
public class Lot {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Integer MAX_BIDS_CLIENTS_SPECIFY_AT_ONE_MOMENT = 1;
    private Integer id;
    private String lotName;
    private Double lotPrice;
    private Long timeForTrading;
    private List<Client> participatingClients;
    private Map<Client, Double> clientBids;
    private Map<Client, Double> winner;
    private Double percentOfInitialPrice = 0.0;
    private IState state;
    private static CyclicBarrier barrierForClientBids;
    private Semaphore semaphoreForTradingLot;
    private ReentrantLock lock;
    private static Phaser phaser;
    private static Semaphore semaphoreForClients;

    public Lot(String lotName, Double startPrice) {
        id = IdGenerator.nextId();
        this.lotName = lotName;
        lotPrice = startPrice;
        lock = new ReentrantLock();
        clientBids = new HashMap<>();
        winner = new HashMap<>();
        participatingClients = new ArrayList<>();
        state = new StartState();
        semaphoreForClients = new Semaphore(MAX_BIDS_CLIENTS_SPECIFY_AT_ONE_MOMENT);
    }

    public void tradeLot() {
        try {
            state.trade(this);
        } catch (AuctionException e) {
            LOGGER.log(Level.ERROR, e.getMessage());
        }
    }

    public void cancelTradeLot() {
        try {
            state.cancelTrade(this);
        } catch (AuctionException e) {
            LOGGER.log(Level.ERROR, e.getMessage());
        }
    }

    public void cancel() {
        if (clientBids.isEmpty()) {
            LOGGER.log(Level.INFO, "No bids were made on this lot. No winner");
        } else {
            LOGGER.log(Level.INFO, "Defining winner ....");
            winner = defineWinner(this);
            LOGGER.log(Level.INFO, "Winner is : " + winner.keySet() +", " + this +
                    ", final price : " + winner.values());
        }
        semaphoreForTradingLot.release();
    }

    public void trade() throws AuctionException {
        LOGGER.log(Level.INFO, "Clients, participating in trading : " + participatingClients + " " + this);

        if (participatingClients.isEmpty()) {
            state.cancelTrade(this);
        } else {
            int numberOfParticipants = participatingClients.size();
            phaser = new Phaser(numberOfParticipants);
            barrierForClientBids = new CyclicBarrier(numberOfParticipants, () -> {
                cancelTradeLot();
            });
            long timeWhenTradingStarted = System.currentTimeMillis();
            if (participatingClients.size() == 1) {
                Client client = participatingClients.get(0);
                prepareClientForTrading(timeWhenTradingStarted, client);
                new Thread(client).start();
            } else {
                for (Client client : participatingClients) {
                    prepareClientForTrading(timeWhenTradingStarted, client);
                    new Thread(client).start();
                }
            }
        }
    }

    public long getTimeForTrading() {
        return timeForTrading;
    }

    public void setTimeForTrading(long millis) {
        timeForTrading = millis;
    }

    public Map<Client, Double> getClientBids() {
        return clientBids;
    }

    public void addBid(Client client, Double bid) {
        clientBids.put(client, bid);
    }

    public void addClient(Client client) {
        participatingClients.add(client);
    }

    public String getLotName() {
        return lotName;
    }

    public Double getLotPrice() {
        return lotPrice;
    }

    public Semaphore getSemaphoreForTradingLot() {
        return semaphoreForTradingLot;
    }

    public void setSemaphoreForTradingLot(Semaphore semaphoreForTradingLot) {
        this.semaphoreForTradingLot = semaphoreForTradingLot;
    }

    public Double getPercentOfInitialPrice() {
        return percentOfInitialPrice;
    }

    public void setPercentOfInitialPrice(Double percentOfInitialPrice) {
        lock.lock();
        try {
            this.percentOfInitialPrice = percentOfInitialPrice;
        } finally {
            lock.unlock();
        }
    }

    public void setLotState(IState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Lot{" +
                "id=" + id +
                ", name='" + lotName + '\'' +
                ", initial price =" + lotPrice +
                '}';
    }

    private void prepareClientForTrading(long timeWhenTradingStarted, Client client) {
        client.addLot(this);
        client.setBarrier(barrierForClientBids);
        client.setPhaser(phaser);
        client.setSemaphore(semaphoreForClients);
        client.setTimeWhenTradingStarted(timeWhenTradingStarted);
    }

    private Map<Client, Double> defineWinner(Lot lot) {
        double maxBid = Collections.max(lot.getClientBids().values());
        winner = lot.getClientBids().entrySet().stream()
                .filter(entry -> entry.getValue().equals(maxBid))
                .collect(Collectors.toMap(p -> p.getKey(), v -> v.getValue()));
        winner.keySet().stream().forEach(client -> client.setCash(client.getCash() - lot.getLotPrice() * (1 + percentOfInitialPrice)));
        return winner;
    }
}
