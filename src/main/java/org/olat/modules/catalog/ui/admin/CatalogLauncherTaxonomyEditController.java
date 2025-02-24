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

import static org.olat.core.gui.components.util.SelectionValues.VALUE_ASC;
import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler.Config;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.handlers.RepositoryHandlerFactory.OrderedRepositoryHandler;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLauncherTaxonomyEditController extends AbstractLauncherEditController {
	
	private static final String TAXONOMY_PREFIX = "Taxonomy::";
	
	private SingleSelection taxonomyLevelEl;
	private MultipleSelectionElement educationalTypeEl;
	private MultipleSelectionElement resourceTypeEl;
	
	private final TaxonomyLevelLauncherHandler handler;
	
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;

	public CatalogLauncherTaxonomyEditController(UserRequest ureq, WindowControl wControl,
			TaxonomyLevelLauncherHandler handler, CatalogLauncher catalogLauncher) {
		super(ureq, wControl, handler, catalogLauncher);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.handler = handler;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer generalCont) {
		Config config = null;
		if (getCatalogLauncher() != null) {
			config = handler.fromXML(getCatalogLauncher().getConfig());
		}
		
		List<TaxonomyRef> taxonomyRefs = repositoryModule.getTaxonomyRefs();
		if (!taxonomyRefs.isEmpty()) {
			List<TaxonomyLevel> allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomyRefs);
			SelectionValues keyValues = RepositoyUIFactory.createTaxonomyLevelKV(getTranslator(), allTaxonomyLevels);
			allTaxonomyLevels.stream().map(TaxonomyLevel::getTaxonomy)
					.distinct()
					.forEach(taxonomy -> keyValues.add(entry(TAXONOMY_PREFIX + taxonomy.getKey(), taxonomy.getDisplayName())));
			keyValues.sort(VALUE_ASC);
			taxonomyLevelEl = uifactory.addDropdownSingleselect("taxonomyLevels", "admin.taxonomy.levels", generalCont,
					keyValues.keys(), keyValues.values());
			taxonomyLevelEl.setMandatory(true);
			String key = null;
			if (config != null) {
				if (config.getTaxonomyKey() != null) {
					key = TAXONOMY_PREFIX + config.getTaxonomyKey();
				} else if (config.getTaxonomyLevelKey() != null) {
					key = config.getTaxonomyLevelKey().toString();
				}
			}
			if (StringHelper.containsNonWhitespace(key) && taxonomyLevelEl.containsKey(key)) {
				taxonomyLevelEl.select(key, true);
			}
		}
		
		// educational type
		SelectionValues educationalTypeKV = new SelectionValues();
		repositoryManager.getAllEducationalTypes()
				.forEach(type -> educationalTypeKV.add(entry(type.getKey().toString(), translate(RepositoyUIFactory.getI18nKey(type)))));
		educationalTypeKV.sort(SelectionValues.VALUE_ASC);
		educationalTypeEl = uifactory.addCheckboxesDropdown("educationalType", "admin.educational.types",
				generalCont, educationalTypeKV.keys(), educationalTypeKV.values());
		if (config != null && config.getEducationalTypeKeys() != null && !config.getEducationalTypeKeys().isEmpty()) {
			for (Long key : config.getEducationalTypeKeys()) {
				if (educationalTypeEl.getKeys().contains(key.toString())) {
					educationalTypeEl.select(key.toString(), true);
				}
			}
		}
		
		// type of resources
		SelectionValues resourceTypeSV = new SelectionValues();
		List<OrderedRepositoryHandler> supportedHandlers = repositoryHandlerFactory.getOrderRepositoryHandlers();
		for (OrderedRepositoryHandler handler:supportedHandlers) {
			String type = handler.getHandler().getSupportedType();
			String iconLeftCss = RepositoyUIFactory.getIconCssClass(type);
			resourceTypeSV.add(new SelectionValue(type, translate(type), null, "o_icon o_icon-fw ".concat(iconLeftCss), null, true));
		}
		resourceTypeEl = uifactory.addCheckboxesDropdown("resourceType", "admin.resource.types",
				generalCont, resourceTypeSV.keys(), resourceTypeSV.values(), null, resourceTypeSV.icons());
		if (config != null && config.getResourceTypes()!= null && !config.getResourceTypes().isEmpty()) {
			for (String type : config.getResourceTypes()) {
				if (resourceTypeEl.getKeys().contains(type)) {
					resourceTypeEl.select(type, true);
				}
			}
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (taxonomyLevelEl != null && !taxonomyLevelEl.isOneSelected()) {
			taxonomyLevelEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected String getConfig() {
		Config config = new Config();
		
		String selectedKey = taxonomyLevelEl.getSelectedKey();
		if (selectedKey.startsWith(TAXONOMY_PREFIX)) {
			String taxonomyKey = selectedKey.substring(TAXONOMY_PREFIX.length());
			config.setTaxonomyKey(Long.valueOf(taxonomyKey));
		} else {
			config.setTaxonomyLevelKey(Long.valueOf(selectedKey));
		}
		
		if (educationalTypeEl.isAtLeastSelected(1)) {
			Collection<Long> educationalTypeKeys = educationalTypeEl.getSelectedKeys().stream()
					.map(Long::valueOf)
					.collect(Collectors.toSet());
			config.setEducationalTypeKeys(educationalTypeKeys);
		} else {
			config.setEducationalTypeKeys(null);
		}
		
		if (resourceTypeEl.isAtLeastSelected(1)) {
			config.setResourceTypes(new HashSet<>(resourceTypeEl.getSelectedKeys()));
		} else {
			config.setResourceTypes(null);
		}
		
		return handler.toXML(config);
	}

}
