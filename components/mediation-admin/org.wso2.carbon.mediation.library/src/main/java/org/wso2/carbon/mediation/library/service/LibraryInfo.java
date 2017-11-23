/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediation.library.service;

public class LibraryInfo {

    private String libName;
    private String packageName;
    private String description;
    private LibraryArtifiactInfo[] artifacts;
    private boolean status;

    private String qName;

    public String getLibName() {
	return libName;
    }

    public void setLibName(String libName) {
	this.libName = libName;
    }

    public String getPackageName() {
	return packageName;
    }

    public void setPackageName(String packageName) {
	this.packageName = packageName;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public void setQName(String qName) {
        this.qName = qName;
    }

    public String getQName() {
        return this.qName;
    }

    public LibraryArtifiactInfo[] getArtifacts() {
	return artifacts;
    }

    public void setArtifacts(LibraryArtifiactInfo[] artifacts) {
	this.artifacts = artifacts;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
    
    
}
