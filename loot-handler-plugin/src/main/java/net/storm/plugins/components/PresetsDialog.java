package net.storm.plugins.components;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class PresetsDialog extends JDialog {

    private static final String     BASE_DIR                                = System.getProperty("user.home") 
                                                                                                            + File.separator + ".storm2"            + File.separator + "data" 
                                                                                                            + File.separator + "oneirataxia"    + File.separator + "loot_handler";
    private static final URL        ICON_URL                            = PresetsDialog.class.getResource("/net/storm/plugins/resources/icon.png");
    // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private final Dimension windowSize                              = new Dimension(600, 180);
    // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private final Properties props                                          = new Properties();
    // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private final JTextField fileNameField                          = new PromptTextField("Preset name...");
    // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private final DefaultListModel<String> fileListModel    = new DefaultListModel<>();
    // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private final JList<String> fileList                                    = new JList<>(fileListModel);
    // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    private String selectedFileName;
    private JTextArea textArea;
    
    /**
     * 
     * @param owner
     * @param config
     */
    public PresetsDialog(Frame owner, Supplier<String> config) {
        super(owner, "Presets", true);

        setIconImage(Toolkit.getDefaultToolkit().getImage(ICON_URL));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                refreshWindowTitle();
            }
        });
        setMinimumSize(windowSize);
        setSize(windowSize);
        setLocationRelativeTo(owner);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(10, 10));
        
        // Ensure config dir exists
        final File dir = new File(BASE_DIR);
        if (!dir.exists()) dir.mkdirs();

        // Load file names into list
        final File[] files = dir.listFiles((f, name) -> name.endsWith(".ini"));
        if (files != null) {
            for (File file : files)
                fileListModel.addElement(file.getName().replace(".ini", ""));
        }

        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setVisibleRowCount(5);
        fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    loadSelectedFile();
                }
            }
        });
        
        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        final JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(new JScrollPane(fileList), BorderLayout.CENTER);
        listPanel.setBorder(BorderFactory.createTitledBorder("Saved:"));

        final JPanel formPanel = new JPanel();
        formPanel.setLayout(new BorderLayout(0, 0));
        
        final JPanel panel = new JPanel();
        final JLabel label = new JLabel("Name:");
        panel.setLayout(new BorderLayout(0, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(fileNameField, BorderLayout.CENTER);
        formPanel.add(panel, BorderLayout.NORTH);
        
        final JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(listPanel);
        splitPane.setRightComponent(formPanel);
        listPanel.setMinimumSize(new Dimension(200, 0));
        formPanel.setMinimumSize(new Dimension(300, 0));
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        
        // Set textArea to the 'items to sell' config value.
        textArea = new JTextArea(config.get());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true); // wrap complete words
        
        final JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setColumnHeaderView( new JLabel("Config:"));
        formPanel.add(scrollPane, BorderLayout.CENTER);
        
        final JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout(0, 0));
        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        final JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveFile());
        southPanel.add(saveButton);
    }
    
    private void refreshWindowTitle() {
        setTitle("[" + getWidth() + ":" + getHeight() + "] " + "Presets");
    }
    
    /**
     * Executed on "Save" button.
     */
    private void saveFile() {
        
        final String name = fileNameField.getText().trim();
        final String regex = "[\\\\/:*?\"<>|\\s]+";
        
        if (name.isEmpty() || Pattern.compile(regex).matcher(name).find())
            return;
        
        final File file = new File(BASE_DIR, name + ".ini");
        
        // Clean out white space throughout the String.
        props.setProperty("itemNames", textArea.getText().replaceAll("[\\t\\n\\r]", ""));

        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, null);
            // selectedFileName = file.getName().replace(".ini", "");
            dispose();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Executed on JList row's double click.
     */
    private void loadSelectedFile() {
        
        final String selected = fileList.getSelectedValue();
        
        if (selected == null)
            return;
        
        final File file = new File(BASE_DIR, String.format("%s.ini", selected));
        try (FileInputStream fis = new FileInputStream(file)) {
            props.clear();
            props.load(fis);
            textArea.setText(props.getProperty("savedText", ""));
            fileNameField.setText(selected.replace(".ini", ""));
            selectedFileName = selected;
            dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @return Name of the preset that's selected in the JList.
     */
    public String getSelectedFileName() {
        return selectedFileName;
    }
    
    /**
     * 
     * @param presetName
     * @param key - Config keyName
     * @return Preset's specified config value
     */
    public static String loadIniValue(String presetName, String key) {
        
        try {   final Path path = Paths.get(BASE_DIR + String.format(File.separator + "%s.ini", presetName));
            
                    if (path.toFile().exists()) {
                        return Files.lines(path) .map(String::trim) .filter(line -> line.startsWith(key + "="))
                                    .map(line -> line.substring((key + "=").length()).trim()) .findFirst() .orElse(null);
                    }
            
        } catch (IOException e) {
                e.printStackTrace();
        }
        
        return null;
    }
    
}
