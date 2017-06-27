package com.vbokhan.auction.parser;


import com.vbokhan.auction.entity.Client;
import com.vbokhan.auction.exception.WrongDataException;
import com.vbokhan.auction.validator.AuctionDataValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vbokh on 04.06.2017.
 */
public class ClientParser {
    private static final String DELIMITER = "\\s";

    public List<Client> parseData(List<String> unparsedData) throws WrongDataException {
        if (unparsedData == null || unparsedData.isEmpty()) {
            throw new WrongDataException("No data was received for parsing");
        }
        ArrayList<Client> clients = new ArrayList<>();
        for (String parsingString : unparsedData) {
            if (AuctionDataValidator.validateData(parsingString)) {
                List<String> dataForCreatingClient = new ArrayList<String>(Arrays.asList(parsingString.split(DELIMITER)));
                String name = dataForCreatingClient.get(0);
                Double cash = Double.valueOf(dataForCreatingClient.get(1));
                Client client = new Client(name, cash);
                clients.add(client);
            }
        }
        return clients;
    }
}
