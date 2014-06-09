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

import static org.jbpm.persistence.mongodb.task.util.MongoPersistenceUtil.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.model.AccessType;
import org.kie.internal.task.api.model.InternalAttachment;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class MongoAttachmentImpl implements InternalAttachment {
    
    private Long   id = 0L;

    /**
     * Several attachments may have the same name
     */
    private String name;

    /**
     * current "inline" and "URL" are allowed, this is extendable though and others may be added
     */
    private AccessType accessType = AccessType.Unknown;

    /**
     * MIME type
     */
    private String contentType;

    private MongoUserImpl   attachedBy;
    
    private Date   attachedAt;    

    private int    size;    
    
    private long   attachmentContentId;
    
    public MongoAttachmentImpl() {}
    public MongoAttachmentImpl(Attachment attachment) {
    	this.id = attachment.getId();
    	this.name = attachment.getName();
    	this.accessType = null;
    	this.attachedAt = attachment.getAttachedAt();
    	this.attachedBy = convertToUserImpl(attachment.getAttachedBy());
    	this.contentType = attachment.getContentType();
    	this.size = attachment.getSize();
    	this.attachmentContentId = attachment.getAttachmentContentId();
    	if (attachment instanceof InternalAttachment) {
    		this.accessType = ((InternalAttachment)attachment).getAccessType();
    	}
    }
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong( id );
        if( name == null ) { 
            name = "";
        }
        out.writeUTF( name );
        out.writeUTF( accessType.toString() );
        if( contentType == null ) {
          contentType = "";
        }
        out.writeUTF( contentType );
        // There are no guarantees that attachedBy is not null == potential bug
        attachedBy.writeExternal( out );
        long attachedAtTime = 0;
        if( attachedAt != null ) { 
            attachedAtTime = attachedAt.getTime();
        }
        out.writeLong( attachedAtTime );
        out.writeInt( size );
        out.writeLong( attachmentContentId );
    }
    
    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        id = in.readLong();
        name = in.readUTF();
        accessType = AccessType.valueOf( in.readUTF() );
        contentType = in.readUTF();
        attachedBy = new MongoUserImpl();
        attachedBy.readExternal( in );        
        attachedAt = new Date( in.readLong() );
        size = in.readInt( );
        attachmentContentId = in.readLong();
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Date getAttachedAt() {
        return attachedAt;
    }

    public void setAttachedAt(Date attachedAt) {
        this.attachedAt = attachedAt;
    }

    public User getAttachedBy() {
        return attachedBy;
    }

    public void setAttachedBy(User attachedBy) {
        this.attachedBy = convertToUserImpl(attachedBy);
    }    
    
    public int getSize() {
        return size;
    }

    /**
     * Sets the content for this attachment, i.e. the <field>size</field> and the <field>attachmentContentId</field>.
     * @param content attachment content
     */
    public void setContent(Content content) {
        setSize(content.getContent().length);
        setAttachmentContentId(content.getId());
    }

    public void setSize(int size) {
        this.size = size;
    }
        
    public long getAttachmentContentId() {
        return attachmentContentId;
    }

    public void setAttachmentContentId(long contentId) {
        this.attachmentContentId = contentId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessType == null) ? 0 : accessType.hashCode());
        result = prime * result + ((attachedAt == null) ? 0 : attachedAt.hashCode());
        result = prime * result + ((attachedBy == null) ? 0 : attachedBy.hashCode());
        result = prime * result + size;
        result = prime * result + (int) (attachmentContentId ^ (attachmentContentId >>> 32));
        result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof MongoAttachmentImpl) ) return false;
        MongoAttachmentImpl other = (MongoAttachmentImpl) obj;
        if ( accessType == null ) {
            if ( other.accessType != null ) return false;
        } else if ( !accessType.equals( other.accessType ) ) return false;
        if ( attachedAt == null ) {
            if ( other.attachedAt != null ) return false;            
        } else if ( attachedAt.getTime() != other.attachedAt.getTime() ) return false;
        if ( attachedBy == null ) {
            if ( other.attachedBy != null ) return false;
        } else if ( !attachedBy.equals( other.attachedBy ) ) return false;
        if ( size != other.size ) return false;
        if ( attachmentContentId != other.attachmentContentId ) return false;
        if ( contentType == null ) {
            if ( other.contentType != null ) return false;
        } else if ( !contentType.equals( other.contentType ) ) return false;
        if ( name == null ) {
            if ( other.name != null ) return false;
        } else if ( !name.equals( other.name ) ) return false;
        return true;
    }


 
 
    
    

}