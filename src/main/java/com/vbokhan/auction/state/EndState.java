package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Lot;
import com.vbokhan.auction.exception.AuctionException;

/**
 * Created by vbokh on 14.06.2017.
 */
public class EndState implements IState {

    @Override
    public void trade(Lot lot) throws AuctionException {
        lot.getSemaphore().release();
        throw new AuctionException("Can not start trade " + lot + ". It is already sold");
    }

    @Override
    public void cancelTrade(Lot lot) throws AuctionException {
        lot.getSemaphore().release();
        throw new AuctionException("Can not cancel trade " + lot + ". It is already sold");
    }
}
