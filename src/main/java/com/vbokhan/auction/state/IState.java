package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Lot;

/**
 * Created by vbokh on 14.06.2017.
 */
public interface IState {
    void trading(Lot lot);

    void toCancel(Lot lot);
}
