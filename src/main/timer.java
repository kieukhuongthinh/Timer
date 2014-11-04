package main;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import static java.awt.Frame.NORMAL;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import java.awt.Image;
import java.awt.SystemTray;

import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.script.ScriptEngineManager;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.JDialog;
import javax.swing.JLabel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Khuong Thinh
 */
public class timer extends JFrame {

    public timer() {
        initUI();
    }

    Timer t = null;
    TrayIcon icon;
    JOptionPane optionPane = new JOptionPane("Hết thời gian rồi nhé. Thư giãn đi nhé. Sleep?");
    JDialog dialog = optionPane.createDialog("Hết thời gian");

    private void initUI() {

        BufferedImage image16x16 = null;
        List<Image> icons = new ArrayList<>();
        try {
            //src/logout_...
            image16x16 = ImageIO.read(getClass().getResource("/logout_16x16.png"));
            icons.add(image16x16);
            icons.add(ImageIO.read(getClass().getResourceAsStream("/logout_24x24.png")));
            icons.add(ImageIO.read(getClass().getResourceAsStream("/logout_32x32.png")));
            icons.add(ImageIO.read(getClass().getResourceAsStream("/logout_64x64.png")));
        } catch (IOException ex) {
            Logger.getLogger(timer.class.getName()).log(Level.SEVERE, null, ex);
        }
        setIconImages(icons);
        if (SystemTray.isSupported()) {
            icon = new TrayIcon(image16x16);
            icon.setToolTip("Nhắc nhỡ");
            icon.addActionListener((ActionEvent e) -> {
                setVisible(true);
                setExtendedState(NORMAL);
                SystemTray.getSystemTray().remove(icon);
            });
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowIconified(WindowEvent e) {
                    setVisible(false);
                    try {
                        SystemTray.getSystemTray().add(icon);
                    } catch (AWTException e1) {
                    }
                }

            });
        }

        JTextField inputfld = new JTextField("(s)");
        JButton startbtn = new JButton("START");
        JLabel statusbar = new JLabel("Start");

        inputfld.addActionListener((ActionEvent e) -> {
            startbtn.doClick();
        });

        inputfld.setAlignmentY(CENTER_ALIGNMENT);

        startbtn.addActionListener((ActionEvent e) -> {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            String input = inputfld.getText();
            int after = 0;
            try {
                after = (int) Float.parseFloat(engine.eval(input).toString());
            } catch (ScriptException ex) {
                JOptionPane.showMessageDialog(new JFrame(), "Input lỗi" + ex, "Thông báo", JOptionPane.ERROR_MESSAGE);
            }

            if (after > 0) {
                if (t != null) {
                    t.stop();
                }
                Date now = new Date();
                statusbar.setText(now.toString());
                replay(after);

            }
        });
        startbtn.setAlignmentX(CENTER_ALIGNMENT);

        inputfld.setPreferredSize(new Dimension(200, 30));
        startbtn.setPreferredSize(new Dimension(200, 50));
        statusbar.setPreferredSize(new Dimension(200, 20));

        setLayout(new BorderLayout(0, 0));
        add(inputfld, BorderLayout.NORTH);
        add(startbtn, BorderLayout.CENTER);
        add(statusbar, BorderLayout.SOUTH);

        pack();

        dialog.setAlwaysOnTop(true);

        setTitle("Nhắc nhỡ");
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
    }

    private void replay(int after) {
        t = new Timer(after * 1000, (ActionEvent e) -> {
            dialog.setVisible(true);
            t.stop();
            if (null == optionPane.getValue()) {
            } else if (((Integer) optionPane.getValue()) == JOptionPane.OK_OPTION) {
                try {
                    Runtime.getRuntime().exec("rundll32.exe powrprof.dll,SetSuspendState 0,1,0");
                    //Hibernate must be turned off
                } catch (IOException ex) {
                    Logger.getLogger(timer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {}
        });
        t.start();
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> {
            timer t = new timer();
            t.setVisible(true);
        });
    }
}
