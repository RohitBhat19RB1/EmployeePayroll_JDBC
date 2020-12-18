import java.sql.Connection;
import java.sql.Driver;
import java.util.Enumeration;

import static java.sql.DriverManager.getConnection;
import static java.sql.DriverManager.getDrivers;

public class DBDemo {
    public static void main(String[] args) {
        String jdbcURL = "jdbc:mysql://localhost:3306/Payroll_Services?useSSL=false";
        String userName = "root";
        String password = "123456";
        Connection con;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("****  Driver loaded!  ****");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find the driver in the classpath!", e);
        }
        listDrivers();
        try {
            System.out.println("****  Connecting to database:" + jdbcURL + "   ****");
            con = getConnection(jdbcURL, userName, password);
            System.out.println("******   Connection is successful!!   *******  " + con);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void listDrivers() {
        Enumeration<Driver> driverList = getDrivers();
        while (((Enumeration<?>) driverList).hasMoreElements()) {
            Driver driverClass = (Driver) driverList.nextElement();
            System.out.println("  " + driverClass.getClass().getName());
        }
    }
}
