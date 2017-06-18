package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Lot;
import com.vbokhan.auction.exception.AuctionException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by vbokh on 14.06.2017.
 */
public class StartState implements IState {
    private static final Logger LOGGER = LogManager.getLogger();
    @Override
    public void start(Lot lot) {
        LOGGER.log(Level.INFO, "starting trading " + lot);
        lot.setLotState(new TradingState());
        lot.trading();
    }

    @Override
    public void trading(Lot lot) {
        LOGGER.log(Level.INFO, "Cant begin trading. it's not started yet");
    }

    @Override
    public void toCancel(Lot lot) {
        LOGGER.log(Level.INFO, "Can not cancel trading " + lot + ". It has not started yet");
    }
}
