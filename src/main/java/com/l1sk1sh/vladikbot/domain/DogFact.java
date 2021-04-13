package com.l1sk1sh.vladikbot.domain;

import lombok.Getter;

@SuppressWarnings({"unused", "MismatchedReadAndWriteOfArray"})
@Getter
public class DogFact {
    private String[] facts;

    public String getFact() {
        return facts[0];
    }
}
