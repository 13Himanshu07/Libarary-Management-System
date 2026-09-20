package com.library.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {

    private static final String CARD_BOOKS = "BOOKS";
    private static final String CARD_MEMBERS = "MEMBERS";
    private static final String CARD_ISSUE_RETURN = "ISSUE_RETURN";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JLabel sectionTitleLabel = new JLabel();
    private final JLabel sectionSubtitleLabel = new JLabel();

    private final BookPanel bookPanel = new BookPanel();
    private final MemberPanel memberPanel = new MemberPanel();
    private final IssueReturnPanel issueReturnPanel = new IssueReturnPanel();

    private JButton selectedNavButton;

    public MainFrame() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContentArea(), BorderLayout.CENTER);
    }

    // ---------------- Sidebar ----------------

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));

        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(UITheme.SIDEBAR_BG);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(26, 24, 26, 24));
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Library MS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel taglineLabel = new JLabel("Management System");
        taglineLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        taglineLabel.setForeground(UITheme.SIDEBAR_TEXT_MUTED);
        taglineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(titleLabel);
        logoPanel.add(Box.createVerticalStrut(2));
        logoPanel.add(taglineLabel);
        sidebar.add(logoPanel);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x334155));
        sep.setBackground(UITheme.SIDEBAR_BG);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);

        sidebar.add(Box.createVerticalStrut(12));
        JButton booksBtn = createNavButton("Books", "Catalog & inventory", CARD_BOOKS);
        JButton membersBtn = createNavButton("Members", "Registered patrons", CARD_MEMBERS);
        JButton issueBtn = createNavButton("Issue / Return", "Circulation desk", CARD_ISSUE_RETURN);
        sidebar.add(booksBtn);
        sidebar.add(membersBtn);
        sidebar.add(issueBtn);

        sidebar.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("v1.0");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(UITheme.SIDEBAR_TEXT_MUTED);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 24, 16, 24));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        // select "Books" by default now that all buttons exist
        SwingUtilities.invokeLater(() -> selectSection(CARD_BOOKS, booksBtn));

        return sidebar;
    }

    private JButton createNavButton(String label, String subtitle, String cardName) {
        JButton button = new JButton("<html><div style='width:150px'>"
                + "<span style='font-size:13px;font-weight:bold'>" + label + "</span><br>"
                + "<span style='font-size:10px;color:#94A3B8'>" + subtitle + "</span></div></html>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        button.setPreferredSize(new Dimension(220, 56));
        button.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 16));
        button.setForeground(UITheme.SIDEBAR_TEXT);
        button.setBackground(UITheme.SIDEBAR_BG);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != selectedNavButton) {
                    button.setBackground(UITheme.SIDEBAR_BG_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != selectedNavButton) {
                    button.setBackground(UITheme.SIDEBAR_BG);
                }
            }
        });

        button.addActionListener(e -> selectSection(cardName, button));
        return button;
    }

    private void selectSection(String cardName, JButton clicked) {
        cardLayout.show(contentPanel, cardName);

        if (selectedNavButton != null) {
            selectedNavButton.setBackground(UITheme.SIDEBAR_BG);
            selectedNavButton.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 16));
        }
        clicked.setBackground(UITheme.SIDEBAR_SELECTED);
        clicked.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, Color.WHITE),
                BorderFactory.createEmptyBorder(8, 20, 8, 16)));
        selectedNavButton = clicked;

        switch (cardName) {
            case CARD_BOOKS -> {
                sectionTitleLabel.setText("Books");
                sectionSubtitleLabel.setText("Manage your library's catalog and copies");
                bookPanel.refreshTable();
            }
            case CARD_MEMBERS -> {
                sectionTitleLabel.setText("Members");
                sectionSubtitleLabel.setText("View and manage registered members");
                memberPanel.refreshTable();
            }
            case CARD_ISSUE_RETURN -> {
                sectionTitleLabel.setText("Issue / Return");
                sectionSubtitleLabel.setText("Circulation desk \u2014 lend and receive books");
                issueReturnPanel.refreshAll();
            }
            default -> { /* no-op */ }
        }
    }

    // ---------------- Content area ----------------

    private JPanel buildContentArea() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BACKGROUND);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)));

        sectionTitleLabel.setFont(UITheme.FONT_HEADER);
        sectionTitleLabel.setForeground(UITheme.TEXT_PRIMARY);
        sectionTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sectionSubtitleLabel.setFont(UITheme.FONT_SUBHEADER);
        sectionSubtitleLabel.setForeground(UITheme.TEXT_SECONDARY);
        sectionSubtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(sectionTitleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(sectionSubtitleLabel);
        wrapper.add(header, BorderLayout.NORTH);

        contentPanel.setBackground(UITheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        contentPanel.add(bookPanel, CARD_BOOKS);
        contentPanel.add(memberPanel, CARD_MEMBERS);
        contentPanel.add(issueReturnPanel, CARD_ISSUE_RETURN);
        wrapper.add(contentPanel, BorderLayout.CENTER);

        return wrapper;
    }
}
