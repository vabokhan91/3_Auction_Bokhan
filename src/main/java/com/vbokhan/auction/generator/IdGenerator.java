package com.vbokhan.auction.generator;

/**
 * Created by vbokh on 11.06.2017.
 */
public class IdGenerator {
    private static Integer id = 0;

    public static Integer nextId() {
        return ++id;
    }
}
