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
package org.olat.modules.catalog.ui.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractFilterEditController extends FormBasicController {
	
	private static final String[] ON_KEYS = new String[] { "on" };
	
	private MultipleSelectionElement enabledEl;
	
	private final CatalogFilterHandler handler;
	private CatalogFilter catalogFilter;
	
	@Autowired
	private CatalogV2Service catalogService;

	public AbstractFilterEditController(UserRequest ureq, WindowControl wControl, CatalogFilterHandler handler, CatalogFilter catalogFilter) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CatalogV2UIFactory.class, ureq.getLocale(), getTranslator()));
		this.handler = handler;
		this.catalogFilter = catalogFilter;
	}

	protected abstract void initForm(FormItemContainer formLayout);

	protected abstract String getConfig();
	
	protected CatalogFilter getCatalogFilter() {
		return catalogFilter;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] onValues = new String[]{ translate("on") };
		enabledEl = uifactory.addCheckboxesHorizontal("admin.filter.enabled", formLayout, ON_KEYS, onValues);
		enabledEl.select(ON_KEYS[0], catalogFilter == null || catalogFilter.isEnabled());
		
		initForm(formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (catalogFilter == null) {
			catalogFilter = catalogService.createCatalogFilter(handler.getType());
		}
		
		catalogFilter.setEnabled(enabledEl.isAtLeastSelected(1));
		catalogFilter.setConfig(getConfig());
		catalogFilter = catalogService.update(catalogFilter);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
