package com.vbokhan.auction.auctionfiller;

import com.vbokhan.auction.entity.Auction;
import com.vbokhan.auction.entity.Client;
import com.vbokhan.auction.entity.Lot;

import java.util.List;

/**
 * Created by vbokh on 13.06.2017.
 */
public class AuctionFiller {
    public static void fillAuctionWithLots(List<Lot> lots) {
        Auction.getInstance().setLots(lots);
    }

    public static void fillAuctionWithClients(List<Client> clients) {
        Auction.getInstance().setClients(clients);
    }
}
