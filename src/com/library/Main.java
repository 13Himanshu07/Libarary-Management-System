package com.library;

import com.library.gui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        boolean flatLafLoaded = trySetFlatLaf();

        if (flatLafLoaded) {
            // FlatLaf can draw its own clean, flat window title bar instead of the OS default.
            JFrame.setDefaultLookAndFeelDecorated(true);
        } else {
            // No FlatLaf jar on the classpath yet - fall back to the JDK's built-in Nimbus,
            // which still looks noticeably better than the plain default Metal look.
            trySetNimbus();
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    /**
     * Tries to activate FlatLaf's light theme. Returns false (without crashing) if the
     * flatlaf jar hasn't been added to lib/ yet, so the app still runs either way.
     */
    private static boolean trySetFlatLaf() {
        try {
            Class<?> flatLightLaf = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            UIManager.setLookAndFeel((LookAndFeel) flatLightLaf.getDeclaredConstructor().newInstance());
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void trySetNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignored) {
            // fall back to whatever the platform default is
        }
    }
}
