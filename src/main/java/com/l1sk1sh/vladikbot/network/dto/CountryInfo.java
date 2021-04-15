package com.l1sk1sh.vladikbot.network.dto;


import lombok.Getter;

import java.text.NumberFormat;

/**
 * @author Oliver Johnson
 */
@SuppressWarnings({"unused", "MismatchedReadAndWriteOfArray"})
@Getter
public class CountryInfo {
    private String name;
    private String capital;
    private String subregion;
    private long population;
    private Currency[] currencies;
    private String demonym;
    private String nativeName;
    private double area;
    private String flag;

    public String getFormattedPopulation() {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        return format.format(population);
    }

    public String getMainCurrencyName() {
        return currencies[0].getName();
    }

    public String getMainCurrencySymbol() {
        return currencies[0].getSymbol();
    }

    public String getFormattedArea() {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        return format.format(area);
    }

    @Getter
    private static class Currency {
        private String name;
        private String symbol;
    }
}
