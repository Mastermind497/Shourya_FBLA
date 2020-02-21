package com.backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

/**
 * This Class will hold all the methods to allow manipulation of the Data in the Database.
 * This uses MySQL to actively store and edit data.
 *
 * @author Shourya Bansal
 */
public class MySQLMethods {
    //Login Credentials ot create/edit on MySQL
    private static final String username = "java";
    private static final String unlocker = "password";
    //The Driver
    private static final String Driver = "com.mysql.cj.jdbc.Driver";
    //The name of the database and table. It is a public static string because it is the same all the time in any file
    public static String databaseName = "student_data";
    private static final String tableName = "tracker";
    //The location of the database
    private static String DB_URL = "jdbc:mysql://localhost:3306/";
    //Connection and Statement
    private static Statement statement = null;
    private static Connection connection = null;
    private static ResultSet resultSet = null;

//    public static void main(String[] args) throws Exception {
//        createDatabase();
//        createTable();
//        createStudent("Shourya", "Bansal", 224272, (short)10 , "T", "t", (short) 0);
//        System.out.println(selectTrackerDouble("Shourya", "Bansal", 224272, "communityServiceHours"));
//        createStudentTable("Shourya", "Bansal", 224272);
//        addStudentHours("Shourya", "Bansal", 224272, "Fun", 8.65, 2020, 1, 26);
//        System.out.println(selectTrackerDouble("Shourya", "Bansal", 224272, "communityServiceHours"));
//        System.out.println("Test" + selectTrackerString("John", "Doe", 123456, "firstName"));
//    }

    /**
     * This method runs at the start of any Query. It checks
     * if the database has already been created and, if it hasn't, creates
     * it.
     */
    public static void createDatabase() {
        try {
            //Creates a connection to the MySQL using getConnection
            connection = getConnection();

            //Runs a statement on MySQL to create a database
            statement = connection.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
            System.out.println("Executed Query");
            //Closes Variables to avoid errors
            statement.close();
            connection.close();

            //Selects this new database for all queries if it is not already selected
            if (!DB_URL.contains(databaseName)) DB_URL += databaseName;
        } catch (Exception e) {
            System.out.println("Database Creation Failed");
            e.printStackTrace();
        }
    }

    /**
     * This method creates the main table which would store student information
     * and current hours.
     */
    public static void createTable() {
        //Tries, protects against errors
        try {
            //Uses getConnection to create a connection to the DataBase
            connection = getConnection();

            //Executes Query & Creates Table
            statement = connection.createStatement();
            //Uses Statement to Create MySQL Table in database
            String table = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY," //The primary key, an auto-incrementing ID
                    + "firstName VARCHAR(255) NOT NULL,"
                    + "lastName VARCHAR(255) NOT NULL,"
                    + "fullName VARCHAR(255) NOT NULL,"//A combination of first and last in the format first_last to allow for easy identification
                    + "studentID INT NOT NULL,"
                    + "grade TINYINT NOT NULL,"
                    + "communityServiceHours DOUBLE,"
                    + "communityServiceCategory VARCHAR(40),"
                    + "email VARCHAR(255),"
                    + "yearsDone TINYINT,"
                    + "lastEdited DATE"
                    + ")";
            statement.executeUpdate(table);
            try {
                connection.close();
                statement.close();
            } catch (Exception e) {
                //Do Nothing
            }
        }
        //What it does if there is an error
        catch (Exception e) {
            System.out.println("Create Table FAIL");
            e.printStackTrace();
        }
    }

    /**
     * This method creates a specific student table. This would allow more
     * specific storage of data, such as data of specific events and such and make
     * it much easier to analyze a students data.
     *
     * @param firstName This is the student's First Name
     * @param lastName  This is the student's Last Name
     * @param studentID The Student's Student ID Number
     */
    public static void createStudentTable(String firstName, String lastName, int studentID) {
        //Changes name to a better format for tables
        String studentName = makeName(firstName, lastName, studentID);
        //Tries, protects against errors
        try {
            //Creates a connection
            connection = getConnection();

            //Executes Query & Creates Table
            statement = connection.createStatement();

            //Uses Statement to Create MySQL Table in database
            String table = "CREATE TABLE IF NOT EXISTS " + studentName + " ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "eventName VARCHAR(2555),"
                    + "eventHours DOUBLE NOT NULL,"
                    + "date DATE"
                    + ")";
            statement.executeUpdate(table);

            //Closes all variables to prevent error
            statement.close();
            connection.close();
        }
        //What it does if there is an error
        catch (Exception e) {
            System.out.println("Create Student Table FAIL");
            e.printStackTrace();
        }
    }

    /**
     * This method makes it very easy to create a student in the main tracker table.
     *
     * @param firstName                The First Name of the Student
     * @param lastName                 The Last Name of the Student
     * @param studentID                The Student's Student ID Number
     * @param grade                    The Student's Grade Number
     * @param communityServiceCategory The Category of Award Aimed for by the Student
     * @param email                    The Student's Email Address
     * @param yearsDone                The number of years of FBLA Completed
     */
    public static void createStudent(String firstName, String lastName, int studentID, short grade,
                                     String communityServiceCategory, String email, short yearsDone) {
        try {
            //Formats name
            String fullName = changeName(firstName, lastName);

            createStudentTable(firstName, lastName, studentID);

            //creates a connection to the MySQL
            connection = getConnection();

            //Calculates Today's Date
            java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());

            String query = " insert into " + tableName + " (firstName, lastName, fullName, studentID, grade, communityServiceCategory,"
                    + "email, yearsDone, lastEdited)"
                    + " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            //Creates a preparedStatement to insert into mysql command line
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, fullName);
            preparedStatement.setInt(4, studentID);
            preparedStatement.setShort(5, grade);
            preparedStatement.setString(6, communityServiceCategory);
            preparedStatement.setString(7, email);
            preparedStatement.setShort(8, yearsDone);
            preparedStatement.setDate(9, date);

            //executes statement
            preparedStatement.execute();

            //Ends everything
            connection.close();
            statement.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * This adds events and hours into a students unique table. At the
     * same time, this updates the hours in the main table
     *
     * @param firstName The First Name of the Student
     * @param lastName  The Last Name of the Student
     * @param studentID The Student's Student ID Number
     * @param eventName The Name of the Event Completed
     * @param hours     The Length of the Event
     * @param year      The Year the Event Was Done
     * @param month     The Month of the Event
     * @param day       The Day of the Event
     * @throws Exception This is throws in case the Database is not found
     */
    public static void addStudentHours(String firstName, String lastName, int studentID, String eventName, double hours,
                                       int year, int month, int day) throws Exception {
        //Creates a database connection
        connection = getConnection();

        //Creates a date for MySQL Table
        Calendar calendar = new Calendar.Builder().setCalendarType("iso8601").setDate(year, month, day).build();
        java.sql.Date date = new java.sql.Date(calendar.getTime().getTime());

        //Converts hours into two parts: int and decimal
        String query = " insert into " + makeName(firstName, lastName, studentID) + " (eventName, eventHours, date)"
                + " values (?, ?, ?)";

        //Creates a preparedStatement to insert into mysql command line
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, eventName);
        preparedStatement.setDouble(2, hours);
        preparedStatement.setDate(3, date);

        //executes statement
        preparedStatement.execute();

        //Ends everything
        connection.close();
        statement.close();

        //next step, update hours in the main table
        //gets current main hours:
        double currentHours = selectTrackerDouble(firstName, lastName, studentID, "communityServiceHours");
        updateTracker(firstName, lastName, studentID, "communityServiceHours",
                Double.toString(round(currentHours + hours)));//rounds Hours to nearest hundredth to account for inaccuracy of doubles

        //now, update lastEdited in main table
        updateToCurrentDate(firstName, lastName, studentID);
    }

    /**
     * This allows someone to get specific integer-type data from the Main Table.
     *
     * @param firstName The Student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @param data      the data field being accessed
     * @return an integer containing the data
     * @throws Exception for SQL Errors
     */
    public static int selectTrackerInt(String firstName, String lastName, int studentID, String data) throws Exception {
        String studentName = makeName(firstName, lastName, studentID);

        //Uses getConnection to create a connection with the database
        connection = getConnection();

        //SQL Query to find find data
        String query = "select " + data + " from " + tableName + " where fullName = '" + studentName + "'";

        //Create the java Statement (Goes in Query)
        statement = connection.createStatement();

        //The Result after executing the query
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();

        //returns the String inside column "data"
        int output = resultSet.getInt(data);

        //Ends everything
        connection.close();
        statement.close();
        resultSet.close();

        return output;
    }

    /**
     * This allows someone to get Specific String-type data from the Main table.
     *
     * @param firstName The student's First Name
     * @param lastName  The student's Last Name
     * @param studentID The Student's Student ID Number
     * @param data      The data field being accessed
     * @return the String Type data
     */
    public static String selectTrackerString(String firstName, String lastName, int studentID, String data) {
        String output;
        try {
            String studentName = makeName(firstName, lastName, studentID);
            //Creates a connection
            connection = getConnection();

            //SQL Query to find find data
            String query = "select " + data + " from " + tableName + " where fullName = '" + studentName + "'";

            //Create the java Statement (Goes in Query)
            statement = connection.createStatement();

            //The Result after executing the query
            ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();

            //returns the String inside column "data"
            output = resultSet.getString(data);

            try {
                //Ends everything
                connection.close();
                statement.close();
                resultSet.close();
            } catch (Exception e) {
                //Do Nothing
            }
            if (output == null || output.equals("")) {
                return null;
            }
            return output;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * This allows someone to get specific Double-Type data from the Main Table.
     *
     * @param firstName The Student's First Name
     * @param lastName  The Student's Last name
     * @param studentID The Student's Student ID Number
     * @param data      The data field being accessed
     * @return A double containing the data
     * @throws Exception for SQL Errors
     */
    public static double selectTrackerDouble(String firstName, String lastName, int studentID, String data) throws Exception {
        String studentName = changeName(firstName, lastName);

        //Uses getConnection to create a connection with the database
        connection = getConnection();

        //SQL Query to find find data
        String query = "SELECT " + data + " FROM " + tableName + " WHERE fullName = '" + studentName + "' AND studentID = '" + studentID + "'";

        //Create the java Statement (Goes in Query)
        statement = connection.createStatement();

        //The Result after executing the query
        ResultSet resultSet = statement.executeQuery(query);

        //returns the String inside column "data"
        resultSet.next();
        double output = resultSet.getDouble(data);

        //Ends everything
        connection.close();
        statement.close();
        resultSet.close();

        return output;
    }

    /**
     * This allows someone to get specific short-type data from the Main Table.
     *
     * @param firstName the student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @param data      the data field being accessed
     * @return a short containing the data
     * @throws Exception for SQL Errors
     */
    public static short selectTrackerShort(String firstName, String lastName, int studentID, String data) throws Exception {
        String studentName = makeName(firstName, lastName, studentID);

        //Uses method getConnection() to create a connection to the database
        connection = getConnection();

        //SQL Query to find find data
        String query = "select " + data + " from " + tableName + " where fullName = '" + studentName + "'";

        //Create the java Statement (Goes in Query)
        statement = connection.createStatement();

        //The Result after executing the query
        ResultSet resultSet = statement.executeQuery(query);

        //returns the String inside column "data"
        resultSet.next();
        short output = resultSet.getShort(data);

        //Ends everything
        connection.close();
        statement.close();
        resultSet.close();

        return output;
    }

    /**
     * This allows someone to get specific boolean-type data from the Main database.
     *
     * @param firstName the student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @param data      the data field being accessed
     * @return a boolean contain the data
     * @throws Exception for SQL Errors
     */
    public static boolean selectTrackerBoolean(String firstName, String lastName, int studentID, String data) throws Exception {
        String studentName = makeName(firstName, lastName, studentID);

        //Uses method getConnection to get a connection to the database
        connection = getConnection();

        //SQL Query to find find data
        String query = "select " + data + " from " + tableName + " where fullName = '" + studentName + "'";

        //Create the java Statement (Goes in Query)
        statement = connection.createStatement();

        //The Result after executing the query
        ResultSet resultSet = statement.executeQuery(query);

        //returns the String inside column "data"
        resultSet.next();
        boolean output = resultSet.getBoolean(data);

        //Closes all variables
        resultSet.close();
        statement.close();
        connection.close();

        return output;
    }

    /**
     * This allows someone to get a specific Date from the main table (if one exists)
     *
     * @param firstName the student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @param data      the data field being accessed
     * @return A String containing the date
     * @throws Exception for SQL Errors
     */
    public static String selectTrackerDate(String firstName, String lastName, int studentID, String data) throws Exception {
        String studentName = makeName(firstName, lastName, studentID);

        //Creates a connection
        connection = getConnection();

        //SQL Query to find find data
        String query = "select " + data + " from " + tableName + " where fullName = '" + studentName + "'";

        //Create the java Statement (Goes in Query)
        statement = connection.createStatement();

        //The Result after executing the query
        ResultSet resultSet = statement.executeQuery(query);

        //returns the String inside column "data"
        resultSet.next();
        String output = resultSet.getString(data);

        //Closes Variables
        resultSet.close();
        connection.close();
        statement.close();

        return output;
    }

    /**
     * Allows someone to get the full data of a Student from the Main Table.
     *
     * @param firstName the student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @return A String containing all of the Data
     * @throws Exception for SQL Errors
     */
    public static String selectTracker(String firstName, String lastName, int studentID) throws Exception {
        //converts name to studentName following convention format
        String studentName = makeName(firstName, lastName, studentID);

        //creates a connection
        connection = getConnection();

        //SQL Query to find find data
        String query = "select * from " + tableName + " where fullName = '" + studentName + "'";

        //Create the java Statement (Goes in Query)
        Statement statement = connection.createStatement();

        //The Result after executing the query
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();

        //Creates the Output String
        String output = "Primary Key ID Number: " + resultSet.getInt("id") + ", "
                + "First Name: " + resultSet.getString("firstName") + ", "
                + "Last Name: " + resultSet.getString("lastName") + ", "
                + "Student ID: " + resultSet.getInt("studentID") + ", "
                + "Grade: " + resultSet.getShort("grade") + ", "
                + "Community Service Hours: " + resultSet.getInt("communityServiceHours") + ", "
                + "Community Service Category: " + resultSet.getString("communityServiceCategory") + ", "
                + "Years Done: " + resultSet.getShort("yearsDone");

        //closes resources
        statement.close();
        resultSet.close();
        connection.close();
        return output;
    }

    /**
     * This method gets the data of a student with the information listed, but returns it all in its
     * special class as a StudentData object.
     *
     * @param firstName The Student's First Name
     * @param lastName  The Student's Last Name
     * @param studentID The Student's ID
     * @return A StudentData Object with all of the Student's Information
     * @throws Exception In case column names don't exist or connection to database fails
     */
    public static StudentData selectTrackerAsStudent(String firstName, String lastName, int studentID) throws Exception {
        //converts name to studentName following convention format
        String studentName = makeName(firstName, lastName, studentID);

        StudentData studentData = new StudentData();

        //creates a connection
        connection = getConnection();

        //SQL Query to find find data
        String query = "select * from " + tableName + " where fullName = '" + studentName + "'";

        //Create the java Statement (Goes in Query)
        Statement statement = connection.createStatement();

        //The Result after executing the query
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();

        //Adds the Data to the StudentData Object
        studentData.setFirstName(resultSet.getString("firstName"));
        studentData.setLastName(resultSet.getString("lastName"));
        studentData.setStudentID(resultSet.getInt("studentID"));
        studentData.setGrade(resultSet.getShort("Grade"));
        studentData.setCommunityServiceHours(resultSet.getInt("communityServiceHours"));
        studentData.setCommunityServiceCategory(resultSet.getString("communityServiceCategory"));
        studentData.setYearsDone(resultSet.getShort("yearsDone"));
        studentData.setEmail(resultSet.getString("email"));
        studentData.setLastEdited(resultSet.getDate("lastEdited"));

        //closes resources
        statement.close();
        resultSet.close();
        connection.close();

        return studentData;
    }

    /**
     * This method gets the data of a student with the information listed, but returns it all in its
     * special class as a StudentData object.
     *
     * @param student The Student who is being queried
     * @return A StudentData Object with all of the Student's Information
     * @throws Exception In case column names don't exist or connection to database fails
     */
    public static StudentData selectTrackerAsStudent(Student student) throws Exception {
        //converts name to studentName following convention format
        String studentName = makeName(student.getFirstName(), student.getLastName(), student.getStudentID());

        StudentData studentData = new StudentData();

        //creates a connection
        connection = getConnection();

        //SQL Query to find find data
        String query = "select * from " + tableName + " where fullName = '" + studentName + "'";

        //Create the java Statement (Goes in Query)
        Statement statement = connection.createStatement();

        //The Result after executing the query
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();

        //Adds the Data to the StudentData Object
        studentData.setFirstName(resultSet.getString("firstName"));
        studentData.setLastName(resultSet.getString("lastName"));
        studentData.setStudentID(resultSet.getInt("studentID"));
        studentData.setGrade(resultSet.getShort("Grade"));
        studentData.setCommunityServiceHours(resultSet.getInt("communityServiceHours"));
        studentData.setCommunityServiceCategory(resultSet.getString("communityServiceCategory"));
        studentData.setYearsDone(resultSet.getShort("yearsDone"));
        studentData.setEmail(resultSet.getString("email"));
        studentData.setLastEdited(resultSet.getDate("lastEdited"));

        //closes resources
        statement.close();
        resultSet.close();
        connection.close();

        return studentData;
    }

    /**
     * Creates an ArrayList which holds the data of all Student's whose data is stored.
     * This makes it much faster to view data of every student.
     *
     * @return An ArrayList storing all Student Data
     * @throws Exception For Column Names and Failing Connections
     */
    public static ArrayList<StudentData> selectFullTracker() throws Exception {
        //The ArrayList
        ArrayList<StudentData> studentDataList = new ArrayList<>();
        //creates a connection
        connection = getConnection();

        //SQL Query to find find data
        String query = "select * from " + tableName;

        //Create the java Statement (Goes in Query)
        Statement statement = connection.createStatement();

        //The Result after executing the query
        ResultSet resultSet = statement.executeQuery(query);

        //While there is more data, it keeps running
        while (resultSet.next()) {
            //Creates a StudentData Object
            StudentData studentData = new StudentData();

            //Adds the Data to the StudentData Object
            studentData.setFirstName(resultSet.getString("firstName"));
            studentData.setLastName(resultSet.getString("lastName"));
            studentData.setStudentID(resultSet.getInt("studentID"));
            studentData.setGrade(resultSet.getShort("Grade"));
            studentData.setCommunityServiceHours(resultSet.getInt("communityServiceHours"));
            studentData.setCommunityServiceCategory(resultSet.getString("communityServiceCategory"));
            studentData.setYearsDone(resultSet.getShort("yearsDone"));
            studentData.setEmail(resultSet.getString("email"));
            studentData.setLastEdited(resultSet.getDate("lastEdited"));

            //Adds the StudentData to the List
            studentDataList.add(studentData);
        }

        //closes resources
        statement.close();
        resultSet.close();
        connection.close();

        return studentDataList;
    }

    /**
     * This allows access to all of a student's events
     *
     * @param firstName the student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @return an array of Strings containing all of the Events and their data
     * @throws Exception for SQL Errors
     */
    public static String[] selectStudentEvents(String firstName, String lastName, int studentID) throws Exception {
        //converts to the studentName format
        String studentName = makeName(firstName, lastName, studentID);

        //Uses getConnection to create a connection
        connection = getConnection();

        //SQL Query to find find data
        String query = "select * from " + studentName;

        //Create the java Statement (Runs the Query)
        statement = connection.createStatement();

        //The Result after executing the query
        ResultSet resultSet = statement.executeQuery(query);

        //Creates Output Strings
        String[] output = new String[numberOfRows(studentName)];
        int count = 0;

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String eventName = resultSet.getString("eventName");
            double eventHours = resultSet.getDouble("eventHours");
            String date = resultSet.getDate("date").toString();
            output[count] = String.format("Event ID: %s, Event Name: %s, Event Hours: %s, Date of Event: %s",
                    id, eventName, eventHours, date);
            count++;
        }
        resultSet.close();
        statement.close();

        return output;
    }

    /**
     * Selects data on just one event given its eventName
     *
     * @param firstName The student's first name
     * @param lastName  The student's last name
     * @param studentID The Student's Student ID Number
     * @param eventName The name of the event
     * @return A String containing all of the Event Details
     * @throws Exception for SQL Exceptions
     */
    public static String selectStudentEvent(String firstName, String lastName, int studentID, String eventName) throws Exception {
        //converts to the studentName format
        String studentName = makeName(firstName, lastName, studentID);

        //Uses getConnection to create a connection
        connection = getConnection();

        //SQL Query to find find data
        String query = "select * from " + studentName + " where eventName = '" + eventName + "'";

        //Create the java Statement (Runs the Query)
        statement = connection.createStatement();

        //The Result after executing the query
        ResultSet resultSet = statement.executeQuery(query);

        //Creates Output Strings
        resultSet.next();
        int id = resultSet.getInt("id");
        double eventHours = resultSet.getDouble("eventHours");
        String date = resultSet.getDate("date").toString();
        String output = String.format("Event ID: %s, Event Name: %s, Event Hours: %s, Date of Event: %s",
                id, eventName, eventHours, date);
        resultSet.close();
        statement.close();
        return output;
    }

    /**
     * This allows a user to get the hours of a specific event
     *
     * @param firstName the student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @param eventName the name of the event
     * @return a double containing the hours
     * @throws Exception for SQL Errors
     */
    public static double selectEventHours(String firstName, String lastName, int studentID, String eventName) throws Exception {
        //converts to the studentName format
        String studentName = makeName(firstName, lastName, studentID);

        //Uses getConnection to create a connection
        connection = getConnection();

        //SQL Query to find find data
        String query = "select eventHours from " + studentName + " where eventName = '" + eventName + "'";

        //Create the java Statement (Runs the Query)
        statement = connection.createStatement();

        //The Result after executing the query
        resultSet = statement.executeQuery(query);
        resultSet.next();

        //Creates Output Strings
        double output = resultSet.getDouble("eventHours");

        resultSet.close();
        connection.close();
        statement.close();

        return output;
    }

    /**
     * This updates a value in the Main Table
     *
     * @param firstName the student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @param dataType  the data field being changed
     * @param newData   a new data value (in string form) for the location
     * @throws Exception for SQL Errors
     */
    public static void updateTracker(String firstName, String lastName, int studentID, String dataType, String newData) throws Exception {
        //gets connection with database
        connection = getConnection();

        String fullName = changeName(firstName, lastName);

        //Generates a query
        String query = "update tracker set " + dataType + " = " + newData + " where fullName = '" + fullName + "' and studentID = '" + studentID + "'";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //executes query
        preparedStatement.executeUpdate();

        //close resources
        preparedStatement.close();
        connection.close();

        //Update last edited
        updateToCurrentDate(firstName, lastName, studentID);
    }

    /**
     * This allows changing student-specific event data
     *
     * @param firstName the student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @param dataType  the data field name
     * @param newData   the new data value (in String form)
     * @param eventName the name of the event being changed
     * @throws Exception for SQL Errors
     */
    public static void updateEvent(String firstName, String lastName, int studentID, String dataType, String newData, String eventName) throws Exception {
        //gets connection with database
        connection = getConnection();

        String tableName = makeName(firstName, lastName, studentID);

        //only used if the data field is hours
        double initialHours = 0;
        if (dataType.equals("eventHours")) {
            initialHours = selectEventHours(firstName, lastName, studentID, eventName);
        }

        //Generates a query
        String query = "update " + tableName + " set " + dataType + " = " + newData + " where eventName = '" + eventName + "'";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //executes query
        preparedStatement.executeUpdate();

        //close resources
        preparedStatement.close();
        connection.close();

        if (dataType.equals("eventHours")) {
            double currentHours = selectTrackerDouble(firstName, lastName, studentID, "communityServiceHours");
            updateTracker(firstName, lastName, studentID, "communityServiceHours",
                    Double.toString(round(currentHours + Double.parseDouble(newData) - initialHours)));//rounds Hours to nearest hundredth to account for inaccuracy of doubles
        }

        updateToCurrentDate(firstName, lastName, studentID);
    }

    /**
     * This method is used to change the name of a student to a more useful
     * and easier to use format.
     *
     * @param nameIn the input name, which should only consist of first name and last name
     * @return Name in the Format first_last
     * @deprecated {@link #changeName(String, String)}
     */
    @Deprecated
    public static String changeName(String nameIn) {
        String name = nameIn.toLowerCase();
        StringTokenizer st = new StringTokenizer(name);
        try {
            return st.nextToken() + "_" + st.nextToken();
        } catch (Exception e) {
            return name;
        }
    }

    /**
     * This turns a name into the format first_last for more uses in a database and to allow for more
     * consistency.
     *
     * @param firstName the student's first name
     * @param lastName  the student's last name
     * @return the String containing the new name
     */
    public static String changeName(String firstName, String lastName) {
        return (firstName + "_" + lastName).toLowerCase();
    }

    /**
     * This method would be used to make student table names in MySQL (firstName_lastName_studentID)
     *
     * @param firstName First Name
     * @param lastName  Last Name
     * @param studentID Student ID
     * @return formatted Name
     */
    public static String makeName(String firstName, String lastName, int studentID) {
        return (firstName + "_" + lastName + "_" + studentID).toLowerCase();
    }

    /**
     * Creates a connection with the database, which is necessary for all SQL Updates
     *
     * @return a connection which we can use
     * @throws Exception for Connection Errors
     */
    private static Connection getConnection() throws Exception {
        //Uses Driver
        Class.forName(Driver);
        connection = DriverManager.getConnection(DB_URL, username, unlocker);
        return connection;
    }

    /**
     * Returns the number of rows in a table. This is extremely useful in knowing the number
     * of times to loop a certain event.
     *
     * @param tableName the name of the table being counted
     * @return the number of rows
     * @throws Exception for SQL Errors
     */
    public static int numberOfRows(String tableName) throws Exception {
        //Sets up connection to database
        connection = getConnection();

        //Creates Statement
        statement = connection.createStatement();

        //Creates variable for count
        int count;

        String query = "select count(*) from " + tableName;
        try {
            //Result
            resultSet = statement.executeQuery(query);
            resultSet.next();
            count = resultSet.getInt(1);
        } finally {
            //Closes ends
            resultSet.close();
            connection.close();
            statement.close();
        }
        return count;
    }

    /**
     * Updates the date in the tracker to the current date for lastEdited
     *
     * @param firstName The First Name of the Student to know who to update
     * @param lastName  The Last Name of the Student to Update
     * @param studentID The Student's Student ID Number
     */
    public static void updateToCurrentDate(String firstName, String lastName, int studentID) {
        //Get's Current Date
        java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());

        try {
            updateTracker(firstName, lastName, studentID, "lastEdited", date.toString());
        } catch (Exception e) {
            //Do Nothing
        }
    }

    /**
     * Rounds the double to the nearest hundredth
     *
     * @param input the double being rounded
     * @return the rounded double
     */
    public static double round(double input) {
        int stageOne = (int) (Math.round(input * 100));
        return (double) (stageOne) / 100;
    }

    public static void setUp() {
        createDatabase();
        createTable();
    }
}