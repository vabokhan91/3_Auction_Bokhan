package com.vbokhan.auction.exception;

/**
 * Created by vbokh on 12.06.2017.
 */
public class AuctionException extends Exception {
    public AuctionException() {
        super();
    }

    public AuctionException(String message) {
        super(message);
    }

    public AuctionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuctionException(Throwable cause) {
        super(cause);
    }
}
