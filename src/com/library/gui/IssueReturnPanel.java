package com.library.gui;

import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.TransactionDAO;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class IssueReturnPanel extends JPanel {

    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private final JComboBox<Book> bookCombo = new JComboBox<>();
    private final JComboBox<Member> memberCombo = new JComboBox<>();
    private final JTextField dueDateField = new JTextField(10);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Txn ID", "Book", "Member", "Issue Date", "Due Date", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JCheckBox showAllCheckBox = new JCheckBox("Show full history (including returned)");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public IssueReturnPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BACKGROUND);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        add(buildIssuePanel(), BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.cardBorder("Transactions"));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        refreshAll();
    }

    /** Colors the Status column: red for OVERDUE, indigo for ISSUED, green/amber for RETURNED. */
    private static class StatusCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
            String text = String.valueOf(value);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setFont(UITheme.FONT_BUTTON);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : UITheme.ROW_ALT);
                if (text.startsWith("OVERDUE")) {
                    c.setForeground(UITheme.DANGER);
                } else if (text.startsWith("ISSUED")) {
                    c.setForeground(UITheme.ACCENT);
                } else if (text.contains("fine")) {
                    c.setForeground(UITheme.WARNING);
                } else {
                    c.setForeground(UITheme.SUCCESS);
                }
            }
            return c;
        }
    }

    private JPanel buildIssuePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                UITheme.cardBorder("Issue a Book"),
                BorderFactory.createEmptyBorder(6, 6, 10, 6)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        bookCombo.setFont(UITheme.FONT_BODY);
        memberCombo.setFont(UITheme.FONT_BODY);
        dueDateField.setFont(UITheme.FONT_BODY);
        dueDateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(themedLabel("Book:"), gbc);
        gbc.gridx = 1;
        panel.add(bookCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(themedLabel("Member:"), gbc);
        gbc.gridx = 1;
        panel.add(memberCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(themedLabel("Due Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        dueDateField.setText(LocalDate.now().plusDays(TransactionDAO.DEFAULT_LOAN_DAYS).format(DATE_FMT));
        panel.add(dueDateField, gbc);

        JButton issueBtn = UITheme.primaryButton("Issue Book");
        issueBtn.addActionListener(e -> issueBook());
        JButton refreshListsBtn = UITheme.secondaryButton("Refresh Lists");
        refreshListsBtn.addActionListener(e -> loadCombos());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(issueBtn);
        btnPanel.add(refreshListsBtn);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private JLabel themedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBackground(UITheme.BACKGROUND);
        JButton returnBtn = UITheme.primaryButton("Return Selected Book");
        returnBtn.addActionListener(e -> returnSelectedBook());
        showAllCheckBox.setBackground(UITheme.BACKGROUND);
        showAllCheckBox.setFont(UITheme.FONT_BODY);
        showAllCheckBox.addActionListener(e -> refreshTable());

        panel.add(returnBtn);
        panel.add(showAllCheckBox);
        return panel;
    }

    private void loadCombos() {
        bookCombo.removeAllItems();
        memberCombo.removeAllItems();
        try {
            List<Book> books = bookDAO.getAllBooks();
            for (Book b : books) {
                if (b.getAvailableCopies() > 0) {
                    bookCombo.addItem(b);
                }
            }
            List<Member> members = memberDAO.getAllMembers();
            for (Member m : members) {
                memberCombo.addItem(m);
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void issueBook() {
        Book book = (Book) bookCombo.getSelectedItem();
        Member member = (Member) memberCombo.getSelectedItem();
        if (book == null || member == null) {
            JOptionPane.showMessageDialog(this, "Select both a book and a member.", "Missing selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueDateField.getText().trim(), DATE_FMT);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Due date must be in YYYY-MM-DD format.", "Invalid date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            transactionDAO.issueBook(book.getBookId(), member.getMemberId(), LocalDate.now(), dueDate);
            JOptionPane.showMessageDialog(this, "\"" + book.getTitle() + "\" issued to " + member.getName() + ".");
            refreshAll();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot issue book", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void returnSelectedBook() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a transaction in the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int transactionId = (Integer) tableModel.getValueAt(row, 0);
        String status = String.valueOf(tableModel.getValueAt(row, 5));
        if (status.startsWith("RETURNED")) {
            JOptionPane.showMessageDialog(this, "That book has already been returned.", "Already returned", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            double fine = transactionDAO.returnBook(transactionId);
            if (fine > 0) {
                JOptionPane.showMessageDialog(this, String.format("Book returned. Late fine due: Rs. %.2f", fine),
                        "Returned with fine", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Book returned on time. No fine.");
            }
            refreshAll();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot return book", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    public void refreshAll() {
        loadCombos();
        refreshTable();
    }

    private void refreshTable() {
        try {
            List<Transaction> transactions = showAllCheckBox.isSelected()
                    ? transactionDAO.getAllTransactions()
                    : transactionDAO.getActiveIssues();
            populateTable(transactions);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void populateTable(List<Transaction> transactions) {
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (Transaction t : transactions) {
            String statusDisplay;
            if ("RETURNED".equals(t.getStatus())) {
                statusDisplay = "RETURNED" + (t.getFine() > 0 ? String.format(" (fine Rs.%.2f)", t.getFine()) : "");
            } else if (t.getDueDate().isBefore(today)) {
                statusDisplay = "OVERDUE";
            } else {
                statusDisplay = "ISSUED";
            }
            tableModel.addRow(new Object[]{
                    t.getTransactionId(), t.getBookTitle(), t.getMemberName(),
                    t.getIssueDate(), t.getDueDate(), statusDisplay
            });
        }
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
