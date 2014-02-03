package org.wso2.carbon.mediator.bridge;/*
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */

import org.wso2.carbon.server.CarbonLaunchExtension;
import org.wso2.carbon.server.LauncherConstants;
import org.wso2.carbon.server.util.Utils;

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Create OSGi bundles out of normal Synapse Mediators
 */
public class MediatorBundleCreator implements CarbonLaunchExtension {
    private final static String MEDIATORS_DIR =
            "repository" + File.separator + "components" + File.separator + "mediators";

    public void perform() {
        String carbonHome = System.getProperty("carbon.home");

        if (carbonHome == null) {
            carbonHome = System.getenv("CARBON_HOME");
        }

        if (carbonHome == null || carbonHome.length() == 0) {
            return;
        }
        File carbonHomeFile = new File(carbonHome, MEDIATORS_DIR);
        if (!carbonHomeFile.exists()) {
            carbonHomeFile.mkdirs();
        }

        File dropinsFolder = new File(Utils.getCarbonComponentRepo(), "dropins");

        File[] files = carbonHomeFile.listFiles(new Utils.JarFileFilter());
        if (files != null) {
            for (File file : files) {
                try {
                    Manifest mf = new Manifest();
                    Attributes attribs = mf.getMainAttributes();
                    attribs.putValue(LauncherConstants.FRAGMENT_HOST, "synapse-core");
                    Utils.createBundle(file, dropinsFolder, mf, "synapse.mediator.");
                } catch (Throwable e) {
                    System.err.println("Cannot create mediator bundle from jar file " +
                                       file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }
    }
}
