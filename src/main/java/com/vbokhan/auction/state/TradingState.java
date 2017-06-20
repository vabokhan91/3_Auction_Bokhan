package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Client;
import com.vbokhan.auction.entity.Lot;
import com.vbokhan.auction.exception.AuctionException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vbokh on 14.06.2017.
 */
public class TradingState implements IState {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void trade(Lot lot) throws AuctionException {
        int numberOfClients = lot.getParticipatingClients().size();
        if (numberOfClients < 1) {
            LOGGER.log(Level.INFO, "Nobody made auction bids on this lot. Cancelling trade " + lot);
            lot.setLotState(new EndState());
            lot.getSemaphore().release();
        } else {
            lot.tradeLot();
        }
    }

    @Override
    public void cancelTrade(Lot lot) {
        LOGGER.log(Level.INFO, "Defining winner ....");
        if (lot.getBids().isEmpty()) {
            LOGGER.log(Level.INFO, "No bids were made on this lot. No winner");
        } else {
            Map<Client, Double> winner = defineWinner(lot);
            LOGGER.log(Level.INFO, "Winner is : " + winner.keySet());
        }
        lot.setLotState(new EndState());
        lot.getSemaphore().release();
    }

    private Map<Client, Double> defineWinner(Lot lot) {
        Map<Client, Double> winnerInfo = new HashMap<>();
        double maxBid = Collections.max(lot.getBids().values());
        List<Client> winner = lot.getBids().entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), maxBid))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        winner.forEach(client -> client.setCash(client.getCash() - lot.getLotPrice()));
        winnerInfo.put(winner.get(0), maxBid);
        return winnerInfo;
    }
}
