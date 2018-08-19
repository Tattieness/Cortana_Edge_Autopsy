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
 * Sample ingest module factory in the public domain.  
 * Feel free to use this as a template for your inget module factories.
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

// The following import is required for the ServiceProvider annotation (see 
// below) used by the Autopsy ingest framework to locate ingest module 
// factories. You will need to add a dependency on the Lookup API NetBeans 
// module to your NetBeans module to use this import.
import org.openide.util.lookup.ServiceProvider;

// The following import is required to participate in Autopsy 
// internationalization and localization. Autopsy core is currently localized 
// for Japan. Please consult the NetBeans documentation for details.
import org.openide.util.NbBundle;

import org.sleuthkit.autopsy.ingest.IngestModuleFactory;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.FileIngestModule;
import org.sleuthkit.autopsy.ingest.IngestModuleGlobalSettingsPanel;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettingsPanel;

/**
 * A factory that creates sample data source and file ingest modules.
 * <p>
 * This factory implements an interface that must be implemented by all
 * providers of Autopsy ingest modules. An ingest module factory is used to
 * create instances of a type of data source ingest module, a type of file
 * ingest module, or both.
 * <p>
 * Autopsy will generally use the factory to create several instances of each
 * type of module for each ingest job it performs. Completing an ingest job
 * entails processing a single data source (e.g., a disk image) and all of the
 * files from the data source, including files extracted from archives and any
 * unallocated space (made to look like a series of files). The data source is
 * passed through one or more pipelines of data source ingest modules. The files
 * are passed through one or more pipelines of file ingest modules.
 * <p>
 * Autopsy may use multiple threads to complete an ingest job, but it is
 * guaranteed that there will be no more than one module instance per thread.
 * However, if the module instances must share resources, the modules are
 * responsible for synchronizing access to the shared resources and doing
 * reference counting as required to release those resources correctly. Also,
 * more than one ingest job may be in progress at any given time. This must also
 * be taken into consideration when sharing resources between module instances.
 * <p>
 * An ingest module factory may provide global and per ingest job settings user
 * interface panels. The global settings should apply to all module instances.
 * The per ingest job settings should apply to all module instances working on a
 * particular ingest job. Autopsy supports context-sensitive and persistent per
 * ingest job settings, so per ingest job settings must be serializable.
 * <p>
 To be discovered at runtime by the ingest framework, CortanaEdgeIngestModuleFactory
 implementations must be marked with the following NetBeans Service provider
 annotation (see below).
 <p>
 IMPORTANT TIP: This sample ingest module factory directly implements
 CortanaEdgeIngestModuleFactory. A practical alternative, recommended if you do not need
 to provide implementations of all of the CortanaEdgeIngestModuleFactory methods, is to
 extend the abstract class IngestModuleFactoryAdapter to get default
 implementations of most of the CortanaEdgeIngestModuleFactory methods.
 */
@ServiceProvider(service = IngestModuleFactory.class) // Sample is discarded at runtime 
public class CortanaEdgeIngestModuleFactory implements IngestModuleFactory {

    private static final String VERSION_NUMBER = "1.0.0";
    private static final String moduleName = "Cortana/Edge Artifacts";
    private static final String displayName = "Cortana/Edge Artifacts";
    private static final String description = "Cortana/Edge Artifacts";


    // This class method allows the ingest module instances created by this 
    // factory to use the same display name that is provided to the Autopsy
    // ingest framework by the factory.
    static String getModuleName() {
    //    return NbBundle.getMessage(CortanaEdgeIngestModuleFactory.class, "CortanaEdgeIngestModuleFactory.class");
          return moduleName;
    }

    /**
     * Gets the display name that identifies the family of ingest modules the
     * factory creates. Autopsy uses this string to identify the module in user
     * interface components and log messages. The module name must be unique. so
     * a brief but distinctive name is recommended.
     *
     * @return The module family display name.
     */
    @Override
    public String getModuleDisplayName() {
        return displayName;
    }

    /**
     * Gets a brief, user-friendly description of the family of ingest modules
     * the factory creates. Autopsy uses this string to describe the module in
     * user interface components.
     *
     * @return The module family description.
     */
    @Override
    public String getModuleDescription() {
        return description;
    }

    /**
     * Gets the version number of the family of ingest modules the factory
     * creates.
     *
     * @return The module family version number.
     */
    @Override
    public String getModuleVersionNumber() {
        return VERSION_NUMBER;
    }

    /**
     * Queries the factory to determine if it provides a user interface panel to
     * allow a user to change settings that are used by all instances of the
     * family of ingest modules the factory creates. For example, the Autopsy
     * core hash lookup ingest module factory provides a global settings panel
     * to import and create hash databases. The hash databases are then enabled
     * or disabled per ingest job using an ingest job settings panel. If the
     * module family does not have global settings, the factory may extend
     * IngestModuleFactoryAdapter to get an implementation of this method that
     * returns false.
     *
     * @return True if the factory provides a global settings panel.
     */
    @Override
    public boolean hasGlobalSettingsPanel() {
        return false;
    }

    /**
     * Gets a user interface panel that allows a user to change settings that
     * are used by all instances of the family of ingest modules the factory
     * creates. For example, the Autopsy core hash lookup ingest module factory
     * provides a global settings panel to import and create hash databases. The
     * imported hash databases are then enabled or disabled per ingest job using
     * ingest an ingest job settings panel. If the module family does not have a
     * global settings, the factory may extend IngestModuleFactoryAdapter to get
     * an implementation of this method that throws an
     * UnsupportedOperationException.
     *
     * @return A global settings panel.
     */
    @Override
    public IngestModuleGlobalSettingsPanel getGlobalSettingsPanel() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the default per ingest job settings for instances of the family of
     * ingest modules the factory creates. For example, the Autopsy core hash
     * lookup ingest modules family uses hash databases imported or created
     * using its global settings panel. All of the hash databases are enabled by
     * default for an ingest job. If the module family does not have per ingest
     * job settings, the factory may extend IngestModuleFactoryAdapter to get an
     * implementation of this method that returns an instance of the
     * NoIngestModuleJobSettings class.
     *
     * @return The default ingest job settings.
     */
    @Override
    public IngestModuleIngestJobSettings getDefaultIngestJobSettings() {
        return new CortanaEdgeModuleIngestJobSettings();
    }

    /**
     * Queries the factory to determine if it provides user a interface panel to
     * allow a user to make per ingest job settings for instances of the family
     * of ingest modules the factory creates. For example, the Autopsy core hash
     * lookup ingest module factory provides an ingest job settings panels to
     * enable or disable hash databases per ingest job. If the module family
     * does not have per ingest job settings, the factory may extend
     * IngestModuleFactoryAdapter to get an implementation of this method that
     * returns false.
     *
     * @return True if the factory provides ingest job settings panels.
     */
    @Override
    public boolean hasIngestJobSettingsPanel() {
        return false;
    }

    /**
     * Gets a user interface panel that can be used to set per ingest job
     * settings for instances of the family of ingest modules the factory
     * creates. For example, the core hash lookup ingest module factory provides
     * an ingest job settings panel to enable or disable hash databases per
     * ingest job. If the module family does not have per ingest job settings,
     * the factory may extend IngestModuleFactoryAdapter to get an
     * implementation of this method that throws an
     * UnsupportedOperationException.
     *
     * @param settings Per ingest job settings to initialize the panel.
     *
     * @return An ingest job settings panel.
     */
    @Override
    public IngestModuleIngestJobSettingsPanel getIngestJobSettingsPanel(IngestModuleIngestJobSettings settings) {
        if (!(settings instanceof CortanaEdgeModuleIngestJobSettings)) {
            throw new IllegalArgumentException("Expected settings argument to be instanceof SampleModuleIngestJobSettings");
        }
        return new CortanaEdgeIngestModuleIngestJobSettingsPanel((CortanaEdgeModuleIngestJobSettings) settings);
    }

    /**
     * Queries the factory to determine if it is capable of creating data source
     * ingest modules. If the module family does not include data source ingest
     * modules, the factory may extend IngestModuleFactoryAdapter to get an
     * implementation of this method that returns false.
     *
     * @return True if the factory can create data source ingest modules.
     */
    @Override
    public boolean isDataSourceIngestModuleFactory() {
        return true;
    }

    /**
     * Creates a data source ingest module instance.
     * <p>
     * Autopsy will generally use the factory to several instances of each type
     * of module for each ingest job it performs. Completing an ingest job
     * entails processing a single data source (e.g., a disk image) and all of
     * the files from the data source, including files extracted from archives
     * and any unallocated space (made to look like a series of files). The data
     * source is passed through one or more pipelines of data source ingest
     * modules. The files are passed through one or more pipelines of file
     * ingest modules.
     * <p>
     * The ingest framework may use multiple threads to complete an ingest job,
     * but it is guaranteed that there will be no more than one module instance
     * per thread. However, if the module instances must share resources, the
     * modules are responsible for synchronizing access to the shared resources
     * and doing reference counting as required to release those resources
     * correctly. Also, more than one ingest job may be in progress at any given
     * time. This must also be taken into consideration when sharing resources
     * between module instances. modules.
     * <p>
     * If the module family does not include data source ingest modules, the
     * factory may extend IngestModuleFactoryAdapter to get an implementation of
     * this method that throws an UnsupportedOperationException.
     *
     * @param settings The settings for the ingest job.
     *
     * @return A data source ingest module instance.
     */
    @Override
    public DataSourceIngestModule createDataSourceIngestModule(IngestModuleIngestJobSettings settings) {
        if (!(settings instanceof CortanaEdgeModuleIngestJobSettings)) {
            throw new IllegalArgumentException("Expected settings argument to be instanceof SampleModuleIngestJobSettings");
        }
        return new CortanaEdgeDataSourceIngestModule((CortanaEdgeModuleIngestJobSettings) settings);
    }

    /**
     * Queries the factory to determine if it is capable of creating file ingest
     * modules. If the module family does not include file ingest modules, the
     * factory may extend IngestModuleFactoryAdapter to get an implementation of
     * this method that returns false.
     *
     * @return True if the factory can create file ingest modules.
     */
    @Override
    public boolean isFileIngestModuleFactory() {
        return true;
    }

    /**
     * Creates a file ingest module instance.
     * <p>
     * Autopsy will generally use the factory to several instances of each type
     * of module for each ingest job it performs. Completing an ingest job
     * entails processing a single data source (e.g., a disk image) and all of
     * the files from the data source, including files extracted from archives
     * and any unallocated space (made to look like a series of files). The data
     * source is passed through one or more pipelines of data source ingest
     * modules. The files are passed through one or more pipelines of file
     * ingest modules.
     * <p>
     * The ingest framework may use multiple threads to complete an ingest job,
     * but it is guaranteed that there will be no more than one module instance
     * per thread. However, if the module instances must share resources, the
     * modules are responsible for synchronizing access to the shared resources
     * and doing reference counting as required to release those resources
     * correctly. Also, more than one ingest job may be in progress at any given
     * time. This must also be taken into consideration when sharing resources
     * between module instances. modules.
     * <p>
     * If the module family does not include file ingest modules, the factory
     * may extend IngestModuleFactoryAdapter to get an implementation of this
     * method that throws an UnsupportedOperationException.
     *
     * @param settings The settings for the ingest job.
     *
     * @return A file ingest module instance.
     */
    @Override
    public FileIngestModule createFileIngestModule(IngestModuleIngestJobSettings settings) {
        if (!(settings instanceof CortanaEdgeModuleIngestJobSettings)) {
            throw new IllegalArgumentException("Expected settings argument to be instanceof CortanaEdgeModuleIngestJobSettings");
        }
        return new CortanaEdgeFileIngestModule((CortanaEdgeModuleIngestJobSettings) settings);
    }
}