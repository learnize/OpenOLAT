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

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 20 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementExceptionalObligationHandlerTest {

	@Test
	public void shouldMatchIdentityByCurriculumElement() {
		CurriculumElementExceptionalObligationHandler sut = new CurriculumElementExceptionalObligationHandler();
		Identity identity = new TransientIdentity();
		TestingObligationContext obligationContext = new TestingObligationContext();
		
		CurriculumElementExceptionalObligation hit = new CurriculumElementExceptionalObligation();
		CurriculumElementRef curriculumElementRef = () -> 12L;
		hit.setCurriculumElementRef(curriculumElementRef);
		hit.setObligation(AssessmentObligation.optional);
		obligationContext.addCurriculumElementRef(curriculumElementRef);
		CurriculumElementExceptionalObligation miss = new CurriculumElementExceptionalObligation();
		miss.setCurriculumElementRef(() -> 13L);
		miss.setObligation(AssessmentObligation.excluded);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.matchesIdentity(hit, identity, obligationContext, null, null)).isTrue();
		softly.assertThat(sut.matchesIdentity(miss, identity, obligationContext, null, null)).isFalse();
		softly.assertAll();
	}

}
