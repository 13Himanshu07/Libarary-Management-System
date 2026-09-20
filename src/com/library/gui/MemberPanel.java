package com.library.gui;

import com.library.dao.MemberDAO;
import com.library.model.Member;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MemberPanel extends JPanel {

    private final MemberDAO memberDAO = new MemberDAO();

    private final JTextField nameField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);
    private final JTextField phoneField = new JTextField(15);
    private final JTextField addressField = new JTextField(15);
    private final JTextField searchField = new JTextField(15);

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"ID", "Name", "Email", "Phone", "Address", "Member Since"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);

    private Integer selectedMemberId = null;

    public MemberPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BACKGROUND);
        UITheme.styleTable(table);

        add(buildFormPanel(), BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.cardBorder("Registered Members"));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
        add(buildSearchAndActionsPanel(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadSelectedRowIntoForm();
            }
        });

        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                UITheme.cardBorder("Member Details"),
                BorderFactory.createEmptyBorder(6, 6, 10, 6)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(panel, gbc, row++, "Name:", nameField);
        addFormRow(panel, gbc, row++, "Email:", emailField);
        addFormRow(panel, gbc, row++, "Phone:", phoneField);
        addFormRow(panel, gbc, row++, "Address:", addressField);

        JButton addBtn = UITheme.primaryButton("Add Member");
        JButton updateBtn = UITheme.secondaryButton("Update Selected");
        JButton deleteBtn = UITheme.dangerButton("Delete Selected");
        JButton clearBtn = UITheme.secondaryButton("Clear Form");

        addBtn.addActionListener(e -> addMember());
        updateBtn.addActionListener(e -> updateMember());
        deleteBtn.addActionListener(e -> deleteMember());
        clearBtn.addActionListener(e -> clearForm());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 4;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(l, gbc);
        gbc.gridx = 1;
        field.setFont(UITheme.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        panel.add(field, gbc);
    }

    private JPanel buildSearchAndActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBackground(UITheme.BACKGROUND);
        JButton searchBtn = UITheme.primaryButton("Search");
        JButton refreshBtn = UITheme.secondaryButton("Show All");

        searchBtn.addActionListener(e -> searchMembers());
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            refreshTable();
        });

        JLabel searchLabel = new JLabel("Search (name/email/phone):");
        searchLabel.setFont(UITheme.FONT_BODY);
        searchLabel.setForeground(UITheme.TEXT_PRIMARY);
        searchField.setFont(UITheme.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(searchBtn);
        panel.add(refreshBtn);
        return panel;
    }

    private void addMember() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Missing info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Member member = new Member();
            member.setName(name);
            member.setEmail(emailField.getText().trim());
            member.setPhone(phoneField.getText().trim());
            member.setAddress(addressField.getText().trim());

            memberDAO.addMember(member);
            clearForm();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Member added.");
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void updateMember() {
        if (selectedMemberId == null) {
            JOptionPane.showMessageDialog(this, "Select a member in the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Member member = new Member();
            member.setMemberId(selectedMemberId);
            member.setName(nameField.getText().trim());
            member.setEmail(emailField.getText().trim());
            member.setPhone(phoneField.getText().trim());
            member.setAddress(addressField.getText().trim());

            memberDAO.updateMember(member);
            clearForm();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Member updated.");
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void deleteMember() {
        if (selectedMemberId == null) {
            JOptionPane.showMessageDialog(this, "Select a member in the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this member? This also removes their transaction history.",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            memberDAO.deleteMember(selectedMemberId);
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void searchMembers() {
        String keyword = searchField.getText().trim();
        try {
            List<Member> results = keyword.isEmpty() ? memberDAO.getAllMembers() : memberDAO.searchMembers(keyword);
            populateTable(results);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    public void refreshTable() {
        try {
            populateTable(memberDAO.getAllMembers());
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void populateTable(List<Member> members) {
        tableModel.setRowCount(0);
        for (Member m : members) {
            tableModel.addRow(new Object[]{
                    m.getMemberId(), m.getName(), m.getEmail(), m.getPhone(),
                    m.getAddress(), m.getMembershipDate()
            });
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        selectedMemberId = (Integer) tableModel.getValueAt(row, 0);
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        emailField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        phoneField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        addressField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
    }

    private void clearForm() {
        selectedMemberId = null;
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
        table.clearSelection();
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
