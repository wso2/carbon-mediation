/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediation.dependency.mgt;

import org.wso2.carbon.mediation.dependency.mgt.resolvers.*;
import org.wso2.carbon.mediators.router.impl.RouterMediator;
import org.apache.synapse.mediators.builtin.SendMediator;
import org.apache.synapse.mediators.builtin.CalloutMediator;
import org.apache.synapse.mediators.builtin.ValidateMediator;
import org.apache.synapse.mediators.builtin.CacheMediator;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.xquery.XQueryMediator;
import org.apache.synapse.mediators.transform.XSLTMediator;
import org.apache.synapse.mediators.eip.splitter.CloneMediator;
import org.apache.synapse.mediators.eip.splitter.IterateMediator;
import org.apache.synapse.mediators.bsf.ScriptMediator;
import org.apache.synapse.mediators.spring.SpringMediator;
import org.apache.synapse.mediators.throttle.ThrottleMediator;
import org.apache.synapse.Mediator;

import java.util.Map;
import java.util.HashMap;

public class DependencyResolverFactory {

    private static final DependencyResolverFactory INSTANCE = new DependencyResolverFactory();

    private Map<String,DependencyResolver> resolversMap = new HashMap<String,DependencyResolver>();

    private DependencyResolverFactory() {
        resolversMap.put(CacheMediator.class.getName(), new CacheMediatorResolver());
        resolversMap.put(CalloutMediator.class.getName(), new CalloutMediatorResolver());
        resolversMap.put(CloneMediator.class.getName(), new CloneMediatorResolver());
        resolversMap.put(IterateMediator.class.getName(), new IterateMediatorResolver());
        resolversMap.put(RouterMediator.class.getName(), new RouterMediatorResolver());
        resolversMap.put(ScriptMediator.class.getName(), new ScriptMediatorResolver());
        resolversMap.put(SendMediator.class.getName(), new SendMediatorResolver());
        resolversMap.put(SequenceMediator.class.getName(), new SequenceMediatorResolver());
        resolversMap.put(SpringMediator.class.getName(), new SpringMediatorResolver());
        resolversMap.put(ThrottleMediator.class.getName(), new ThrottleMediatorResolver());
        resolversMap.put(ValidateMediator.class.getName(), new ValidateMediatorResolver());
        resolversMap.put(XQueryMediator.class.getName(), new XQueryMediatorResolver());
        resolversMap.put(XSLTMediator.class.getName(), new XSLTMediatorResolver());
    }

    public static DependencyResolverFactory getInstance() {
        return INSTANCE;
    }

    public void addResolver(String className, DependencyResolver resolver) {
        if (!resolversMap.containsKey(className)) {
            resolversMap.put(className, resolver);
        }
    }

    public DependencyResolver getResolver(Mediator m) {
        return resolversMap.get(m.getClass().getName());
    }
}
