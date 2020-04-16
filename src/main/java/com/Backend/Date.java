package com.Backend;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.StringTokenizer;

/**
 * Creates a date class to store the date. This would allow easy
 * storage and manipulation of the date
 */
public class Date implements Comparator<Date>, Comparable<Date>, Cloneable {
    private int year;
    private int month;
    private int day;

    private boolean noDate = false;

    public Date(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public Date() {
        setDate(LocalDate.now());
    }

    public Date(boolean noDate) {
        this.noDate = noDate;
    }

    public static Date today() {
        Date returning = new Date();
        returning.setDate(LocalDate.now());
        return returning;
    }

    @Override
    protected Date clone() {
        try {
            return (Date) super.clone();
        } catch (CloneNotSupportedException e) {
            System.err.println("Cloning Not Supported");
            return this;
        }
    }

    public boolean fakeDate() {
        return noDate;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public java.sql.Date getDateSQL() {
        String string_date = toStringRegular();

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        long milliseconds = 0;
        try {
            java.util.Date d = f.parse(string_date);
            milliseconds = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new java.sql.Date(milliseconds);
    }

    public static int getMonth(String month) {
        month = month.toLowerCase();
        System.out.print("Month: " + month);
        switch (month) {
            case "january":
            case "jan":
                return 1;
            case "february":
            case "feb":
            case "f":
                return 2;
            case "march":
            case "mar":
                return 3;
            case "april":
            case "ap":
            case "apr":
                return 4;
            case "may":
                return 5;
            case "june":
            case "jun":
                return 6;
            case "july":
            case "jul":
                return 7;
            case "august":
            case "aug":
                return 8;
            case "september":
            case "sept":
                return 9;
            case "october":
            case "oct":
                return 10;
            case "november":
            case "nov":
                return 11;
            case "december":
            case "dec":
                return 12;
            default:
                return -1;
        }
    }

    public static String getMonth(int month) {
        switch (month) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return null;
        }
    }

    public void setDate(String date) {
        StringTokenizer st = new StringTokenizer(date, "-");
        int year = Integer.parseInt(st.nextToken());
        if (year > 1900) {
            this.setYear(year);
        } else {
            this.setYear(year + 1900);
        }
        this.setMonth(Integer.parseInt(st.nextToken()));
        this.setDay(Integer.parseInt(st.nextToken()));
    }

    public void setDate(LocalDate date) {
        this.year = date.getYear();
        this.month = date.getMonthValue();
        this.day = date.getDayOfMonth();
    }

    public String getMonthString() {
        return getMonth(month);
    }

    @Override
    public String toString() {
        return day + " " + getMonthString() + ", " + year;
    }

    public String toStringRegular() {
        String year = "" + this.year;

        String month = "";
        if (this.month < 10) {
            month += "0";
        }
        month += this.month;

        String day = "";
        if (this.day < 10) {
            day += "0";
        }
        day += this.day;

        return year + "-" + month + "-" + day;
    }

    @Override
    public int compareTo(Date o) {
        if (this.year != o.getYear()) {
            Integer year = o.getYear();
            return year.compareTo(this.getYear());
        }
        else {
            if (this.month != o.getMonth()) {
                Integer month = o.getMonth();
                return month.compareTo(this.getMonth());
            }
            else {
                Integer day = o.getDay();
                return day.compareTo(this.getDay());
            }
        }
    }

    @Override
    public int compare(Date o2, Date o1) {
        if (o1.getYear() != o2.getYear()) {
            return o1.getYear() - o2.getYear();
        } else {
            if (o1.getMonth() != o2.getMonth()) {
                return o1.getMonth() - o2.getMonth();
            } else {
                return o1.getDay() - o2.getDay();
            }
        }
    }

    /**
     * Compares to see if two dates are the same
     *
     * @param date the Date compared
     * @return Whether they are equal or not
     */
    public boolean equals(Date date) {
        return (this.day == date.getDay()) && (this.month == date.getMonth()) && (this.year == date.getYear());
    }

    /**
     * Compares to see if two dates are equal, ignoring the day
     *
     * @param date The date being compared
     * @return Whether they are equal
     */
    public boolean equalsNoDay(Date date) {
        return (this.month == date.getMonth()) && (this.year == date.getYear());
    }

    public void incrementDay() {
        day++;
    }

    public void incrementByMonth() {
        if (month < 12) month++;
        else {
            month = 0;
            year++;
        }
    }

    public void incrementYear() {
        year++;
    }
}
