package com.vbokhan.auction.main;

import com.vbokhan.auction.entity.Auction;
import com.vbokhan.auction.entity.Client;
import com.vbokhan.auction.entity.Lot;
import com.vbokhan.auction.exception.NoFileException;
import com.vbokhan.auction.exception.WrongDataException;
import com.vbokhan.auction.parser.ClientParser;
import com.vbokhan.auction.parser.LotParser;
import com.vbokhan.auction.reader.AuctionReader;

import java.util.List;

/**
 * Created by vbokh on 12.06.2017.
 */
public class Test {
    private static final String FILE_FOR_LOTS = Test.class.getClassLoader().getResource("data/lots.txt").getPath();
    private static final String FILE_FOR_CLIENTS = Test.class.getClassLoader().getResource("data/clients.txt").getPath();
    public static void main(String[] args) {
        AuctionReader reader = new AuctionReader();
        try {
            List<String> dataForLots = reader.readDataFromFile(FILE_FOR_LOTS);
            List<String> dataForClients = reader.readDataFromFile(FILE_FOR_CLIENTS);
            LotParser lotParser = new LotParser();
            ClientParser clientParser = new ClientParser();
            List<Lot> lots = lotParser.parseData(dataForLots);
            List<Client> clients = clientParser.parseData(dataForClients);
           Auction.AuctionFiller.fillAuctionWithClients(clients);
            Auction.AuctionFiller.fillAuctionWithLots(lots);
            Auction auction = Auction.getInstance();
            auction.startTrade();
        } catch (NoFileException e) {
            e.printStackTrace();
        } catch (WrongDataException e) {
            e.printStackTrace();
        }
    }
}
