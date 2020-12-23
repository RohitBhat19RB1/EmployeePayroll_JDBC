import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class EmployeePayrollServiceTest {
    @Test
    public void given3EmployeesWhenWrittenToFileShouldMatchEmployeeEntries() {
        EmployeePayrollData[] arrayOfEmps = {
                new EmployeePayrollData(1, "Jeff Bezos", 100000.0),
                new EmployeePayrollData(2, "Bill Gates", 200000.0),
                new EmployeePayrollData(3, "Mark Zuckerberg", 300000.0)
        };
        EmployeePayrollService employeePayrollService;
        employeePayrollService = new EmployeePayrollService(asList(arrayOfEmps));
        employeePayrollService.writeEmployeePayrollData(EmployeePayrollService.IOService.FILE_IO);
        employeePayrollService.printData(EmployeePayrollService.IOService.FILE_IO);
        long entries = employeePayrollService.countEntries(EmployeePayrollService.IOService.FILE_IO);
        Assert.assertEquals(3, entries);
    }

    @Test
    public void givenEmployeePayrollInDB_WhenRetrieved_ShouldMatchEmployeeCount() {
        EmployeePayrollService employeePayrollService = new EmployeePayrollService();
        List<EmployeePayrollData> employeePayrollData = employeePayrollService
                .readEmployeePayrollData(EmployeePayrollService.IOService.DB_IO);
        Assert.assertEquals(3, employeePayrollData.size());
    }

    @Test
    public void givenNewSalaryForEmployee_WhenUpdated_ShouldSyncWithDB() {
        EmployeePayrollService employeePayrollService = new EmployeePayrollService();
        List<EmployeePayrollData> employeePayrollData = employeePayrollService
                .readEmployeePayrollData(EmployeePayrollService.IOService.DB_IO);
        employeePayrollService.updateEmployeeSalary("Terisa", 2000000.00);
        boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Terisa");
        Assert.assertTrue(result);
    }


    @Test
    public void givenDateRange_WhenRetrieved_ShouldMatchEmployeeCount() {
        EmployeePayrollService employeePayrollService = new EmployeePayrollService();
        employeePayrollService.readEmployeePayrollData(EmployeePayrollService.IOService.DB_IO);
        LocalDate startDate = LocalDate.of(2018, 01, 01);
        LocalDate endDate = LocalDate.now();
        List<EmployeePayrollData> employeePayrollData =
                employeePayrollService.readEmployeePayrollForDateRange(EmployeePayrollService
                        .IOService.DB_IO, startDate, endDate);
        Assert.assertEquals(3, employeePayrollData.size());
    }

    @Test
    public void givenPayrollData_WhenAverageSalaryRetrievedByGender_ShouldReturnProperValue() {
        EmployeePayrollService employeePayrollService = new EmployeePayrollService();
        employeePayrollService.readEmployeePayrollData(EmployeePayrollService.IOService.DB_IO);
        Map<String, Double> averageSalaryByGender = employeePayrollService
                .readAverageSalaryByGender(EmployeePayrollService.IOService.DB_IO);
        Assert.assertTrue(averageSalaryByGender.get("M").equals(2000000.00) &&
                averageSalaryByGender.get("F").equals(2000000.00));
    }

    @Test
    public void givenNewEmployee_WhenAdded_ShouldSyncWithDB() {
        EmployeePayrollService employeePayrollService = new EmployeePayrollService();
        employeePayrollService.readEmployeePayrollData(EmployeePayrollService.IOService.DB_IO);
        employeePayrollService.addEmployeeTOPayrollService("Mark","M", 5000000.00, LocalDate.now());
        boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Mark");
        Assert.assertTrue(result);
    }

    @Test
    public void given6Employees_WhenAddedToDB_ShouldMatchEmployeeEntries() {
        EmployeePayrollData[] arrayOfEmps = {
                new EmployeePayrollData(0, "Jeff Bezos","M",100000.0,  LocalDate.now()),
                new EmployeePayrollData(0, "Bill Gates", "M" ,200000.0, LocalDate.now()),
                new EmployeePayrollData(0, "Mark Zuckerberg","M",300000.0, LocalDate.now()),
                new EmployeePayrollData(0, "Sunder","M", 600000.0, LocalDate.now()),
                new EmployeePayrollData(0, "Mukesh","M",100000.0, LocalDate.now()),
                new EmployeePayrollData(0, "Anil","M",200000.0, LocalDate.now())
        };
        EmployeePayrollService employeePayrollService =new EmployeePayrollService();
        employeePayrollService.readEmployeePayrollData(EmployeePayrollService.IOService.DB_IO);
        Instant start = Instant.now();
        employeePayrollService.addEmployeeToPayroll(Arrays.asList(arrayOfEmps));
        Instant end = Instant.now();
        System.out.println("Duration without Thread: "+ Duration.between(start, end));
        Instant threadStart = Instant.now();
        employeePayrollService.addEmployeeToPayrollWithThreads(Arrays.asList(arrayOfEmps));
        Instant threadEnd = Instant.now();
        System.out.println("Duration with Thread: " +Duration.between(threadStart, threadEnd));
        Assert.assertEquals(15, employeePayrollService.countEntries(EmployeePayrollService.IOService.DB_IO));

    }

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    public EmployeePayrollData[] getEmployeeList() {
        Response response = RestAssured.get("/employee_payroll");
        System.out.println("Employee Payroll entries in jsonServer:\n" + response.asString());
        EmployeePayrollData[] arrayOfEmps = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
        return arrayOfEmps;
    }

    public Response addEmployeeToJsonServer(EmployeePayrollData employeePayrollData) {
        String empJson = new Gson().toJson(employeePayrollData);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(empJson);
        return request.post("/employee_payroll");
    }

    @Test
    public void givenEmployeeDataInJsonServer_WhenRetrieved_ShouldMatchTheCount() {
        EmployeePayrollData[] arrayOfEmps = getEmployeeList();
        EmployeePayrollService employeePayrollService;
        employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
        long entries = employeePayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
        Assert.assertEquals(2, entries);
    }

    @Test
    public void givenNewEmployee_WhenAdded_ShouldMatch201ResponseAndCount() {
        EmployeePayrollService employeePayrollService;
        EmployeePayrollData[] arrayOfEmps = getEmployeeList();
        employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));

        EmployeePayrollData employeePayrollData = new EmployeePayrollData(0, "Mark Zuckerberg", "M", 300000, LocalDate.now());
        Response response = addEmployeeToJsonServer(employeePayrollData);
        int statusCode = response.getStatusCode();
        Assert.assertEquals(201, statusCode);

        employeePayrollData = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
        employeePayrollService.addEmployeeToPayroll(employeePayrollData, EmployeePayrollService.IOService.REST_IO);
        long entries = employeePayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
        Assert.assertEquals(3, entries);

    }

    @Test
    public void givenListOfNewEmployees_WhenAdded_ShouldMatch201ResponseAndCount() {
        EmployeePayrollService employeePayrollService;
        EmployeePayrollData[] arrayOfEmps = getEmployeeList();
        employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));

        EmployeePayrollData[] arrayOfEmpPayrolls= {
                new EmployeePayrollData(0, "Sunder","M", 600000.0, LocalDate.now()),
                new EmployeePayrollData(0, "Mukesh","M",1000000.0, LocalDate.now()),
                new EmployeePayrollData(0, "Anil","M",200000.0, LocalDate.now())
        };
        for (EmployeePayrollData employeePayrollData : arrayOfEmpPayrolls) {
            Response response = addEmployeeToJsonServer(employeePayrollData);
            int statusCode = response.getStatusCode();
            Assert.assertEquals(201, statusCode);

            employeePayrollData = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
            employeePayrollService.addEmployeeToPayroll(employeePayrollData, EmployeePayrollService.IOService.REST_IO);
        }

        long entries = employeePayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
        Assert.assertEquals(6, entries);
    }

    @Test
    public void givenNewSalaryForEmployee_WhenUpdated_ShouldMatch200Response() {
        EmployeePayrollService employeePayrollService;
        EmployeePayrollData[] arrayOfEmps = getEmployeeList();
        employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));

        employeePayrollService.updateEmployeeSalary("Anil", 3000000.00, EmployeePayrollService.IOService.REST_IO);
        EmployeePayrollData employeePayrollData = employeePayrollService.getEmployeePayrollData("Anil");

        String empJson = new Gson().toJson(employeePayrollData);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(empJson);
        Response response = request.put("/employee_payroll/" + employeePayrollData.id);
        int statusCode = response.getStatusCode();
        Assert.assertEquals(200, statusCode);
    }

    @Test
    public void givenEmployeeToDelete_WhenDeleted_ShouldMatch200ResponseAndCount(){
        EmployeePayrollService employeePayrollService;
        EmployeePayrollData[] arrayOfEmps = getEmployeeList();
        employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));

        EmployeePayrollData employeePayrollData = employeePayrollService.getEmployeePayrollData("Anil");
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        Response response = request.delete("/employee_payroll/" + employeePayrollData.id);
        int statusCode = response.getStatusCode();
        Assert.assertEquals(200, statusCode);

        employeePayrollService.deleteEmployeePayroll(employeePayrollData.name, EmployeePayrollService.IOService.REST_IO);
        long entries = employeePayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
        Assert.assertEquals(5, entries);

    }

}
