package com.joeyturczak.myjavalibrary;

/**
 * Copyright (C) 2016 Joey Turczak
 */
public class DrivingValues {

    public enum Location {
        Alabama("Alabama"),
        Alaska("Alaska"),
        Arizona("Arizona"),
        Arkansas("Arkansas"),
        California("California"),
        Colorado("Colorado"),
        Connecticut("Connecticut"),
        Delaware("Delaware"),
        District_of_Columbia("District_of_Columbia"),
        Florida("Florida"),
        Georgia("Georgia"),
        Hawaii("Hawaii"),
        Idaho("Idaho"),
        Illinois("Illinois"),
        Indiana("Indiana"),
        Iowa("Iowa"),
        Kansas("Kansas"),
        Kentucky("Kentucky"),
        Louisiana("Louisiana"),
        Maine("Maine"),
        Maryland("Maryland"),
        Massachusetts("Massachusetts"),
        Michigan("Michigan"),
        Minnesota("Minnesota"),
        Mississippi("Mississippi"),
        Missouri("Missouri"),
        Montana("Montana"),
        Nebraska("Nebraska"),
        Nevada("Nevada"),
        New_Hampshire("New_Hampshire"),
        New_Jersey("New_Jersey"),
        New_Mexico("New_Mexico"),
        New_York("New_York"),
        North_Carolina("North_Carolina"),
        North_Dakota("North_Dakota"),
        Ohio("Ohio"),
        Oklahoma("Oklahoma"),
        Oregon("Oregon"),
        Pennsylvania("Pennsylvania"),
        Rhode_Island("Rhode_Island"),
        South_Carolina("South_Carolina"),
        South_Dakota("South_Dakota"),
        Tennessee("Tennessee"),
        Texas("Texas"),
        Utah("Utah"),
        Vermont("Vermont"),
        Virginia("Virginia"),
        Washington("Washington"),
        West_Virginia("West_Virginia"),
        Wisconsin("Wisconsin"),
        Wyoming("Wyoming");

        private final String location;

        private Location(final String location) {
            this.location = location;
        }

        public String toString() {
            return location;
        }
    }

    public enum Type {
        Driver("Driver"),
        Motorcycle("Motorcycle"),
        Commercial("Commercial"),
        Farm("Farm"),
        Road_Test("Road_Test"),
        School("School"),
        Senior("Senior"),
        Supplemental("Supplemental"),
        Teen("Teen"),
        Trailer("Trailer");


        private final String type;

        private Type(final String type) {
            this.type = type;
        }

        public String toString() {
            return type;
        }
    }

    public enum Language {
        English("English");

        private final String language;

        private Language(final String language) {
            this.language = language;
        }

        public String toString() {
            return language;
        }
    }
}
