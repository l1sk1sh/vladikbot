package com.l1sk1sh.vladikbot.network.dto;

import lombok.Getter;

/**
 * @author Oliver Johnson
 */
@SuppressWarnings({"unused", "MismatchedReadAndWriteOfArray"})
@Getter
public class DogFact {
    private String[] facts;

    public String getFact() {
        return facts[0];
    }
}
