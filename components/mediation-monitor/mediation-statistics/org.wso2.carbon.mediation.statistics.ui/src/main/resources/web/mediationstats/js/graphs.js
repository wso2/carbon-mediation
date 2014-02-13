/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

/*This js mainly concern with the data structures related to graphs*/

//////////////////////////////////////////////////////////////////////

function DataSetArray() {
    this.dataSet = new Array();
}
function XTicksArray() {
    this.array = new Array();
}
function getXTicks() {
    return this.array;
}
function getDataSets() {
    return this.dataSet;
}
function addXTicks(name) {
    this.array[this.array.length] = {v:this.array.length,label:name};
}
function addData(value) {     
    this.dataSet[this.dataSet.length] = [this.dataSet.length,value];
}


XTicksArray.prototype.get = getXTicks;
XTicksArray.prototype.add = addXTicks;

DataSetArray.prototype.get = getDataSets;
DataSetArray.prototype.add = addData;


