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
import java.util.Collections;
import java.util.List;

import org.jbpm.persistence.mongodb.task.util.CollectionUtils;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class MongoPeopleAssignmentsImpl implements InternalPeopleAssignments {

	private MongoUserImpl taskInitiator;

	private List<OrganizationalEntity> potentialOwners = Collections.emptyList();

	private List<OrganizationalEntity> excludedOwners = Collections.emptyList();

	private List<OrganizationalEntity> taskStakeholders = Collections.emptyList();

	private List<OrganizationalEntity> businessAdministrators = Collections.emptyList();

	private List<OrganizationalEntity> recipients = Collections.emptyList();

	public MongoPeopleAssignmentsImpl() {
	}

	public MongoPeopleAssignmentsImpl(PeopleAssignments peopleAssignments) {
		taskInitiator = new MongoUserImpl(peopleAssignments.getTaskInitiator());
		businessAdministrators = convertToPersistentOrganizationalEntity(peopleAssignments
				.getBusinessAdministrators());
		potentialOwners = convertToPersistentOrganizationalEntity(peopleAssignments
				.getPotentialOwners());
		if (peopleAssignments instanceof InternalPeopleAssignments) {
			InternalPeopleAssignments intPAS = (InternalPeopleAssignments)peopleAssignments;
			excludedOwners = convertToPersistentOrganizationalEntity(intPAS.getExcludedOwners());
			taskStakeholders = convertToPersistentOrganizationalEntity(intPAS.getTaskStakeholders());
			recipients = convertToPersistentOrganizationalEntity(intPAS.getRecipients());
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		if (taskInitiator != null) {
			out.writeBoolean(true);
			taskInitiator.writeExternal(out);
		} else {
			out.writeBoolean(false);
		}
		CollectionUtils.writeOrganizationalEntityList(potentialOwners, out);
		CollectionUtils.writeOrganizationalEntityList(excludedOwners, out);
		CollectionUtils.writeOrganizationalEntityList(taskStakeholders, out);
		CollectionUtils.writeOrganizationalEntityList(businessAdministrators,
				out);
		CollectionUtils.writeOrganizationalEntityList(recipients, out);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		if (in.readBoolean()) {
			taskInitiator = new MongoUserImpl();
			taskInitiator.readExternal(in);
		}
		potentialOwners = CollectionUtils.readOrganizationalEntityList(in);
		excludedOwners = CollectionUtils.readOrganizationalEntityList(in);
		taskStakeholders = CollectionUtils.readOrganizationalEntityList(in);
		businessAdministrators = CollectionUtils
				.readOrganizationalEntityList(in);
		recipients = CollectionUtils.readOrganizationalEntityList(in);
	}

	public User getTaskInitiator() {
		return taskInitiator;
	}

	public void setTaskInitiator(User taskInitiator) {
		this.taskInitiator = convertToUserImpl(taskInitiator);
	}

	public List<OrganizationalEntity> getPotentialOwners() {
		return potentialOwners;
	}

	public void setPotentialOwners(List<OrganizationalEntity> potentialOwners) {
		this.potentialOwners = convertToPersistentOrganizationalEntity(potentialOwners);
	}

	public List<OrganizationalEntity> getExcludedOwners() {
		return excludedOwners;
	}

	public void setExcludedOwners(List<OrganizationalEntity> excludedOwners) {
		this.excludedOwners = convertToPersistentOrganizationalEntity(excludedOwners);
	}

	public List<OrganizationalEntity> getTaskStakeholders() {
		return taskStakeholders;
	}

	public void setTaskStakeholders(List<OrganizationalEntity> taskStakeholders) {
		this.taskStakeholders = convertToPersistentOrganizationalEntity(taskStakeholders);
	}

	public List<OrganizationalEntity> getBusinessAdministrators() {
		return businessAdministrators;
	}

	public void setBusinessAdministrators(
			List<OrganizationalEntity> businessAdministrators) {
		this.businessAdministrators = convertToPersistentOrganizationalEntity(businessAdministrators);
	}

	public List<OrganizationalEntity> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<OrganizationalEntity> recipients) {
		this.recipients = convertToPersistentOrganizationalEntity(recipients);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ CollectionUtils.hashCode(businessAdministrators);
		result = prime * result + CollectionUtils.hashCode(excludedOwners);
		result = prime
				* result
				+ ((potentialOwners == null) ? 0 : CollectionUtils
						.hashCode(potentialOwners));
		result = prime * result + CollectionUtils.hashCode(recipients);
		result = prime * result
				+ ((taskInitiator == null) ? 0 : taskInitiator.hashCode());
		result = prime * result + CollectionUtils.hashCode(taskStakeholders);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MongoPeopleAssignmentsImpl))
			return false;
		MongoPeopleAssignmentsImpl other = (MongoPeopleAssignmentsImpl) obj;

		if (taskInitiator == null) {
			if (other.taskInitiator != null)
				return false;
		} else if (!taskInitiator.equals(other.taskInitiator))
			return false;

		return CollectionUtils.equals(businessAdministrators,
				other.businessAdministrators)
				&& CollectionUtils.equals(excludedOwners, other.excludedOwners)
				&& CollectionUtils.equals(potentialOwners,
						other.potentialOwners)
				&& CollectionUtils.equals(recipients, other.recipients)
				&& CollectionUtils.equals(taskStakeholders,
						other.taskStakeholders);
	}
}
