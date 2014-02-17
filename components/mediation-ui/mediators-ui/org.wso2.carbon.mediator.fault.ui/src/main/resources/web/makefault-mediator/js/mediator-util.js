/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */



function loadFaultCode(soap){

    if(soap == '2'){
        document.getElementById('fault_code_12').style.display = '';
        document.getElementById('reason').style.display = '';
        document.getElementById('node_row').style.display = '';
        document.getElementById('role_row').style.display = '';

        document.getElementById('fault_code_11').style.display = 'none';
        document.getElementById('fault_string_row').style.display = 'none';
        document.getElementById('fault_actor_row').style.display = 'none';

        document.getElementById('fault_actor_table_row').style.display = '';

        }
    if(soap == '1'){
        document.getElementById('fault_code_12').style.display = 'none';
        document.getElementById('reason').style.display = 'none';
        document.getElementById('node_row').style.display = 'none';
        document.getElementById('role_row').style.display = 'none';

        document.getElementById('fault_code_11').style.display = '';
        document.getElementById('fault_string_row').style.display = '';
        document.getElementById('fault_actor_row').style.display = '';
      
        document.getElementById('fault_actor_table_row').style.display = '';
    }
    
    if(soap == '3'){
    	document.getElementById('fault_code_11').style.display = 'none';
        document.getElementById('fault_code_12').style.display = 'none';
 	    document.getElementById('fault_actor_table_row').style.display = 'none';
         
        document.getElementById('node_row').style.display = 'none';
        document.getElementById('role_row').style.display = 'none';
    }
   }

                                                                                            
function changeButton(fault_string){
    if(fault_string == 'value'){
        document.getElementById('nmsp_button').style.display = 'none';
    }
    if(fault_string == 'expression'){
        document.getElementById('nmsp_button').style.display = '';
    }
    if(fault_string == 'detail_value'){
        document.getElementById('detail_nmsp_button').style.display = 'none';
    }
    if(fault_string == 'detail_expression'){
        document.getElementById('detail_nmsp_button').style.display = '';
    }
}

function makefaultMediatorValidate(){
    var faultString = document.getElementById('name_space');
    if(faultString == null || faultString == undefined || faultString.value == "" ||faultString.value == undefined){
        CARBON.showWarningDialog(faulti18n["makefault.blank.fault"]);
        return false;
    }
    return true;
}
