package com.l1sk1sh.vladikbot.domain;

import lombok.Getter;

@SuppressWarnings({"unused"})
@Getter
public class DogPicture {
    private String message;

    public String getPicture() {
        return message;
    }
}
