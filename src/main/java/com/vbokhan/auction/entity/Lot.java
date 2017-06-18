package com.vbokhan.auction.entity;

import com.vbokhan.auction.generator.IdGenerator;
import com.vbokhan.auction.state.EndState;
import com.vbokhan.auction.state.IState;
import com.vbokhan.auction.state.StartState;
import com.vbokhan.auction.state.TradingState;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by vbokh on 11.06.2017.
 */
public class Lot {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final long TIME_FOR_TRADING = 6 * 1000;
    private Integer id;
    private String name;
    private Double price;
    private double percentOfInitialPrice;
    private IState state;
    private CyclicBarrier barrier;
    private Semaphore semaphore;
    private List<Client> clients;
    private Map<Client, Double> bids;
    private ReentrantLock lock = new ReentrantLock();

    public Lot(String name, Double startPrice) {
        id = IdGenerator.nextId();
        this.name = name;
        bids = new HashMap<>();
        price = startPrice;
        clients = new ArrayList<>();
        state = new StartState();
    }

    public void run() {
        LOGGER.log(Level.INFO, "Clients, participating in trading : " + clients);
        int numberOfClients = clients.size();
        if (clients.size() == 1) {
            if (clients.get(0).getCash() >= price) {
                LOGGER.log(Level.INFO, " Winner is " + clients.get(0) + " " + this);
            } else {
                LOGGER.log(Level.INFO," No winner. Client " + clients.get(0) + " does not have enough money to pay " + this);
                state = new EndState();
            }
            semaphore.release();
        } else {
            barrier = new CyclicBarrier(numberOfClients, () -> {
                cancelTrading();
                semaphore.release();
            });
            Phaser phaser = new Phaser(numberOfClients);
            Semaphore semaphoreForClients = new Semaphore(1);
            for (Client client : clients) {
                client.addLot(this);
                client.setBarrier(barrier);
                client.setPhaser(phaser);
                client.setSemaphore(semaphoreForClients);
                client.setTimeWhenTradingStarted(System.currentTimeMillis());
                new Thread(client).start();
            }
        }
    }


    public void initiateTrading() {
        state.start(this);
    }

    public void trading() {
        state.trading(this);
    }

    public void cancelTrading() {
        state.toCancel(this);
        state = new EndState();
    }

    public Map<Client, Double> getBids() {
        return bids;
    }

    public void setNewBids(Client client, Double bid) {
        bids.put(client, bid);
    }

    public void addClient(Client client) {
        clients.add(client);
    }

    public Integer getLotId() {
        return id;
    }

    public String getLotName() {
        return name;
    }

    public void setLotName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void setPrice(Double price) {
        lock.lock();
        this.price = price;
        lock.unlock();
    }

    public Double getPercentOfInitialPrice() {
        return percentOfInitialPrice;
    }

    public void setPercentOfInitialPrice(Double percentOfInitialPrice) {
        this.percentOfInitialPrice = percentOfInitialPrice;
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
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
