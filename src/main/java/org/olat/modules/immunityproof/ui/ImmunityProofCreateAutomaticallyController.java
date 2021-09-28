package org.olat.modules.immunityproof.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.qrscanner.GenericQrScanController;
import org.olat.core.gui.qrscanner.QrCodeDetectedEvent;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofContext;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.manager.ImmunityProofCertificateChecker;
import org.springframework.beans.factory.annotation.Autowired;

public class ImmunityProofCreateAutomaticallyController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(ImmunityProofCreateAutomaticallyController.class);

	private FormLink scanLink;
	private FormLink uploadLink;

	private FormLayoutContainer qrScanContainer;
	private FileElement certificateUploadEl;

	private GenericQrScanController scanController;
	private ImmunityProofContext context;

	@Autowired
	private ImmunityProofModule immunityProofModule;

	public ImmunityProofCreateAutomaticallyController(UserRequest ureq, WindowControl wControl,
			Identity editedIdentity) {
		super(ureq, wControl);

		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));

		context = new ImmunityProofContext();
		context.setIdentity(editedIdentity);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);
		buttonLayout.setLabel("automatic.scan.import", null);
		formLayout.add(buttonLayout);

		scanLink = uifactory.addFormLink("automatic.scan", buttonLayout, Link.BUTTON);
		scanLink.setIconLeftCSS("o_icon o_icon_lg o_icon_qrcode");
		uploadLink = uifactory.addFormLink("automatic.import", buttonLayout, Link.BUTTON);
		uploadLink.setIconLeftCSS("o_icon o_icon_lg o_icon_upload");

		qrScanContainer = FormLayoutContainer.createCustomFormLayout("pdfPreview", getTranslator(),
				velocity_root + "/immunity_proof_scanner.html");
		qrScanContainer.setRootForm(mainForm);
		qrScanContainer.setLabel("automatic.scan", null);
		qrScanContainer.setVisible(false);
		formLayout.add(qrScanContainer);

		certificateUploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "automatic.import",
				formLayout);
		certificateUploadEl.setMaxUploadSizeKB(10000, "automatic.import.file.size.error", null);
		certificateUploadEl.setVisible(false);

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == uploadLink) {
			doShowUpload();
		} else if (source == scanLink) {
			doShowScan(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == scanController) {
			if (event instanceof QrCodeDetectedEvent) {
				QrCodeDetectedEvent qrCodeEvent = (QrCodeDetectedEvent) event;
				context.setQrCode(qrCodeEvent.getQrCode());
				
				// getWindowControl().setInfo("QR Code Detected", qrCodeEvent.getQrCode());

				doCheckCertificate(context);
			}
		}
	}

	private void doShowUpload() {
		certificateUploadEl.setVisible(true);
		qrScanContainer.setVisible(false);

		if (scanController != null) {
			scanController.stopScanner();
		}
	}

	private void doShowScan(UserRequest ureq) {
		if (scanController == null) {
			scanController = new GenericQrScanController(ureq, getWindowControl());
			listenTo(scanController);

			qrScanContainer.put("scanner", scanController.getInitialComponent());
		}

		certificateUploadEl.setVisible(false);
		qrScanContainer.setVisible(true);

		scanController.startScanner();
	}

	private void doCheckCertificate(ImmunityProofContext context) {
		List<String> cmds = new ArrayList<String>();
		cmds.add(immunityProofModule.getPythonDir());
		cmds.add(immunityProofModule.getValidationScriptDir() + "/verify_ehc.py");
		cmds.add(context.getQrCode());
		CountDownLatch doneSignal = new CountDownLatch(1);

		ImmunityProofCertificateChecker certificateChecker = new ImmunityProofCertificateChecker(context, cmds,
				doneSignal);
		certificateChecker.start();

		try {
			if (doneSignal.await(3000, TimeUnit.MILLISECONDS)) {
				if (log.isDebugEnabled()) {
					log.debug("Successfully validated certificate: {}", context);
				}

				if (context.isCertificateFound()) {
				getWindowControl()
						.setWarning("Successfully validated COVID Certificate" + "<br><br>" + context.getQrCode());
				} else {
					getWindowControl().setError("No Certificate found" + "<br><br>" + context.getQrCode());
				}
			} else {
				log.warn("Cannot validate certificate in 3s: {}", context);
				
				getWindowControl()
						.setError("Timeout - Could not validate Certificate" + "<br><br>" + context.getQrCode());
			}
		} catch (InterruptedException e) {
			log.error("", e);
		}

		certificateChecker.destroyProcess();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		if (scanController != null) {
			scanController.stopScanner();
		}

		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		
	}

}
