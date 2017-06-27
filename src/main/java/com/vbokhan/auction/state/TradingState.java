package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Lot;
import com.vbokhan.auction.exception.AuctionException;

/**
 * Created by vbokh on 14.06.2017.
 */
public class TradingState implements IState {

    @Override
    public void trade(Lot lot) throws AuctionException {
        throw new AuctionException("Can not trade" + lot + ". It is already trading");
    }

    @Override
    public void cancelTrade(Lot lot) {
        lot.setLotState(new EndState());
        lot.cancel();
    }
}
