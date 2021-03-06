package com.vbokhan.auction.validator;


import com.vbokhan.auction.exception.WrongDataException;

import java.util.regex.Pattern;

/**
 * Created by vbokh on 04.06.2017.
 */
public class AuctionDataValidator {
    private final static String REGEX_FOR_CLIENT = "\\s*([^\\d\\W]+||[^\\d\\W]+_[^\\d\\W]+)\\s+(\\d+||\\d+.\\d+)\\s*";
    private final static String REGEX_FOR_LOT = "\\s*(\\w+)\\s+(\\d+||\\d+\\.\\d+)\\s*";

    public static boolean validateData(String unvalidatedData) throws WrongDataException {
        if (unvalidatedData == null || unvalidatedData.isEmpty()) {
            throw new WrongDataException("No data was received to validate");
        }
        boolean flag = false;
        Pattern patternForClients = Pattern.compile(REGEX_FOR_CLIENT);
        Pattern patternForLots = Pattern.compile(REGEX_FOR_LOT);
        if (patternForClients.matcher(unvalidatedData).matches()) {
            flag = true;
        }
        if (patternForLots.matcher(unvalidatedData).matches()) {
            flag = true;
        }
        return flag;
    }
}
