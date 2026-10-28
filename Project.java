package com.payroll;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * PayFlow HR - Employee Payroll Management System
 *
 * Java Swing desktop application with local serialized storage.
 *
 * Default login:
 * Username: admin
 * Password: admin123
 *
 * This is an educational payroll-management prototype.
 * It does not calculate statutory tax, PF, ESI, or other
 * country-specific payroll contributions.
 */
public class Main extends JFrame {

    private static final String APP_TITLE =
            "PayFlow HR | Employee Payroll Management";

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path DATA_FILE =
            DATA_DIR.resolve("payroll-data.ser");

    private static final NumberFormat MONEY =
            NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+91[\\s-]?)?[0]?[6-9]\\d{9}$"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-z][A-Za-z .'-]{1,59}$"
    );

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "^\\d+(\\.\\d{1,2})?$"
    );

    private static final Pattern INTEGER_PATTERN = Pattern.compile(
            "^-?\\d+$"
    );

    private static final Color NAVY = new Color(9, 13, 24);
    private static final Color BG = new Color(13, 18, 30);
    private static final Color SURFACE = new Color(24, 31, 47);
    private static final Color SURFACE_ALT = new Color(31, 40, 59);
    private static final Color BORDER = new Color(48, 59, 81);
    private static final Color TEXT = new Color(242, 245, 252);
    private static final Color MUTED = new Color(180, 191, 211);
    private static final Color INDIGO = new Color(99, 102, 241);
    private static final Color GREEN = new Color(16, 130, 95);
    private static final Color ORANGE = new Color(180, 100, 40);
    private static final Color RED = new Color(185, 55, 65);
    private static final Color GRAY = new Color(92, 105, 127);

    private AppData data;

    private final JTabbedPane tabs = new JTabbedPane();

    private DefaultTableModel employeeModel;
    private DefaultTableModel departmentModel;
    private DefaultTableModel salaryModel;
    private DefaultTableModel payrollModel;
    private DefaultTableModel auditModel;

    private JTable employeeTable;
    private JTable departmentTable;
    private JTable salaryTable;
    private JTable payrollTable;
    private JTable auditTable;

    private JTextField employeeSearch;

    private JLabel dashboardEmployees;
    private JLabel dashboardPayroll;
    private JLabel dashboardApproved;
    private JLabel dashboardPending;

    public Main() {
        data = loadData();
        seedDefaultsIfNeeded();
        showLogin();
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private void showLogin() {
        JFrame login = new JFrame("PayFlow HR | Sign in");
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        login.setSize(520, 430);
        login.setLocationRelativeTo(null);
        login.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(32, 38, 32, 38));

        JLabel title = new JLabel("PayFlow HR", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Employee Payroll Management",
                SwingConstants.CENTER
        );
        subtitle.setForeground(MUTED);

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 5));
        top.setOpaque(false);
        top.add(title);
        top.add(subtitle);

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        styleTextField(username);
        styleTextField(password);

        JLabel userLabel = new JLabel("Username");
        JLabel passLabel = new JLabel("Password");
        userLabel.setForeground(TEXT);
        passLabel.setForeground(TEXT);

        JPanel form = new JPanel(new GridLayout(2, 2, 12, 18));
        form.setOpaque(false);
        form.add(userLabel);
        form.add(username);
        form.add(passLabel);
        form.add(password);

        JButton signIn = button("Sign in", INDIGO);

        JLabel hint = new JLabel(
                "WELCOME TO PAYROLL SYSTEM ! ",
                SwingConstants.CENTER
        );
        hint.setFont(new Font("SansSerif", Font.PLAIN, 14));
        hint.setForeground(MUTED);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 8));
        bottom.setOpaque(false);
        bottom.add(signIn);
        bottom.add(hint);

        root.add(top, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        Runnable loginAction = () -> {
            String enteredUsername = username.getText().trim();
            String enteredPassword =
                    new String(password.getPassword());

            try {
                requireNonBlank(
                        enteredUsername,
                        "Username",
                        50
                );

                requireNonBlank(
                        enteredPassword,
                        "Password",
                        100
                );

                User user = findUser(enteredUsername);

                if (user == null || !verifyPassword(
                        enteredPassword,
                        user.passwordSalt,
                        user.passwordHash
                )) {
                    throw new IllegalArgumentException(
                            "Incorrect username or password."
                    );
                }

                login.dispose();
                buildMainWindow();

                log(
                        "LOGIN",
                        "User '" + user.username + "' signed in"
                );

                saveData();

            } catch (IllegalArgumentException ex) {
                error(login, ex.getMessage());
            }
        };

        signIn.addActionListener(e -> loginAction.run());
        password.addActionListener(e -> loginAction.run());

        login.setContentPane(root);
        login.setVisible(true);
    }

    private User findUser(String username) {
        return data.users.stream()
                .filter(
                        u -> u.username.equalsIgnoreCase(username)
                )
                .findFirst()
                .orElse(null);
    }

    // =========================================================
    // MAIN WINDOW
    // =========================================================

    private void buildMainWindow() {
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 830);
        setMinimumSize(new Dimension(1200, 760));
        setLocationRelativeTo(null);

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainArea(), BorderLayout.CENTER);

        refreshAll();
        setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(255, 0));
        sidebar.setBackground(NAVY);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(28, 16, 20, 16));

        JLabel logo = new JLabel("  PayFlow HR");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setBorder(new EmptyBorder(0, 5, 28, 0));
        sidebar.add(logo);

        JLabel workspace = new JLabel("WORKSPACE");
        workspace.setForeground(MUTED);
        workspace.setFont(new Font("SansSerif", Font.BOLD, 10));
        workspace.setBorder(new EmptyBorder(0, 12, 10, 0));
        sidebar.add(workspace);

        String[] names = {
                "Dashboard",
                "Employees",
                "Departments",
                "Salary Structures",
                "Payroll",
                "Payslips",
                "Reports",
                "Audit Log"
        };

        String[] icons = {
                "▦", "♙", "▤", "₹",
                "▣", "▧", "▥", "◷"
        };

        for (int i = 0; i < names.length; i++) {
            final int index = i;

            JButton b = new JButton(icons[i] + "   " + names[i]);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(220, 46));
            b.setPreferredSize(new Dimension(220, 46));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFont(new Font("SansSerif", Font.PLAIN, 15));
            b.setForeground(new Color(225, 230, 241));
            b.setBackground(NAVY);
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setContentAreaFilled(true);
            b.setBorder(new EmptyBorder(0, 13, 0, 5));
            b.addActionListener(e -> tabs.setSelectedIndex(index));

            sidebar.add(b);
            sidebar.add(Box.createVerticalStrut(7));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel admin = new JLabel("  ● Administrator");
        admin.setForeground(Color.WHITE);
        admin.setBorder(new EmptyBorder(15, 5, 8, 0));
        sidebar.add(admin);

        JButton logout = new JButton("↪  Sign out");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setForeground(Color.WHITE);
        logout.setBackground(NAVY);
        logout.setOpaque(true);
        logout.setFocusPainted(false);
        logout.setBorder(new EmptyBorder(10, 13, 10, 5));
        logout.addActionListener(e -> {
            dispose();
            showLogin();
        });

        sidebar.add(logout);

        return sidebar;
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout(0, 15));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Workforce overview");
        heading.setFont(new Font("SansSerif", Font.BOLD, 30));
        heading.setForeground(TEXT);

        JLabel sub = new JLabel(
                "Manage employees, monthly payroll and salary records."
        );
        sub.setForeground(MUTED);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 15));

        titles.add(heading);
        titles.add(Box.createVerticalStrut(6));
        titles.add(sub);

        JButton addEmployee = button("+  Add Employee", INDIGO);
        addEmployee.addActionListener(e -> addEmployeeDialog());

        header.add(titles, BorderLayout.WEST);
        header.add(addEmployee, BorderLayout.EAST);

        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.setBackground(SURFACE);
        tabs.setForeground(TEXT);
        tabs.setOpaque(true);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        buildDashboardTab();
        buildEmployeesTab();
        buildDepartmentsTab();
        buildSalaryTab();
        buildPayrollTab();
        buildPayslipsTab();
        buildReportsTab();
        buildAuditTab();

        main.add(header, BorderLayout.NORTH);
        main.add(tabs, BorderLayout.CENTER);

        return main;
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private void buildDashboardTab() {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(18, 2, 2, 2));

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);

        dashboardEmployees = new JLabel("0");
        dashboardPayroll = new JLabel(money(BigDecimal.ZERO));
        dashboardApproved = new JLabel("0");
        dashboardPending = new JLabel("0");

        cards.add(statCard(
                "Active Employees",
                dashboardEmployees,
                "Employees available for payroll",
                "♙",
                INDIGO
        ));

        cards.add(statCard(
                "Current Net Payroll",
                dashboardPayroll,
                "Current month payroll total",
                "₹",
                new Color(14, 165, 233)
        ));

        cards.add(statCard(
                "Approved Records",
                dashboardApproved,
                "Current month approved payroll",
                "✓",
                GREEN
        ));

        cards.add(statCard(
                "Pending Records",
                dashboardPending,
                "Current month pending payroll",
                "◷",
                ORANGE
        ));

        JPanel info = titledPanel("Welcome to PayFlow HR");

JTextArea text = new JTextArea(
        "Welcome to PayFlow HR!\n\n"
      + "Your workplace, organized smarter.\n\n"
      + "PayFlow HR is designed to make employee management "
      + "and payroll processing simpler, faster, and more organized. "
      + "Manage your workforce, maintain salary records, "
      + "and keep track of payroll — all in one place.\n\n"
      + "What you can do:\n\n"
      + "• Manage employee profiles and departments.\n"
      + "• Configure salary structures and allowances.\n"
      + "• Generate monthly payroll records.\n"
      + "• Review payroll approvals and payment status.\n"
      + "• View payslips and export useful reports.\n"
      + "• Track important activities through the audit log.\n\n"
      + "Your workflow, made simple:\n"
      + "Add Employees  →  Configure Salaries  →  Process Payroll\n"
      + "→  Approve Payroll  →  Record Payment  →  Generate Payslips\n\n"
      + "\"Better organization. Clearer records. Smoother payroll.\"\n\n"
      + "Get started by exploring the tabs and setting up "
      + "your departments and employee records.\n\n"
      + "Note: PayFlow HR stores data locally in "
      + "data/payroll-data.ser. Back up your data regularly."
);


        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(new Font("SansSerif", Font.PLAIN, 14));
        text.setForeground(TEXT);
        text.setBackground(SURFACE);
        text.setCaretColor(TEXT);
        text.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(text);
        styleScrollPane(scroll);

        info.add(scroll, BorderLayout.CENTER);

        page.add(cards, BorderLayout.NORTH);
        page.add(info, BorderLayout.CENTER);

        tabs.addTab("Dashboard", page);
    }

    private JPanel statCard(
            String title,
            JLabel value,
            String note,
            String symbol,
            Color accent
    ) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setForeground(MUTED);

        JLabel icon = new JLabel(symbol, SwingConstants.CENTER);
        icon.setForeground(accent);
        icon.setFont(new Font("SansSerif", Font.BOLD, 20));
        icon.setPreferredSize(new Dimension(40, 40));
        icon.setOpaque(true);
        icon.setBackground(SURFACE_ALT);

        top.add(label, BorderLayout.CENTER);
        top.add(icon, BorderLayout.EAST);

        value.setFont(new Font("SansSerif", Font.BOLD, 28));
        value.setForeground(TEXT);

        JLabel noteLabel = new JLabel(note);
        noteLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        noteLabel.setForeground(MUTED);

        JPanel lower = new JPanel();
        lower.setOpaque(false);
        lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));
        lower.add(value);
        lower.add(Box.createVerticalStrut(6));
        lower.add(noteLabel);

        card.add(top, BorderLayout.NORTH);
        card.add(lower, BorderLayout.CENTER);

        return card;
    }

    // =========================================================
    // EMPLOYEES
    // =========================================================

    private void buildEmployeesTab() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(14, 2, 2, 2));

        JPanel tools = new JPanel(new BorderLayout(10, 0));
        tools.setOpaque(false);

        employeeSearch = new JTextField();
        styleTextField(employeeSearch);
        employeeSearch.setPreferredSize(new Dimension(360, 42));
        employeeSearch.setToolTipText(
                "Search by ID, name, department, designation or email"
        );
        employeeSearch.addActionListener(e -> filterEmployees());

        JButton search = button("Search", INDIGO);
        search.addActionListener(e -> filterEmployees());

        JButton reset = button("Show All", GRAY);
        reset.addActionListener(e -> {
            employeeSearch.setText("");
            refreshEmployeeTable();
        });

        JButton add = button("+ Add Employee", INDIGO);
        add.addActionListener(e -> addEmployeeDialog());

        JPanel left = new JPanel(new BorderLayout(8, 0));
        left.setOpaque(false);
        left.add(employeeSearch, BorderLayout.CENTER);
        left.add(search, BorderLayout.EAST);

        JPanel right = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        right.setOpaque(false);
        right.add(reset);
        right.add(add);

        tools.add(left, BorderLayout.CENTER);
        tools.add(right, BorderLayout.EAST);

        employeeModel = model(
                "ID",
                "Name",
                "Department",
                "Designation",
                "Email",
                "Phone",
                "Net Salary",
                "Status"
        );

        employeeTable = table(employeeModel);

        JPanel box = titledPanel("Employee Directory");
        box.add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        actions.setOpaque(false);

        JButton edit = button("Edit Selected", GRAY);
        edit.addActionListener(e -> editEmployee());

        JButton toggle = button("Activate / Deactivate", ORANGE);
        toggle.addActionListener(e -> toggleEmployee());

        JButton delete = button("Delete Selected", RED);
        delete.addActionListener(e -> deleteEmployee());

        actions.add(edit);
        actions.add(toggle);
        actions.add(delete);

        page.add(tools, BorderLayout.NORTH);
        page.add(box, BorderLayout.CENTER);
        page.add(actions, BorderLayout.SOUTH);

        tabs.addTab("Employees", page);
    }

    private void addEmployeeDialog() {
        JTextField name = new JTextField();
        JTextField designation = new JTextField();
        JTextField email = new JTextField();
        JTextField phone = new JTextField();

        styleTextField(name);
        styleTextField(designation);
        styleTextField(email);
        styleTextField(phone);

        JComboBox<String> department = new JComboBox<>();

        for (Department d : data.departments) {
            department.addItem(d.name);
        }

        styleComboBox(department);

        Object[] fields = {
                "Full name *", name,
                "Department *", department,
                "Designation *", designation,
                "Email *", email,
                "Phone *", phone
        };

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    fields,
                    "Add Employee",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            try {
                String employeeName =
                        requireValidName(name.getText(), "Full name");

                String employeeDesignation = requireNonBlank(
                        designation.getText(),
                        "Designation",
                        60
                );

                String employeeEmail = requireValidEmail(
                        email.getText(),
                        true
                );

                String employeePhone = requireValidPhone(
                        phone.getText(),
                        true
                );

                String selectedDepartment =
                        String.valueOf(department.getSelectedItem());

                Department dept = data.departments.stream()
                        .filter(
                                d -> d.name.equalsIgnoreCase(
                                        selectedDepartment
                                )
                        )
                        .findFirst()
                        .orElse(null);

                if (dept == null) {
                    throw new IllegalArgumentException(
                            "Selected department no longer exists."
                    );
                }

                boolean duplicateEmail = data.employees.stream()
                        .anyMatch(
                                e -> e.email.equalsIgnoreCase(
                                        employeeEmail
                                )
                        );

                if (duplicateEmail) {
                    throw new IllegalArgumentException(
                            "An employee with this email already exists."
                    );
                }

                boolean duplicatePhone = data.employees.stream()
                        .anyMatch(
                                e -> e.phone.equals(employeePhone)
                        );

                if (duplicatePhone) {
                    throw new IllegalArgumentException(
                            "An employee with this phone number already exists."
                    );
                }

                int id = nextEmployeeId();

                Employee employee = new Employee(
                        id,
                        employeeName,
                        dept.id,
                        employeeDesignation,
                        employeeEmail,
                        employeePhone
                );

                data.employees.add(employee);
                data.salaries.put(
                        id,
                        new SalaryStructure(
                                id,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO
                        )
                );

                log(
                        "EMPLOYEE_ADDED",
                        "Added employee " + id + " - " + employee.name
                );

                saveData();
                refreshAll();

                information(
                        "Employee added successfully.\n"
                                + "Configure salary from Salary Structures."
                );

                return;

            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        }
    }

    private void editEmployee() {
        Employee employee = selectedEmployee();

        if (employee == null) {
            return;
        }

        JTextField name = new JTextField(employee.name);
        JTextField designation = new JTextField(employee.designation);
        JTextField email = new JTextField(employee.email);
        JTextField phone = new JTextField(employee.phone);

        styleTextField(name);
        styleTextField(designation);
        styleTextField(email);
        styleTextField(phone);

        JComboBox<String> department = new JComboBox<>();

        for (Department d : data.departments) {
            department.addItem(d.name);
        }

        Department current = findDepartment(employee.departmentId);

        if (current != null) {
            department.setSelectedItem(current.name);
        }

        styleComboBox(department);

        Object[] fields = {
                "Employee ID", String.valueOf(employee.id),
                "Full name *", name,
                "Department *", department,
                "Designation *", designation,
                "Email *", email,
                "Phone *", phone
        };

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    fields,
                    "Edit Employee",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            try {
                String newName =
                        requireValidName(name.getText(), "Full name");

                String newDesignation = requireNonBlank(
                        designation.getText(),
                        "Designation",
                        60
                );

                String newEmail = requireValidEmail(
                        email.getText(),
                        true
                );

                String newPhone = requireValidPhone(
                        phone.getText(),
                        true
                );

                String selectedDepartment =
                        String.valueOf(department.getSelectedItem());

                Department dept = data.departments.stream()
                        .filter(
                                d -> d.name.equalsIgnoreCase(
                                        selectedDepartment
                                )
                        )
                        .findFirst()
                        .orElse(null);

                if (dept == null) {
                    throw new IllegalArgumentException(
                            "Selected department no longer exists."
                    );
                }

                boolean duplicateEmail = data.employees.stream()
                        .anyMatch(
                                e -> e.id != employee.id
                                        && e.email.equalsIgnoreCase(newEmail)
                        );

                if (duplicateEmail) {
                    throw new IllegalArgumentException(
                            "Another employee already uses this email."
                    );
                }

                boolean duplicatePhone = data.employees.stream()
                        .anyMatch(
                                e -> e.id != employee.id
                                        && e.phone.equals(newPhone)
                        );

                if (duplicatePhone) {
                    throw new IllegalArgumentException(
                            "Another employee already uses this phone number."
                    );
                }

                employee.name = newName;
                employee.departmentId = dept.id;
                employee.designation = newDesignation;
                employee.email = newEmail;
                employee.phone = newPhone;

                log(
                        "EMPLOYEE_EDITED",
                        "Updated employee " + employee.id
                );

                saveData();
                refreshAll();

                return;

            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        }
    }

    private void toggleEmployee() {
        Employee employee = selectedEmployee();

        if (employee == null) {
            return;
        }

        employee.active = !employee.active;

        log(
                employee.active
                        ? "EMPLOYEE_ACTIVATED"
                        : "EMPLOYEE_DEACTIVATED",
                "Employee " + employee.id + " - " + employee.name
        );

        saveData();
        refreshAll();
    }

    private void deleteEmployee() {
        Employee employee = selectedEmployee();

        if (employee == null) {
            return;
        }

        boolean hasPayroll = data.payroll.stream()
                .anyMatch(p -> p.employeeId == employee.id);

        String message = hasPayroll
                ? "Employee has payroll history.\n"
                + "Deleting will keep payroll records but remove the employee.\n\n"
                + "Permanently delete " + employee.name + "?"
                : "Permanently delete " + employee.name + "?";

        int answer = JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        data.employees.remove(employee);
        data.salaries.remove(employee.id);

        log(
                "EMPLOYEE_DELETED",
                "Deleted employee " + employee.id + " - " + employee.name
        );

        saveData();
        refreshAll();
    }

    private Employee selectedEmployee() {
        int row = employeeTable.getSelectedRow();

        if (row < 0) {
            error("Select an employee row first.");
            return null;
        }

        int modelRow = employeeTable.convertRowIndexToModel(row);

        try {
            int id = requireValidInteger(
                    String.valueOf(
                            employeeModel.getValueAt(modelRow, 0)
                    ),
                    "Employee ID"
            );

            return data.employees.stream()
                    .filter(e -> e.id == id)
                    .findFirst()
                    .orElse(null);

        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
            return null;
        }
    }

    private void filterEmployees() {
        String query = employeeSearch.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        employeeModel.setRowCount(0);

        for (Employee e : data.employees) {
            Department d = findDepartment(e.departmentId);

            String searchable = (
                    e.id + " "
                            + e.name + " "
                            + (d == null ? "" : d.name) + " "
                            + e.designation + " "
                            + e.email + " "
                            + e.phone
            ).toLowerCase(Locale.ROOT);

            if (searchable.contains(query)) {
                addEmployeeRow(e);
            }
        }
    }

    private void refreshEmployeeTable() {
        if (employeeModel == null) {
            return;
        }

        employeeModel.setRowCount(0);

        for (Employee e : data.employees) {
            addEmployeeRow(e);
        }
    }

    private void addEmployeeRow(Employee employee) {
        SalaryStructure salary = data.salaries.getOrDefault(
                employee.id,
                new SalaryStructure(
                        employee.id,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );

        Department department =
                findDepartment(employee.departmentId);

        employeeModel.addRow(new Object[]{
                employee.id,
                employee.name,
                department == null ? "Unknown" : department.name,
                employee.designation,
                employee.email,
                employee.phone,
                money(salary.net()),
                employee.active ? "Active" : "Inactive"
        });
    }

    private int nextEmployeeId() {
        return data.employees.stream()
                .mapToInt(e -> e.id)
                .max()
                .orElse(1000) + 1;
    }

    // =========================================================
    // DEPARTMENTS
    // =========================================================

    private void buildDepartmentsTab() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(14, 2, 2, 2));

        departmentModel = model(
                "Department ID",
                "Department Name",
                "Description",
                "Employees"
        );

        departmentTable = table(departmentModel);

        JPanel box = titledPanel("Department Directory");
        box.add(new JScrollPane(departmentTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        actions.setOpaque(false);

        JButton add = button("+ Add Department", INDIGO);
        add.addActionListener(e -> addDepartment());

        JButton delete = button("Delete Selected", RED);
        delete.addActionListener(e -> deleteDepartment());

        actions.add(add);
        actions.add(delete);

        page.add(box, BorderLayout.CENTER);
        page.add(actions, BorderLayout.SOUTH);

        tabs.addTab("Departments", page);
    }

    private void addDepartment() {
        JTextField name = new JTextField();
        JTextField description = new JTextField();

        styleTextField(name);
        styleTextField(description);

        Object[] fields = {
                "Department name *", name,
                "Description", description
        };

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    fields,
                    "Add Department",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            try {
                String departmentName =
                        requireValidName(name.getText(), "Department name");

                String departmentDescription = optionalText(
                        description.getText(),
                        150
                );

                boolean exists = data.departments.stream()
                        .anyMatch(
                                d -> d.name.equalsIgnoreCase(departmentName)
                        );

                if (exists) {
                    throw new IllegalArgumentException(
                            "That department already exists."
                    );
                }

                int id = data.departments.stream()
                        .mapToInt(d -> d.id)
                        .max()
                        .orElse(0) + 1;

                data.departments.add(
                        new Department(
                                id,
                                departmentName,
                                departmentDescription
                        )
                );

                log("DEPARTMENT_ADDED", departmentName);
                saveData();
                refreshAll();

                return;

            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        }
    }

    private void deleteDepartment() {
        int row = departmentTable.getSelectedRow();

        if (row < 0) {
            error("Select a department first.");
            return;
        }

        int modelRow = departmentTable.convertRowIndexToModel(row);

        try {
            int id = requireValidInteger(
                    String.valueOf(
                            departmentModel.getValueAt(modelRow, 0)
                    ),
                    "Department ID"
            );

            Department department = data.departments.stream()
                    .filter(d -> d.id == id)
                    .findFirst()
                    .orElse(null);

            if (department == null) {
                return;
            }

            boolean inUse = data.employees.stream()
                    .anyMatch(
                            e -> e.departmentId == department.id
                    );

            if (inUse) {
                throw new IllegalArgumentException(
                        "This department is assigned to one or more employees. "
                                + "Reassign those employees first."
                );
            }

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Delete department '" + department.name + "'?",
                    "Confirm deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (answer == JOptionPane.YES_OPTION) {
                data.departments.remove(department);

                log(
                        "DEPARTMENT_DELETED",
                        department.name
                );

                saveData();
                refreshAll();
            }

        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private Department findDepartment(int departmentId) {
        return data.departments.stream()
                .filter(d -> d.id == departmentId)
                .findFirst()
                .orElse(null);
    }

    // =========================================================
    // SALARY STRUCTURES
    // =========================================================

    private void buildSalaryTab() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(14, 2, 2, 2));

        salaryModel = model(
                "Employee ID",
                "Employee Name",
                "Basic",
                "Allowances",
                "Bonus",
                "Deductions",
                "Gross",
                "Net Salary"
        );

        salaryTable = table(salaryModel);

        JPanel box = titledPanel("Salary Structures");
        box.add(new JScrollPane(salaryTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        actions.setOpaque(false);

        JButton configure = button(
                "Configure Selected Salary",
                INDIGO
        );
        configure.addActionListener(e -> configureSalary());

        actions.add(configure);

        page.add(box, BorderLayout.CENTER);
        page.add(actions, BorderLayout.SOUTH);

        tabs.addTab("Salary Structures", page);
    }

    private void configureSalary() {
        int row = salaryTable.getSelectedRow();

        if (row < 0) {
            error("Select an employee salary row first.");
            return;
        }

        int modelRow = salaryTable.convertRowIndexToModel(row);

        try {
            int employeeId = requireValidInteger(
                    String.valueOf(
                            salaryModel.getValueAt(modelRow, 0)
                    ),
                    "Employee ID"
            );

            Employee employee = data.employees.stream()
                    .filter(e -> e.id == employeeId)
                    .findFirst()
                    .orElse(null);

            if (employee == null) {
                return;
            }

            SalaryStructure salary = data.salaries.computeIfAbsent(
                    employeeId,
                    k -> new SalaryStructure(
                            employeeId,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO
                    )
            );

            JTextField basic = new JTextField(
                    salary.basic.toPlainString()
            );

            JTextField allowances = new JTextField(
                    salary.allowances.toPlainString()
            );

            JTextField bonus = new JTextField(
                    salary.bonus.toPlainString()
            );

            JTextField deductions = new JTextField(
                    salary.deductions.toPlainString()
            );

            styleTextField(basic);
            styleTextField(allowances);
            styleTextField(bonus);
            styleTextField(deductions);

            Object[] fields = {
                    "Employee", employee.name,
                    "Basic salary *", basic,
                    "Allowances *", allowances,
                    "Bonus *", bonus,
                    "Deductions *", deductions
            };

            while (true) {
                int result = JOptionPane.showConfirmDialog(
                        this,
                        fields,
                        "Configure Salary",
                        JOptionPane.OK_CANCEL_OPTION
                );

                if (result != JOptionPane.OK_OPTION) {
                    return;
                }

                try {
                    BigDecimal newBasic = requireAmount(
                            basic.getText(),
                            "Basic salary"
                    );

                    BigDecimal newAllowances = requireAmount(
                            allowances.getText(),
                            "Allowances"
                    );

                    BigDecimal newBonus = requireAmount(
                            bonus.getText(),
                            "Bonus"
                    );

                    BigDecimal newDeductions = requireAmount(
                            deductions.getText(),
                            "Deductions"
                    );

                    BigDecimal gross = newBasic
                            .add(newAllowances)
                            .add(newBonus);

                    if (gross.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException(
                                "Gross salary must be greater than zero."
                        );
                    }

                    if (newDeductions.compareTo(gross) > 0) {
                        throw new IllegalArgumentException(
                                "Deductions cannot exceed gross salary."
                        );
                    }

                    salary.basic = newBasic;
                    salary.allowances = newAllowances;
                    salary.bonus = newBonus;
                    salary.deductions = newDeductions;

                    log(
                            "SALARY_UPDATED",
                            "Updated salary for employee "
                                    + employee.id
                    );

                    saveData();
                    refreshAll();

                    return;

                } catch (IllegalArgumentException ex) {
                    error(ex.getMessage());
                }
            }

        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    // =========================================================
    // PAYROLL
    // =========================================================

    private void buildPayrollTab() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(14, 2, 2, 2));

        JPanel tools = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        tools.setOpaque(false);

        JComboBox<String> month = new JComboBox<>();
        styleComboBox(month);

        YearMonth current = YearMonth.now();

        for (int i = 0; i < 24; i++) {
            month.addItem(current.minusMonths(i).toString());
        }

        JButton process = button(
                "Process Monthly Payroll",
                INDIGO
        );
        process.addActionListener(
                e -> processPayroll(
                        (String) month.getSelectedItem()
                )
        );

        JButton approve = button("Approve Selected", ORANGE);
        approve.addActionListener(e -> changePayrollStatus(
                "APPROVED"
        ));

        JButton markPaid = button("Mark Selected Paid", GREEN);
        markPaid.addActionListener(e -> changePayrollStatus(
                "PAID"
        ));

        JButton export = button("Export Payroll CSV", GRAY);
        export.addActionListener(e -> exportPayrollCsv());

        JLabel periodLabel = new JLabel("Pay period:");
        periodLabel.setForeground(TEXT);

        tools.add(periodLabel);
        tools.add(month);
        tools.add(process);
        tools.add(approve);
        tools.add(markPaid);
        tools.add(export);

        payrollModel = model(
                "Payroll ID",
                "Period",
                "Employee ID",
                "Employee Name",
                "Basic",
                "Allowances",
                "Bonus",
                "Gross",
                "Deductions",
                "Net Pay",
                "Status",
                "Processed On"
        );

        payrollTable = table(payrollModel);

        JPanel box = titledPanel("Payroll History");
        box.add(new JScrollPane(payrollTable), BorderLayout.CENTER);

        page.add(tools, BorderLayout.NORTH);
        page.add(box, BorderLayout.CENTER);

        tabs.addTab("Payroll", page);
    }

    private void processPayroll(String period) {
        if (period == null || period.isBlank()) {
            error("Select a payroll period.");
            return;
        }

        List<Employee> activeEmployees = data.employees.stream()
                .filter(Employee::isActive)
                .toList();

        if (activeEmployees.isEmpty()) {
            error("There are no active employees.");
            return;
        }

        List<Employee> missingSalary = activeEmployees.stream()
                .filter(employee -> {
                    SalaryStructure salary =
                            data.salaries.get(employee.id);

                    return salary == null
                            || salary.gross()
                            .compareTo(BigDecimal.ZERO) <= 0;
                })
                .toList();

        if (!missingSalary.isEmpty()) {
            String names = missingSalary.stream()
                    .map(e -> e.name)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            error(
                    "Configure a positive salary for every active employee "
                            + "before processing payroll.\nMissing: "
                            + names
            );

            return;
        }

        List<Employee> alreadyProcessed = activeEmployees.stream()
                .filter(
                        employee -> data.payroll.stream()
                                .anyMatch(
                                        p -> p.employeeId == employee.id
                                                && p.period.equals(period)
                                )
                )
                .toList();

        if (!alreadyProcessed.isEmpty()) {
            error(
                    "Payroll already exists for " + period
                            + " for one or more active employees.\n"
                            + "Duplicate processing is blocked."
            );

            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Process payroll for " + period + " for "
                        + activeEmployees.size() + " active employees?\n\n"
                        + "New records will be created with status PENDING.",
                "Confirm payroll",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        String processedOn = LocalDate.now().toString();

        for (Employee employee : activeEmployees) {
            SalaryStructure salary = data.salaries.get(employee.id);

            data.payroll.add(
                    new Payroll(
                            data.nextPayrollId++,
                            period,
                            employee.id,
                            employee.name,
                            salary.basic,
                            salary.allowances,
                            salary.bonus,
                            salary.gross(),
                            salary.deductions,
                            salary.net(),
                            "PENDING",
                            processedOn
                    )
            );
        }

        log(
                "PAYROLL_PROCESSED",
                "Processed " + period + " payroll for "
                        + activeEmployees.size() + " employees"
        );

        saveData();
        refreshAll();

        information(
                "Payroll processed for " + period + ".\n"
                        + "Records are currently PENDING approval."
        );
    }

    private void changePayrollStatus(String targetStatus) {
        int row = payrollTable.getSelectedRow();

        if (row < 0) {
            error("Select a payroll record first.");
            return;
        }

        int modelRow = payrollTable.convertRowIndexToModel(row);

        try {
            int payrollId = requireValidInteger(
                    String.valueOf(
                            payrollModel.getValueAt(modelRow, 0)
                    ),
                    "Payroll ID"
            );

            Payroll payroll = data.payroll.stream()
                    .filter(p -> p.id == payrollId)
                    .findFirst()
                    .orElse(null);

            if (payroll == null) {
                return;
            }

            if (targetStatus.equals("APPROVED")
                    && !payroll.status.equals("PENDING")) {
                throw new IllegalArgumentException(
                        "Only PENDING payroll can be approved."
                );
            }

            if (targetStatus.equals("PAID")
                    && !payroll.status.equals("APPROVED")) {
                throw new IllegalArgumentException(
                        "Only APPROVED payroll can be marked as paid."
                );
            }

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Change payroll #" + payroll.id + " status from "
                            + payroll.status + " to " + targetStatus + "?",
                    "Confirm status change",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }

            payroll.status = targetStatus;

            log(
                    "PAYROLL_STATUS_CHANGED",
                    "Payroll #" + payroll.id + " changed to "
                            + targetStatus
            );

            saveData();
            refreshAll();

        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    // =========================================================
    // PAYSLIPS
    // =========================================================

    private void buildPayslipsTab() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(14, 2, 2, 2));

        JTextArea output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 14));
        output.setMargin(new Insets(15, 15, 15, 15));
        output.setForeground(TEXT);
        output.setBackground(SURFACE);
        output.setCaretColor(TEXT);

        JScrollPane outputScroll = new JScrollPane(output);
        styleScrollPane(outputScroll);

        JPanel box = titledPanel("Payslip Viewer");
        box.add(outputScroll, BorderLayout.CENTER);

        JPanel tools = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );
        tools.setOpaque(false);

        JComboBox<String> payrollChoices = new JComboBox<>();
        styleComboBox(payrollChoices);

        JButton view = button("View Selected Payslip", INDIGO);
        view.addActionListener(e -> {
            Payroll payroll = selectedPayroll(payrollChoices);

            if (payroll != null) {
                output.setText(payslipText(payroll));
                output.setCaretPosition(0);
            }
        });

        JButton save = button("Save Payslip as TXT", GREEN);
        save.addActionListener(e -> {
            Payroll payroll = selectedPayroll(payrollChoices);

            if (payroll != null) {
                saveTextFile(
                        "payslip-"
                                + payroll.employeeId
                                + "-"
                                + payroll.period
                                + ".txt",
                        payslipText(payroll)
                );
            }
        });

        JLabel recordLabel = new JLabel("Payroll record:");
        recordLabel.setForeground(TEXT);

        tools.add(recordLabel);
        tools.add(payrollChoices);
        tools.add(view);
        tools.add(save);

        page.add(tools, BorderLayout.NORTH);
        page.add(box, BorderLayout.CENTER);

        tabs.addTab("Payslips", page);

        page.putClientProperty("choices", payrollChoices);
    }

    private Payroll selectedPayroll(JComboBox<String> choices) {
        Object selected = choices.getSelectedItem();

        if (selected == null) {
            error("No payroll records available. Process payroll first.");
            return null;
        }

        String value = selected.toString();

        try {
            int id = requireValidInteger(
                    value.substring(0, value.indexOf(" | ")),
                    "Payroll ID"
            );

            return data.payroll.stream()
                    .filter(p -> p.id == id)
                    .findFirst()
                    .orElse(null);

        } catch (Exception ex) {
            error("Unable to identify the selected payroll record.");
            return null;
        }
    }

    private String payslipText(Payroll payroll) {
        return "                         PAYFLOW HR\n"
                + "                    EMPLOYEE PAYSLIP\n"
                + "================================================\n"
                + "Payroll reference : " + payroll.id + "\n"
                + "Pay period        : " + payroll.period + "\n"
                + "Processed on      : " + payroll.processedOn + "\n"
                + "Employee ID       : " + payroll.employeeId + "\n"
                + "Employee name     : " + payroll.employeeName + "\n"
                + "------------------------------------------------\n"
                + "Basic salary      : " + money(payroll.basic) + "\n"
                + "Allowances        : " + money(payroll.allowances) + "\n"
                + "Bonus             : " + money(payroll.bonus) + "\n"
                + "Gross salary      : " + money(payroll.gross) + "\n"
                + "Deductions        : " + money(payroll.deductions) + "\n"
                + "================================================\n"
                + "NET PAY           : " + money(payroll.net) + "\n"
                + "Status            : " + payroll.status + "\n\n"
                + "Prototype payslip. Tax and statutory contributions "
                + "are not calculated.";
    }

    // =========================================================
    // REPORTS
    // =========================================================

    private void buildReportsTab() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(14, 2, 2, 2));

        JPanel box = titledPanel("Reports & Exports");

        JTextArea info = new JTextArea(
                "Generate reports from the latest application data.\n\n"
                        + "Employee report: employee details, department, "
                        + "status and salary structure.\n"
                        + "Payroll report: all processed payroll records.\n"
                        + "Department report: departments and employee counts.\n"
                        + "Audit report: complete action history.\n\n"
                        + "CSV files can be opened using Microsoft Excel "
                        + "or Google Sheets."
        );

        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setBackground(SURFACE);
        info.setForeground(TEXT);
        info.setCaretColor(TEXT);
        info.setFont(new Font("SansSerif", Font.PLAIN, 14));
        info.setBorder(new EmptyBorder(5, 5, 5, 5));

        box.add(info, BorderLayout.CENTER);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        buttons.setOpaque(false);

        JButton employees = button("Export Employees CSV", INDIGO);
        employees.addActionListener(e -> exportEmployeesCsv());

        JButton payroll = button("Export Payroll CSV", GREEN);
        payroll.addActionListener(e -> exportPayrollCsv());

        JButton departments = button("Export Departments CSV", GRAY);
        departments.addActionListener(e -> exportDepartmentsCsv());

        JButton audit = button("Export Audit CSV", GRAY);
        audit.addActionListener(e -> exportAuditCsv());

        JButton backup = button("Create Data Backup", ORANGE);
        backup.addActionListener(e -> backupData());

        buttons.add(employees);
        buttons.add(payroll);
        buttons.add(departments);
        buttons.add(audit);
        buttons.add(backup);

        page.add(box, BorderLayout.CENTER);
        page.add(buttons, BorderLayout.SOUTH);

        tabs.addTab("Reports", page);
    }

    private void exportPayrollCsv() {
        StringBuilder csv = new StringBuilder(
                "Payroll ID,Period,Employee ID,Employee Name,"
                        + "Basic,Allowances,Bonus,Gross,Deductions,"
                        + "Net Pay,Status,Processed On\n"
        );

        for (Payroll p : data.payroll) {
            csv.append(p.id).append(',')
                    .append(csvValue(p.period)).append(',')
                    .append(p.employeeId).append(',')
                    .append(csvValue(p.employeeName)).append(',')
                    .append(p.basic).append(',')
                    .append(p.allowances).append(',')
                    .append(p.bonus).append(',')
                    .append(p.gross).append(',')
                    .append(p.deductions).append(',')
                    .append(p.net).append(',')
                    .append(csvValue(p.status)).append(',')
                    .append(csvValue(p.processedOn))
                    .append('\n');
        }

        saveTextFile("payroll-report.csv", csv.toString());
    }

    private void exportEmployeesCsv() {
        StringBuilder csv = new StringBuilder(
                "Employee ID,Name,Department,Designation,Email,"
                        + "Phone,Status,Basic,Allowances,Bonus,"
                        + "Deductions,Net Salary\n"
        );

        for (Employee e : data.employees) {
            SalaryStructure salary = data.salaries.getOrDefault(
                    e.id,
                    new SalaryStructure(
                            e.id,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO
                    )
            );

            Department department = findDepartment(e.departmentId);

            csv.append(e.id).append(',')
                    .append(csvValue(e.name)).append(',')
                    .append(csvValue(
                            department == null
                                    ? "Unknown"
                                    : department.name
                    ))
                    .append(',')
                    .append(csvValue(e.designation)).append(',')
                    .append(csvValue(e.email)).append(',')
                    .append(csvValue(e.phone)).append(',')
                    .append(e.active ? "Active" : "Inactive")
                    .append(',')
                    .append(salary.basic).append(',')
                    .append(salary.allowances).append(',')
                    .append(salary.bonus).append(',')
                    .append(salary.deductions).append(',')
                    .append(salary.net())
                    .append('\n');
        }

        saveTextFile("employee-report.csv", csv.toString());
    }

    private void exportDepartmentsCsv() {
        StringBuilder csv = new StringBuilder(
                "Department ID,Name,Description,Employee Count\n"
        );

        for (Department d : data.departments) {
            long count = data.employees.stream()
                    .filter(e -> e.departmentId == d.id)
                    .count();

            csv.append(d.id).append(',')
                    .append(csvValue(d.name)).append(',')
                    .append(csvValue(d.description)).append(',')
                    .append(count)
                    .append('\n');
        }

        saveTextFile("department-report.csv", csv.toString());
    }

    private void exportAuditCsv() {
        StringBuilder csv = new StringBuilder(
                "Timestamp,Action,Details\n"
        );

        for (Audit audit : data.audit) {
            csv.append(csvValue(audit.timestamp)).append(',')
                    .append(csvValue(audit.action)).append(',')
                    .append(csvValue(audit.details))
                    .append('\n');
        }

        saveTextFile("audit-log.csv", csv.toString());
    }

    private String csvValue(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private void saveTextFile(String fileName, String content) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(fileName));

        if (chooser.showSaveDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            Files.writeString(
                    chooser.getSelectedFile().toPath(),
                    content,
                    StandardCharsets.UTF_8
            );

            information(
                    "File saved:\n"
                            + chooser.getSelectedFile().getAbsolutePath()
            );

        } catch (IOException ex) {
            error("Could not save file: " + ex.getMessage());
        }
    }

    private void backupData() {
        try {
            Files.createDirectories(DATA_DIR);

            Path target = DATA_DIR.resolve(
                    "payroll-backup-"
                            + LocalDate.now()
                            + "-"
                            + System.currentTimeMillis()
                            + ".ser"
            );

            Files.copy(
                    DATA_FILE,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            information(
                    "Backup created:\n"
                            + target.toAbsolutePath()
            );

        } catch (IOException ex) {
            error("Could not create backup: " + ex.getMessage());
        }
    }

    // =========================================================
    // AUDIT LOG
    // =========================================================

    private void buildAuditTab() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(14, 2, 2, 2));

        auditModel = model(
                "Timestamp",
                "Action",
                "Details"
        );

        auditTable = table(auditModel);

        JPanel box = titledPanel("Audit Log");
        box.add(new JScrollPane(auditTable), BorderLayout.CENTER);

        JButton export = button("Export Audit CSV", INDIGO);
        export.addActionListener(e -> exportAuditCsv());

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        actions.setOpaque(false);
        actions.add(export);

        page.add(box, BorderLayout.CENTER);
        page.add(actions, BorderLayout.SOUTH);

        tabs.addTab("Audit Log", page);
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private JPanel titledPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(TEXT);

        panel.add(label, BorderLayout.NORTH);

        return panel;
    }

    private DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable table(DefaultTableModel tableModel) {
        JTable jTable = new JTable(tableModel);

        jTable.setRowHeight(40);
        jTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        jTable.setForeground(TEXT);
        jTable.setBackground(SURFACE);
        jTable.setGridColor(BORDER);
        jTable.setShowVerticalLines(false);
        jTable.setSelectionBackground(new Color(57, 66, 111));
        jTable.setSelectionForeground(Color.WHITE);
        jTable.setAutoCreateRowSorter(true);
        jTable.setFillsViewportHeight(true);

        jTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );
        jTable.getTableHeader().setBackground(SURFACE_ALT);
        jTable.getTableHeader().setForeground(Color.WHITE);
        jTable.getTableHeader().setOpaque(true);
        jTable.getTableHeader().setPreferredSize(
                new Dimension(0, 44)
        );
        jTable.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, BORDER
                )
        );

        return jTable;
    }

    private JButton button(String text, Color color) {
        JButton jButton = new JButton(text);

        jButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        jButton.setForeground(Color.WHITE);
        jButton.setBackground(color);
        jButton.setFocusPainted(false);
        jButton.setOpaque(true);
        jButton.setContentAreaFilled(true);
        jButton.setBorderPainted(false);
        jButton.setBorder(new EmptyBorder(10, 14, 10, 14));
        jButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return jButton;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(SURFACE_ALT);
        field.setCaretColor(Color.WHITE);
        field.setSelectionColor(INDIGO);
        field.setSelectedTextColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        comboBox.setForeground(TEXT);
        comboBox.setBackground(SURFACE_ALT);
        comboBox.setOpaque(true);
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );
        scrollPane.getViewport().setBackground(SURFACE);
        scrollPane.setBackground(SURFACE);
    }

    private String money(BigDecimal amount) {
        return MONEY.format(
                amount.setScale(2, RoundingMode.HALF_UP)
        );
    }

    // =========================================================
    // INPUT VALIDATION
    // =========================================================

    private String requireNonBlank(
            String value,
            String fieldName,
            int maxLength
    ) {
        String text = value == null ? "" : value.trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }

        if (text.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " must be at most "
                            + maxLength + " characters."
            );
        }

        return text;
    }

    private String optionalText(String value, int maxLength) {
        String text = value == null ? "" : value.trim();

        if (text.length() > maxLength) {
            throw new IllegalArgumentException(
                    "Text must be at most "
                            + maxLength + " characters."
            );
        }

        return text;
    }

    private String requireValidName(String value, String fieldName) {
        String text = requireNonBlank(value, fieldName, 60);

        if (!NAME_PATTERN.matcher(text).matches()) {
            throw new IllegalArgumentException(
                    fieldName + " must start with a letter and contain "
                            + "only letters, spaces, dots, apostrophes "
                            + "or hyphens."
            );
        }

        return text;
    }

    private String requireValidEmail(
            String value,
            boolean required
    ) {
        String text = value == null ? "" : value.trim();

        if (text.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(
                        "Email is required."
                );
            }

            return text;
        }

        if (text.length() > 100
                || !EMAIL_PATTERN.matcher(text).matches()) {
            throw new IllegalArgumentException(
                    "Enter a valid email address, "
                            + "for example name@example.com."
            );
        }

        return text.toLowerCase(Locale.ROOT);
    }

    private String requireValidPhone(
            String value,
            boolean required
    ) {
        String text = value == null ? "" : value.trim();

        if (text.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(
                        "Phone number is required."
                );
            }

            return text;
        }

        if (!PHONE_PATTERN.matcher(text).matches()) {
            throw new IllegalArgumentException(
                    "Enter a valid 10-digit Indian mobile number, "
                            + "for example 9876543210."
            );
        }

        return text;
    }

    private BigDecimal requireAmount(
            String value,
            String fieldName
    ) {
        String text = value == null ? "" : value.trim();

        if (!AMOUNT_PATTERN.matcher(text).matches()) {
            throw new IllegalArgumentException(
                    fieldName + " must be a valid non-negative amount "
                            + "with up to 2 decimal places."
            );
        }

        BigDecimal amount = new BigDecimal(text);

        if (amount.compareTo(
                new BigDecimal("999999999.99")
        ) > 0) {
            throw new IllegalArgumentException(
                    fieldName + " is too large."
            );
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private int requireValidInteger(
            String value,
            String fieldName
    ) {
        String text = value == null ? "" : value.trim();

        if (!INTEGER_PATTERN.matcher(text).matches()) {
            throw new IllegalArgumentException(
                    fieldName + " must be a whole number."
            );
        }

        try {
            return Integer.parseInt(text);

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    fieldName + " is outside the allowed range."
            );
        }
    }

    // =========================================================
    // SECURITY
    // =========================================================

    private String generateSalt() {
        SecureRandom random = new SecureRandom();

        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    (salt + password).getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to hash password.", ex
            );
        }
    }

    private boolean verifyPassword(
            String password,
            String salt,
            String expectedHash
    ) {
        String actualHash = hashPassword(password, salt);

        return MessageDigest.isEqual(
                actualHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    // =========================================================
    // REFRESH
    // =========================================================

    private void refreshAll() {
        refreshEmployeeTable();

        if (departmentModel != null) {
            departmentModel.setRowCount(0);

            for (Department department : data.departments) {
                long count = data.employees.stream()
                        .filter(
                                e -> e.departmentId == department.id
                        )
                        .count();

                departmentModel.addRow(new Object[]{
                        department.id,
                        department.name,
                        department.description,
                        count
                });
            }
        }

        if (salaryModel != null) {
            salaryModel.setRowCount(0);

            for (Employee employee : data.employees) {
                SalaryStructure salary = data.salaries.getOrDefault(
                        employee.id,
                        new SalaryStructure(
                                employee.id,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO
                        )
                );

                salaryModel.addRow(new Object[]{
                        employee.id,
                        employee.name,
                        money(salary.basic),
                        money(salary.allowances),
                        money(salary.bonus),
                        money(salary.deductions),
                        money(salary.gross()),
                        money(salary.net())
                });
            }
        }

        if (payrollModel != null) {
            payrollModel.setRowCount(0);

            for (Payroll payroll : data.payroll) {
                payrollModel.addRow(new Object[]{
                        payroll.id,
                        payroll.period,
                        payroll.employeeId,
                        payroll.employeeName,
                        money(payroll.basic),
                        money(payroll.allowances),
                        money(payroll.bonus),
                        money(payroll.gross),
                        money(payroll.deductions),
                        money(payroll.net),
                        payroll.status,
                        payroll.processedOn
                });
            }
        }

        if (auditModel != null) {
            auditModel.setRowCount(0);

            List<Audit> reversedAudit =
                    new ArrayList<>(data.audit);

            Collections.reverse(reversedAudit);

            for (Audit audit : reversedAudit) {
                auditModel.addRow(new Object[]{
                        audit.timestamp,
                        audit.action,
                        audit.details
                });
            }
        }

        refreshDashboard();
        refreshPayslipChoices();
    }

    private void refreshDashboard() {
        if (dashboardEmployees == null) {
            return;
        }

        long activeEmployees = data.employees.stream()
                .filter(Employee::isActive)
                .count();

        String currentPeriod = YearMonth.now().toString();

        List<Payroll> currentPayroll = data.payroll.stream()
                .filter(p -> p.period.equals(currentPeriod))
                .toList();

        BigDecimal totalNet = currentPayroll.stream()
                .map(p -> p.net)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long approved = currentPayroll.stream()
                .filter(p -> p.status.equals("APPROVED"))
                .count();

        long pending = currentPayroll.stream()
                .filter(p -> p.status.equals("PENDING"))
                .count();

        dashboardEmployees.setText(
                String.valueOf(activeEmployees)
        );

        dashboardPayroll.setText(money(totalNet));
        dashboardApproved.setText(String.valueOf(approved));
        dashboardPending.setText(String.valueOf(pending));
    }

    private void refreshPayslipChoices() {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (!tabs.getTitleAt(i).equals("Payslips")) {
                continue;
            }

            Component component = tabs.getComponentAt(i);

            if (!(component instanceof JPanel panel)) {
                continue;
            }

            Object property = panel.getClientProperty("choices");

            if (!(property instanceof JComboBox<?> rawComboBox)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            JComboBox<String> choices =
                    (JComboBox<String>) rawComboBox;

            Object previous = choices.getSelectedItem();

            choices.removeAllItems();

            for (Payroll payroll : data.payroll) {
                choices.addItem(
                        payroll.id
                                + " | "
                                + payroll.period
                                + " | "
                                + payroll.employeeName
                );
            }

            if (previous != null) {
                choices.setSelectedItem(previous);
            }
        }
    }

    // =========================================================
    // AUDIT + STORAGE
    // =========================================================

    private void log(String action, String details) {
        data.audit.add(
                new Audit(
                        LocalDateTime.now()
                                .withNano(0)
                                .toString()
                                .replace('T', ' '),
                        action,
                        details
                )
        );

        if (data.audit.size() > 5000) {
            data.audit.remove(0);
        }
    }

    private void saveData() {
        try {
            Files.createDirectories(DATA_DIR);

            Path tempFile =
                    DATA_DIR.resolve("payroll-data.tmp");

            try (ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(
                            Files.newOutputStream(tempFile)
                    )
            )) {
                out.writeObject(data);
            }

            try {
                Files.move(
                        tempFile,
                        DATA_FILE,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(
                        tempFile,
                        DATA_FILE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save data. Check folder permissions.\n"
                            + ex.getMessage(),
                    "File save error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private AppData loadData() {
        if (!Files.exists(DATA_FILE)) {
            return new AppData();
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(
                        Files.newInputStream(DATA_FILE)
                )
        )) {
            Object object = in.readObject();

            if (object instanceof AppData appData) {
                return appData;
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Could not read saved data.\n"
                            + "A new empty data file will be created.\n\n"
                            + ex.getMessage(),
                    "Data file warning",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        return new AppData();
    }

    private void seedDefaultsIfNeeded() {
        if (data.users.isEmpty()) {
            String salt = generateSalt();

            data.users.add(
                    new User(
                            "admin",
                            salt,
                            hashPassword("admin123", salt),
                            "ADMIN"
                    )
                );
        }

        if (data.departments.isEmpty()) {
            data.departments.add(
                    new Department(
                            1,
                            "Engineering",
                            "Software and technology"
                    )
            );

            data.departments.add(
                    new Department(
                            2,
                            "Human Resources",
                            "People and recruitment"
                    )
            );

            data.departments.add(
                    new Department(
                            3,
                            "Finance",
                            "Accounts and payroll"
                    )
            );

            data.departments.add(
                    new Department(
                            4,
                            "Design",
                            "Product and visual design"
                    )
            );
        }

        saveData();
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void error(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Sign-in failed",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void information(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =========================================================
    // DATA CLASSES
    // =========================================================

    private static class AppData implements Serializable {
        private static final long serialVersionUID = 1L;

        int nextPayrollId = 1;

        List<User> users = new ArrayList<>();
        List<Employee> employees = new ArrayList<>();
        List<Department> departments = new ArrayList<>();

        Map<Integer, SalaryStructure> salaries = new HashMap<>();

        List<Payroll> payroll = new ArrayList<>();
        List<Audit> audit = new ArrayList<>();
    }

    private static class User implements Serializable {
        private static final long serialVersionUID = 1L;

        String username;
        String passwordSalt;
        String passwordHash;
        String role;

        User(
                String username,
                String passwordSalt,
                String passwordHash,
                String role
        ) {
            this.username = username;
            this.passwordSalt = passwordSalt;
            this.passwordHash = passwordHash;
            this.role = role;
        }
    }

    private static class Employee implements Serializable {
        private static final long serialVersionUID = 1L;

        int id;
        String name;
        int departmentId;
        String designation;
        String email;
        String phone;
        boolean active = true;

        Employee(
                int id,
                String name,
                int departmentId,
                String designation,
                String email,
                String phone
        ) {
            this.id = id;
            this.name = name;
            this.departmentId = departmentId;
            this.designation = designation;
            this.email = email;
            this.phone = phone;
        }

        boolean isActive() {
            return active;
        }
    }

    private static class Department implements Serializable {
        private static final long serialVersionUID = 1L;

        int id;
        String name;
        String description;

        Department(
                int id,
                String name,
                String description
        ) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }

    private static class SalaryStructure implements Serializable {
        private static final long serialVersionUID = 1L;

        int employeeId;

        BigDecimal basic;
        BigDecimal allowances;
        BigDecimal bonus;
        BigDecimal deductions;

        SalaryStructure(
                int employeeId,
                BigDecimal basic,
                BigDecimal allowances,
                BigDecimal bonus,
                BigDecimal deductions
        ) {
            this.employeeId = employeeId;
            this.basic = basic;
            this.allowances = allowances;
            this.bonus = bonus;
            this.deductions = deductions;
        }

        BigDecimal gross() {
            return basic.add(allowances).add(bonus);
        }

        BigDecimal net() {
            return gross().subtract(deductions);
        }
    }

    private static class Payroll implements Serializable {
        private static final long serialVersionUID = 1L;

        int id;
        int employeeId;

        String period;
        String employeeName;
        String status;
        String processedOn;

        BigDecimal basic;
        BigDecimal allowances;
        BigDecimal bonus;
        BigDecimal gross;
        BigDecimal deductions;
        BigDecimal net;

        Payroll(
                int id,
                String period,
                int employeeId,
                String employeeName,
                BigDecimal basic,
                BigDecimal allowances,
                BigDecimal bonus,
                BigDecimal gross,
                BigDecimal deductions,
                BigDecimal net,
                String status,
                String processedOn
        ) {
            this.id = id;
            this.period = period;
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.basic = basic;
            this.allowances = allowances;
            this.bonus = bonus;
            this.gross = gross;
            this.deductions = deductions;
            this.net = net;
            this.status = status;
            this.processedOn = processedOn;
        }
    }

    private static class Audit implements Serializable {
        private static final long serialVersionUID = 1L;

        String timestamp;
        String action;
        String details;

        Audit(
                String timestamp,
                String action,
                String details
        ) {
            this.timestamp = timestamp;
            this.action = action;
            this.details = details;
        }
    }

    // =========================================================
    // GLOBAL THEME + ENTRY POINT
    // =========================================================

    private static void configureUIDefaults() {
        Font body = new Font("SansSerif", Font.PLAIN, 14);
        Font bold = new Font("SansSerif", Font.BOLD, 14);

        UIManager.put("Label.font", body);
        UIManager.put("Label.foreground", TEXT);

        UIManager.put("Button.font", bold);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.background", SURFACE_ALT);
        UIManager.put("Button.select", INDIGO);
        UIManager.put("Button.disabledText", MUTED);

        UIManager.put("Panel.background", BG);

        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", INDIGO);
        UIManager.put("Table.selectionForeground", Color.WHITE);

        UIManager.put("TableHeader.background", SURFACE_ALT);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("TableHeader.font", bold);

        UIManager.put("TextField.background", SURFACE_ALT);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", Color.WHITE);
        UIManager.put("TextField.font", body);

        UIManager.put("PasswordField.background", SURFACE_ALT);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("PasswordField.caretForeground", Color.WHITE);
        UIManager.put("PasswordField.font", body);

        UIManager.put("TextArea.background", SURFACE);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("TextArea.caretForeground", Color.WHITE);
        UIManager.put("TextArea.font", body);

        UIManager.put("ComboBox.background", SURFACE_ALT);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.font", body);
        UIManager.put("ComboBox.selectionBackground", INDIGO);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);

        UIManager.put("TabbedPane.background", SURFACE);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.selected", SURFACE_ALT);
        UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
        UIManager.put("TabbedPane.unselectedBackground", SURFACE);
        UIManager.put("TabbedPane.contentAreaColor", SURFACE);
        UIManager.put("TabbedPane.focus", INDIGO);
        UIManager.put("TabbedPane.font", bold);

        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", body);
        UIManager.put("OptionPane.buttonFont", bold);

        UIManager.put("List.background", SURFACE);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("List.selectionBackground", INDIGO);
        UIManager.put("List.selectionForeground", Color.WHITE);
        UIManager.put("List.font", body);

        UIManager.put("ScrollPane.background", SURFACE);
        UIManager.put("Viewport.background", SURFACE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName()
            );

        } catch (Exception ex) {
            System.err.println(
                    "Could not set Swing look and feel: "
                            + ex.getMessage()
            );
        } 

        configureUIDefaults();

        SwingUtilities.invokeLater(Main::new);
    }
}