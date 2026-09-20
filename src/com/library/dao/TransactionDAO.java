package com.library.dao;

import com.library.db.DBConnection;
import com.library.model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    /** Loan period in days, used when no explicit due date is supplied. */
    public static final int DEFAULT_LOAN_DAYS = 14;
    /** Fine charged per day late, in rupees. */
    public static final double FINE_PER_DAY = 5.0;

    private final BookDAO bookDAO = new BookDAO();

    /**
     * Issues a book to a member: creates a transaction row and decrements the
     * book's available copy count. Throws IllegalStateException if no copies
     * are currently available.
     */
    public void issueBook(int bookId, int memberId, LocalDate issueDate, LocalDate dueDate) throws SQLException {
        var book = bookDAO.getBookById(bookId);
        if (book == null) {
            throw new IllegalStateException("Book not found.");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies of this book are currently available.");
        }

        String sql = "INSERT INTO transactions (book_id, member_id, issue_date, due_date, status) "
                + "VALUES (?, ?, ?, ?, 'ISSUED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, memberId);
            ps.setDate(3, Date.valueOf(issueDate));
            ps.setDate(4, Date.valueOf(dueDate));
            ps.executeUpdate();
        }
        bookDAO.adjustAvailableCopies(bookId, -1);
    }

    /**
     * Marks a transaction as returned today, increments the book's available
     * copy count, and calculates/stores a late fine if applicable.
     *
     * @return the fine amount charged (0 if returned on or before the due date)
     */
    public double returnBook(int transactionId) throws SQLException {
        Transaction txn = getTransactionById(transactionId);
        if (txn == null) {
            throw new IllegalStateException("Transaction not found.");
        }
        if ("RETURNED".equals(txn.getStatus())) {
            throw new IllegalStateException("This book has already been returned.");
        }

        LocalDate today = LocalDate.now();
        long daysLate = Math.max(0, ChronoUnit.DAYS.between(txn.getDueDate(), today));
        double fine = daysLate * FINE_PER_DAY;

        String sql = "UPDATE transactions SET return_date = ?, status = 'RETURNED', fine = ? "
                + "WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(today));
            ps.setDouble(2, fine);
            ps.setInt(3, transactionId);
            ps.executeUpdate();
        }
        bookDAO.adjustAvailableCopies(txn.getBookId(), 1);
        return fine;
    }

    public Transaction getTransactionById(int transactionId) throws SQLException {
        String sql = "SELECT t.*, b.title AS book_title, m.name AS member_name "
                + "FROM transactions t "
                + "JOIN books b ON t.book_id = b.book_id "
                + "JOIN members m ON t.member_id = m.member_id "
                + "WHERE t.transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /** All transactions currently out (not yet returned), newest first. */
    public List<Transaction> getActiveIssues() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title AS book_title, m.name AS member_name "
                + "FROM transactions t "
                + "JOIN books b ON t.book_id = b.book_id "
                + "JOIN members m ON t.member_id = m.member_id "
                + "WHERE t.status = 'ISSUED' "
                + "ORDER BY t.due_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Full transaction history, newest first. */
    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title AS book_title, m.name AS member_name "
                + "FROM transactions t "
                + "JOIN books b ON t.book_id = b.book_id "
                + "JOIN members m ON t.member_id = m.member_id "
                + "ORDER BY t.transaction_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Date returnDate = rs.getDate("return_date");
        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("book_id"),
                rs.getString("book_title"),
                rs.getInt("member_id"),
                rs.getString("member_name"),
                rs.getDate("issue_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                returnDate != null ? returnDate.toLocalDate() : null,
                rs.getString("status"),
                rs.getDouble("fine")
        );
    }
}
