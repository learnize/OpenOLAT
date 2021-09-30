/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.learningpath.obligation;

import java.io.Serializable;

import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 17 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationExceptionalObligation extends AbstractExceptionalObligation implements Serializable {

	private static final long serialVersionUID = 3387070561911204825L;

	private Long organisationKey;
	private transient OrganisationRef organisationRef;

	public OrganisationRef getOrganisationRef() {
		if (organisationKey != null && organisationRef == null) {
			organisationRef = new OrganisationRefImpl(organisationKey);
		}
		return organisationRef;
	}
	
	public void setOrganisationRef(OrganisationRef organisationRef) {
		this.organisationRef = organisationRef;
		if (organisationRef != null) {
			this.organisationKey = organisationRef.getKey();
		} else {
			this.organisationKey = null;
		}
	}

}
