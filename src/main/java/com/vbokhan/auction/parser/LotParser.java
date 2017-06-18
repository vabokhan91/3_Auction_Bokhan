package com.vbokhan.auction.parser;


import com.vbokhan.auction.entity.Lot;
import com.vbokhan.auction.exception.WrongDataException;
import com.vbokhan.auction.validator.ClientValidator;
import com.vbokhan.auction.validator.LotValidator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vbokh on 04.06.2017.
 */
public class LotParser {
    private static final String DELIMITER = "\\s";

    public List<Lot> parseData(List<String> unparsedData) throws WrongDataException {
        if (unparsedData == null || unparsedData.isEmpty()) {
            throw new WrongDataException("No data was received for parsing");
        }

        ArrayList<Lot> lots = new ArrayList<>();
        for (String parsingString : unparsedData) {
            if (LotValidator.validateData(parsingString)) {
                List<String> dataForCreatingLot = new ArrayList<>(Arrays.asList(parsingString.split(DELIMITER)));
                String name = dataForCreatingLot.get(0);
                Double price = Double.valueOf(dataForCreatingLot.get(1));
                Lot lot = new Lot(name, price);
                lots.add(lot);
            }
        }
        return lots;
    }
}
