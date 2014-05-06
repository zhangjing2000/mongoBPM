/**
 * Copyright 2010 JBoss Inc
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

package org.jbpm.persistence.mongodb.task.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.kie.internal.task.api.model.InternalOrganizationalEntity;

public abstract class MongoOrganizationalEntityImpl implements InternalOrganizationalEntity {
    
    private String id;   
    
    public MongoOrganizationalEntityImpl() {
    }
        
    
    public MongoOrganizationalEntityImpl(String id ) {
        this.id = id;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        // id should never be "", given that it's the only field here!
        if( id == null ) { 
            id = "";
        }
        out.writeUTF( id );
    } 
    
    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        id = in.readUTF();
    }      
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof MongoOrganizationalEntityImpl) ) return false;
        MongoOrganizationalEntityImpl other = (MongoOrganizationalEntityImpl) obj;
        if ( id == null ) {
            if ( other.id != null ) return false;
        } else if ( !id.equals( other.id ) ) return false;
        return true;
    }     
    
    public String toString() {
        return "[" + getClass().getSimpleName() + ":'" + id + "']";
    }
}
