package com.library.gui;

import com.library.dao.BookDAO;
import com.library.model.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class BookPanel extends JPanel {

    private final BookDAO bookDAO = new BookDAO();

    private final JTextField titleField = new JTextField(15);
    private final JTextField authorField = new JTextField(15);
    private final JTextField isbnField = new JTextField(15);
    private final JTextField categoryField = new JTextField(15);
    private final JTextField copiesField = new JTextField(5);
    private final JTextField searchField = new JTextField(15);

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"ID", "Title", "Author", "ISBN", "Category", "Total", "Available"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);

    private Integer selectedBookId = null;

    public BookPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BACKGROUND);
        UITheme.styleTable(table);

        add(buildFormPanel(), BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.cardBorder("Book Catalog"));
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
                UITheme.cardBorder("Book Details"),
                BorderFactory.createEmptyBorder(6, 6, 10, 6)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(panel, gbc, row++, "Title:", titleField);
        addFormRow(panel, gbc, row++, "Author:", authorField);
        addFormRow(panel, gbc, row++, "ISBN:", isbnField);
        addFormRow(panel, gbc, row++, "Category:", categoryField);
        addFormRow(panel, gbc, row++, "Total Copies:", copiesField);

        JButton addBtn = UITheme.primaryButton("Add Book");
        JButton updateBtn = UITheme.secondaryButton("Update Selected");
        JButton deleteBtn = UITheme.dangerButton("Delete Selected");
        JButton clearBtn = UITheme.secondaryButton("Clear Form");

        addBtn.addActionListener(e -> addBook());
        updateBtn.addActionListener(e -> updateBook());
        deleteBtn.addActionListener(e -> deleteBook());
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

        searchBtn.addActionListener(e -> searchBooks());
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            refreshTable();
        });

        JLabel searchLabel = new JLabel("Search (title/author/category/ISBN):");
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

    private void addBook() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            if (title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and Author are required.", "Missing info", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int copies = parsePositiveInt(copiesField.getText().trim(), "Total Copies");
            if (copies < 0) return;

            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setIsbn(isbnField.getText().trim());
            book.setCategory(categoryField.getText().trim());
            book.setTotalCopies(copies);

            bookDAO.addBook(book);
            clearForm();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Book added.");
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void updateBook() {
        if (selectedBookId == null) {
            JOptionPane.showMessageDialog(this, "Select a book in the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int copies = parsePositiveInt(copiesField.getText().trim(), "Total Copies");
            if (copies < 0) return;

            Book existing = bookDAO.getBookById(selectedBookId);
            if (existing == null) {
                JOptionPane.showMessageDialog(this, "That book no longer exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int issuedOut = existing.getTotalCopies() - existing.getAvailableCopies();
            int newAvailable = Math.max(0, copies - issuedOut);

            Book book = new Book();
            book.setBookId(selectedBookId);
            book.setTitle(titleField.getText().trim());
            book.setAuthor(authorField.getText().trim());
            book.setIsbn(isbnField.getText().trim());
            book.setCategory(categoryField.getText().trim());
            book.setTotalCopies(copies);
            book.setAvailableCopies(newAvailable);

            bookDAO.updateBook(book);
            clearForm();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Book updated.");
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void deleteBook() {
        if (selectedBookId == null) {
            JOptionPane.showMessageDialog(this, "Select a book in the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this book? This also removes its transaction history.",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            bookDAO.deleteBook(selectedBookId);
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void searchBooks() {
        String keyword = searchField.getText().trim();
        try {
            List<Book> results = keyword.isEmpty() ? bookDAO.getAllBooks() : bookDAO.searchBooks(keyword);
            populateTable(results);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    public void refreshTable() {
        try {
            populateTable(bookDAO.getAllBooks());
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void populateTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getBookId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                    b.getCategory(), b.getTotalCopies(), b.getAvailableCopies()
            });
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        selectedBookId = (Integer) tableModel.getValueAt(row, 0);
        titleField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        authorField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        isbnField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        categoryField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        copiesField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
    }

    private void clearForm() {
        selectedBookId = null;
        titleField.setText("");
        authorField.setText("");
        isbnField.setText("");
        categoryField.setText("");
        copiesField.setText("");
        table.clearSelection();
    }

    private int parsePositiveInt(String text, String fieldName) {
        try {
            int value = Integer.parseInt(text);
            if (value < 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, fieldName + " must be a non-negative whole number.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
