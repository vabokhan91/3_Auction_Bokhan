package com.vbokhan.auction.entity;

import com.vbokhan.auction.generator.IdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by vbokh on 11.06.2017.
 */

public class Client implements Runnable {
    private Integer id;
    private String name;
    private CyclicBarrier barrier;
    private List<Lot> lots;
    private Phaser phaser;
    private Semaphore semaphore;

    public Client(String name) {
        id = IdGenerator.nextId();
        this.name = name;
        lots = new ArrayList<>();
    }

    private boolean isParticipating() {
        boolean flag = true;
        int i = new Random().nextInt(2);
        if (i == 0) {
            flag = false;
            System.out.println("Client " + this + " decided not to participate in trading");
        } else if (i == 1) {
            flag = true;
            System.out.println("Client " + this + " decided to participate in trading");
        }
        return flag;
    }

    public double specifyPrice(double oldPrice) {
        double result = oldPrice * 1.1;
        lots.get(0).setPrice(result);
        return result;
    }

    public void run() {
        try {
            phaser.arriveAndAwaitAdvance();
            while (true) {
                phaser.arriveAndAwaitAdvance();
                if (semaphore.tryAcquire(3, TimeUnit.SECONDS)) {
                    double oldPrice = lots.get(0).getPrice();
                    double newPrice = specifyPrice(oldPrice);
                    lots.get(0).setNewBids(this, newPrice);
                    System.out.println("Client " + this + "specified price " + newPrice);
                    semaphore.release();
                }
                phaser.arriveAndAwaitAdvance();
                if (onlyOneLeft()) {
                    phaser.arriveAndDeregister();
                    break;
                }
                if (!isParticipating()) {
                    phaser.arriveAndDeregister();
                    break;
                }
                phaser.arriveAndAwaitAdvance();
            }
            lots.clear();
            this.barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }


    private boolean onlyOneLeft() {
        if (phaser.getRegisteredParties() == 1) {
            return true;
        }
        return false;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public CyclicBarrier getBarrier() {
        return barrier;
    }

    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    public Phaser getPhaser() {
        return phaser;
    }

    public void setPhaser(Phaser phaser) {
        this.phaser = phaser;
    }

    public void addLot(Lot lot) {
        lots.add(lot);
    }

    public Integer getClientId() {
        return id;
    }

    public String getClientName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        return id != null ? id.equals(client.id) : client.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "id= " + id +
                ", name= " + name + " ";
    }
}
