package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Lot;
import com.vbokhan.auction.exception.AuctionException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by vbokh on 14.06.2017.
 */
public class StartState implements IState {

    @Override
    public void trading(Lot lot) {
        int numberOfClients = lot.getClients().size();
        if (numberOfClients < 1) {
            System.out.println("Nobody made auction bids on this lot. Cancelling trading " + lot);
            lot.getSemaphore().release();

        }else {
            System.out.println("Starting trading " + lot);
            lot.start();
        }
    }

    @Override
    public void toCancel(Lot lot) {
        System.out.println("Can not cancel trading " + lot + ". It has not started yet");
    }
}
