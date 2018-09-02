/* 
 * Copyright (C) 2018 Clare Taylor
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package CortanaEdgeArtifacts;

import java.awt.Component;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

import org.sleuthkit.autopsy.corecomponentinterfaces.DataContentViewer;
import org.sleuthkit.autopsy.coreutils.StringExtract;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * This is based on the Sample Content Viewer module from
 * http://www.sleuthkit.org/autopsy/docs/api-docs/4.4/classorg_1_1sleuthkit_1_1autopsy_1_1corecomponents_1_1_data_content_viewer_hex.html
 */
public @ServiceProvider(service = DataContentViewer.class, position = 8)
class CortanaContentViewer extends javax.swing.JPanel implements DataContentViewer {

    /**
     * Creates new form CortanaContentViewer
     */
    public CortanaContentViewer() {
        initComponents();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CortanaContentViewer.class, "SampleContentViewer.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 369, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(21, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(243, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;

    // End of variables declaration//GEN-END:variables
    @Override
    public void setNode(Node selectedNode) {
        try {
            String cmpFileName = "";
            Content contentToProcess = null;

            if (selectedNode == null) {   // If no data in the selected node then
                setText("");  //  set the text on the panel to blank
                return; // return
            }

            contentToProcess = getContentToProcess(selectedNode);  //Ensure that the file is available for processing and not the artifact
            cmpFileName = contentToProcess.getName(); // Get the name of the file

            if (contentToProcess == null) { // If no content in the selected node then
                resetComponent(); // reset the panel
                setText(""); //  set the text on the panel to blank
                return; // return
            }
            setText("Finding text");  //  Have something to process so set the Panel Text to something
            // content.getname not working !!!!!
            if (cmpFileName.startsWith("speech_render[") && cmpFileName.endsWith("].htm")) {
                //  If the content is a speech_render[*}.htm file then
                int offset = 0; //  Set the offet for the buffer
                int pageLength = 4096; //  Set the size of the buffer
                byte data[] = new byte[pageLength]; // create a byte array the size of the buffer
                int bytesRead = 0; // Set a variable to return the number of bytes read
                StringExtract stringExtract = new StringExtract(); // Instantiate the StringExtract class to get strings from the buffer
                String extract; // Set a variable to hold the return from the StringExtract extract method

                bytesRead = contentToProcess.read(data, offset, pageLength); // read first 4K of the file
                extract = stringExtract.extract(data, bytesRead, 0).getText(); // execute StringExtract get extract and getText methods

                int startIndexOf = extract.indexOf("<title>") + 7;  // Find the location of the start of the Title Text
                int endIndexOf = extract.indexOf(" - Bing"); // Find the location of the end of the Title Text

                if ((endIndexOf - startIndexOf < 1) || (startIndexOf < 7)) {  // If no text found then
                    setText("No text found"); // set the text on the panel to No text found
                } else {
                    setText(extract.substring(startIndexOf, endIndexOf)); // else set the text to the title 
                }

            }
            if (cmpFileName.startsWith("search[") && cmpFileName.endsWith("].htm")) {  //  If the content is a search[*}.htm file then
                int offset = 0; // Set the offet for the buffer
                int pageLength = 65536; //  Set the size of the buffer
                byte data[] = new byte[pageLength]; // create a byte array the size of the buffer
                int bytesRead = 0; // Set a variable to return the number of bytes read
                StringExtract stringExtract = new StringExtract(); // Instantiate the StringExtract class to get strings from the buffer
                String extract; // Set a variable to hold the return from the StringExtract extract method

                bytesRead = contentToProcess.read(data, offset, pageLength);  // read first 4K of the file
                extract = stringExtract.extract(data, bytesRead, 0).getText(); // execute StringExtract get extract and getText methods

                int startIndexOf = extract.indexOf("<title>") + 7; // Find the location of the start of the Title Text
                int endIndexOf = extract.indexOf(" - Bing"); // Find the location of the end of the Title Text

                if (endIndexOf - startIndexOf < 1) { // If no text found then
                    setText("No text found"); // set the text on the panel to No text found
                } else {
                    setText(extract.substring(startIndexOf, endIndexOf)); // else set the text to the title 
                }
            }

            if (cmpFileName.startsWith("th[") && cmpFileName.endsWith("].dat")) { //  If the content is a th[*}.dat file then

                int offset = 75357; // Set the offet for the buffer
                int pageLength = 1024; //  Set the size of the buffer
                byte data[] = new byte[pageLength]; // create a byte array the size of the buffer
                char aChar[] = new char[pageLength]; // create a char array the size of the buffer
                int bytesRead = 0; // Set a variable to return the number of bytes read
                String extract; // Set a variable to hold the return from the
                boolean found = false; // Flag for when "TAG" is found
                int j = 0; // counter

                bytesRead = contentToProcess.read(data, offset, pageLength); // read 1K of the file from position 75357

                for (int i = 0; i < data.length; i++) {  // loop to work through the byte buffer to look for "TAG"

                    if (data[i] == 84 && data[i + 1] == 65 && data[i + 2] == 71) {  //  If "TAG" has been found in the byte buffer then 
                        while (!found) {  // while "#" is not found then collect the bytes
                            j++; // count 
                            if (data[i + 2 + j] == 35) { //  if "#" is found then
                                i = data.length;  // set the value of i to the length of the buffer to end the for loop
                                found = true;  // the found flag to true to end the while loop
                            } else {
                                aChar[j] = (char) data[i + 2 + j];  // else add the byte to the char array
                            }
                        }
                    }
                }

                extract = new String(aChar);  // cast the char array to a String

                if (extract.isEmpty()) { // If no text found then
                    setText("No Text Found"); // set the text on the panel to No text found
                } else {
                    setText(extract); // else set the text to the extracted string
                }
            }

        } catch (TskCoreException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    // set the text in the lable in the JPanel
    private void setText(String str) {
        jLabel1.setText(str);
    }

    @Override
    public String getTitle() {
        return "Cortana Search";
    }

    @Override
    public String getToolTip() {
        return "Cortana Search";
    }

    @Override
    public DataContentViewer createInstance() {
        return new CortanaContentViewer();
    }

    @Override
    public Component getComponent() {
        // we can do this because this class extends JPanel
        return this;
    }

    @Override
    public void resetComponent() {
        setText(""); //  set the text on the panel to blank
    }

    @Override
    public boolean isSupported(Node node) {

        // string to extract the current file name from content.getUniquePath() as content.getname() no longer gives the file name.
        String cmpFileName = "";
        Content contentToProcess = null;

        contentToProcess = getContentToProcess(node);  //Ensure that the file is available for processing and not the artifact
        cmpFileName = contentToProcess.getName(); // Get the name of the file

        // get a Content datamodel object out of the node
        if (contentToProcess == null) {
            return false; // disable the Cortana Search Tab
        }

        if (cmpFileName.startsWith("th[") && cmpFileName.endsWith("].dat")) {
            setText(""); //  set the text on the panel to blank
            resetComponent(); // reset the panel
            return true; // enable the Cortana Search Tab
        }

        if (cmpFileName.startsWith("search[") && cmpFileName.endsWith("].htm")) {
            setText(""); //  set the text on the panel to blank
            resetComponent();  // reset the panel
            return true; // enable the Cortana Search Tab
        }

        if (cmpFileName.startsWith("speech_render[") && cmpFileName.endsWith("].htm")) {
            setText(""); //  set the text on the panel to blank
            resetComponent();  // reset the panel
            return true; // enable the Cortana Search Tab
        }

        return false; // disable the Cortana Search Tab

    }

    @Override
    public int isPreferred(Node node) {
        // return 8 since this module will operate only on Cortana Files so lower priority
        return 8;
    }

    private Content getContentToProcess(Node node) {

        for (Content content : (node).getLookup().lookupAll(Content.class)) {

            Content bbContent = null;
            if (content instanceof BlackboardArtifact) {
                bbContent = content;
            } else {
                return content;
            }
        }
        return null;
    }

}
