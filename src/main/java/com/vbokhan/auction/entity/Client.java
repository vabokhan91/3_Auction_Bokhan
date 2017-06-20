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
            double clientBid = 0.00;

            while (!isTimeForTradingEnded()) {
                phaser.arriveAndAwaitAdvance();

                semaphore.acquire();
                if (isEnoughMoney()) {
                    LOGGER.log(Level.INFO, this + "has enough money to participate. Cash : " + cash + ", nextbid : " + nextBid());
                    clientBid = makeBid(clientBid);
                } else {
                    LOGGER.log(Level.INFO, this + "does not have enough money to participate. Cash : " + cash + ", nextbid : " + nextBid());
                    phaser.arriveAndDeregister();
                    break;
                }
                semaphore.release();

                phaser.arriveAndAwaitAdvance();
                if (isClientTheLastParticipant()) {
                    clientBid = makeBidIfClientsBidNotHighest(clientBid);
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
                    clientBid = makeBidIfClientsBidNotHighest(clientBid);
                    phaser.arriveAndDeregister();
                    break;
                }
            }
            timeWhenTradingStarted = 0;
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private double makeBid(double clientBid) throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        clientBid = specifyAndSetNewPrice();
        return clientBid;
    }


    private double specifyAndSetNewPrice() {
        double newPrice = nextBid();
        double newPercent = nextPercent();
        tradingLot.setPercentOfInitialPrice(newPercent);
        tradingLot.setNewBids(this, newPrice);
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

    private double makeBidIfClientsBidNotHighest(double clientBid) throws InterruptedException {
        double actualPrice = Math.round(tradingLot.getLotPrice() * (1 + tradingLot.getPercentOfInitialPrice()) * 100) / 100d;
        if (clientBid < actualPrice) {
            clientBid = makeBid(clientBid);
        }
        return clientBid;
    }

    private boolean isContinueTrading() throws InterruptedException {
        boolean flag;
        TimeUnit.SECONDS.sleep(2);
        if (isParticipating() && isEnoughMoney()) {
            flag = true;
            LOGGER.log(Level.INFO, this + " decided to participate in trade.Amount available : " + cash);
        } else {
            LOGGER.log(Level.INFO, this + " decided not to participate in further trade");
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
        if (currentTime - timeWhenTradingStarted >= Lot.TIME_FOR_TRADING) {
            flag = true;
            phaser.arriveAndDeregister();
            LOGGER.log(Level.INFO, "Time for trade " + tradingLot + " ended");
        }
        return flag;
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
        this.tradingLot = lot;
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
        return clientName;
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
                ", clientName= " + clientName + " ";
    }
}
