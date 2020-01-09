package com.l1sk1sh.vladikbot.domain;


import java.text.NumberFormat;

@SuppressWarnings({"unused", "MismatchedReadAndWriteOfArray"})
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

    public String getName() {
        return name;
    }

    public String getCapitalCity() {
        return capital;
    }

    public String getSubregion() {
        return subregion;
    }

    public String getFormattedPopulation() {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        return format.format(population);
    }

    public String getMainCurrencyName() {
        return currencies[0].name;
    }

    public String getMainCurrencySymbol() {
        return currencies[0].symbol;
    }

    public String getDemonym() {
        return demonym;
    }

    public String getNativeName() {
        return nativeName;
    }

    public String getFormattedArea() {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        return format.format(area);
    }

    private static class Currency {
        String name;
        String symbol;
    }

    public String getFlag() {
        return flag;
    }
}
