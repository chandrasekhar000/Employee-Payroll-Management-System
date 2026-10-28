# Employee Payroll Management System

Java Swing desktop application using MySQL + JDBC.

## Features
- Employee CRUD (add, update, delete, list and search)
- Monthly payroll and payslip generation
- Net salary = basic salary + allowance - deduction
- Duplicate employee/month payroll updates the existing row
- Payroll history table and CSV export

## Requirements
- JDK 17+
- MySQL Server
- Maven 3.8+

## Setup
1. Start MySQL.
2. Run `schema.sql` in MySQL Workbench or MySQL CLI. This creates the `employee_payroll` database and tables.
3. Set your database password using environment variables if needed:
   - Windows PowerShell: `$env:PAYROLL_DB_USER="root"` and `$env:PAYROLL_DB_PASSWORD="111"`
   - Optional URL override: `PAYROLL_DB_URL`
4. Open this folder as a Maven project in IntelliJ IDEA and wait for dependencies to download.
5. Run `com.sonu.payroll.Main`.

From terminal:
```bash
mvn clean package
mvn dependency:copy-dependencies
```
Windows:
```powershell
java -cp "target/classes;target/dependency/*" com.sonu.payroll.Main
```
macOS/Linux:
```bash
java -cp "target/classes:target/dependency/*" com.sonu.payroll.Main
```

## Use
1. In Employees, add an employee.
2. Select that employee from the table.
3. Open Payroll & Payslips.
4. Enter month as `YYYY-MM`, allowance and deduction.
5. Generate the payslip. Net salary is calculated automatically.
6. Export payroll records to CSV if required.

Student-project note: This is a demo system; it does not implement statutory tax, PF/ESI, attendance, authentication or production payroll compliance. Avoid real sensitive employee data.
