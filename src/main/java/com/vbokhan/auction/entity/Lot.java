package com.vbokhan.auction.entity;

import com.vbokhan.auction.exception.AuctionException;
import com.vbokhan.auction.generator.IdGenerator;
import com.vbokhan.auction.state.IState;
import com.vbokhan.auction.state.StartState;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by vbokh on 11.06.2017.
 */
public class Lot {
    public static final long TIME_FOR_TRADING = 4 * 1000;
    private static final Logger LOGGER = LogManager.getLogger();
    private Integer id;
    private String lotName;
    private Double lotPrice;
    private List<Client> participatingClients;
    private Map<Client, Double> bids;
    private Double percentOfInitialPrice = 0.0;
    private IState state;
    private static CyclicBarrier barrier;
    private Semaphore semaphore;
    private ReentrantLock lock = new ReentrantLock();
    private static Phaser phaser;
    private static Semaphore semaphoreForClients = new Semaphore(1);

    public Lot(String lotName, Double startPrice) {
        id = IdGenerator.nextId();
        this.lotName = lotName;
        bids = new HashMap<>();
        lotPrice = startPrice;
        participatingClients = new ArrayList<>();
        state = new StartState();
    }

    public void startTrading() throws AuctionException {
        state.trade(this);
    }

    public void cancelTrading() {
        try {
            state.cancelTrade(this);
        } catch (AuctionException e) {
            LOGGER.log(Level.INFO, e.getMessage());
        }
    }

    public void tradeLot() throws AuctionException {
        LOGGER.log(Level.INFO, "Clients, participating in trade : " + participatingClients);

        int numberOfParticipants = participatingClients.size();
        barrier = new CyclicBarrier(numberOfParticipants, () ->
                cancelTrading()
        );
        long timeWhenTradingStarted = System.currentTimeMillis();

        phaser = new Phaser(numberOfParticipants);
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

    private void prepareClientForTrading(long timeWhenTradingStarted, Client client) {
        client.addLot(this);
        client.setBarrier(barrier);
        client.setPhaser(phaser);
        client.setSemaphore(semaphoreForClients);
        client.setTimeWhenTradingStarted(timeWhenTradingStarted);
    }

    public Map<Client, Double> getBids() {
        return bids;
    }

    public void setNewBids(Client client, Double bid) {
        bids.put(client, bid);
    }

    public void addClient(Client client) {
        participatingClients.add(client);
    }

    public Integer getLotId() {
        return id;
    }

    public String getLotName() {
        return lotName;
    }

    public void setLotName(String name) {
        this.lotName = name;
    }

    public Double getLotPrice() {
        return lotPrice;
    }

    public List<Client> getParticipatingClients() {
        return participatingClients;
    }

    public void setParticipatingClients(List<Client> participatingClients) {

        this.participatingClients = participatingClients;

    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void setLotPrice(Double lotPrice) {
        lock.lock();
        this.lotPrice = lotPrice;
        lock.unlock();
    }

    public Double getPercentOfInitialPrice() {
        return percentOfInitialPrice;
    }

    public void setPercentOfInitialPrice(Double percentOfInitialPrice) {
        lock.lock();
        this.percentOfInitialPrice = percentOfInitialPrice;
        lock.unlock();
    }

    public IState getLotState() {
        return state;
    }

    public void setLotState(IState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Lot{" +
                "id=" + id +
                ", lotName='" + lotName + '\'' +
                ", lotPrice=" + lotPrice +
                '}';
    }
}
