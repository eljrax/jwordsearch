package org.eljrax.jwordsearch;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author Erik Ljungstrom
 */
public class App extends javax.swing.JFrame {

    private Object[][] letters;
    Integer rows;
    Integer columns;
    File wordFile;
    ArrayList<Tuple> coordBuf;
    /* Contains coordinates to be highlighted as well as their colour */
    ArrayList<Tuple> coordinates;

    /* Defaults to painting the background tiles white - then applies the colour indicated by the Tuple object */
    class CustomRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 6703872492730589499L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cellComponent.setBackground(Color.WHITE);

            for (Tuple coordinate : coordinates) {
                if (column == coordinate.getColumn() && row == coordinate.getRow()) {
                    cellComponent.setBackground(coordinate.getColor());
                }
            }

            return cellComponent;
        }
    }

    /* Contains the coordinates of letters found, and colour it should be painted in 
     * The start of a word is always painted green.
    */
    class Tuple {

        private Integer row;
        private Integer column;
        private Color color;

        public Tuple(Integer r, Integer c, Color cl) {
            this.row = r;
            this.column = c;
            this.color = cl;
        }

        public Color getColor() {
            return this.color;
        }

        public Integer getRow() {
            return this.row;
        }

        public Integer getColumn() {
            return this.column;
        }

    }

    /**
     * Creates new form Wibble
     */
    public App() {
        initComponents();
        this.wordToFind.requestFocus();
        this.wordToFind.selectAll();
        this.wordFile = null;
        this.letters = null;
        coordinates = new ArrayList<>();
        coordBuf = new ArrayList<>();

    }
    
    /* Logic for determining whether a given coordinate contains the letter we're looking for, and whether
     * we've found a word etc.
    */
    private Integer handleCharacter(Integer r, Integer c, char ch, Integer matches, Color color) {

        if (ch == this.wordToFind.getText().charAt(matches)) {
     
                coordBuf.add(new Tuple(r, c, matches == 0 ? Color.GREEN : color));
            matches++;
        } else {
     
            if (ch == this.wordToFind.getText().charAt(0)) {
            
                matches = 1;
                coordBuf.clear();
                coordBuf.add(new Tuple(r, c, Color.GREEN));
            } else {
                matches = 0;
                coordBuf.clear();
            }
        }

        /* We've found the word we're looking for, copy the contents of the volatile
         * coordBuf into coordinates to persist and eventually be painted
         */
        if (matches == this.wordToFind.getText().length()) {
            for (Tuple tuple : coordBuf) {
                coordinates.add(tuple);
            }
            matches = 0;
            coordBuf.clear();
        }

        return matches;
    }

    private void findWordDiagonal() {

        char ch;
        Integer c = 0;
        Integer matches = 0;
        coordBuf.clear();

        /* Check up right */
        
        for (Integer slice = 0; slice < rows + columns - 1; slice++) {
            Integer z1 = slice < columns ? 0 : slice - columns + 1;
            Integer z2 = slice < rows ? 0 : slice - rows + 1;
            for (Integer r = slice - z2; r >= z1; r--) {
                c = slice - r;
                matches = handleCharacter(r, c, letters[r][c].toString().charAt(0), matches, Color.GRAY);
            
            }
            coordBuf.clear();
            matches = 0;
        }
        
        matches = 0;
        coordBuf.clear();

        /* Check down left */
        
        for (int slice = 0; slice < rows * 2 - 1; ++slice) {
            
            int z = slice < rows ? 0 : slice - rows + 1;
            for (Integer r = z; r <= slice - z; ++r) {
                c = (slice - r) - 1 < 0 ? 0 : (slice-r)-1;
                c = c > columns - 1 ? columns -1 : c; 
                matches = handleCharacter(r, c, letters[r][c].toString().charAt(0), matches, Color.RED);            
            }
            matches = 0;
            coordBuf.clear();
        }


        coordBuf.clear();
        matches = 0;
        
        /* Check down-right */
        
        for (int slice = 0; slice < rows * 2 - 1; ++slice) {
            
            int z = slice < rows ? 0 : slice - rows + 1;
            for (Integer r = z; r <= slice - z; ++r) {
                c = (rows - 1) - (slice - r) - 1 < 0 ? 0 : (rows - 1) - (slice - r) - 1 ;
                c = c > columns -1 ? columns - 1 : c;
                matches = handleCharacter(r, c, letters[r][c].toString().charAt(0), matches, Color.BLUE);   
            }
            matches = 0;
            coordBuf.clear();
        }

        matches = 0;
        coordBuf.clear();

        /* Check up-left */

        for (Integer slice = 0; slice < rows + columns - 1; slice++) {
            
            Integer z1 = slice < columns ? 0 : slice - columns + 1;
            Integer z2 = slice < rows ? 0 : slice - rows + 1;
            for (Integer r = slice - z2; r >= z1; r--) {
                c = (rows - 1) - (slice - r) - 1 < 0 ? 0 :  (rows - 1) - (slice - r) - 1 ;
                c = c > columns -1 ? columns - 1 : c;
                matches = handleCharacter(r, c, letters[r][c].toString().charAt(0), matches, Color.DARK_GRAY);
            }
            coordBuf.clear();
            matches = 0;
        }
        
        

    }
    


    private void findWordVertical() {
        char ch;
        Integer matches = 0;
        Integer cnt = 0;
        coordBuf.clear();

        /* Left to right */
        for (Integer c = 0; c < columns; c++) {
            for (Integer r = 0; r < rows; r++) {
                matches = handleCharacter(r, c, letters[r][c].toString().charAt(0), matches, Color.ORANGE);
            }
            coordBuf.clear();
            matches = 0;
        }

        coordBuf.clear();
        matches = 0;
        /* Down to up */
        for (Integer c = columns - 1; c > -1; c--) {
            for (Integer r = rows - 1; r > -1; r--) {
                matches = handleCharacter(r, c, letters[r][c].toString().charAt(0), matches, Color.PINK);
            }
            coordBuf.clear();
            matches = 0;
        }

    }

    private void findWordHorizontal() {
        char ch;
        String potentialWord = "";
        Integer matches = 0;
        coordBuf.clear();

        /* Left to right */
        for (Integer r = 0; r < rows; r++) {
            for (Integer c = 0; c < columns; c++) {
                matches = handleCharacter(r, c, letters[r][c].toString().charAt(0), matches, Color.YELLOW);                
            }
            coordBuf.clear();
            matches = 0;
        }

        
        
        coordBuf.clear();
        matches = 0;
        
        /* Right to left */
        
        for (Integer r = 0; r < rows; r++) {
            for (Integer c = columns - 1; c > -1; c--) {
                matches = handleCharacter(r, c, letters[r][c].toString().charAt(0), matches, Color.CYAN);
            }
            matches = 0;
            coordBuf.clear();
        }
    }

    private Integer getRowsInFile(File f) {
        LineNumberReader lnr = null;
        try {
            lnr = new LineNumberReader(new FileReader(f));
            lnr.skip(Long.MAX_VALUE);
            lnr.close();
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return lnr.getLineNumber();
    }

    private Integer getColumnsInFile(File f) {
        Integer ret = 0;
        String buf;
        try {
            BufferedReader r = new BufferedReader(new FileReader(f));
            while ((buf = r.readLine()) != null) {
                if (buf.length() > ret) 
                    ret = buf.length();
                
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        
        return ret;

    }

    private void setupTable() throws java.lang.OutOfMemoryError {
        
        if (rows <= 0) {
            return;
        }
        letters = new Object[rows][columns];

        BufferedReader r;

        Integer column = 0;

        try {
            r = new BufferedReader(new FileReader(wordFile));
            for (Integer row = 0; row < rows; row++) {
                for (char c : r.readLine().toCharArray()) {
                    letters[row][column] = c;
                    column++;
                }
                column = 0;

            }

            grid.setModel(new javax.swing.table.DefaultTableModel(letters, letters[rows - 1]));

            
            for (Integer i = 0; i < grid.getColumnCount(); i++) {
                TableColumn a = grid.getColumnModel().getColumn(i);
                a.setMaxWidth(20);
            }

            gridPane.setColumnHeaderView(null);
            grid.setTableHeader(null);

        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } 
    }

 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        wordFileChooser = new javax.swing.JFileChooser();
        errorDialog = new javax.swing.JOptionPane();
        gridPane = new javax.swing.JScrollPane();
        grid = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        wordToFind = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        checkDiagonal = new javax.swing.JCheckBox();
        checkHorizontal = new javax.swing.JCheckBox();
        checkVertical = new javax.swing.JCheckBox();
        wordFileInput = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        loadButton = new javax.swing.JButton();

        wordFileChooser.setCurrentDirectory(null);
        wordFileChooser.setDialogTitle("");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        grid.setShowGrid(false);
        grid.getTableHeader().setResizingAllowed(false);
        grid.getTableHeader().setReorderingAllowed(false);
        gridPane.setViewportView(grid);

        jLabel1.setText("Word to find");

        wordToFind.setText("WIBBLE");
        wordToFind.setFocusCycleRoot(true);
        wordToFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordToFindActionPerformed(evt);
            }
        });

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        checkDiagonal.setText("Check diagonals");

        checkHorizontal.setText("Check horizontal");

        checkVertical.setText("Check vertical");

        jLabel2.setText("Load word file");

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        loadButton.setText("Load");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gridPane, javax.swing.GroupLayout.PREFERRED_SIZE, 943, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(wordToFind, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkHorizontal)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addGap(42, 42, 42)
                                .addComponent(wordFileInput, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(loadButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkDiagonal)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(checkVertical)))))
                .addContainerGap(41, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(searchButton)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gridPane, javax.swing.GroupLayout.PREFERRED_SIZE, 603, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(wordToFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkDiagonal)
                    .addComponent(checkVertical))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 2, Short.MAX_VALUE)
                        .addComponent(searchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkHorizontal)
                            .addComponent(wordFileInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(browseButton)
                            .addComponent(loadButton))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>                        

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        /* Reset the grid and clear previously found values - if any */
        coordinates.clear();
        for (Integer k = 0; k < grid.getColumnCount(); k++) {
            grid.getColumnModel().getColumn(k).setCellRenderer(new CustomRenderer());
        }
        grid.repaint();

        if (this.wordToFind.getText().isEmpty())
            return;
        
        if (this.checkDiagonal.isSelected())
            findWordDiagonal();
        if (this.checkHorizontal.isSelected())
            findWordHorizontal();
        if (this.checkVertical.isSelected())
            findWordVertical();

        /* Paint the cells in the grid according to contents of coordinates */
        for (Integer k = 0; k < grid.getColumnCount(); k++) {
            grid.getColumnModel().getColumn(k).setCellRenderer(new CustomRenderer());
        }
        grid.repaint();
    }                                            


    private void wordToFindActionPerformed(java.awt.event.ActionEvent evt) {                                           
        this.searchButtonActionPerformed(null);
    }                                          

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
         
        if (this.wordFileChooser.showOpenDialog(this.wordFileChooser) == this.wordFileChooser.APPROVE_OPTION) 
            this.wordFileInput.setText(this.wordFileChooser.getSelectedFile().getAbsolutePath());
       
    }                                            

    private boolean validateFile(){

        if (this.wordFile == null)
            return false;
        
        if (!this.wordFile.exists()){
            JOptionPane.showMessageDialog(rootPane, "File " + wordFile.getName() + " does not exist", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (this.wordFile.length()/1024/1024 > 1){
            JOptionPane.showMessageDialog(rootPane, "File too large - must not be larger than 1M", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!this.wordFile.canRead()){
            JOptionPane.showMessageDialog(rootPane, "File " + wordFile.getName() + " is not readable", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        if (this.wordFileInput.getText().isEmpty()){
            JOptionPane.showMessageDialog(rootPane, "Must specify a valid file name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
                    
        this.wordFile = new File (this.wordFileInput.getText());
        
        if (!validateFile())
            return;

        rows = getRowsInFile(wordFile);
        columns = getColumnsInFile(wordFile);
        
        
        setupTable();
        
        
        
    }                                          

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new App().setVisible(true);
            }
        });

    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox checkDiagonal;
    private javax.swing.JCheckBox checkHorizontal;
    private javax.swing.JCheckBox checkVertical;
    private javax.swing.JOptionPane errorDialog;
    private javax.swing.JTable grid;
    private javax.swing.JScrollPane gridPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton loadButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JFileChooser wordFileChooser;
    private javax.swing.JTextField wordFileInput;
    private javax.swing.JTextField wordToFind;
    // End of variables declaration                   
}
