package com.vbokhan.auction.reader;



import com.vbokhan.auction.exception.NoFileException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by vbokh on 04.06.2017.
 */
public class AuctionReader {
    private static final Logger LOGGER = LogManager.getLogger();

    public List<String> readDataFromFile(String fileName) throws NoFileException {
        if (fileName == null || fileName.isEmpty()) {
            throw new NoFileException(String.format("File %s not found", fileName));
        }
        List<String> dataFromFile = null;
        try {
            dataFromFile = Files.lines(new File(fileName).toPath())
                    .map(s -> s.trim())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, "Error with file. " + e.getMessage());
        }
        return dataFromFile;
    }
}
