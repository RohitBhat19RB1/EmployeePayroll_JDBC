import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class EmployeePayrollServiceTest {

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

}
