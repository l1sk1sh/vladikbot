package com.l1sk1sh.vladikbot.models.entities;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class ParsedMessage {
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
