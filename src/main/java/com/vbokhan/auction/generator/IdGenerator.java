package com.vbokhan.auction.generator;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vbokh on 11.06.2017.
 */
public class IdGenerator {
    private static Integer id = new Integer(0);

    public static int nextId() {
        return ++id;
    }
}
