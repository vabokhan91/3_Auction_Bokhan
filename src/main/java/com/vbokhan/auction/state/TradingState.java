package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Client;
import com.vbokhan.auction.entity.Lot;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Created by vbokh on 14.06.2017.
 */
public class TradingState implements IState {
    private static final Logger LOGGER = LogManager.getLogger();
    @Override
    public void start(Lot lot) {
        System.err.println("Can not begin trading " + lot + ". It is already in progress");
    }

    @Override
    public void trading(Lot lot) {
        int numberOfClients = lot.getClients().size();
        if (numberOfClients < 1) {
            LOGGER.log(Level.INFO, "Nobody made auction bids on this lot. Cancelling trading " + lot);
            lot.getSemaphore().release();
        }else {
            lot.run();
        }
    }

    @Override
    public void toCancel(Lot lot) {
        LOGGER.log(Level.INFO, "Defining winner ....");
        Map<Client, Double> winner = defineWinner(lot);
        if (lot.getBids().isEmpty()) {
            LOGGER.log(Level.INFO, "No Winner");
        }else {
            LOGGER.log(Level.INFO, "Winner is : " + winner.keySet());
        }
    }

    private Map<Client, Double> defineWinner(Lot lot) {
        Map<Client, Double> winnerInfo = new HashMap<>();
            double maxBid = Collections.max(lot.getBids().values());
            List<Client> winner = lot.getBids().entrySet().stream()
                    .filter(entry -> Objects.equals(entry.getValue(), maxBid))
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            winner.forEach(client -> client.setCash(client.getCash() - lot.getPrice()));
            winnerInfo.put(winner.get(0), maxBid);
        return winnerInfo;
    }
}
