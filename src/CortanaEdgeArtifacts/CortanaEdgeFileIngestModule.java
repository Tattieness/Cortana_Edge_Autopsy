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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import org.openide.util.Exceptions;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.Blackboard.BlackboardException;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.FileIngestModule;
import org.sleuthkit.autopsy.ingest.IngestModule;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.autopsy.ingest.ModuleDataEvent;
import org.sleuthkit.autopsy.ingest.IngestModuleReferenceCounter;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskDataException;

/**
 * This is based on the Sample file ingest module from
 * http://www.sleuthkit.org/autopsy/docs/api-docs/4.4/_sample_file_ingest_module_8java_source.html
 */
class CortanaEdgeFileIngestModule implements FileIngestModule {

    private static final HashMap<Long, Long> artifactCountsForIngestJobs = new HashMap<>();  // Counter for number of artifacts
    private final boolean skipKnownFiles; // Skip known files
    private IngestJobContext context = null; // Set up the context 
    private static final IngestModuleReferenceCounter refCounter = new IngestModuleReferenceCounter(); // Instantiate the Reference Counter
    private BlackboardArtifact.Type newArt; // Used to check if the Artifact Type "TSK_CORTANA" is required to be set-up
    private BlackboardArtifact.Type artType; // Used to check if the Artifact Type "TSK_CORTANA" is required to be set-up
    private int artTypeId; // Used to check if the Artifact Type "TSK_CORTANA" is required to be set-up
    private IngestServices ingestServices = IngestServices.getInstance();  // Instantiate the Ingest Services 
    private Logger logger = ingestServices.getLogger(CortanaEdgeIngestModuleFactory.getModuleName());  // Instantiate the Logging Service
    private boolean firstRun = true;

    CortanaEdgeFileIngestModule(CortanaEdgeModuleIngestJobSettings settings) {
        this.skipKnownFiles = settings.skipKnownFiles();  //  Setup the files to skip
    }

    @Override
    public void startUp(IngestJobContext context) throws IngestModuleException {

        this.context = context; // Set the context
        refCounter.incrementAndGet(context.getJobId()); // Get the JobID for the context

        if (firstRun) {
            logger.log(Level.INFO, "Startup Cortana File Ingest Module");  // Log the startup
            logger.log(Level.INFO, "Adding new Artifact for Cortana Files if required"); // Log the checking for the possible addition of the Artifact Type of "TSK_CORTANA" 
            try {
                artType = Case.getCurrentCase().getSleuthkitCase().getArtifactType("TSK_CORTANA"); // Checking to see if it exists

            } catch (TskCoreException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (artType == null) {  // if no artifact type for "TSK_CORTANA" exists then it needs to be added
                try {
                    newArt = addArtifactType("TSK_CORTANA", "Cortana Files"); // Adding the Artifact Type
                } catch (BlackboardException ex) {
                    Exceptions.printStackTrace(ex);
                }
                artTypeId = newArt.getTypeID();  // Get the Identifier of the new Artifact Type
            } else {
                artTypeId = artType.getTypeID(); // Get the Identifier of the Artifact Type if it already exists
            }
        }

    }

    @Override
    public IngestModule.ProcessResult process(AbstractFile file) {

        try {
            Collection<BlackboardArtifact> artifacts = new ArrayList<>();  // Create an array list for the Artifacts to be stored
            Collection<BlackboardAttribute> attributes = new ArrayList<>(); // Create an array list for the Attribures to be stored

            // Check for Cortana Arifact
            if (file.getName().startsWith("SpeechAudio") // SpeechAudio Files  
                    || file.getName().startsWith("speech_render") // speech_render Files 
                    || (file.getName().startsWith("th[") && file.getName().endsWith(".dat")) // th[*].dat Files 
                    || (file.getName().startsWith("search") && file.getName().endsWith(".htm"))) {  // search[*].htm Files 

                if (firstRun) {
                    logger.log(Level.INFO, "Beginning Cortana File Ingest Process " + file.getName());  //Log that the ingest process has began
                }
                String userName;  // Varible to hold the unixTime conversion from Filetime
                userName = getUserName(file.getParentPath()); // extract the username
                attributes.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_USER_NAME, CortanaEdgeIngestModuleFactory.getModuleName(), userName)); // Add User Name Attribute
                attributes.add(new BlackboardAttribute(ATTRIBUTE_TYPE.TSK_NAME, CortanaEdgeIngestModuleFactory.getModuleName(), file.getName()));   // add name of the file
                attributes.add(new BlackboardAttribute(ATTRIBUTE_TYPE.TSK_PATH, CortanaEdgeIngestModuleFactory.getModuleName(), file.getParentPath())); // add path of the file
                attributes.add(new BlackboardAttribute(ATTRIBUTE_TYPE.TSK_DATETIME_ACCESSED, CortanaEdgeIngestModuleFactory.getModuleName(), file.getAtime())); // add access time of the file

                BlackboardArtifact artifact = file.newArtifact(artTypeId);  // Instantiate the Artifact
                artifact.addAttributes(attributes); // Add the attributes to the Artifact
                artifacts.add(artifact); // Add the Artifact to the Blackboard

                addToBlackboardPostCount(context.getJobId(), 1L); // Add to the Count the Blackboard Post Count
                ModuleDataEvent event = new ModuleDataEvent(CortanaEdgeIngestModuleFactory.getModuleName(), ARTIFACT_TYPE.TSK_INTERESTING_FILE_HIT, artifacts);  // Create a refresh the blackboard event.  Have to use a different Artifact Type as the one created is not available
                IngestServices.getInstance().fireModuleDataEvent(event);  // fire the event

            }
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "File query failed", ex);  // Log that there was a Severe exception
            return IngestModule.ProcessResult.ERROR;
        }

        return IngestModule.ProcessResult.OK;
    }

    @Override
    public void shutDown() {
        // This method is thread-safe with per ingest job reference counted
        // management of shared data.
        reportBlackboardPostCount(context.getJobId()); // Report the Blackboard Post Count
        if (firstRun) {
            logger.log(Level.INFO, "Completed Cortana File Ingest Process "); //Log that the ingest process has completed
        }
        firstRun = false;
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
            return Case.getCurrentCase().getSleuthkitCase().addBlackboardArtifactType(typeName, displayName);  // add a new artifact type
        } catch (TskCoreException | TskDataException ex) {
            throw new BlackboardException("New artifact type could not be added", ex);
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

}
