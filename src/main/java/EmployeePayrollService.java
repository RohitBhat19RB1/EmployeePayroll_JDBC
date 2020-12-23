import java.time.LocalDate;
import java.util.*;

public class EmployeePayrollService {

    public void updateEmployeeSalary(String name, double salary) {
            int result = employeePayrollDBService.updateEmployeeData(name, salary);
            if (result == 0) return;

        EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
        if (employeePayrollData != null) employeePayrollData.salary = salary;

    }

    public void deleteEmployeePayroll(String name, IOService ioService) {
        if(ioService.equals(IOService.REST_IO)){
            EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
            employeePayrollList.remove(employeePayrollData);
        }
    }

    public enum IOService {CONSOLE_IO, FILE_IO, DB_IO, REST_IO}

    private List<EmployeePayrollData> employeePayrollList;
    private EmployeePayrollDBService employeePayrollDBService;

    public EmployeePayrollService()
    {
        employeePayrollDBService = EmployeePayrollDBService.getInstance();
    }

    public EmployeePayrollService(List<EmployeePayrollData>
                                          employeePayrollList) {
        this();
        this.employeePayrollList = new ArrayList<>(employeePayrollList);
    }

    public EmployeePayrollData getEmployeePayrollData(String name) {
        return this.employeePayrollList.stream()
                .filter(employeePayrollDataItem -> employeePayrollDataItem.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    public boolean checkEmployeePayrollInSyncWithDB(String name) {
        List<EmployeePayrollData> employeePayrollDataList = employeePayrollDBService.getEmployeePayrollData(name);
        return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));
    }

    public static void main(String[] args) {
        ArrayList<EmployeePayrollData> employeePayrollList = new ArrayList<>();
        EmployeePayrollService employeePayrollService = new EmployeePayrollService(employeePayrollList);
        Scanner consoleInputReader = new Scanner(System.in);
        employeePayrollService.readEmployeePayrollData(consoleInputReader);
        employeePayrollService.writeEmployeePayrollData(IOService.CONSOLE_IO);
    }

    public void writeEmployeePayrollData(IOService ioService) {
        if(ioService.equals(IOService.CONSOLE_IO))
            System.out.println("\nWriting Employee Payroll Roaster to Console\n" + employeePayrollList);
        else if (ioService.equals(IOService.FILE_IO))
            new EmployeePayrollFileIOService().writeData(employeePayrollList);
    }

    public void readEmployeePayrollData(Scanner consoleInputReader) {
        System.out.println("Enter Employee ID: ");
        int id = consoleInputReader.nextInt();
        System.out.println("Enter Employee Name: ");
        String name = consoleInputReader.next();
        System.out.println("Enter Employee Salary: ");
        double salary = consoleInputReader.nextDouble();
        employeePayrollList.add(new EmployeePayrollData(id, name, salary));
    }

    public List<EmployeePayrollData> readEmployeePayrollData(IOService ioService) {
        if(ioService.equals(IOService.DB_IO))
            this.employeePayrollList = employeePayrollDBService.readData();
        return this.employeePayrollList;
    }

    public void updateEmployeeSalary(String name, double salary, IOService ioService) {
        if(ioService.equals(IOService.DB_IO)) {
            int result = employeePayrollDBService.updateEmployeeData(name, salary);
            if (result == 0) return;
        }
        EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
        if (employeePayrollData != null) employeePayrollData.salary = salary;
    }

    public List<EmployeePayrollData> readEmployeePayrollForDateRange(IOService ioService,
                                                                     LocalDate startDate, LocalDate endDate) {
        if(ioService.equals(IOService.DB_IO))
            return employeePayrollDBService.getEmployeePayrollForDateRange(startDate, endDate);
        return null;
    }

    public Map<String, Double> readAverageSalaryByGender(IOService ioService) {
        if (ioService.equals(IOService.DB_IO))
            return employeePayrollDBService.getAverageSalaryByGender();
        return null;
    }

    public void addEmployeeTOPayrollService(String name, String gender, double salary, LocalDate startDate) {
        employeePayrollList.add(employeePayrollDBService.addEmployeeToPayroll(name, gender, salary, startDate));
    }

    public void addEmployeeToPayroll(List<EmployeePayrollData> employeePayrollDataList) {
        employeePayrollDataList.forEach(employeePayrollData -> {
                     System.out.println("Employee Being Added: " + employeePayrollData.name);
            this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.gender, employeePayrollData.salary,
                    employeePayrollData.startDate);
                   System.out.println("Employee Added: "+employeePayrollData.name);
        });

          System.out.println(this.employeePayrollList);
    }

    public void addEmployeeToPayrollWithThreads(List<EmployeePayrollData> employeePayrollDataList) {
        Map<Integer, Boolean> employeeAdditionStatus = new HashMap<Integer, Boolean>();
        employeePayrollDataList.forEach(employeePayrollData -> {
            Runnable task = () -> {
                employeeAdditionStatus.put(employeePayrollData.hashCode(), false);
                System.out.println("Employee being added: "+ Thread.currentThread().getName());
                this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.gender, employeePayrollData.salary,
                        employeePayrollData.startDate);
                employeeAdditionStatus.put(employeePayrollData.hashCode(), true);
                System.out.println("Employee Added: " + Thread.currentThread().getName());
            };
            Thread thread = new Thread(task, employeePayrollData.name);
            thread.start();

        });
        while (employeeAdditionStatus.containsValue(false)) {
            try {Thread.sleep(10);
            }catch (InterruptedException e) {}
        }
        System.out.println(employeePayrollList);
    }

    public void addEmployeeToPayroll(EmployeePayrollData employeePayrollData, IOService ioService) {
        if (ioService.equals(IOService.DB_IO))
            this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.gender, employeePayrollData.salary, employeePayrollData.startDate );
        else employeePayrollList.add(employeePayrollData);
    }


    private void addEmployeeToPayroll(String name,String gender, double salary, LocalDate startDate) {
        employeePayrollList.add(employeePayrollDBService.addEmployeeToPayroll(name, gender, salary, startDate ));
    }

    public void printData(IOService ioService) {
        if (ioService.equals(IOService.FILE_IO))
            new EmployeePayrollFileIOService().printData();
        else  System.out.println(employeePayrollList);

    }

    public long countEntries(IOService ioService) {
        if (ioService.equals(IOService.FILE_IO))
            return new EmployeePayrollFileIOService().countEntries();
        return employeePayrollList.size();
    }


}
