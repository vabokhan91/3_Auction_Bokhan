package com.vbokhan.auction.entity;

import com.vbokhan.auction.generator.IdGenerator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by vbokh on 11.06.2017.
 */

public class Client implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private Integer id;
    private String clientName;
    private Double cash;
    private Lot tradingLot;
    private long timeWhenTradingStarted;
    private double clientBid = 0.00;
    private CyclicBarrier barrier;
    private Phaser phaser;
    private Semaphore semaphore;

    public Client(String clientName, Double cash) {
        id = IdGenerator.nextId();
        this.clientName = clientName;
        this.cash = cash;
    }

    public void run() {
        try {
            while (!isTimeForTradingEnded()) {
                phaser.arriveAndAwaitAdvance();

                semaphore.acquire();
                if (isEnoughMoney()) {
                    LOGGER.log(Level.INFO, this + "has enough money to participate. Cash : " + cash + ", next bid : " + nextBid());
                    clientBid = makeBid();
                } else {
                    LOGGER.log(Level.INFO, this + "does not have enough money to participate. Cash : " + cash + ", next bid : " + nextBid());
                    phaser.arriveAndDeregister();
                    semaphore.release();
                    break;
                }
                semaphore.release();

                phaser.arriveAndAwaitAdvance();
                if (isClientTheLastParticipant()) {
                    clientBid = makeBidIfClientsBidNotHighest();
                    phaser.arriveAndDeregister();
                    break;
                }

                phaser.arriveAndAwaitAdvance();
                if (!isContinueTrading()) {
                    phaser.arriveAndDeregister();
                    break;
                }

                phaser.arriveAndAwaitAdvance();
                if (isClientTheLastParticipant()) {
                    clientBid = makeBidIfClientsBidNotHighest();
                    phaser.arriveAndDeregister();
                    break;
                }
            }
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private double makeBid() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        clientBid = specifyAndSetNewPrice();
        return clientBid;
    }


    private double specifyAndSetNewPrice() {
        double newPrice = nextBid();
        double newPercent = nextPercent();
        tradingLot.setPercentOfInitialPrice(newPercent);
        tradingLot.addBid(this, newPrice);
        LOGGER.log(Level.INFO, this + "specified price " + newPrice + ". Cash available : " + cash);
        return newPrice;
    }

    private double nextBid() {
        double nextBid = Math.round((tradingLot.getLotPrice() * (1 + (tradingLot.getPercentOfInitialPrice() + 0.10))) * 100) / 100d;
        return nextBid;
    }

    private double nextPercent() {
        double nextPercent = tradingLot.getPercentOfInitialPrice() + 0.10;
        return nextPercent;
    }

    private boolean isEnoughMoney() {
        boolean flag;
        if (cash < nextBid()) {
            flag = false;
        } else {
            flag = true;
        }
        return flag;
    }

    private double makeBidIfClientsBidNotHighest() throws InterruptedException {
        double actualPrice = Math.round(tradingLot.getLotPrice() * (1 + tradingLot.getPercentOfInitialPrice()) * 100) / 100d;
        if (clientBid < actualPrice) {
            clientBid = makeBid();
        }
        return clientBid;
    }

    private boolean isContinueTrading() throws InterruptedException {
        boolean flag;
        TimeUnit.SECONDS.sleep(2);
        if (isParticipating() && isEnoughMoney()) {
            flag = true;
            LOGGER.log(Level.INFO, this + " decided to participate in trading.Amount available : " + cash);
        } else {
            LOGGER.log(Level.INFO, this + " decided not to participate in further trading");
            flag = false;
        }
        return flag;
    }

    private boolean isClientTheLastParticipant() {
        boolean flag = false;
        if (phaser.getRegisteredParties() == 1) {
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

    private boolean isTimeForTradingEnded() {
        boolean flag = false;
        long currentTime = System.currentTimeMillis();
        if (currentTime - timeWhenTradingStarted >= tradingLot.getTimeForTrading()) {
            flag = true;
            phaser.arriveAndDeregister();
            LOGGER.log(Level.INFO, "Time for trading " + tradingLot + " ended");
        }
        return flag;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    public void setPhaser(Phaser phaser) {
        this.phaser = phaser;
    }

    public void addLot(Lot lot) {
        this.tradingLot = lot;
    }

    public Double getCash() {
        return cash;
    }

    public void setCash(Double cash) {
        this.cash = cash;
    }

    public void setTimeWhenTradingStarted(long timeWhenTradingStarted) {
        this.timeWhenTradingStarted = timeWhenTradingStarted;
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
                ", name= " + clientName + " ";
    }
}
