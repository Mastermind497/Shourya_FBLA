package com.Backend;

import com.Frontend.Charts;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private static final String tableName = "tracker";
    public static final String pwTableName = "passwords";
    //The name of the database and table. It is a public static string because it is the same all the time in any file
    public static String databaseName = "student_data";
    //The location of the database
    private static String DB_URL = "jdbc:mysql://localhost:3306/";
    //Connection and Statement
    private static Statement statement = null;
    private static Connection connection = null;
    private static ResultSet resultSet = null;

    public static void main(String[] args) {
        createDatabase();
        createTable();
        System.out.println(selectStudentEventsAsEvent(new Student("Shourya", "Bansal", 224272)));
    }

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
            System.out.println("Database Created");
            //Closes Variables to avoid errors
            statement.close();
            connection.close();

            //Selects this new database for all queries if it is not already selected
        } catch (Exception e) {
            System.out.println("Database Creation Failed");
            e.printStackTrace();
        } finally {
            if (!DB_URL.contains(databaseName)) DB_URL += databaseName;
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

    public static void createPasswordTable() {
        connection = null;
        statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();

            String query = "CREATE TABLE IF NOT EXISTS " + pwTableName + "("
                    + "username VARCHAR(255) NOT NULL PRIMARY KEY, "
                    + "password VARCHAR(255) NOT NULL"
                    + ")";

            statement.executeUpdate(query);

            try {
                connection.close();
                statement.close();
            } catch (SQLException e) {
                //Do Nothing
            }
        } catch (Exception e) {
            System.out.println("Password Table Fail");
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
                    + "eventName VARCHAR(255),"
                    + "eventHours DOUBLE NOT NULL,"
                    + "eventDate DATE"
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
            String fullName = makeName(firstName, lastName, studentID);

            createStudentTable(firstName, lastName, studentID);

            //creates a connection to the MySQL
            connection = getConnection();

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
            preparedStatement.setObject(9, LocalDate.now());

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


        //Converts hours into two parts: int and decimal
        String query = " insert into " + makeName(firstName, lastName, studentID) + " (eventName, eventHours, eventDate)"
                + " values (?, ?, ?)";

        //Creates a preparedStatement to insert into mysql command line
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, eventName);
        preparedStatement.setDouble(2, hours);
        preparedStatement.setObject(3, LocalDate.of(year, month, day));

        //executes statement
        preparedStatement.execute();

        //Ends everything
        connection.close();
        statement.close();

        //next step, update hours in the main table
        //gets current main hours
        double currentHours = selectTrackerDouble(firstName, lastName, studentID, "CommunityServiceHours");
        System.out.println("Adding an Event");
        updateTracker(firstName, lastName, studentID, "CommunityServiceHours",
                Double.toString(round(currentHours + hours)));//rounds Hours to nearest hundredth to account for inaccuracy of doubles
        System.out.println("Event Added");

        //now, update lastEdited in main table
        updateToCurrentDate(firstName, lastName, studentID);
    }

    /**
     * This adds events and hours into a students unique table. At the
     * same time, this updates the hours in the main table
     *
     * @param student   The student getting hours
     * @param eventName The Name of the Event Completed
     * @param hours     The Length of the Event
     * @param year      The Year the Event Was Done
     * @param month     The Month of the Event
     * @param day       The Day of the Event
     * @throws Exception This is throws in case the Database is not found
     */
    public static void addStudentHours(Student student, String eventName, double hours,
                                       int year, int month, int day) throws Exception {
        if (hours != 0) {
            //Creates a database connection
            connection = getConnection();

            //Converts hours into two parts: int and decimal
            String query = " insert into " + makeName(student) + "(eventName, eventHours, eventDate)"
                    + " values (?, ?, ?)";

            //Creates a preparedStatement to insert into mysql command line
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, eventName);
            preparedStatement.setDouble(2, hours);
            preparedStatement.setObject(3, LocalDate.of(year, month, day));

            //executes statement
            preparedStatement.execute();

            //Ends everything
            connection.close();
            statement.close();

            //next step, update hours in the main table
            //gets current main hours:
            if (!eventName.equals("MANUAL ADJUSTMENT")) {
                double currentHours = selectTrackerDouble(student.getFirstName(), student.getLastName(),
                        student.getStudentID(), "CommunityServiceHours");
                updateTracker(student.getFirstName(), student.getLastName(),
                        student.getStudentID(), "CommunityServiceHours",
                        Double.toString(round(currentHours + hours)));//rounds Hours to nearest hundredth to account for inaccuracy of doubles
                System.out.println("Manually Adjusting because in Event Update");
            }

            //now, update lastEdited in main table
            updateToCurrentDate(student.getFirstName(), student.getLastName(), student.getStudentID());
        }
    }

    /**
     * This stores the password in a database. However, to secure them, they use a hashing function
     * (explained in the documentation) which securely converts the password into an impenetrable
     * String. It is a one way function, thus stopping anyone from figuring out what the password is.
     *
     * @param username       the Username of the user being added
     * @param hashedPassword the password after hash encryption
     * @throws Exception This is throws in case the Database is not found
     */
    public static void addUserPass(String username, String hashedPassword) throws Exception {
        //Creates a database connection
        connection = getConnection();

        //Converts hours into two parts: int and decimal
        String query = " insert into " + pwTableName + " (username, password)"
                + " values (?, ?)";

        //Creates a preparedStatement to insert into mysql command line
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, hashedPassword);

        //executes statement
        preparedStatement.execute();

        //Ends everything
        connection.close();
        statement.close();
    }

    /**
     * Gets the hashed password given a username
     *
     * @param username the Username for which a password is being found
     * @return the hashed password
     */
    public static String selectPass(String username) {
        String output;
        try {
            //Creates a connection
            connection = getConnection();

            //SQL Query to find find data
            String query = "select password  from " + pwTableName + " where username = '" + username + "'";

            //Create the java Statement (Goes in Query)
            statement = connection.createStatement();

            //The Result after executing the query
            ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();

            //returns the String inside column "data"
            output = resultSet.getString("password");

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
     * This gets an List of all users in the table for admin to see
     *
     * @return An List which contains all of the users in the system
     */
    public static List<String> selectUsers() {
        List<String> output = new ArrayList<>();
        try {
            //Creates a connection
            connection = getConnection();

            //SQL Query to find find data
            String query = "select password  from " + pwTableName + " where username = '" + username + "'";

            //Create the java Statement (Goes in Query)
            statement = connection.createStatement();

            //The Result after executing the query
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                output.add(resultSet.getString(1));
            }

            try {
                //Ends everything
                connection.close();
                statement.close();
                resultSet.close();
            } catch (Exception e) {
                //Do Nothing
            }
            if (output.isEmpty()) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        return output;
    }

    /**
     * This allows someone to get specific integer-type data from the Main Table.
     *
     * @param firstName The Student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @param data      the data field being accessed
     * @return an integer containing the data
     */
    public static int selectTrackerInt(String firstName, String lastName, int studentID, String data) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
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
     */
    public static double selectTrackerDouble(String firstName, String lastName, int studentID, String data) {
        try {
            String fullName = makeName(firstName, lastName, studentID);

            //Uses getConnection to create a connection with the database
            connection = getConnection();

            //SQL Query to find find data
            String query = "SELECT " + data + " FROM " + tableName + " WHERE fullName = '" + fullName + "'";

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
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public static double selectTrackerDouble(Student student, String data) {
        return selectTrackerDouble(student.getFirstName(), student.getLastName(), student.getStudentID(), data);
    }

    /**
     * This allows someone to get specific short-type data from the Main Table.
     *
     * @param firstName the student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @param data      the data field being accessed
     * @return a short containing the data
     */
    public static short selectTrackerShort(String firstName, String lastName, int studentID, String data) {
        String studentName = makeName(firstName, lastName, studentID);

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
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
                + "Community Service Hours: " + resultSet.getDouble("communityServiceHours") + ", "
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
     */
    public static StudentData selectTrackerAsStudent(String firstName, String lastName, int studentID) {
        try {
            //converts name to studentName following convention format
            String studentName = makeName(firstName, lastName, studentID);

            StudentData studentData = new StudentData(true);

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
            studentData.setCommunityServiceHours(resultSet.getDouble("communityServiceHours"));
            studentData.setCommunityServiceCategory(resultSet.getString("communityServiceCategory"));
            studentData.setYearsDone(resultSet.getShort("yearsDone"));
            studentData.setEmail(resultSet.getString("email"));
            studentData.setLastEdited(resultSet.getDate("lastEdited").toString());

            //closes resources
            statement.close();
            resultSet.close();
            connection.close();

            return studentData;
        } catch (Exception e) {
            e.printStackTrace();
            return new StudentData();
        }
    }

    /**
     * This method gets the data of a student with the information listed, but returns it all in its
     * special class as a StudentData object.
     *
     * @param student The Student who is being queried
     * @return A StudentData Object with all of the Student's Information
     */
    public static StudentData selectTrackerAsStudent(Student student) {
        try {
            //converts name to studentName following convention format
            String studentName = makeName(student.getFirstName(), student.getLastName(), student.getStudentID());

            StudentData studentData = new StudentData(true);

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
            studentData.setCommunityServiceHours(resultSet.getDouble("communityServiceHours"));
            studentData.setCommunityServiceCategory(resultSet.getString("communityServiceCategory"));
            studentData.setYearsDone(resultSet.getShort("yearsDone"));
            studentData.setEmail(resultSet.getString("email"));
            studentData.setLastEdited(resultSet.getDate("lastEdited").toString());

            //closes resources
            statement.close();
            resultSet.close();
            connection.close();

            return studentData;
        } catch (Exception e) {
            e.printStackTrace();
            return new StudentData();
        }
    }

    /**
     * Creates an List which holds the data of all Student's whose data is stored.
     * This makes it much faster to view data of every student.
     *
     * @return An List storing all Student Data
     */
    public static List<StudentData> selectFullTracker() {
        try {
            //The List
            List<StudentData> studentDataList = new ArrayList<>();
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
                StudentData studentData = new StudentData(true);

                //Adds the Data to the StudentData Object
                studentData.setFirstName(resultSet.getString("firstName"));
                studentData.setLastName(resultSet.getString("lastName"));
                studentData.setStudentID(resultSet.getInt("studentID"));
                studentData.setGrade(resultSet.getShort("Grade"));
                studentData.setCommunityServiceHours(resultSet.getDouble("communityServiceHours"));
                studentData.setCommunityServiceCategory(resultSet.getString("communityServiceCategory"));
                studentData.setYearsDone(resultSet.getShort("yearsDone"));
                studentData.setEmail(resultSet.getString("email"));
                studentData.setLastEdited(resultSet.getDate("lastEdited").toString());

                //Adds the StudentData to the List
                studentDataList.add(studentData);
            }

            //closes resources
            statement.close();
            resultSet.close();
            connection.close();

            return studentDataList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<Student> getStudents() {
        List<Student> students = new ArrayList<>();

        try {
            connection = getConnection();

            String query = "select * from " + tableName;

            statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Student student = new Student();

                //Adds the Data to the StudentData Object
                student.setFirstName(resultSet.getString("firstName"));
                student.setLastName(resultSet.getString("lastName"));
                student.setStudentID(resultSet.getInt("studentID"));

                students.add(student);
            }

            connection.close();
            statement.close();
            resultSet.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Get Students Failed");
        }

        return students;
    }

    public static List<StudentData> getStudentData() {
        List<StudentData> studentData = new ArrayList<>();

        try {
            connection = getConnection();

            String query = "select * from " + tableName;

            statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Student student = new Student();
                //Adds the Data to the StudentData Object
                student.setFirstName(resultSet.getString("firstName"));
                student.setLastName(resultSet.getString("lastName"));
                student.setStudentID(resultSet.getInt("studentID"));

                studentData.add(student.getStudentData());
            }

            connection.close();
            statement.close();
            resultSet.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Get Students Failed");
        }

        return studentData;
    }

    public static int numOfStudents() {
        int numOfStudents = 0;
        try {
            connection = getConnection();

            String query = "select COUNT(*) from " + tableName;

            statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(query);

            resultSet.next();

            numOfStudents = resultSet.getInt(1);

            connection.close();
            statement.close();
            resultSet.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Get Students Failed");
        }

        return numOfStudents;
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
            String date = resultSet.getDate("eventDate").toString();
            output[count] = String.format("Event ID: %s, Event Name: %s, Event Hours: %s, Date of Event: %s",
                    id, eventName, eventHours, date);
            count++;
        }
        resultSet.close();
        statement.close();

        return output;
    }

    /**
     * This allows access to all of a student's events
     *
     * @param firstName the student's first name
     * @param lastName  the student's last name
     * @param studentID The Student's Student ID Number
     * @return an array of Strings containing all of the Events and their data
     */
    public static List<Event> selectStudentEventsAsEvent(String firstName, String lastName, int studentID) {
        try {
            //converts to the studentName format
            String studentName = makeName(firstName, lastName, studentID);

            Student student = new Student(firstName, lastName, studentID);

            //Uses getConnection to create a connection
            connection = getConnection();

            //SQL Query to find data
            String query = "select * from " + studentName;

            //Create the java Statement (Runs the Query)
            statement = connection.createStatement();

            //The Result after executing the query
            ResultSet resultSet = statement.executeQuery(query);

            //Creates Output Strings
            List<Event> output = new ArrayList<>();

            while (resultSet.next()) {
                Event next = new Event();
                next.setEventName(resultSet.getString("eventName"));
                next.setHours(resultSet.getDouble("eventHours"));
                LocalDate date = (LocalDate) resultSet.getObject("eventDate");
                next.setDate(date);
                next.setStudent(student);
                output.add(next);
            }
            resultSet.close();
            statement.close();

            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * This allows access to all of a student's events
     *
     * @param student the selected student
     * @return an array of Strings containing all of the Events and their data
     */
    public static List<Event> selectStudentEventsAsEvent(Student student) {
        return selectStudentEventsAsEvent(student.getFirstName(), student.getLastName(), student.getStudentID());
    }

    /**
     * This is mainly used in reports, where we want to get dates in a certain range. Thus,
     * this method gets a start date and adds a limitation on what to select from the student table.
     *
     * @param student   The Student whose events we need
     * @param startDate The Start Date of the Events
     * @return An List containing the events being used
     */
    public static List<Event> selectStudentEventsInRange(Student student, Date startDate) {
        List<Event> output = new ArrayList<>();
        try {
            String fullName = makeName(student);

            connection = getConnection();

            //Creates a query
            String query = "SELECT * FROM " + fullName + " WHERE eventDate >= ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, startDate.getLocalDate());
            ResultSet resultSet = preparedStatement.executeQuery();

            //Creates Output Strings
            while (resultSet.next()) {
                Event next = new Event();
                next.setEventName(resultSet.getString("eventName"));
                next.setHours(resultSet.getDouble("eventHours"));
                LocalDate date = (LocalDate) resultSet.getObject("eventDate");
                next.setDate(date);
                next.setStudent(student);
                output.add(next);
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Couldn't Get Student Events in Range");
        }

        return output;
    }

    public static List<StudentData> getStudentData(String range) {
        List<StudentData> studentDataList = getStudentData();

        //Replaces each student's hours with the hours from their time range
        for (StudentData s : studentDataList) {
            switch (range) {
                case Charts.WEEK_CHART:
                    s.setCommunityServiceHoursTemp(getHoursWeek(s));
                    break;
                case Charts.MONTH_CHART:
                    s.setCommunityServiceHoursTemp(getHoursMonth(s));
                    break;
                case Charts.YEAR_CHART:
                    s.setCommunityServiceHoursTemp(getHoursYear(s));
                    break;
                default:
                    s.setCommunityServiceHoursTemp(getHoursAll(s));
                    break;
            }
        }

        return studentDataList;
    }

    public static double getHoursWeek(Student student) {
        double hours = 0; //Prepares the hours variable for taking in numbers
        try {
            String formattedName = makeName(student);
            connection = getConnection();
            //Creates a query which selects the communityServiceHours of Events which were done in the past week for a specific student
            String query = "SELECT eventHours FROM " + formattedName + " WHERE eventDate >= ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, LocalDate.now().minusDays(7));

            //Gets the results from executing the query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                hours += resultSet.getDouble("eventHours");
            }

            resultSet.close();
            connection.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Weekly Hours Failed");
        }
        return hours;
    }

    public static double getHoursMonth(Student student) {
        double hours = 0; //Prepares the hours variable for taking in numbers
        try {
            String formattedName = makeName(student);
            connection = getConnection();
            //Creates a query which selects the communityServiceHours of Events which were done in the past week for a specific student
            String query = "SELECT eventHours FROM " + formattedName + " WHERE eventDate >= ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, LocalDate.now().minusMonths(1));

            //Gets the results from executing the query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                hours += resultSet.getDouble("eventHours");
            }

            resultSet.close();
            connection.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Weekly Hours Failed");
        }
        return hours;
    }

    public static double getHoursYear(Student student) {
        double hours = 0; //Prepares the hours variable for taking in numbers
        try {
            String formattedName = makeName(student);
            connection = getConnection();
            //Creates a query which selects the communityServiceHours of Events which were done in the past week for a specific student
            String query = "SELECT eventHours FROM " + formattedName + " WHERE eventDate >= ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, LocalDate.now().minusYears(1));

            //Gets the results from executing the query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                hours += resultSet.getDouble("eventHours");
            }

            resultSet.close();
            connection.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Weekly Hours Failed");
        }
        return hours;
    }

    public static double getHoursAll(Student student) {
        double hours = 0; //Prepares the hours variable for taking in numbers
        try {
            String formattedName = makeName(student);
            connection = getConnection();
            //Creates a query which selects the communityServiceHours of Events which were done in the past week for a specific student
            String query = "SELECT eventHours FROM " + formattedName;
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            //Gets the results from executing the query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                hours += resultSet.getDouble("eventHours");
            }

            resultSet.close();
            connection.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Weekly Hours Failed");
        }
        return hours;
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
     * @deprecated replaced by Event Class. To Be deleted in a different commit
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
        String date = resultSet.getDate("eventDate").toString();
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

        String fullName = makeName(firstName, lastName, studentID);

        if (dataType.equals("communityServiceHours")) {
            //Rounds hours to the nearest hundredth
            double newDataDouble = round(Double.parseDouble(newData));
            newData = Double.toString(newDataDouble);

            Student selected = new Student(firstName, lastName, studentID);

            double currentHours = selectTrackerDouble(
                    selected.getFirstName(), selected.getLastName(), selected.getStudentID(), "communityServiceHours");

            double hourChange = round(newDataDouble - currentHours);

            //Gets today's date
            MySQLMethods.addStudentHours(selected, "MANUAL ADJUSTMENT", hourChange, LocalDate.now().getYear(),
                    LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
        }

        //Generates a query
        connection = getConnection();
        String query = "update tracker set " + dataType + " = '" + newData + "' where fullName = '" + fullName + "'";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //executes query
        preparedStatement.executeUpdate();

        //close resources
        preparedStatement.close();
        connection.close();

        //Update last edited
        if (!dataType.equals("lastEdited")) updateToCurrentDate(firstName, lastName, studentID);
    }

    public static void updateFirstName(String firstName, String lastName, int studentID, String newFirstName) {
        try {
            connection = getConnection();
            String fullName = makeName(firstName, lastName, studentID);
            String newFullName = makeName(newFirstName, lastName, studentID);
            //First, change full name
            String query = "update tracker set fullName = '" + newFullName + "' where fullName = '" + fullName + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            //Now, Change Individual Name Component
            String individualQuery = "UPDATE tracker SET firstName = '" + newFirstName + "' where fullName = '" + newFullName + "'";
            PreparedStatement individualPreparedStatement = connection.prepareStatement(individualQuery);
            individualPreparedStatement.executeUpdate();
            individualPreparedStatement.close();

            //Finally, update table name to match
            String nextQuery = "RENAME TABLE " + fullName + " TO " + newFullName;
            PreparedStatement preparedStatementNext = connection.prepareStatement(nextQuery);
            preparedStatementNext.executeUpdate();
            preparedStatementNext.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateLastName(String firstName, String lastName, int studentID, String newLastName) {
        try {
            connection = getConnection();
            String fullName = makeName(firstName, lastName, studentID);
            String newFullName = makeName(firstName, newLastName, studentID);

            //First, change full name
            String query = "update tracker set fullName = '" + newFullName + "' where fullName = '" + fullName + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            //Now, Change Individual Name Component
            String individualQuery = "UPDATE tracker SET lastName = '" + newLastName + "' where fullName = '" + newFullName + "'";
            PreparedStatement individualPreparedStatement = connection.prepareStatement(individualQuery);
            individualPreparedStatement.executeUpdate();
            individualPreparedStatement.close();

            //Finally, change the table name to match
            String nextQuery = "RENAME TABLE " + fullName + " TO " + newFullName;
            PreparedStatement preparedStatementNext = connection.prepareStatement(nextQuery);
            preparedStatementNext.executeUpdate();
            preparedStatementNext.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateStudentID(String firstName, String lastName, int studentID, int newStudentID) {
        try {
            connection = getConnection();
            String fullName = makeName(firstName, lastName, studentID);
            String newFullName = makeName(firstName, lastName, newStudentID);

            //First, change full name
            String query = "UPDATE tracker SET fullName = '" + newFullName + "' WHERE fullName = '" + fullName + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            //Now, Change Individual Student ID
            String individualQuery = "UPDATE tracker SET studentID = '" + newStudentID + "' where fullName = '" + newFullName + "'";
            PreparedStatement individualPreparedStatement = connection.prepareStatement(individualQuery);
            individualPreparedStatement.executeUpdate();
            individualPreparedStatement.close();

            //Finally, update table name to match
            String nextQuery = "RENAME TABLE " + fullName + " TO " + newFullName;
            PreparedStatement preparedStatementNext = connection.prepareStatement(nextQuery);
            preparedStatementNext.executeUpdate();
            preparedStatementNext.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateTracker(Student initialStudent, StudentData newData) throws Exception {
        String fullName = initialStudent.getFullName();

        //Rounds hours to the nearest hundredth
        double newDataDouble = round(newData.getCommunityServiceHours());
        newData.setCommunityServiceHours(newDataDouble);

        //Updates Last Edited While Name is Known
        updateToCurrentDate(initialStudent.getFirstName(), initialStudent.getLastName(), initialStudent.getStudentID());

        //Generates a query
        String query = "update " + tableName + " set " +
                "firstName = '" + newData.getFirstName() + "', " +
                "lastName = '" + newData.getLastName() + "', " +
                "studentID = '" + newData.getStudentID() + "', " +
                "grade = '" + newData.getGrade() + "', " +
                "communityServiceHours = '" + newData.getCommunityServiceHours() + "', " +
                "communityServiceCategory = '" + newData.getCommunityServiceCategory() + "', " +
                "email = '" + newData.getEmail() + "', " +
                "yearsDone = '" + newData.getYearsDone() + "', " +
                "fullName = '" + makeName(newData.getFirstName(), newData.getLastName(), newData.getStudentID()) +
                "' where fullName = '" + fullName + "'";
        PreparedStatement preparedStatement = getConnection().prepareStatement(query);

        //executes query
        preparedStatement.executeUpdate();

        //close resources
        preparedStatement.close();
        connection.close();
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
     */
    public static void updateEvent(String firstName, String lastName, int studentID, String dataType, String newData, String eventName) {
        try {
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
                updateTracker(firstName, lastName, studentID, "CommunityServiceHours",
                        Double.toString(round(currentHours + Double.parseDouble(newData) - initialHours)));//rounds Hours to nearest hundredth to account for inaccuracy of doubles
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            updateToCurrentDate(firstName, lastName, studentID);
        }
    }

    public static void updateEvent(Student student, Event oldEvent, Event newEvent) {
        //Gets connection to database
        try {
            connection = getConnection();
            String tableName = makeName(student);

            String query =
                    "UPDATE " + tableName + " SET eventName = ?, eventHours = ?, eventDate = ? " +
                            "WHERE eventName = ? AND eventHours = ? AND eventDate = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newEvent.getEventName());
            preparedStatement.setDouble(2, newEvent.getHours());
            preparedStatement.setObject(3, newEvent.getLocalDate());
            preparedStatement.setString(4, oldEvent.getEventName());
            preparedStatement.setDouble(5, oldEvent.getHours());
            preparedStatement.setObject(6, oldEvent.getLocalDate());
            preparedStatement.executeUpdate();

            //closes resources
            preparedStatement.close();
            connection.close();

            updateToCurrentDate(student.getFirstName(), student.getLastName(), student.getStudentID());
            System.out.println("Executed Event Update");

            //Fix main table total
            StudentData data = student.getStudentData();
            double newEventHours = newEvent.getHours() - oldEvent.getHours();
            double finalHours = data.getCommunityServiceHours() + newEventHours;
            data.setCommunityServiceHours(finalHours);
            updateTracker(student, data);
//            String mainQuery = "UPDATE tracker SET eventHours = ? WHERE fullName = ?";
//            PreparedStatement mainPreparedStatement = connection.prepareStatement(query);
//            mainPreparedStatement.setDouble(1, finalHours);
//            mainPreparedStatement.setString(2, tableName);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Update Event Failed");
        }
    }

    /**
     * This method deletes a student and his or her respective data table
     *
     * @param student The Deleted Student
     */
    public static void delete(Student student) {
        //Name of student being deleted in usable form
        String fullName = makeName(student);

        try {
            connection = getConnection();

            //Deletes Student from tracker
            String query = "DELETE FROM tracker WHERE fullName = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, fullName);
            statement.executeUpdate();
            statement.close();

            //Drops table with Student Events
            connection = getConnection();
            query = "DROP TABLE " + fullName;
            statement = connection.prepareStatement(query);
            statement.executeUpdate();
            statement.close();

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Couldn't Delete Student");
        }
    }

    /**
     * Deletes an Event
     *
     * @param event the Event
     */
    public static void delete(Event event) {
        //Name of student being deleted in usable form
        String fullName = makeName(event.getStudent());

        try {
            connection = getConnection();

            //Deletes Event from Student
            String query = "DELETE FROM " + fullName + " WHERE eventName = ? AND eventHours = ? AND eventDate = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, event.getEventName());
            statement.setDouble(2, event.getHours());
            statement.setObject(3, event.getLocalDate());
            statement.executeUpdate();
            statement.close();

            //Updates Total Student Hours with new amount
            double currentHours = event.getStudentData().getCommunityServiceHours();
            double newHours = round(currentHours - event.getHours());
            query = "UPDATE tracker SET communityServiceHours = ? WHERE fullName = ?";
            connection = getConnection();
            statement = connection.prepareStatement(query);
            statement.setDouble(1, newHours);
            statement.setString(2, fullName);
            statement.executeUpdate();
            statement.close();

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Couldn't Delete Event");
        }
    }

    /**
     * This method is used to change the name of a student to a more useful
     * and easier to use format.
     *
     * @param nameIn the input name, which should only consist of first name and last name
     * @return Name in the Format first_last
     * @deprecated {@link #makeName(String, String, int)}
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
     * @deprecated {@link #makeName(String, String, int)}{@link #makeName(Student)}
     */
    @Deprecated
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
     * Formats the name of a student
     *
     * @param student The Student whose name is being formatted
     * @return the name in the format of firstName_lastName_studentID
     */
    public static String makeName(Student student) {
        return makeName(student.getFirstName(), student.getLastName(), student.getStudentID());
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

        try {
            updateTracker(firstName, lastName, studentID, "lastEdited", LocalDate.now().toString());
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
