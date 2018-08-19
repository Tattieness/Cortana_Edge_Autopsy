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

/*
 * Sample module in the public domain.  Feel free to use this as a template
 * for your modules.
 * 
 *  Contact: Brian Carrier [carrier <at> sleuthkit [dot] org]
 *
 *  This is free and unencumbered software released into the public domain.
 *  
 *  Anyone is free to copy, modify, publish, use, compile, sell, or
 *  distribute this software, either in source code form or as a compiled
 *  binary, for any purpose, commercial or non-commercial, and by any
 *  means.
 *  
 *  In jurisdictions that recognize copyright laws, the author or authors
 *  of this software dedicate any and all copyright interest in the
 *  software to the public domain. We make this dedication for the benefit
 *  of the public at large and to the detriment of our heirs and
 *  successors. We intend this dedication to be an overt act of
 *  relinquishment in perpetuity of all present and future rights to this
 *  software under copyright law.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 *  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE. 
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.openide.util.Exceptions;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.Blackboard;
import org.sleuthkit.autopsy.casemodule.services.Blackboard.BlackboardException;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestModule;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestModuleReferenceCounter;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.autopsy.ingest.ModuleDataEvent;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;
import org.sleuthkit.datamodel.TskDataException;

/**
 * This is based on the Sample file ingest module from
 * http://www.sleuthkit.org/autopsy/docs/api-docs/4.4/_sample_data_source_ingest_module_8java.html
 */
class CortanaEdgeDataSourceIngestModule implements DataSourceIngestModule {

    private static final HashMap<Long, Long> artifactCountsForIngestJobs = new HashMap<>();  // Counter for number of artifacts
    private final boolean skipKnownFiles; // Skip known files
    private IngestJobContext context = null; // Set up the context 
    private static final IngestModuleReferenceCounter refCounter = new IngestModuleReferenceCounter(); // Instantiate the Reference Counter
    private ArrayList<String> edgeArtifacts = new ArrayList<>();  // Create an array list for the Artifacts to be stored
    private ArrayList<String> cortanaArtifacts = new ArrayList<>(); // Create an array list for the Attribures to be stored
    private Collection<BlackboardArtifact> artifacts;  // Instantiate a Collection for the Artifacts to be stored
    private Collection<BlackboardAttribute> attributes; // Instantiate a Collection for the Artifacts to be stored
    private BlackboardArtifact.Type newArt; // Used to check if the Artifact Type "TSK_REMINDER" is required to be set-up
    private BlackboardArtifact.Type artType; // Used to check if the Artifact Type "TSK_REMINDER" is required to be set-up
    int artTypeId; // Used to check if the Artifact Type "TSK_REMINDER" is required to be set-up
    private IngestServices ingestServices = IngestServices.getInstance();  // Instantiate the Ingest Services 
    private Logger logger = ingestServices.getLogger(CortanaEdgeIngestModuleFactory.getModuleName());  // Instantiate the Logging Service

    CortanaEdgeDataSourceIngestModule(CortanaEdgeModuleIngestJobSettings settings) {
        this.skipKnownFiles = settings.skipKnownFiles(); //  Setup the files to skip

    }

    @Override
    public void startUp(IngestJobContext context) throws IngestModuleException {
        logger.log(Level.INFO, "Startup Cortana/Edge Data Ingest Module");  // Log the startup
        this.context = context;
        refCounter.incrementAndGet(context.getJobId()); // Get the JobID for the context

        logger.log(Level.INFO, "Adding new Artifact for Cortana Reminders if required"); // Log the checking for the possible addition of the Artifact Type of "TSK_REMINDER" 
        try {
            artType = Case.getCurrentCase().getSleuthkitCase().getArtifactType("TSK_REMINDER"); // Checking to see if it exists

        } catch (TskCoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (artType == null) { // if no artifact type for "TSK_REMINDER" exists then it needs to be added
            try {
                newArt = addArtifactType("TSK_REMINDER", "Cortana Reminders"); // Adding the Artifact Type
            } catch (Blackboard.BlackboardException ex) {
                Exceptions.printStackTrace(ex);
            }
            artTypeId = newArt.getTypeID(); // Get the Identifier of the new Artifact Type
        } else {
            artTypeId = artType.getTypeID(); // Get the Identifier of the Artifact Type if it already exists
        }
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {

        logger.log(Level.INFO, "Beginning Cortana/Cortana Data Ingest Process");  //Log that the ingest process has began
        progressBar.switchToDeterminate(2);  // Progress Bar

        try {

            FileManager fileManager = Case.getCurrentCase().getServices().getFileManager(); // Start the File Manager process
            List<AbstractFile> edgeFiles = fileManager.findFiles(dataSource, "WebCacheV01.dat");  // Get files for WebCacheV01.dat
            List<AbstractFile> cortanaFiles = fileManager.findFiles(dataSource, "CortanaCoreDb.dat"); // Get files for CortanaCoreDb.dat

            for (AbstractFile edgeFile : edgeFiles) {    // loop for each of the WebCacheV01.dat files found             
                if (!skipKnownFiles || edgeFile.getKnown() != TskData.FileKnown.KNOWN) {
                    File outFile = new File(Case.getCurrentCase().getCacheDirectory() + "\\" + edgeFile.getId() + "_WebCacheV01.dat");  // create a unique file in the cache directory for each file
                    try {
                        ContentUtils.writeToFile(edgeFile, outFile); // write out the contents to the files
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    logger.log(Level.INFO, "Processing {0}_WebCacheV01.dat File ", edgeFile.getId()); // Log the the start of file processing 

                    try {
                        EsentReader ese = new EsentReader();   //  instantiate a new EsentReader object
                        edgeArtifacts = ese.ESErun(Case.getCurrentCase().getCacheDirectory() + "\\" + edgeFile.getId() + "_WebCacheV01.dat", "EDGE");  // run the ESErun method
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    logger.log(Level.INFO, "No of Artifacts: {0} found", edgeArtifacts.size() / 2);  // Log the number of Artifacts found

                    artifacts = new ArrayList<>(); // Create an array list for the Artifacts to be stored
                    attributes = new ArrayList<>(); // Create an array list for the Attribures to be stored

                    for (int i = 0; i < edgeArtifacts.size(); i = i + 2) {  // For loop to work through the Artifacts and add attributes.  Two loops = 1 Artifact
                        long unixTime;  // Varible to hold the unixTime conversion from Filetime
                        unixTime = dateConversion(edgeArtifacts.get(i + 1)); // Convert Filetime to Unixtime
                        String userName;  // Varible to hold the unixTime conversion from Filetime
                        userName = getUserName(edgeFile.getParentPath()); // extract the username
                        attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_USER_NAME, CortanaEdgeIngestModuleFactory.getModuleName(), userName)); // Add User Name Attribute
                        attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_URL, CortanaEdgeIngestModuleFactory.getModuleName(), edgeArtifacts.get(i)));  // Add URL Attribute 
                        attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_ACCESSED, CortanaEdgeIngestModuleFactory.getModuleName(), unixTime)); // Add Time Attribute 
                        attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PROG_NAME, CortanaEdgeIngestModuleFactory.getModuleName(), "EDGE/I.E 11")); // Add Programme Name Attribute 

                        BlackboardArtifact artifact = edgeFile.newArtifact(ARTIFACT_TYPE.TSK_WEB_HISTORY);  // Instantiate the Artifact
                        artifact.addAttributes(attributes); // Add the attributes to the Artifact
                        artifacts.add(artifact); // Add the Artifact to the Blackboard

                        addToBlackboardPostCount(context.getJobId(), 1L); // Add to the Count the Blackboard Post Count
                    }
                }
            }

            for (AbstractFile cortanaFile : cortanaFiles) {  // loop for each of the CortanaCoreDb.dat files found  
                if (!skipKnownFiles || cortanaFile.getKnown() != TskData.FileKnown.KNOWN) {
                    File outFile = new File(Case.getCurrentCase().getCacheDirectory() + "\\" + cortanaFile.getId() + "_CortanaCoreDb.dat"); // create a unique file in the cache directory for each file
                    try {
                        ContentUtils.writeToFile(cortanaFile, outFile); // write out the contents to the files
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);

                    }

                    logger.log(Level.INFO, "Processing {0}_CortanaCoreDb.dat File ", cortanaFile.getId()); // Log the the start of file processing 

                    try {
                        EsentReader ese = new EsentReader();  //  instantiate a new EsentReader object
                        cortanaArtifacts = ese.ESErun(Case.getCurrentCase().getCacheDirectory() + "\\" + cortanaFile.getId() + "_CortanaCoreDb.dat", "CORTANA_R"); // run the ESErun method
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    logger.log(Level.INFO, "No of Artifacts: {0} found", cortanaArtifacts.size() / 3);  // Log the number of Artifacts found

                    artifacts = new ArrayList<>();
                    attributes = new ArrayList<>();

                    for (int i = 0; i < cortanaArtifacts.size(); i = i + 3) {
                        long unixTime; // Varible to hold the unixTime conversion from Filetime
                        String userName;  // Varible to hold the username
                        userName = getUserName(cortanaFile.getParentPath()); // extract the username
                        unixTime = dateConversion(cortanaArtifacts.get(i)); // convert the time
                        attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_USER_NAME, CortanaEdgeIngestModuleFactory.getModuleName(), userName)); // Add User Name Attribute
                        attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_CREATED, CortanaEdgeIngestModuleFactory.getModuleName(), unixTime)); // Add Time Attribute
                        attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_TITLE, CortanaEdgeIngestModuleFactory.getModuleName(), cortanaArtifacts.get(i + 1))); // Add Message Type Attribute
                        attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_TEXT, CortanaEdgeIngestModuleFactory.getModuleName(), cortanaArtifacts.get(i + 2))); // Add Subject Type Attribute
                     //   attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DESCRIPTION, CortanaEdgeIngestModuleFactory.getModuleName(), cortanaArtifacts.get(i + 3))); // Add Device ID Attribute
                     //   attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_TEXT, CortanaEdgeIngestModuleFactory.getModuleName(), cortanaArtifacts.get(i + 4))); // Add Device Model Attribute
                     //   attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_USER_ID, CortanaEdgeIngestModuleFactory.getModuleName(), cortanaArtifacts.get(i + 5))); // Add Device Model Attribute
                        attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PROG_NAME, CortanaEdgeIngestModuleFactory.getModuleName(), "Cortana Reminder")); // Add Programme Name Attribute

                        BlackboardArtifact artifact = cortanaFile.newArtifact(artTypeId); // Instantiate the Artifact
                        artifact.addAttributes(attributes); // Add the attributes to the Artifact
                        artifacts.add(artifact); // Add the Artifact to the Blackboard

                        addToBlackboardPostCount(context.getJobId(), 1L); // Add to the Count the Blackboard Post Count

                    }

                }
            }

            ModuleDataEvent event = new ModuleDataEvent(CortanaEdgeIngestModuleFactory.getModuleName(), BlackboardArtifact.ARTIFACT_TYPE.TSK_WEB_HISTORY, artifacts); // Create a refresh the blackboard event.
            IngestServices.getInstance().fireModuleDataEvent(event); // fire the event

            progressBar.progress(1); // progress

            // check if we were cancelled
            if (context.dataSourceIngestIsCancelled()) {
                return IngestModule.ProcessResult.OK;
            }

            progressBar.progress(1); // progress

            if (context.dataSourceIngestIsCancelled()) {
                return IngestModule.ProcessResult.OK;
            }

            reportBlackboardPostCount(context.getJobId()); // Report the Blackboard Post Count
            return IngestModule.ProcessResult.OK;

        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "File query failed", ex);
            return IngestModule.ProcessResult.ERROR;
        }

    }

    String getUserName(String inFullPath) {
        String userName; // Varible to hold the username being returned
        int startPos; // Start Position
        int endPos; // End Position

        startPos = inFullPath.indexOf("/"); // Location of First /
        startPos = inFullPath.indexOf("/", startPos + 1); // Location of second / username should be after here
        endPos = inFullPath.indexOf("/", startPos + 1); // Location of third / username should end here
        userName = inFullPath.substring(startPos + 1, endPos); // Select the substring containing the Useraname

        return userName; // return the Username
    }

    long dateConversion(String inDate) {
        long unixTime;  // long variable to return 
        unixTime = Long.parseLong(inDate); // Cast string to a long
        unixTime = unixTime / 10000000l; // Divide by 10000000l
        unixTime = unixTime - 11644473600l;  // minus 11644473600l

        return unixTime;
    }

    synchronized static void addToBlackboardPostCount(long ingestJobId, long countToAdd) {
        Long fileCount = artifactCountsForIngestJobs.get(ingestJobId);  // create the file count for the blackboard

        // Ensures that this job has an entry
        if (fileCount == null) {
            fileCount = 0L;
            artifactCountsForIngestJobs.put(ingestJobId, fileCount);
        }

        fileCount += countToAdd; // add one to the count 
        artifactCountsForIngestJobs.put(ingestJobId, fileCount);
    }

    synchronized static void reportBlackboardPostCount(long ingestJobId) {
        Long refCount = refCounter.decrementAndGet(ingestJobId);  // Get the ingest jobId
        if (refCount == 0) {
            Long filesCount = artifactCountsForIngestJobs.remove(ingestJobId); //  get the count and from the ingest job
            String msgText = String.format("Posted %d times to the blackboard", filesCount); // post how many files counted 
            IngestMessage message = IngestMessage.createMessage(
                    IngestMessage.MessageType.INFO,
                    CortanaEdgeIngestModuleFactory.getModuleName(),
                    msgText);
            IngestServices.getInstance().postMessage(message);    // create and post message
        }
    }

    public BlackboardArtifact.Type addArtifactType(String typeName, String displayName) throws BlackboardException {
        try {
            return Case.getCurrentCase().getSleuthkitCase().addBlackboardArtifactType(typeName, displayName); // add a new artifact type
        } catch (TskCoreException | TskDataException ex) {
            throw new BlackboardException("New artifact type could not be added", ex);
        }
    }
    
}
