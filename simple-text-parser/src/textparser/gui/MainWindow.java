package textparser.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import textparser.engine.SettingsContainer;

import com.amamchych.Collections.ListUtils;
import com.amamchych.gui.ClipboardUtils;

public class MainWindow extends JFrame {
    public static final String VERSION = "0.4e";
    public static final String WINDOW_TITLE = "Simple Text Parser ";
    public static final String MAIN_PANEL_TITLE = "mainWindowHeaderPanelSize";
    public static final int WINDOW_DEFAULT_WIDTH = 650;
    public static final int WINDOW_DEFAULT_HEIGTH = 500;
    public static final int WINDOW_MIN_WIDTH = 100;//WINDOW_DEFAULT_WIDTH;
    public static final int WINDOW_SINGLE_BUTTONS_PANEL_MAX_WIDTH = WINDOW_DEFAULT_WIDTH;
    public static final int WINDOW_MIN_HEIGTH = 250;
    public static final String[] DEFAULT_REGEXES = {";", "<.*?>",
        "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b"};
    private static final long serialVersionUID = -6814687856283774860L;
    public static final Dimension INPUT_AREA_MINIMUM_SIZE = new Dimension(50, 50);
    public static final Dimension INPUT_AREA_DAFAULT_SIZE = new Dimension(100, 100);
    public static final Dimension OUTPUT_AREA_MINIMUM_SIZE = new Dimension(90, 90);

    public static final int TEXT_TAB_INDEX = 0;
    public static final int LIST_TAB_INDEX = 1;
    public static final int TABLE_TAB_INDEX = 2;

    private JFrame frame; 
    private JTabbedPane jtpOutputTabs;
    private JLabel jlStatusLine;
    private JLabel jlStatusLine2;
    private JTextArea inputText;
    private JTextArea jtaOutputText;
    private JList jlOutputText;
    private JCheckBox jcbSortOutput;
    private JCheckBox jcbWordWrap;
    private JCheckBox jcbHighlightDuplicates;
    private JComboBox jtfParseExpression;
    private JButton jbRemoveDuplicates;
    private ClipboardUtils clipboard;
    private Component mainWindowHeaderPanel;
    private Dimension mainWindowHeaderPanelSize;

    private static SettingsContainer settings = new SettingsContainer("settings.cfg");

    MainWindow() {
        this.setLayout(new BorderLayout()); 
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        this.setSize(WINDOW_DEFAULT_WIDTH, WINDOW_DEFAULT_HEIGTH); 
        this.setTitle(WINDOW_TITLE);
        this.setMinimumSize(new Dimension(WINDOW_MIN_WIDTH, WINDOW_MIN_HEIGTH));
        frame = this;
        settings.get(frame);
        clipboard = new ClipboardUtils();

        //Test area
        Dimension d1234 = new Dimension(12, 20);
        settings.put("A", d1234);
        settings.save();
        Dimension d1111 = new Dimension(0, 0);
        d1111 = settings.getDimension("A");
        System.out.println("CMP::" + d1234);
        System.out.println("CMP::" + d1111);

        mainWindowHeaderPanel = getHeader();
        //settings.get(MAIN_PANEL_TITLE, mainWindowHeaderPanelSize);
        //System.err.println("BOOTmainWindowHeaderPanelSize" + mainWindowHeaderPanelSize);
        add(mainWindowHeaderPanel, BorderLayout.NORTH);
        add(getMainContentPanel(), BorderLayout.CENTER);
        add(getFooter(), BorderLayout.SOUTH);
        this.setVisible(true); 

        addWindowListener(new WindowAdapter(){	
            public void windowClosing(WindowEvent event){
                settings.put(frame);
                settings.put(MAIN_PANEL_TITLE, mainWindowHeaderPanelSize);
                settings.save();
            }
        });

        addComponentListener(new ComponentListener() {
                public void componentResized(ComponentEvent evt) {
                    Dimension size = mainWindowHeaderPanel.getSize();
                    if (size != null && size.height > 0 && size.width > 0) {
                        mainWindowHeaderPanelSize = size;
                    }
                    remove(mainWindowHeaderPanel);
                    mainWindowHeaderPanel = getHeader();
                    add(mainWindowHeaderPanel, BorderLayout.NORTH);
                    System.out.println("componentResized::" + mainWindowHeaderPanelSize);
                }
                @Override
                public void componentMoved(ComponentEvent e) {}
                @Override
                public void componentShown(ComponentEvent e) {}
                @Override
                public void componentHidden(ComponentEvent e) {}
        });
    }

    private void addBigButtonToPanel(Component comp, JPanel pnl) {
        Dimension dim = null;
        if (mainWindowHeaderPanelSize == null) {
            mainWindowHeaderPanelSize = settings.getDimension(MAIN_PANEL_TITLE);
        }
        System.out.println("addBigButtonToPanel]]]" + mainWindowHeaderPanelSize);
        if (mainWindowHeaderPanelSize != null) {
            dim = new Dimension(70, 50);//FIXME calculate size according to panel size.
            comp.setPreferredSize(dim);
            System.err.println("skip::setPreferredSize");
        }

        pnl.add(comp);
    }

    private Component getHeader() {
        JButton jbSaveOutput = new JButton("<html>Save<br>output</html>");
        jbSaveOutput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String outputText = getParsedText("\n");
                if (outputText.length() == 0) {
                    JOptionPane.showMessageDialog(frame, "Output is empty",
                            "File Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                JFileChooser saveResultsToFileWindow = new JFileChooser();
                int result = saveResultsToFileWindow.showSaveDialog(frame);

                if (result == JFileChooser.CANCEL_OPTION) { return; }
                File file = saveResultsToFileWindow.getSelectedFile();
                try {
                   BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                   bw.write(outputText);
                   bw.close();
                } catch (Exception e) {
                   JOptionPane.showMessageDialog(frame, e.getMessage(),
                           "File Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        jbRemoveDuplicates = new JButton("Remove duplicates");
        jbRemoveDuplicates.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                Set<Object> listElements = ListUtils.highlightDuplicates(jlOutputText);
                DefaultListModel model = (DefaultListModel) jlOutputText.getModel();
                cleanOutputs();
                for (Object object : listElements) {
                    appendLineToOutput((String)object, model);
                }
            }
        });

        jcbSortOutput = new JCheckBox("Sort output");
        jcbWordWrap = new JCheckBox("Word wrap");
        jcbWordWrap.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                inputText.setLineWrap(jcbWordWrap.isSelected());
                inputText.setWrapStyleWord(jcbWordWrap.isSelected());
            }
        });

        jcbHighlightDuplicates = new JCheckBox("Highlight duplicates");
        jcbHighlightDuplicates.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                if (jcbHighlightDuplicates.isSelected()) {
                    jtpOutputTabs.setSelectedIndex(LIST_TAB_INDEX);
                    ListUtils.highlightDuplicates(jlOutputText);
                } else {
                    jlOutputText.getSelectionModel().clearSelection();
                }
            }
        });

        JLabel jlRegex = new JLabel("  Regex: ");
        DefaultComboBoxModel parseExpressionComboBoxModel = new DefaultComboBoxModel(DEFAULT_REGEXES);

        jtfParseExpression = new JComboBox(parseExpressionComboBoxModel);//TODO add history of regexes
        jtfParseExpression.setEditable(true);

        JButton jbCleanRegex = new JButton("Clean");
        jbCleanRegex.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                inputText.setText("");
                //jtfParseExpression.setText("");
            }
        });

        final JButton jb = new JButton("..");
        jb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new RegexSelectPopupWindow(jb);
            }
        });

        Box boxButtons2 = Box.createHorizontalBox();
        //boxButtons2.add(jb);//TODO implement this
        boxButtons2.add(jbCleanRegex);

        JPanel jpnlParseExpression = new JPanel(new BorderLayout());
        jpnlParseExpression.add(jlRegex, BorderLayout.WEST);
        jpnlParseExpression.add(jtfParseExpression, BorderLayout.CENTER);
        jpnlParseExpression.add(boxButtons2, BorderLayout.EAST);
        jpnlParseExpression.setBorder(BorderFactory.createEtchedBorder());
        JPanel jpnlControls = new JPanel(new BorderLayout());
        jpnlControls.add(jpnlParseExpression, BorderLayout.NORTH);
        jpnlControls.add(getResizableToolbarPanel(), BorderLayout.CENTER);

        JPanel jpnlMainButtons = new JPanel(new FlowLayout());
        //Box jpnlMainButtons = Box.createHorizontalBox();
        //JPanel jpnlMainButtons = new JPanel(new GridLayout(1,3));
        Component jbParseButton = getParseButton();
        addBigButtonToPanel(jbSaveOutput, jpnlMainButtons);
        addBigButtonToPanel(jbParseButton, jpnlMainButtons);

        JPanel jpnlHeader = new JPanel(new BorderLayout());
        jpnlHeader.add(jpnlControls, BorderLayout.CENTER);
        jpnlHeader.add(jpnlMainButtons, BorderLayout.EAST);

        return jpnlHeader;
    }

    private Component getResizableToolbarPanel() {
        JPanel panel;
        Box boxButtons = Box.createHorizontalBox();
        Box boxButtons2 = Box.createHorizontalBox();

        if (frame.getWidth() >= WINDOW_SINGLE_BUTTONS_PANEL_MAX_WIDTH) {
            boxButtons.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));
            boxButtons.add(jcbWordWrap);
            boxButtons.add(Box.createRigidArea(new Dimension(10, 10)));
            boxButtons.add(jcbSortOutput);
            boxButtons.add(Box.createRigidArea(new Dimension(10, 10)));
            boxButtons.add(jcbHighlightDuplicates);
            boxButtons.add(Box.createRigidArea(new Dimension(10, 10)));
            boxButtons.add(jbRemoveDuplicates);

            panel = new JPanel(new GridLayout(1, 1));
            panel.add(boxButtons);
        } else {
            boxButtons.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));
            boxButtons.add(jcbWordWrap);
            boxButtons.add(Box.createRigidArea(new Dimension(10, 10)));
            boxButtons.add(jcbSortOutput);
            boxButtons2.add(Box.createRigidArea(new Dimension(10, 10)));
            boxButtons2.add(jcbHighlightDuplicates);
            boxButtons2.add(Box.createRigidArea(new Dimension(10, 10)));
            boxButtons2.add(jbRemoveDuplicates);

            panel = new JPanel(new GridLayout(2, 1));
            panel.add(boxButtons);
            panel.add(boxButtons2);
        }
        return panel;
    }

    private Component getFooter() {
        final JTextField jtfSeparator = new JTextField(";");
        jtfSeparator.setMaximumSize(new Dimension(30, 30));

        JButton jbCopyToClipboard = new JButton("Copy to clipboard");
        jbCopyToClipboard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int activeTab = jtpOutputTabs.getSelectedIndex();
                jtpOutputTabs.setSelectedIndex(LIST_TAB_INDEX);
                if (jtfSeparator.getText().length() == 0) {
                    jtfSeparator.setText(";");
                }
                clipboard.setClipboardContents(getParsedText(jtfSeparator.getText()));
                jtpOutputTabs.setSelectedIndex(activeTab);
            }
        });

        jlStatusLine = new JLabel(WINDOW_TITLE + "  ver.: " + VERSION, SwingConstants.LEADING);
        jlStatusLine2 = new JLabel();
        jlStatusLine2.setForeground(Color.blue);
        Box boxFooter = Box.createHorizontalBox();
        boxFooter.add(jtfSeparator);
        boxFooter.add(jbCopyToClipboard);
        boxFooter.add(Box.createRigidArea(new Dimension(4, 4)));
        boxFooter.add(jlStatusLine);
        boxFooter.add(jlStatusLine2);

        return boxFooter;
    }

    private Component getMainContentPanel() {

        inputText = new JTextArea();
      //options:
        inputText.setLineWrap(false);
        inputText.setWrapStyleWord(false);
        //inputText.setFont(new Font("ROMAN_BASELINE", Font.PLAIN, 12));
        JScrollPane sp_inputText = new JScrollPane(inputText);

        jtaOutputText = new JTextArea();
        jtaOutputText.setLineWrap(false);
        jtaOutputText.setWrapStyleWord(false);
        //jtaOutputText.setFont(new Font("ROMAN_BASELINE", Font.PLAIN, 12));
        JScrollPane sp_jtaOutputText = new JScrollPane(jtaOutputText);

        DefaultListModel listModel = new DefaultListModel();
        jlOutputText = new JList(listModel);
        JScrollPane p_jlOutputText = new JScrollPane(jlOutputText);

        jtpOutputTabs = new JTabbedPane();
        jtpOutputTabs.add("Text", sp_jtaOutputText);
        jtpOutputTabs.add("List", p_jlOutputText);
        //jtpOutputTabs.add("Table", new JLabel("Table view in development")); //Temp off

        sp_inputText.setMinimumSize(INPUT_AREA_MINIMUM_SIZE);
        sp_inputText.setPreferredSize(INPUT_AREA_DAFAULT_SIZE);
        jtpOutputTabs.setMinimumSize(OUTPUT_AREA_MINIMUM_SIZE);

        JSplitPane jspMainContent = new JSplitPane( //options:
            JSplitPane.VERTICAL_SPLIT, true, sp_inputText, jtpOutputTabs);

        return jspMainContent;
    }

    private Component getParseButton() {
        JButton jbParse = new JButton("Parse");
        jbParse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                    String text = inputText.getText();
                    String regex = jtfParseExpression.getSelectedItem().toString();
                    if (regex.length() == 0) {
                        regex = ";";
                    }
                    String[] prasedText = text.split(regex);

                    setParsedResults(prasedText);
            }
        });
        return jbParse;
    }

    private String getParsedText(String lineSeperator) {
        StringBuilder parsedText = new StringBuilder();
        if (jtpOutputTabs.getSelectedIndex() == TEXT_TAB_INDEX) {
            return jtaOutputText.getText();
        } else if (jtpOutputTabs.getSelectedIndex() == LIST_TAB_INDEX) {
            DefaultListModel model = (DefaultListModel) jlOutputText.getModel();
            model.trimToSize();

            //Nothing selected, save all
            if (jlOutputText.getSelectedIndices().length == 0) {
                for (Object element : model.toArray()) {
                    parsedText.append(element);
                    parsedText.append(lineSeperator);
                }
            } else { //Some lines selected, save only selection
                int[] selection = jlOutputText.getSelectedIndices();
                for (int i = 0; i < selection.length; i++) {
                    parsedText.append(model.getElementAt(selection[i]));
                    parsedText.append(lineSeperator);
                }
            }
        } else if (jtpOutputTabs.getSelectedIndex() == TABLE_TAB_INDEX) {
            return jtaOutputText.getText(); //TODO not implemented
        }
        return parsedText.toString();
    }

    private void appendLineToOutput(String line, DefaultListModel model) {
        jtaOutputText.append(line);
        jtaOutputText.append("\n");
        model.addElement(line);
    }

    private void cleanOutputs() {
        jtaOutputText.setText("");
        ((DefaultListModel) jlOutputText.getModel()).removeAllElements();
    }

    private void setParsedResults(String[] prasedText) {
        if (jcbSortOutput.isSelected()) {
            Arrays.sort(prasedText);
        }
        DefaultListModel listModel = (DefaultListModel) jlOutputText.getModel();
        cleanOutputs();
        int parsedLinesCount = 0;
        for (String line : prasedText) {
            appendLineToOutput(line, listModel);
            parsedLinesCount++;
        }
        if (jcbHighlightDuplicates.isSelected()) {
            jtpOutputTabs.setSelectedIndex(LIST_TAB_INDEX);
            ListUtils.highlightDuplicates(jlOutputText);
        }
        jlStatusLine.setText("Output: " + parsedLinesCount + " lines. Parsed with regex: ");
        jlStatusLine2.setText(jtfParseExpression.getSelectedItem().toString());
    }

    public static void main(String args[]) {
        for (UIManager.LookAndFeelInfo laf : UIManager
                .getInstalledLookAndFeels()) {
            if ("Nimbus".equals(laf.getName()))//option
                try {
                    UIManager.setLookAndFeel(laf.getClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainWindow();
            }
        });
    }
}
