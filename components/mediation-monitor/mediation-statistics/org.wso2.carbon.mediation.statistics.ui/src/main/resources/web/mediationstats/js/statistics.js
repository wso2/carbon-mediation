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

var xslFilepath  ;
var serverDataSet ;
var proxyServiceDataSet ;
var endPointDataSet ;
var sequenceDataSet ;

var showGraphDivHome = false;
var showGraphDivInterval = 0;
var REFRESH_GRAPHS = 70000;
var shouldRefesh = 0;

// Response Times
var graphAvgResponseTimeArrayObj;

function draw(dataSetArray, target) {
    if (dataSetArray == null || dataSetArray.length == null) {
        return;
    }
    $.plot($("#" + target), dataSetArray,
    {
        series: {
               pie: {
                   show: true,
                   label: {
                    show: true,
                    formatter: function(label, series){
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:black;">'+label+' '+Math.round(series.percent)+'%</div>';
                    },
                    background: { opacity: 0 }
                }
               }
           },
            legend: {
                show: false
            }
       });
    return;
}
function drawGraphs() {
    drawServerGraph();
    drawProxyServiceGraph();
    drawEndPointGraph();
    drawSequencesGraph();
    resetGraphData();
}
function drawServerGraph() {
    draw(serverDataSet, "serverGraph");
}
function drawProxyServiceGraph() {
    draw(proxyServiceDataSet, "proxyServiceGraph");
}
function drawEndPointGraph() {
    draw(endPointDataSet, "endPointGraph");
}
function drawSequencesGraph() {
    draw(sequenceDataSet, "sequenceGraph");
}
function fillDataForGraph(valueStr) {
    var values = valueStr.split(";");
    var data = [];
    for (var i = 0; i < values.length - 1; i++) {
        var aValue = values[i].split(",");
        data.push({ label: aValue[0], data: parseFloat(aValue[1])});
    }
    return data;
}
function resetGraphData() {
    serverDataSet = [];
    proxyServiceDataSet = [];
    endPointDataSet = [];
    sequenceDataSet = [];
}
function populateAllGraphs(serverStr, psStr, epStr, seqStr) {

    if (serverStr != "") {
        serverDataSet = fillDataForGraph(serverStr);
    }
    if (psStr != "") {
        proxyServiceDataSet = fillDataForGraph(psStr);
    }
    if (epStr != "") {
        endPointDataSet = fillDataForGraph(epStr);
    }
    if (seqStr != "") {
        sequenceDataSet = fillDataForGraph(seqStr);
    }
}
function isNumeric(sText) {
    var validChars = "0123456789.";
    var isNumber = true;
    var character;
    for (var i = 0; i < sText.length && isNumber; i++) {
        character = sText.charAt(i);
        if (validChars.indexOf(character) == -1) {
            isNumber = false;
        }
    }
    return isNumber;
}

function initResponseTimeGraph(responseTimeXScale) {
    if (responseTimeXScale < 1 || !isNumeric(responseTimeXScale)) {
        return;
    }
    graphAvgResponseTimeArrayObj = new QueueForGraphs(responseTimeXScale);
}