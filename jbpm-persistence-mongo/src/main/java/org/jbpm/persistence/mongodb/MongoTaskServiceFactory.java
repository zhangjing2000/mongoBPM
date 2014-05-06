/*
 * Copyright 2013 JBoss Inc
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
package org.jbpm.persistence.mongodb;

import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.jbpm.services.task.HumanTaskConfigurator;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.kie.api.runtime.manager.RegisterableItemsFactory;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.manager.TaskServiceFactory;

/**
 * Regular <code>TaskServiceFactory</code> implementation that shall be used for non CDI environments.
 * Creates new <code>TaskService</code> instance for every call to the factory.
 * <code>TaskService</code> instance will be equipped with <code>JbpmJTATransactionManager</code> 
 * for transaction management, this is mandatory as it must participate in already active
 * transaction if such exists.
 */
public class MongoTaskServiceFactory implements TaskServiceFactory {

    private RuntimeEnvironment runtimeEnvironment;
    
    public MongoTaskServiceFactory(RuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }
    @Override
    public TaskService newTaskService() {
    	// all to reuse an already given instance of task service instead of producing new one
    	TaskService providedTaskService = (TaskService) ((SimpleRuntimeEnvironment) runtimeEnvironment)
    													.getEnvironmentTemplate().get("org.kie.api.task.TaskService");
   		return providedTaskService;
    }

    @Override
    public void close() {
        
    }
}
