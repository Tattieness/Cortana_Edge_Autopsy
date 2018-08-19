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
// http://www.scriptol.com/programming/jna.php
// http://www.rgagnon.com/javadetails/java-read-write-windows-registry-using-jna.html

import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.Blackboard;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;

/**
 *
 * @author Clare
 */
public class EsentReader {

    PointerByReference ptableid = new PointerByReference(); // pointer by reference for Table ID
    PointerByReference p = new PointerByReference();  // temporary pointer by reference
    Pointer pVal; // General Pointer used by esent functions
    Pointer psesidVal; // pointer to the session ID value
    Pointer pdbidVal;  // pointer to the database ID value
    Pointer ptableidVal; // pointer to the Table ID value
    NativeLong cbMax = new NativeLong(); // used by esent functions to set buffer sizes
    NativeLong InfoLevel = new NativeLong(); // used by esent functions to set infomation levels to be returned
    NativeLong jetGrbit = new NativeLong();  // used by esent functions to set flags on functions 
    EsentLibrary.JET_TABLEID ptableidPtr = new EsentLibrary.JET_TABLEID(); // Instantiate the JET_TABLEID object return for the TableId Pointer
    HashMap<String, ArrayList<String>> schema = new HashMap<>(); // Hashmap to hold the schema
    int jetErr = 0; //  jetErr return value
    String szTableName; // String to hold Table name
    String szFilename; //  Variable to hold the passed in Database File Name
    private IngestServices ingestServices = IngestServices.getInstance();  // Instantiate the Ingest Services 
    private Logger logger = ingestServices.getLogger(CortanaEdgeIngestModuleFactory.getModuleName());

    public ArrayList<String> ESErun(String inFile, String runType) throws IOException {
        EsentLibrary.JET_INSTANCE pinstancePtr = new EsentLibrary.JET_INSTANCE();   // Instantiate the JET_INSTANCE object return for the instance Pointer
        EsentLibrary.JET_SESID psesidPtr = new EsentLibrary.JET_SESID(); // Instantiate the JET_SESID object return for the Session ID Pointer
        EsentLibrary.JET_DBID pdbidPtr = new EsentLibrary.JET_DBID();// Instantiate the JET_DBIDID object return for the Database Id Pointer
        PointerByReference pdbid = new PointerByReference(); // pointer by reference to the database ID
        PointerByReference pinstance = new PointerByReference(); // pointer by reference to the instance
        PointerByReference psesid = new PointerByReference(); // pointer by reference to the session ID
        PointerByReference pageSize = new PointerByReference(); // pointer by reference to the Pagesize for the database
        ArrayList<String> outRows = new ArrayList<>(); // return array for the data found
        Pointer pinstanceVal;  // pointer to the Instance value
        String szInstanceName = "";  //  Variable to hold the Instance Name
        String szPathname = Case.getCurrentCase().getCacheDirectory(); //  Variable to hold the Path of the Database 
        szFilename = inFile; //  Variable to hold the passed in Database File Name 

        //  Setting up the parameters required before creating the Instance
        cbMax.setValue(4);  //  Set the return buffer to 4 Bytes
        InfoLevel.setValue(EsentLibrary.JET_DbInfoPageSize);  // Find out the Pagesize of the database to be opened
        jetErr = EsentLibrary.INSTANCE.JetGetDatabaseFileInfo(szFilename, pageSize, cbMax, InfoLevel);  // get the pagesize
        callJetErrorCheck(jetErr, "Getting Instance Parameters", szFilename);  // Handle any Jet Errors

        InfoLevel.setValue(EsentLibrary.JET_paramDatabasePageSize); // Set the page size for the instance
        p.getPointer().setInt(0, pageSize.getPointer().getInt(0)); // Get the value from the pageSize pointer and set a pointer to this value
        pVal = p.getValue(); // get the value from the temp pointer
        jetErr = EsentLibrary.INSTANCE.JetSetSystemParameter(pinstancePtr, psesidPtr, InfoLevel, pVal, ""); // set the pagesize
        callJetErrorCheck(jetErr, "Setting up Instance Parameters", szFilename);  // Handle any Jet Errors

        // Create Instance
        jetErr = EsentLibrary.INSTANCE.JetCreateInstance(pinstance, szFilename);   // Create the Instance
        pinstanceVal = pinstance.getValue(); // Get the value of the Instance
        pinstancePtr.setPointer(pinstance.getPointer()); // Set the pointer of the instance for the JET_INSTANCE Object
        callJetErrorCheck(jetErr, "Creating Instance", szFilename);  // Handle any Jet Errors

        InfoLevel.setValue(EsentLibrary.JET_paramRecovery);  // Set the Recovery mode for the instance
        p.getPointer().setInt(0, 0); // Set Number value to Zero as this is a String input
        pVal = p.getValue(); // Set the pointer for the value
        jetErr = EsentLibrary.INSTANCE.JetSetSystemParameter(pinstancePtr, psesidPtr, InfoLevel, pVal, "Off"); // Set the recovery mode to off
        callJetErrorCheck(jetErr, "Setting up Recovery Off", szFilename);  // Handle any Jet Errors

        jetErr = EsentLibrary.INSTANCE.JetInit(pinstancePtr); // Iniitialize the instance
        callJetErrorCheck(jetErr, "Initialize the Instance", szFilename);  // Handle any Jet Errors

        // Start the Session
        jetErr = EsentLibrary.INSTANCE.JetBeginSession(pinstanceVal, psesid, szInstanceName, szInstanceName); // Start Session
        callJetErrorCheck(jetErr, "Starting the Session", szFilename);  // Handle any Jet Errors

        // Attach the database 
        psesidVal = psesid.getValue();  // Get the pointer to the session ID value
        psesidPtr.setPointer(psesid.getPointer()); // Set the pointer to the session ID value
        jetGrbit.setValue(EsentLibrary.JET_bitDbReadOnly);  // Set the flags to Readonly
        jetErr = EsentLibrary.INSTANCE.JetAttachDatabase(psesidVal, szFilename, jetGrbit);  // Attach the database

        if (jetErr == -550) {  // jetErr -550 is returned the database is internally inconsistent and needs to be repaired
            String command = "esentutl /p -o \"" + szFilename + "\""; // set up the operating system call for the repair

            Process process = Runtime.getRuntime().exec(command, null, new File(szPathname));  // execute operating system call for the repair
            while (process.isAlive()) {
            } // wait while the repair completes

            if (process.exitValue() == 0) {  // if the repair was successful then 
                jetErr = EsentLibrary.INSTANCE.JetAttachDatabase(psesidVal, szFilename, jetGrbit); // Attach the database
            }
        } else // handle all other error codes
        {
            callJetErrorCheck(jetErr, "Attaching the Database", szFilename);  // Handle any Jet Errors
        }

        // Open Database
        jetErr = EsentLibrary.INSTANCE.JetOpenDatabase(psesidVal, szFilename, "", pdbid, jetGrbit);
        callJetErrorCheck(jetErr, "Open the Database", szFilename);  // Handle any Jet Errors

        //     Open Table MSysObjects. This holds the schema for the database
        pdbidVal = pdbid.getValue();  // Get the pointer to the database ID value
        pdbidPtr.setPointer(pdbid.getPointer()); // Get the Pointer to the database ID value
        p.getPointer().setInt(0, 0); // Set Number value to Zero 
        pVal = p.getValue(); // Set the pointer for the value
        szTableName = "MSysObjects";  // Set the table name to the Table that holds the schema
        jetErr = EsentLibrary.INSTANCE.JetOpenTable(psesidVal, pdbidVal, szTableName, pVal, cbMax, jetGrbit, ptableid); // Open the "MSysObjects" Table
        callJetErrorCheck(jetErr, "Opening the table MSysObjects ", szFilename);  // Handle any Jet Errors

        ptableidVal = ptableid.getValue();  // Get the pointer to the table ID value
        ptableidPtr.setPointer(ptableid.getPointer()); // Set the pointer to the table ID value
        cbMax.setValue(EsentLibrary.JET_MoveNext);  // Set value of cbMax to move to next record
        jetGrbit.setValue(0); // Set the flags to zero
        String tableName = null;  // Variable to hold the table name 

        //Populate the columns from MSysObjects
        while (jetErr == 0) {  // While rows are being retrieved continue
            MsObjValues getObjectInfo = new MsObjValues(); // Instantiate the MsObjValues Object to populate the schemea
            getObjectInfo.getColumns(psesidVal, ptableidVal); // execute the MsObjValues method getColumns
            long type = getObjectInfo.typeValue; // Retrieve the type value
            String objName = getObjectInfo.objNameValue; // Retrieve the object name value
            if (type == 1) {  // If type 1 then (this is a table)
                tableName = objName; // save the table name will need it later
                schema.putIfAbsent(tableName, new ArrayList<String>()); // If the tableName is not already in the Hashtable add it in
            }
            if (type == 2) { // If type 2 then (this is a column)
                if (objName != null) { // If the objname is not empty
                    schema.get(tableName).add(objName); // add the column name into the array list associated with the table name
                }
            }
            jetErr = EsentLibrary.INSTANCE.JetMove(psesidVal, ptableidVal, cbMax, jetGrbit);  // Move to next record

        }
        jetErr = EsentLibrary.INSTANCE.JetCloseTable(psesidVal, ptableidVal);  // Close the table
        callJetErrorCheck(jetErr, "Closing the table MSysObjects ", szFilename);  // Handle any Jet Errors

        //  Now that the schema has been collected need to run the data collection based up the the run type selected
        if (runType.equals("EDGE")) { // Run Type Edge
            outRows = edgeHistory();  // Get the Edge Web Browsing History
        }

        if (runType.equals("CORTANA_R")) { // Run Type Cortana
            outRows = cortanaReminders();  // Get the Cortana Reminders
        }

        //  Closedown
        jetGrbit.setValue(0);
        jetErr = EsentLibrary.INSTANCE.JetCloseDatabase(psesidVal, pdbidVal, jetGrbit); // Close the database
        callJetErrorCheck(jetErr, "Closing the Database ", szFilename);  // Handle any Jet Errors
        jetErr = EsentLibrary.INSTANCE.JetDetachDatabase(psesidVal, szFilename); // Detach the database
        callJetErrorCheck(jetErr, "Detaching the Database ", szFilename);  // Handle any Jet Errors
        jetErr = EsentLibrary.INSTANCE.JetEndSession(psesidVal, jetGrbit); // End the session
        callJetErrorCheck(jetErr, "End the Session ", szFilename);  // Handle any Jet Errors
        jetErr = EsentLibrary.INSTANCE.JetTerm(pinstanceVal); // Terminate the instance
        callJetErrorCheck(jetErr, "Terminate the instance ", szFilename);  // Handle any Jet Errors
        return outRows;
    }

    ArrayList<String> getRows(String inTableName) {

        p.getPointer().setInt(0, 0); // Set Number value to Zero 
        pVal = p.getValue(); // get the pointer for the value from the temp pointer
        String row = ""; // Variable to hold the row
        szTableName = inTableName; // Set the table name to the table name passed in 
        ArrayList<String> rows = new ArrayList<>(); // Array to hold the rows to be returned
        ArrayList<String> singleSchema = new ArrayList<>();  // Array to hold the schema of the table    

        jetErr = EsentLibrary.INSTANCE.JetOpenTable(psesidVal, pdbidVal, szTableName, pVal, cbMax, jetGrbit, ptableid);  // Open the table 
        callJetErrorCheck(jetErr, "Opening Table " + szTableName, szFilename);
        ptableidVal = ptableid.getValue(); // Get the pointer to the table ID value
        ptableidPtr.setPointer(ptableid.getPointer()); // Set the pointer to the table ID value

        singleSchema = schema.get(szTableName); // Get the columns from the schema for the table

        if (singleSchema != null) { // If the schema is not empty then

            while (jetErr == 0) {  // While rows are being retrieved continue
                EsentColInfo colInfo = new EsentColInfo(); // Instantiate the EsentColInfo Object to get the column metadata
                EsentColData colData = new EsentColData(); // Instantiate the EsentColData Object to get the column data

                for (String col : singleSchema) { // for each column in the schema 
                    colInfo.getColumns(psesidVal, ptableidVal, col);  // execute the getColumns method for the column on the schema 
                    long coltyp = colInfo.pColValInfo.coltyp.longValue(); // retrieve the Column Type
                    long colbuff = colInfo.pColValInfo.cbMax.longValue(); // retrieve the Column Width
                    long colId = colInfo.pColValInfo.columnid.longValue(); // retrieve the Column Id 

                    colData.getColumnData(psesidVal, ptableidVal, colId, coltyp, colbuff); // execute the getColumndata method for the column on the schema 

                    if (coltyp == 1 || coltyp == 2) {  // if coltyp is a byte then 
                        row = row + " " + col + " Col Type: " + coltyp + " " + colData.byteValue;  // Retrieve the byte value and add to the row
                    }
                    if (coltyp == 6 || coltyp == 7 || coltyp == 8) { // if coltyp is a float then 
                        row = row + " " + col + " Col Type: " + coltyp + " " + colData.floatValue; // Retrieve the float value and add to the row
                    }
                    if (coltyp == 4 || coltyp == 14) { // if coltyp is an int then 
                        row = row + " " + col + " Col Type: " + coltyp + " " + colData.intValue; // Retrieve the in value and add to the row
                    }
                    if (coltyp == 11 || coltyp == 15 || coltyp == 16) { // if coltyp is a long then 
                        row = row + " " + col + " Col Type: " + coltyp + " " + colData.longValue; // Retrieve the long value and add to the row
                    }
                    if (coltyp == 10 || coltyp == 12) { // if coltyp is a string then 
                        row = row + " " + col + " Col Type: " + coltyp + " " + colData.stringValue; // Retrieve the string value and add to the row
                    }
                    if (coltyp == 3) { // if coltyp is a short then 
                        row = row + " " + col + " Col Type: " + coltyp + " " + colData.shortValue; // Retrieve the short value and add to the row
                    }
                }

                if (row != null) {  // If the row has a value then add to rows
                    rows.add(row); // add the row
                }

                row = "";  // Clear down the row before starting processing again
                cbMax.setValue(EsentLibrary.JET_MoveNext); // Set value of cbMax to move to next record
                jetErr = EsentLibrary.INSTANCE.JetMove(psesidVal, ptableidVal, cbMax, jetGrbit); // move to the next record
            }
        }
        jetErr = EsentLibrary.INSTANCE.JetCloseTable(psesidVal, ptableidVal);  // Close the table
        callJetErrorCheck(jetErr, "Closing Table " + szTableName, szFilename);
        return rows; // Return what has been found
    }

    ArrayList<String> edgeHistory() { //  Returns an array of Web History found that relate to Edge or I.E. 11
        ArrayList<String> selectedRows = new ArrayList<>(); // Holds the rows from the database for the required table
        ArrayList<String> historyTables = new ArrayList<>(); // Holds the History Tables collected from WebCacheV01.dat
        ArrayList<String> historyRows = new ArrayList<>(); // Holds the history rows collected from each selected table from WebCacheV01.dat
        ArrayList<String> history = new ArrayList<>(); // Holds the history data collected from WebCacheV01.dat to be returned

        selectedRows = getRows("Containers");  //  This gets the contents of the Containers table from WebCacheV01.dat which is an index table to all of the container tables

        if (!selectedRows.isEmpty()) {  // If the selected rows array is not empty then
            for (String row : selectedRows) {  // for each row in the array then
                if (row.contains("PartitionId") && row.contains("Name")) { //  if the row has the columns "PartitionId" &&  "Name" exist then the row should be processed 
                    if (row.substring(row.indexOf("Name"), row.indexOf("PartitionId")).contains("History")) {  //  if the row has the data on a column containing "History" (first instance)
                        historyTables.add("Container_" + row.substring(26, 28).trim()); //  Add this Container + the number table to the array for processing in the next stage
                    }
                }
            }

            for (String historyTable : historyTables) {  // for each row in the array then
                historyRows = getRows(historyTable); // get the rows for the table
                for (String historyRow : historyRows) {   // for each row in the array then
                    if (historyRow.indexOf("Filename ") - (historyRow.indexOf("Url ") + 16) > 10) { // if the row has the columns "Filename" &&  "Url " exist and more that 10 chars between then the row should be processed 
                        history.add(historyRow.substring(historyRow.indexOf("Url ") + 17, historyRow.indexOf("Filename ")));  // Add the URL to the array to be returned
                        history.add(historyRow.substring(historyRow.indexOf("AccessedTime ") + 26, historyRow.indexOf("AccessedTime ") + 44)); // Add the Accessed Time to the array to be returned
                    }
                }
            }
        }

        return history; // Return what has been found
    }

    ArrayList<String> cortanaReminders() {      //  Returns an array of any reminders found that relate to Cortana

        ArrayList<String> selectedRows = new ArrayList<>();  // Holds the rows from the database for the required table
        ArrayList<String> returnedRows = new ArrayList<>();  // Holds the data collected from CortanaCoreDb.dat to be returned
        int start = 0; // Variable for location of the start of substring
        int end = 0; // Variable for location of the end of substring
        String tempRow = ""; // Variable temporary processing of a row

//        boolean phoneDetails = false;

        selectedRows = getRows("Reminders");   //  Get each row of data for the Reminders Table
        
        if (!selectedRows.isEmpty()) {   // If the selected rows array is not empty then
            for (String selectedRow : selectedRows) {  // for each row in the array then

                start = selectedRow.indexOf("CreationTime");   // find the start of location of the Creation Time
                end = selectedRow.indexOf("LastUpdateTime", start); // find the end of location of the Creation Time

                returnedRows.add(selectedRow.substring(start + 26, end - 1)); //  Add the Creation Time to the array to be returned

                start = selectedRow.indexOf("Title Col Type");   // find the start of location of the Title
                end = selectedRow.indexOf("Text Col Type", start); // find the end of location of the Title

                if ((end == -1) || (end <= start)) {
                    returnedRows.add("No Title");
                } else {
                    returnedRows.add(removeHTMLEncoding(selectedRow.substring(start + 18, end - 1))); //  Add the Title to the array to be returned
                }

                start = selectedRow.indexOf("Text Col Type");   // find the start of location of the Text
                end = selectedRow.indexOf("ToastAction Col Type", start); // find the end of location of the Text

                if ((end == -1) || (end <= start)) {
                    returnedRows.add("No Text");
                } else {
                    returnedRows.add(removeHTMLEncoding(selectedRow.substring(start + 17, end - 1))); //  Add the Text to the array to be returned
                }
               
            }
        }
        
        
        selectedRows = getRows("Attachments");   //  Get each row of data for the Attachments Table

        if (!selectedRows.isEmpty()) {   // If the selected rows array is not empty then
            for (String selectedRow : selectedRows) {  // for each row in the array then

                start = selectedRow.indexOf("CreationTime");   // find the start of location of the Creation Time
                end = selectedRow.indexOf("Flags", start); // find the end of location of the Creation Time

                returnedRows.add(selectedRow.substring(start + 26, end - 1)); //  Add the Creation Time to the array to be returned

                start = selectedRow.indexOf("Title Col Type");   // find the start of location of the Title
                end = selectedRow.indexOf("Uri Col Type", start); // find the end of location of the Title

                if ((end == -1) || (end <= start)) {
                    returnedRows.add("No Title");
                } else {
                    returnedRows.add(removeHTMLEncoding(selectedRow.substring(start + 18, end - 1))); //  Add the Title to the array to be returned
                }

                start = selectedRow.indexOf("Uri Col Type");   // find the start of location of the Uri
                end = selectedRow.indexOf("Description Col Type", start); // find the end of location of the Uri

                if ((end == -1) || (end <= start)) {
                    returnedRows.add("No Uri");
                } else {
                    returnedRows.add(removeHTMLEncoding(selectedRow.substring(start + 17, end - 1))); //  Add the Uri to the array to be returned
                }
               
            }
        }
        return returnedRows; // Return what has been found
    }

    public void callJetErrorCheck(int retJetErr, String operation, String szFilename) {
        if (retJetErr != 0) {
            String msgText = "The " + operation + " for the file " + szFilename + " Failed with Error " + retJetErr + " Goto: https://msdn.microsoft.com/en-us/library/gg269297(v=exchg.10).aspx for more information";
            logger.log(Level.SEVERE, msgText);
            IngestMessage message = IngestMessage.createMessage(
                    IngestMessage.MessageType.INFO,
                    CortanaEdgeIngestModuleFactory.getModuleName(),
                    msgText);
            IngestServices.getInstance().postMessage(message);

        }
    }

    public String removeHTMLEncoding(String inString) {
        String stringTemp = inString;

        stringTemp = stringTemp.replace("%20", " ");
        stringTemp = stringTemp.replace("%22", "\"");
        stringTemp = stringTemp.replace("%2B", "+");
        stringTemp = stringTemp.replace("%2C", ",");
        stringTemp = stringTemp.replace("%3A", ":");
        stringTemp = stringTemp.replace("%5B", "[");
        stringTemp = stringTemp.replace("%5D", "]");
        stringTemp = stringTemp.replace("%7B", "{");
        stringTemp = stringTemp.replace("%7D", "}");

        return stringTemp;
    }

}
