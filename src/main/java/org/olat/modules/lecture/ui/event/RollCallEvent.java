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
package org.olat.modules.lecture.ui.event;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.modules.lecture.LectureBlock;

/**
 * 
 * Initial date: 29 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RollCallEvent extends Event {

	private static final long serialVersionUID = -1005289996137191724L;
	public static final String WORK_ON_ROLL_CALL = "work-on-roll-call";
	
	private final List<LectureBlock> lectureBlocks;
	
	public RollCallEvent(String command, List<LectureBlock> lectureBlocks) {
		super(command);
		this.lectureBlocks = new ArrayList<>(lectureBlocks);
	}
	
	public List<LectureBlock> getLectureBlocks() {
		return lectureBlocks;
	}
}
