package main;

import recordmodels.ExamApplicationRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    final static String url = "jdbc:mysql://localhost:3306/javabase";
    final static String username = "user";
    final static String password = "password";

    public static void main(final String[] args) {

        List<Float> dels = new ArrayList<>();
        Random rand = new Random();
        String recordTableCreate =
                "create table application_records (" +
                        "  id int unsigned auto_increment not null," +
                        "  name varchar(32) not null," +
                        "  subject varchar(32) not null," +
                        "  date_created timestamp default now()," +
                        "  timestamp bigint(255)" +
                        "  primary key (id)" +
                        ");";

        System.out.println("Connecting database...");
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database connected!");
            Statement stmt = connection.createStatement();
            //stmt.executeUpdate(recordTableCreate)
            for (int j = 0; j < 1; j++) {
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(rand.nextInt(50) + 20);
                    insertRecord(new ExamApplicationRecord("Peter", "XYUXUA",
                            ExamApplicationRecord.Subject.TERMINFO, new Date(System.currentTimeMillis()),
                            System.nanoTime()), connection);

                    ResultSet rs = stmt.executeQuery("select * from application_records");
                    while (rs.next()) {
                        float delay = (float) (System.nanoTime() - rs.getLong(5)) / 1000000;
                        dels.add(delay);
                        //dels.set(i, dels.get(i) + delay);
                        System.out.println(i + 1 + ". record with " + delay + " ms");
                    }
                    stmt.executeUpdate(
                            "delete from application_records;"
                    );
                }
            }
            //reading

            try {
                FileWriter myWriter = null;
                Scanner myReader = new Scanner(new File("C:\\Users\\koss6\\IdeaProjects\\MySQL_example\\src\\output2PC.txt"));
                int numberOfTests = Integer.parseInt(myReader.nextLine());
                int i = 0;
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    Float newAverage = (numberOfTests * Float.parseFloat(data) + dels.get(i)) / (numberOfTests+1);
                    dels.set(i, newAverage);
                    i++;
                }
                myWriter = new FileWriter(
                        "C:\\Users\\koss6\\IdeaProjects\\MySQL_example\\src\\output2PC.txt");
                myWriter.write(numberOfTests+1 + "\n");
                for (Float delay : dels) {
                    myWriter.write(delay + "\n");
                }
                myWriter.close();
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            connection.close();
        } catch (SQLException | InterruptedException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }


    private static void insertRecord(ExamApplicationRecord record, Connection conn) throws SQLException {
        String query =
                "INSERT INTO application_records (name, subject, date_created, timestamp)" +
                        " values (?, ?, ?, ?)";
        PreparedStatement prep = conn.prepareStatement(query);
        prep.setString(1, record.getName());
        prep.setString(2, record.getSubject().name());
        prep.setDate(3, new Date(System.currentTimeMillis()));
        prep.setLong(4, record.getTimestamp());
        prep.execute();
    }
}
