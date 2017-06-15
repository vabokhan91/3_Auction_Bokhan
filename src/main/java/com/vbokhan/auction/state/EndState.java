package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Lot;

/**
 * Created by vbokh on 14.06.2017.
 */
public class EndState implements IState {
    @Override
    public void trading(Lot lot) {
        System.out.println("Can not start trading " + lot + ". It is already sold");
    }

    @Override
    public void toCancel(Lot lot) {
        System.out.println("Can not cancel trading " + lot + ". It is already sold");
    }
}
