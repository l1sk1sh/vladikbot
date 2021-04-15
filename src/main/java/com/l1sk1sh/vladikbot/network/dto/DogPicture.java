package com.l1sk1sh.vladikbot.network.dto;

import lombok.Getter;

/**
 * @author Oliver Johnson
 */
@SuppressWarnings({"unused"})
@Getter
public class DogPicture {
    private String message;

    public String getPicture() {
        return message;
    }
}
