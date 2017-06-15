package com.vbokhan.auction.entity;

import com.vbokhan.auction.generator.IdGenerator;

import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by vbokh on 11.06.2017.
 */

public class Client implements Runnable {
    private Integer id;
    private String name;
    private Double cash;
    private CyclicBarrier barrier;
    private Lot lots;
    private Phaser phaser;
    private Semaphore semaphore;

    public Client(String name, Double cash) {
        id = IdGenerator.nextId();
        this.name = name;
        this.cash = cash;
    }

    public void run() {
        try {
            double clientBid = 0;
            while (true) {
                phaser.arriveAndAwaitAdvance();
                if (semaphore.tryAcquire(10, TimeUnit.SECONDS)) {
                    if (isEnoughMoney()) {
                        System.out.println(this + "has enough money to participate. Cash : " + cash + ", nextbid : " + lots.getPrice() * 1.1);
                        clientBid = makeBid(clientBid);
                    } else {
                        System.out.println(this + "does not have enough money to participate. Cash : " + cash + ", nextbid : " + lots.getPrice() * 1.1);
                        phaser.arriveAndDeregister();
                        break;
                    }
                    semaphore.release();
                }
                phaser.arriveAndAwaitAdvance();
                if (isClientTheLastParticipant(clientBid)) {
                    phaser.arriveAndDeregister();
                    break;
                }
                phaser.arriveAndAwaitAdvance();
                if (!isContinueTrading()) {
                    phaser.arriveAndDeregister();
                    break;
                }
                phaser.arriveAndAwaitAdvance();
                if (isClientTheLastParticipant(clientBid)) {
                    phaser.arriveAndDeregister();
                    break;
                }
            }
            lots = null;
            this.barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private boolean isEnoughMoney() {
        boolean flag;
        if (cash < lots.getPrice() * 1.1) {
            flag = false;
        } else {
            flag = true;
        }
        return flag;
    }

    private double makeBid(double clientBid) throws InterruptedException {
        double oldPrice = lots.getPrice();
        TimeUnit.SECONDS.sleep(2);
        double newPrice = specifyAndSetNewPrice(oldPrice);
        clientBid = newPrice;
        return clientBid;
    }

    private double specifyAndSetNewPrice(double oldPrice) {
        double newPrice = specifyPrice(oldPrice);
        lots.setNewBids(this, newPrice);
        System.out.println("Client " + this + "specified price " + newPrice + ". Cash available : " + cash);
        return newPrice;
    }

    private double specifyPrice(double oldPrice) {
        double result = oldPrice * 1.1;
        lots.setPrice(result);
        return result;
    }

    private boolean isClientTheLastParticipant(double clientBid) {
        boolean flag = false;
        if (onlyOneLeft()) {
            makeBidIfClientsBidNotHighest(clientBid);

            flag = true;
        }
        return flag;
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

    private boolean isContinueTrading() {
        boolean flag = true;
        if (isParticipating()) {
            if (isEnoughMoney()) {
                flag = true;
                System.out.println("Client " + this + " decided to participate in trading.Amount available : " + cash);
            }
        } else {
            System.out.println("Client " + this + " decided not to participate in further trading");
            flag = false;
        }
        return flag;
    }

    private void makeBidIfClientsBidNotHighest(double clientBid) {
        double actualPrice = lots.getPrice();
        if (clientBid < actualPrice) {
            specifyAndSetNewPrice(actualPrice);
        }
        phaser.arriveAndDeregister();
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
        lots = lot;
    }

    public Double getCash() {
        return cash;
    }

    public void setCash(Double cash) {
        this.cash = cash;
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
