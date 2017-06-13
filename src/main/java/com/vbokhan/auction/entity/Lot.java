package com.vbokhan.auction.entity;

import com.vbokhan.auction.exception.AuctionException;
import com.vbokhan.auction.generator.IdGenerator;

import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by vbokh on 11.06.2017.
 */
public class Lot {
    private Integer id;
    private String name;
    private Double price;
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
    }

    private Map<Client, Double> defineWinner() {
        double maxBid = Collections.max(bids.values());
        List<Client> winner = bids.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(),maxBid))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        Map<Client, Double> winnerLot = new HashMap<>();
        winnerLot.put(winner.get(0), maxBid);
        return winnerLot;
    }

    public void beginTrade() throws AuctionException{
        System.out.println("Clients, participating in trading : " + clients);
        if (clients.size() < 1) {
            throw new AuctionException("Nobody made auction bids on this lot ");

        }
        if (clients.size() == 1) {
            System.out.println(" Winner is " + clients.get(0) + " " + this);
            semaphore.release();
        }else {
            barrier = new CyclicBarrier(clients.size(), new Runnable() {
                @Override
                public void run() {
                    if (bids.isEmpty()) {
                        System.out.println("No winner");
                    } else {
                        Map<Client, Double> winner = Lot.this.defineWinner();
                        System.out.println(winner.keySet()+ " Lot: " + name + " " + "price : " + winner.values());
                    }
                    semaphore.release();
                }
            });
            Phaser phaser = new Phaser(clients.size());
            Semaphore semaphore1 = new Semaphore(1);
            for (Client client : clients) {
                client.addLot(this);
                client.setBarrier(barrier);
                client.setPhaser(phaser);
                client.setSemaphore(semaphore1);
                new Thread(client).start();
            }
        }
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

    @Override
    public String toString() {
        return "Lot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
