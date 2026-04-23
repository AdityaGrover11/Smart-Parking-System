
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Custom Panel for Gradient UI background
class GradientPanel extends JPanel {
    private Color color1, color2;
    public GradientPanel(Color c1, Color c2) {
        this.color1 = c1;
        this.color2 = c2;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}


public class SmartParkingFrontend extends JFrame implements ActionListener {

    // Connect to our Backend!
    SmartParkingBackend backend = new SmartParkingBackend();

    // UI Elements
    JPanel displayPanel;
    JLabel labelOccupancy;
    JLabel labelTotalVehicles;
    JLabel labelRevenue;

    JButton btnPark, btnRemove, btnFind, btnSearchType, btnRefresh, btnHistory, btnBack;

    boolean isHistoryView = false;
    JTextField searchField = new JTextField(15);
    JComboBox<String> timeFilterCombo = new JComboBox<>(new String[]{"All Time", "Last 1 Week", "Last 1 Month"});

    String currentFilterType = "All"; 
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss");

    public SmartParkingFrontend() {
        setTitle("Smart Parking System - Split Architecture Dashboard");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Setup Top Header (Live Dashboard)
        GradientPanel headerPanel = new GradientPanel(new Color(30, 60, 114), new Color(42, 82, 152));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Smart Parking LIVE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel dashboardPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        dashboardPanel.setOpaque(false);

        labelOccupancy = createDashboardLabel("Occupancy: 0 / 21");
        labelTotalVehicles = createDashboardLabel("Total Vehicles Today: 0");
        labelRevenue = createDashboardLabel("Revenue Today: ₹0");

        dashboardPanel.add(labelOccupancy);
        dashboardPanel.add(labelTotalVehicles);
        dashboardPanel.add(labelRevenue);
        headerPanel.add(dashboardPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Setup Administration Panel
        GradientPanel leftPanel = new GradientPanel(new Color(236, 240, 241), new Color(223, 228, 234));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        leftPanel.setPreferredSize(new Dimension(240, 0));

        JLabel ctrlLabel = new JLabel("Admin Tools");
        ctrlLabel.setFont(new Font("Arial", Font.BOLD, 18));
        ctrlLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(ctrlLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 25))); 

        btnPark = createSimpleButton("Park New Vehicle", new Color(46, 204, 113));
        btnRemove = createSimpleButton("Remove Vehicle", new Color(231, 76, 60));
        btnFind = createSimpleButton("Find Identity", new Color(52, 152, 219));
        btnSearchType = createSimpleButton("Search by Type", new Color(155, 89, 182)); 
        btnRefresh = createSimpleButton("Refresh / Clear Filters", new Color(52, 73, 94));
        btnHistory = createSimpleButton("View History", new Color(243, 156, 18)); 
        btnBack = createSimpleButton("Back to Map", new Color(149, 165, 166)); 

        leftPanel.add(btnPark);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(btnRemove);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(btnFind);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(btnSearchType);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(btnRefresh);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(btnHistory);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(btnBack);

        add(leftPanel, BorderLayout.WEST);

        // Setup Main Display Panel (Parking map / History)
        displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        displayPanel.setBackground(new Color(250, 250, 250));
        
        JScrollPane scrollPane = new JScrollPane(displayPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Listeners for static controls
        btnPark.addActionListener(this);
        btnRemove.addActionListener(this);
        btnFind.addActionListener(this);
        btnSearchType.addActionListener(this);
        btnRefresh.addActionListener(this);
        btnHistory.addActionListener(this);
        btnBack.addActionListener(this);

        // Dynamic History Filters listeners
        timeFilterCombo.addActionListener(e -> { 
            if(isHistoryView) refreshUI(); 
        });
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if(isHistoryView) refreshUI();
            }
        });

        // Initialize user interface graphics
        refreshUI();
        setVisible(true);
    }

    // Helper: Dashboard Label Styling
    private JLabel createDashboardLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    // Helper: Simple Buttons Setup
    private JButton createSimpleButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 45));
        
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true); 
        btn.setContentAreaFilled(true); 
        btn.setBorderPainted(false);
        
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnPark) {
            handleParkVehicle();
        } 
        else if (e.getSource() == btnRemove) {
            handleRemoveVehicle();
        } 
        else if (e.getSource() == btnFind) {
            handleFindVehicle();
        } 
        else if (e.getSource() == btnSearchType) {
            handleFilterByType();
        } 
        else if (e.getSource() == btnRefresh) {
            currentFilterType = "All"; // Reset filter
            refreshUI(); 
        }
        else if (e.getSource() == btnHistory) {
            isHistoryView = true;
            refreshUI();
        }
        else if (e.getSource() == btnBack) {
            isHistoryView = false;
            refreshUI();
        }
    }

    // --- UI Interactions ---
    private void handleParkVehicle() {
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField inputVehicleNumber = new JTextField();
        String[] possibleTypes = {"Bike (₹10/hr)", "Car (₹20/hr)", "Truck (₹40/hr)"};
        JComboBox<String> dropdownType = new JComboBox<>(possibleTypes);

        formPanel.add(new JLabel("Vehicle Number:"));
        formPanel.add(inputVehicleNumber);
        formPanel.add(new JLabel("Vehicle Type:"));
        formPanel.add(dropdownType);

        int choice = JOptionPane.showConfirmDialog(this, formPanel, "Park a Vehicle", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String vehicleNumber = inputVehicleNumber.getText().trim().toUpperCase();
        
        if (vehicleNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid vehicle number!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (backend.isVehicleParked(vehicleNumber)) {
            JOptionPane.showMessageDialog(this, "Vehicle " + vehicleNumber + " is already parked inside!\nMultiple vehicles with the exact same license number physically cannot enter.", "Duplicate Identity Check", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String simpleType = ((String) dropdownType.getSelectedItem()).split(" ")[0]; 

        boolean success = backend.parkVehicle(vehicleNumber, simpleType);
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Vehicle Parked!", "Success", JOptionPane.INFORMATION_MESSAGE);
            currentFilterType = "All";
            refreshUI();
        } else {
            JOptionPane.showMessageDialog(this, "Parking Full!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleRemoveVehicle() {
        String num = JOptionPane.showInputDialog(this, "Enter vehicle number:");
        if (num == null || num.trim().isEmpty()) return;
        
        num = num.trim().toUpperCase();
        int[] location = backend.findVehicleLocation(num);
        
        if (location == null) {
            JOptionPane.showMessageDialog(this, "Not Found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int floor = location[0];
        int slot = location[1];
        
        String vType = backend.vehicleTypes[floor][slot];
        LocalDateTime eTime = backend.entryTimes[floor][slot];

        long fee = backend.calculateFeeAndRemoveVehicle(floor, slot);

        String receipt = "====== RECEIPT ======\n" +
                         "Vehicle: " + num + "\n" +
                         "Type: " + vType + "\n" +
                         "Entry: " + eTime.format(timeFormatter) + "\n" +
                         "Fee Charged: ₹" + fee + "\n" + 
                         "===================";

        JOptionPane.showMessageDialog(this, receipt, "Receipt", JOptionPane.INFORMATION_MESSAGE);
        refreshUI();
    }

    private void handleFindVehicle() {
        String num = JOptionPane.showInputDialog(this, "Find Vehicle:");
        if (num == null || num.trim().isEmpty()) return;
        num = num.trim().toUpperCase();

        int[] loc = backend.findVehicleLocation(num);
        if (loc != null) {
            JOptionPane.showMessageDialog(this, "Vehicle is at Floor " + (loc[0]+1) + ", Slot S" + (loc[1]+1));
        } else {
            JOptionPane.showMessageDialog(this, "Not Found!");
        }
    }

    private void handleFilterByType() {
        String[] types = {"All", "Bike", "Car", "Truck"};
        String filter = (String) JOptionPane.showInputDialog(this, "Select type to show:", 
                           "Live Search Filter", JOptionPane.QUESTION_MESSAGE, null, types, types[0]);

        if (filter != null) {
            currentFilterType = filter;
            refreshUI();
        }
    }

    private void refreshUI() {
        labelOccupancy.setText("Occupancy: " + backend.currentOccupancy + " / " + (backend.totalFloors * backend.slotsPerFloor));
        labelTotalVehicles.setText("Total Vehicles Today: " + backend.totalVehiclesToday);
        labelRevenue.setText("Revenue Today: ₹" + backend.revenueToday);

        displayPanel.removeAll(); 

        if (isHistoryView) {
            drawHistoryView();
        } else {
            drawMapView();
        }

        displayPanel.revalidate();
        displayPanel.repaint();
    }

    private void drawHistoryView() {
        JPanel historyContainer = new JPanel(new BorderLayout());
        historyContainer.setBackground(Color.WHITE);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchBar.setBackground(new Color(236, 240, 241));
        searchBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        
        searchBar.add(new JLabel("Search Vehicle #:"));
        searchBar.add(searchField);
        searchBar.add(new JLabel("Time Filter:"));
        searchBar.add(timeFilterCombo);

        historyContainer.add(searchBar, BorderLayout.NORTH);

        // Fetch History
        java.util.List<String[]> historyData = backend.getHistory(searchField.getText(), (String)timeFilterCombo.getSelectedItem());
        String[][] rowData = new String[historyData.size()][];
        for (int i = 0; i < historyData.size(); i++) {
            rowData[i] = historyData.get(i);
        }

        String[] columnNames = {"Vehicle Number", "Type", "Entry Time", "Exit Time", "Fee Paid"};
        JTable table = new JTable(rowData, columnNames);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(52, 152, 219));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane tableScroll = new JScrollPane(table);
        historyContainer.add(tableScroll, BorderLayout.CENTER);

        displayPanel.add(historyContainer);
    }

    private void drawMapView() {
        // 3. Draw map based on active filters
        for (int f = 0; f < backend.totalFloors; f++) {
            JPanel floorPanel = new JPanel(new BorderLayout());
            floorPanel.setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(10, 10, 10, 10),
                    BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Floor " + (f + 1), 0, 0, new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY)
            ));
            floorPanel.setBackground(Color.WHITE);

            JPanel slotsGrid = new JPanel(new GridLayout(1, backend.slotsPerFloor, 12, 12));
            slotsGrid.setBackground(Color.WHITE);
            slotsGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

            for (int s = 0; s < backend.slotsPerFloor; s++) {
                
                String currentVehicle = backend.parkedVehicles[f][s];
                String currentType = backend.vehicleTypes[f][s];

                JPanel slotPanel = new JPanel(new BorderLayout());
                slotPanel.setPreferredSize(new Dimension(100, 80));
                
                JLabel slotIcon = new JLabel("", SwingConstants.CENTER);
                slotIcon.setFont(new Font("Arial", Font.BOLD, 22));
                
                JLabel slotLabel = new JLabel("S" + (s + 1), SwingConstants.CENTER);
                slotLabel.setFont(new Font("Arial", Font.BOLD, 12));

                boolean showAsHidden = false;
                if (!currentFilterType.equals("All")) {
                    if (currentVehicle == null || !currentType.equals(currentFilterType)) {
                        showAsHidden = true;
                    }
                }

                if (showAsHidden) {
                    slotPanel.setBackground(new Color(230, 230, 230)); // Gray out
                    slotPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
                    slotLabel.setText("Hidden");
                    slotLabel.setForeground(Color.GRAY);
                }
                else if (currentVehicle == null) {
                    slotIcon.setText("P"); // Empty space
                    slotIcon.setForeground(new Color(46, 204, 113));
                    slotPanel.setBackground(new Color(233, 247, 239));
                    slotPanel.setBorder(BorderFactory.createLineBorder(new Color(46, 204, 113), 2));
                    slotLabel.setText("S" + (s + 1) + " (Empty)");
                    slotLabel.setForeground(new Color(39, 174, 96));
                } 
                else {
                    slotIcon.setText(currentType); 
                    slotIcon.setForeground(new Color(192, 57, 43));
                    slotPanel.setBackground(new Color(253, 237, 236));
                    slotPanel.setBorder(BorderFactory.createLineBorder(new Color(231, 76, 60), 2));
                    slotLabel.setText("<html><center>S" + (s + 1) + "<br>" + currentVehicle + "</center></html>");
                    slotLabel.setForeground(new Color(192, 57, 43));
                }

                slotPanel.add(slotIcon, BorderLayout.CENTER);
                slotPanel.add(slotLabel, BorderLayout.SOUTH);
                slotsGrid.add(slotPanel);
            }

            floorPanel.add(slotsGrid, BorderLayout.CENTER);
            displayPanel.add(floorPanel);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmartParkingFrontend());
    }
}
