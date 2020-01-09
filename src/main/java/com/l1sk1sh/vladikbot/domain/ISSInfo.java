package com.l1sk1sh.vladikbot.domain;

import java.util.Arrays;

@SuppressWarnings("unused")
public class ISSInfo {
    private String latitude;
    private String longitude;
    private String altitude;
    private String daynum;
    private Astronauts astronauts;

    public String getLatitude() {
        return latitude.substring(0, 8);
    }

    public String getLongitude() {
        return longitude.substring(0, 8);
    }

    public String getAltitude() {
        return altitude.substring(0, 8);
    }

    public String getNumberOfDaysOnOrbit() {
        return daynum;
    }

    public void setAstronauts(Astronauts astronauts) {
        this.astronauts = astronauts;
    }

    public String getPeopleNames() {
        StringBuilder peopleList = new StringBuilder();

        for (int i = 0; i < astronauts.people.length; i++) {
            peopleList.append(i + 1)
                    .append(". ")
                    .append(astronauts.people[i].name)
                    .append("\r\n");
        }

        return peopleList.toString();
    }

    public static class Astronauts {
        People[] people;

        private static class People {
            String name;

            @Override
            public String toString() {
                return "People{" +
                        "name='" + name + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "Astronauts{" +
                    "people=" + Arrays.toString(people) +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ISSInfo{" +
                "latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", altitude='" + altitude + '\'' +
                ", daynum='" + daynum + '\'' +
                ", people=" + astronauts +
                '}';
    }
}
