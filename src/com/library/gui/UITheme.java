package com.library.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Central place for colors, fonts, and small styling helpers so every panel
 * looks consistent instead of relying on raw Swing defaults.
 */
public final class UITheme {

    private UITheme() {
    }

    // Sidebar
    public static final Color SIDEBAR_BG = new Color(0x1E293B);
    public static final Color SIDEBAR_BG_HOVER = new Color(0x334155);
    public static final Color SIDEBAR_SELECTED = new Color(0x4F46E5);
    public static final Color SIDEBAR_TEXT = new Color(0xE2E8F0);
    public static final Color SIDEBAR_TEXT_MUTED = new Color(0x94A3B8);

    // Page
    public static final Color BACKGROUND = new Color(0xF8FAFC);
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color ROW_ALT = new Color(0xF8FAFC);

    // Brand / status colors
    public static final Color ACCENT = new Color(0x4F46E5);
    public static final Color SUCCESS = new Color(0x16A34A);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color WARNING = new Color(0xD97706);
    public static final Color NEUTRAL = new Color(0xE2E8F0);

    public static final Color TEXT_PRIMARY = new Color(0x0F172A);
    public static final Color TEXT_SECONDARY = new Color(0x64748B);

    // Fonts
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION_TITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 12);

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, ACCENT, Color.WHITE);
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DANGER, Color.WHITE);
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, NEUTRAL, TEXT_PRIMARY);
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(FONT_BUTTON);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
    }

    /** A soft rounded card-style border with a bold section title, replacing the dated default TitledBorder look. */
    public static TitledBorder cardBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                title,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                FONT_SECTION_TITLE,
                TEXT_PRIMARY);
    }

    /** Applies consistent row height, header styling, striped rows, and selection colors to a table. */
    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(FONT_BODY);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        JTableHeaderStyler.style(table);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    c.setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    /** Small helper class just to keep the JTableHeader import local to one spot. */
    private static final class JTableHeaderStyler {
        static void style(JTable table) {
            javax.swing.table.JTableHeader header = table.getTableHeader();
            header.setFont(FONT_TABLE_HEADER);
            header.setBackground(new Color(0xF1F5F9));
            header.setForeground(TEXT_SECONDARY);
            header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
            header.setReorderingAllowed(false);
        }
    }
}
