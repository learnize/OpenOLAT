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
package org.olat.modules.lecture.manager;

import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeToRepositoryEntry;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.model.AbsenceNoticeToRepositoryEntryImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AbsenceNoticeToRepositoryEntryDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AbsenceNoticeToRepositoryEntry createRelation(AbsenceNotice notice, RepositoryEntry entry) {
		AbsenceNoticeToRepositoryEntryImpl rel = new AbsenceNoticeToRepositoryEntryImpl();
		rel.setCreationDate(new Date());
		rel.setAbsenceNotice(notice);
		rel.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public List<AbsenceNoticeToRepositoryEntry> getRelations(AbsenceNotice notice) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select noticeToEntry from absencenoticetoentry noticeToEntry")
		  .append(" inner join fetch noticeToEntry.entry as entry")
		  .append(" where noticeToEntry.absenceNotice.key=:noticeKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AbsenceNoticeToRepositoryEntry.class)
				.setParameter("noticeKey", notice.getKey())
				.getResultList();
	}
	
	public void deleteRelations(List<AbsenceNoticeToRepositoryEntry> relations) {
		for(AbsenceNoticeToRepositoryEntry relation:relations) {
			deleteRelation(relation);
		}
	}
	
	public void deleteRelation(AbsenceNoticeToRepositoryEntry relation) {
		dbInstance.getCurrentEntityManager().remove(relation);	
	}
	
	public List<LectureBlockRollCall> getRollCallsByRepositoryEntry(AbsenceNotice notice) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rollCall from absencenoticetoentry noticeToEntry")
		  .append(" inner join noticeToEntry.entry as entry")
		  .append(" inner join lectureblock as block on (block.entry.key=entry.key)")
		  .append(" inner join lectureblockrollcall as rollCall on (rollCall.lectureBlock.key=block.key)")
		  .append(" inner join fetch rollCall.absenceNotice as currentNotice")
		  .append(" where noticeToEntry.absenceNotice.key=:noticeKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("noticeKey", notice.getKey())
				.getResultList();
	}
	
	public List<LectureBlockRollCall> getRollCallsOfAllEntries(AbsenceNotice notice) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rollCall from lectureblock block")
		  .append(" inner join lectureblockrollcall as rollCall on (rollCall.lectureBlock.key=block.key)")
		  .append(" inner join fetch rollCall.absenceNotice as currentNotice")
		  .append(" where rollCall.identity.key=:identityKey")
		  .append(" and block.startDate>=:startDate and block.endDate<=:endDate");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("identityKey", notice.getIdentity().getKey())
				.setParameter("startDate", notice.getStartDate(), TemporalType.TIMESTAMP)
				.setParameter("endDate", notice.getEndDate(), TemporalType.TIMESTAMP)
				.getResultList();
	}

}
