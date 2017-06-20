package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Lot;
import com.vbokhan.auction.exception.AuctionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by vbokh on 14.06.2017.
 */
public class StartState implements IState {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void trade(Lot lot) throws AuctionException {
        lot.setLotState(new TradingState());
        lot.startTrading();
    }

    @Override
    public void cancelTrade(Lot lot) throws AuctionException {
        throw new AuctionException("Can not cancel trade " + lot + ". It has not started yet");
    }
}
