package com.l1sk1sh.vladikbot.models;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.*;

import java.util.Date;

/**
 * @author l1sk1sh
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
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
