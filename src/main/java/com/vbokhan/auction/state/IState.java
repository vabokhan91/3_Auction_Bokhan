package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Lot;
import com.vbokhan.auction.exception.AuctionException;

/**
 * Created by vbokh on 14.06.2017.
 */
public interface IState {
    void trade(Lot lot) throws AuctionException;

    void cancelTrade(Lot lot) throws AuctionException;

}
