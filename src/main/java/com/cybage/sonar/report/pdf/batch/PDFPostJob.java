/*
 * SonarQube PDF Report
 * Copyright (C) 2010 klicap - ingenieria del puzle
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package com.cybage.sonar.report.pdf.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
//import org.sonar.report.pdf.util.FileUploader;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

public class PDFPostJob implements PostJob {

	private static final Logger LOGGER = LoggerFactory.getLogger(PDFPostJob.class);

	private final FileSystem fs;

	public PDFPostJob(Settings settings, FileSystem fs) {
		this.fs = fs;
	}

	public static final String SKIP_PDF_KEY = "sonar.pdf.skip";
	public static final boolean SKIP_PDF_DEFAULT_VALUE = false;

	public static final String REPORT_TYPE = "report.type";
	public static final String REPORT_TYPE_DEFAULT_VALUE = "pdf";

	public static final String USERNAME = "sonar.pdf.username";
	public static final String USERNAME_DEFAULT_VALUE = "";

	public static final String PASSWORD = "sonar.pdf.password";
	public static final String PASSWORD_DEFAULT_VALUE = "";

	public static final String SONAR_HOST_URL = "sonar.host.url";
	public static final String SONAR_HOST_URL_DEFAULT_VALUE = "http://localhost:9000";

	@Override
	public void describe(PostJobDescriptor arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void execute(PostJobContext postJobContext) {
		String projectKey = postJobContext.settings().getString("sonar.projectKey");
		
		LOGGER.info("Executing decorator: PDF Report");
		String sonarHostUrl = postJobContext.settings().hasKey(SONAR_HOST_URL)
				? postJobContext.settings().getString(SONAR_HOST_URL) : SONAR_HOST_URL_DEFAULT_VALUE;
		String username = postJobContext.settings().hasKey(USERNAME) ? postJobContext.settings().getString(USERNAME)
				: USERNAME_DEFAULT_VALUE;
		String password = postJobContext.settings().hasKey(PASSWORD) ? postJobContext.settings().getString(PASSWORD)
				: PASSWORD_DEFAULT_VALUE;
		String reportType = postJobContext.settings().hasKey(REPORT_TYPE)
				? postJobContext.settings().getString(REPORT_TYPE) : REPORT_TYPE_DEFAULT_VALUE;

		LOGGER.info("sonarHostURL : " + sonarHostUrl);
		LOGGER.info("username : " + username);
		LOGGER.info("password : " + password);
		LOGGER.info("reportType : " + reportType);
		LOGGER.info("projectKey : " + projectKey);

		PDFGenerator generator = new PDFGenerator(projectKey, fs, sonarHostUrl, username, password, reportType);

		generator.execute();

		/*String path = fs.workDir().getAbsolutePath() + "/" + project.getEffectiveKey().replace(':', '-') + ".pdf";

		File pdf = new File(path);
		if (pdf.exists()) {
			FileUploader.upload(pdf, sonarHostUrl + "/pdf_report/store", username, password);
		} else {
			LOG.error("PDF file not found in local filesystem. Report could not be sent to server.");
		}
		 */
	}
	

}