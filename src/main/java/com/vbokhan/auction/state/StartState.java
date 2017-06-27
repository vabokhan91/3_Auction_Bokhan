package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Lot;
import com.vbokhan.auction.exception.AuctionException;

/**
 * Created by vbokh on 14.06.2017.
 */
public class StartState implements IState {

    @Override
    public void trade(Lot lot) throws AuctionException {
        lot.setLotState(new TradingState());
        lot.trade();
    }

    @Override
    public void cancelTrade(Lot lot) throws AuctionException {
        throw new AuctionException("Can not cancel tradeLot " + lot + ". It has not started yet");
    }
}
