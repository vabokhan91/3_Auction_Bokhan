package com.vbokhan.auction.state;

import com.vbokhan.auction.entity.Client;
import com.vbokhan.auction.entity.Lot;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vbokh on 14.06.2017.
 */
public class TradingState implements IState {
    @Override
    public void trading(Lot lot) {
        System.out.println("Can not begin trading " + lot + ". It is already in progress");
    }

    @Override
    public void toCancel(Lot lot) {
        System.out.println("Defining winner ....");
        Map<Client, Double> winner = defineWinner(lot);
        /*List<Client> client = winner.entrySet().stream()
                .map(Map.Entry::getKey).collect(Collectors.toList());
        Client winnerClient = client.get(0);*/
//        winnerClient.setCash(winnerClient.getCash() - lot.getPrice());
        System.out.println("Winner is : " + winner.keySet());
    }

    private Map<Client, Double> defineWinner(Lot lot) {
        double maxBid = Collections.max(lot.getBids().values());
        List<Client> winner = lot.getBids().entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), maxBid))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        winner.forEach(client -> client.setCash(client.getCash() - lot.getPrice()));
        Map<Client, Double> winnerInfo = new HashMap<>();
        winnerInfo.put(winner.get(0), maxBid);
        return winnerInfo;
    }
}
