-- ============================================================
-- Library Management System - Database Setup Script
-- Run this once in MySQL (e.g. via MySQL Workbench, or:
--   mysql -u root -p < library_db.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- ---------------- Books ----------------
CREATE TABLE IF NOT EXISTS books (
    book_id           INT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(150) NOT NULL,
    author            VARCHAR(100) NOT NULL,
    isbn              VARCHAR(20)  UNIQUE,
    category          VARCHAR(50),
    total_copies      INT NOT NULL DEFAULT 1,
    available_copies  INT NOT NULL DEFAULT 1
);

-- ---------------- Members ----------------
CREATE TABLE IF NOT EXISTS members (
    member_id         INT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    email             VARCHAR(100) UNIQUE,
    phone             VARCHAR(15),
    address           VARCHAR(200),
    membership_date   DATE NOT NULL DEFAULT (CURRENT_DATE)
);

-- ---------------- Transactions (issue / return records) ----------------
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id    INT AUTO_INCREMENT PRIMARY KEY,
    book_id           INT NOT NULL,
    member_id         INT NOT NULL,
    issue_date        DATE NOT NULL,
    due_date          DATE NOT NULL,
    return_date       DATE NULL,
    status            ENUM('ISSUED', 'RETURNED') NOT NULL DEFAULT 'ISSUED',
    fine              DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (book_id)   REFERENCES books(book_id)     ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

-- ---------------- Sample data (optional, delete if not wanted) ----------------
INSERT INTO books (title, author, isbn, category, total_copies, available_copies) VALUES
('The Pragmatic Programmer', 'Andrew Hunt', '9780135957059', 'Technology', 3, 3),
('Clean Code', 'Robert C. Martin', '9780132350884', 'Technology', 2, 2),
('Introduction to Algorithms', 'Cormen, Leiserson, Rivest, Stein', '9780262046305', 'Computer Science', 2, 2),
('Wings of Fire', 'A. P. J. Abdul Kalam', '9788173711466', 'Biography', 4, 4),
('The Alchemist', 'Paulo Coelho', '9780062315007', 'Fiction', 5, 5);

INSERT INTO members (name, email, phone, address) VALUES
('Aarav Sharma', 'aarav.sharma@example.com', '9876500001', 'Patna, Bihar'),
('Priya Verma', 'priya.verma@example.com', '9876500002', 'Gaya, Bihar'),
('Rohan Gupta', 'rohan.gupta@example.com', '9876500003', 'Noida, UP');
