package com.l1sk1sh.vladikbot.models;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author Oliver Johnson
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CsvParsedDiscordMessage {

    @CsvBindByName(required = true)
    private long authorID;

    @CsvBindByName(required = true)
    private String author;

    @CsvDate("dd-MMM-yy HH:m a")
    @CsvBindByName(required = true)
    private Date date;

    @CsvBindByName
    private String content;

    @CsvBindByName
    private String attachments;

    @CsvBindByName
    private String reactions;
}
