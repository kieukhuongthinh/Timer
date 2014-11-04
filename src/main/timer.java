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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

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

    private void initUI() {
        createIconTray();
        createGraphicForm();

        pack();

        setTitle("Nhắc nhỡ");
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setAlwaysOnTop(true);

        setUpNotifyDialog("Hết thời gian rồi nhé. Thư giãn đi nhé. Sleep?", "Hết thời gian");

        String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        file = new File("log\\" + timeLog + ".txt");
        file.getParentFile().mkdirs();
        _log = new StringBuilder();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                Date now = new Date();
                _statusbar.setText(now.toString());
                _log.append("END TASK").append(System.lineSeparator()).append(now.toString()).append(": ").append(_descriptionfld.getText());
                savelog();
            }
        });
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> {
            timer t = new timer();
            t.setVisible(true);
        });
    }

    private void createIconTray() {
        _icons = new ArrayList<>();

        try {
            //src/logout_...
            _image16x16 = ImageIO.read(getClass().getResource("/logout_16x16.png"));
            _icons.add(_image16x16);
            _icons.add(ImageIO.read(getClass().getResourceAsStream("/logout_24x24.png")));
            _icons.add(ImageIO.read(getClass().getResourceAsStream("/logout_32x32.png")));
            _icons.add(ImageIO.read(getClass().getResourceAsStream("/logout_64x64.png")));
        } catch (IOException ex) {
            Logger.getLogger(timer.class.getName()).log(Level.SEVERE, null, ex);
        }
        setIconImages(_icons);
        if (SystemTray.isSupported()) {
            _icon = new TrayIcon(_image16x16);
            _icon.setToolTip("Nhắc nhỡ");
            _icon.addActionListener((ActionEvent e) -> {
                setVisible(true);
                setExtendedState(NORMAL);
                SystemTray.getSystemTray().remove(_icon);
            });
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowIconified(WindowEvent e) {
                    setVisible(false);
                    try {
                        SystemTray.getSystemTray().add(_icon);
                    } catch (AWTException e1) {
                    }
                }

            });
        }
    }

    private void createGraphicForm() {

        _inputfld = new JTextField("(s)");
        _descriptionfld = new JTextField("...");
        _startbtn = new JButton("START");
        _statusbar = new JLabel("Start");

        //--------------
        _inputfld.addActionListener((ActionEvent e) -> {
            _startbtn.doClick();
        });
        _inputfld.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(final FocusEvent pE) {
                if (_inputfld.getText().compareTo("") == 0) {
                    _inputfld.setText("(s)");
                }
            }

            @Override
            public void focusGained(final FocusEvent pE) {
                if (_inputfld.getText().compareTo("(s)") == 0) {
                    _inputfld.setText("");
                }
            }
        });

        _inputfld.setAlignmentY(CENTER_ALIGNMENT);
        //--------------

        //--------------
        _descriptionfld.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _startbtn.doClick();
            }
        });
        _descriptionfld.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(final FocusEvent pE) {
                if (_descriptionfld.getText().compareTo("") == 0) {
                    _descriptionfld.setText("...");
                }
            }

            @Override
            public void focusGained(final FocusEvent pE) {
                if (_descriptionfld.getText().compareTo("...") == 0) {
                    _descriptionfld.setText("");
                }
            }
        });
        //--------------

        //--------------
        _startbtn.addActionListener((ActionEvent e) -> {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            String input = _inputfld.getText();
            int after = 0;
            try {
                after = (int) Float.parseFloat(engine.eval(input).toString());
            } catch (ScriptException ex) {
                JOptionPane.showMessageDialog(new JFrame(), "Input lỗi:\n" + ex, "Thông báo", JOptionPane.ERROR_MESSAGE);
            }

            if (after > 0) {
                if (_replayTimer != null) {
                    _replayTimer.stop();
                }
                Date now = new Date();
                _statusbar.setText(now.toString());
                _log.append(now.toString()).append(": ").append(_descriptionfld.getText()).append(System.lineSeparator());
                savelog();
                replay(after);
            }
        });
        _startbtn.setAlignmentX(CENTER_ALIGNMENT);
        //--------------

        //--------------
        _inputfld.setPreferredSize(new Dimension(200, 30));
        _descriptionfld.setPreferredSize(new Dimension(200, 30));
        _startbtn.setPreferredSize(new Dimension(200, 50));
        _statusbar.setPreferredSize(new Dimension(200, 20));
        //--------------

        JPanel fld = new JPanel(new BorderLayout(0, 0));
        fld.add(_inputfld, BorderLayout.NORTH);
        fld.add(_descriptionfld, BorderLayout.SOUTH);

        setLayout(new BorderLayout(0, 0));
        add(fld, BorderLayout.NORTH);
        add(_startbtn, BorderLayout.CENTER);
        add(_statusbar, BorderLayout.SOUTH);
    }

    private void setUpNotifyDialog(String message, String title) {
        _optionPane = new JOptionPane(message);
        _dialog = _optionPane.createDialog(title);
    }

    private void replay(int after) {

        Timer resizeTimer = new Timer(8 * 1000, (ActionEvent e2) -> {
            _dialog.setPreferredSize(new Dimension(_dialog.getWidth() + 50, _dialog.getHeight() + 50));
            //System.out.println(dialog.getWidth() + " : " + dialog.getHeight());
            _dialog.setLocationRelativeTo(null);
            _dialog.setAlwaysOnTop(true);
            _dialog.pack();
        });

        _replayTimer = new Timer(after * 1000, (ActionEvent e) -> {
            _dialog.setPreferredSize(new Dimension(278, 118));
            _dialog.setLocationRelativeTo(null);
            _dialog.setAlwaysOnTop(true);
            _dialog.pack();
            
            resizeTimer.start(); //Increase size of diglog automatic
            _dialog.setVisible(true);
            if (null == _optionPane.getValue()) {
            } else if (((Integer) _optionPane.getValue()) == JOptionPane.OK_OPTION) {
                try {
                    Runtime.getRuntime().exec("rundll32.exe powrprof.dll,SetSuspendState 0,1,0");
                    //Hibernate must be turned off
                } catch (IOException ex) {
                    Logger.getLogger(timer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
            }
            resizeTimer.stop();

        });

        _replayTimer.start();
        _replayTimer.setRepeats(false);
    }

    private void savelog() {
        String output = _log.toString();
        //OutputStreamWriter & FileOutputStream => UTF-8
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.write(output);
        } catch (IOException ex) {
            Logger.getLogger(timer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    private Timer _replayTimer = null;
    private JOptionPane _optionPane;
    private JDialog _dialog;

    private BufferedImage _image16x16 = null;
    private List<Image> _icons;
    private TrayIcon _icon;

    private JTextField _inputfld;
    private JTextField _descriptionfld;
    private JButton _startbtn;
    private JLabel _statusbar;

    private StringBuilder _log;
    private File file;
}
