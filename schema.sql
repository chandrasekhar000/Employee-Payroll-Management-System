CREATE DATABASE IF NOT EXISTS employee_payroll;
USE employee_payroll;
CREATE TABLE IF NOT EXISTS employees (
 id INT PRIMARY KEY AUTO_INCREMENT,
 full_name VARCHAR(120) NOT NULL,
 email VARCHAR(160) NOT NULL UNIQUE,
 department VARCHAR(100) NOT NULL,
 designation VARCHAR(100) NOT NULL,
 basic_salary DECIMAL(12,2) NOT NULL
);
CREATE TABLE IF NOT EXISTS payroll (
 id INT PRIMARY KEY AUTO_INCREMENT,
 employee_id INT NOT NULL,
 pay_month CHAR(7) NOT NULL,
 basic_salary DECIMAL(12,2) NOT NULL,
 allowance DECIMAL(12,2) NOT NULL DEFAULT 0,
 deduction DECIMAL(12,2) NOT NULL DEFAULT 0,
 net_salary DECIMAL(12,2) NOT NULL,
 generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uq_employee_month(employee_id,pay_month),
 FOREIGN KEY(employee_id) REFERENCES employees(id) ON DELETE RESTRICT
);
