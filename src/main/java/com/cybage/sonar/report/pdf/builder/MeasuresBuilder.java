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
package com.cybage.sonar.report.pdf.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.HttpDownloader.HttpException;
import org.sonarqube.ws.WsMeasures.ComponentWsResponse;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.measure.ComponentWsRequest;

import com.cybage.sonar.report.pdf.entity.Measure;
import com.cybage.sonar.report.pdf.entity.Measures;
import com.cybage.sonar.report.pdf.util.MetricKeys;

public class MeasuresBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(MeasuresBuilder.class);

	private static MeasuresBuilder builder;

	private WsClient wsClient;

	private static List<String> measuresKeys = null;

	private static Integer DEFAULT_SPLIT_LIMIT = 20;

	public MeasuresBuilder(final WsClient wsClient) {
		this.wsClient = wsClient;
	}

	public static MeasuresBuilder getInstance(final WsClient wsClient) {
		if (builder == null) {
			return new MeasuresBuilder(wsClient);
		}

		return builder;
	}

	public Measures initMeasuresByProjectKey(final String projectKey) throws HttpException, IOException {

		Measures measures = new Measures();

		if (measuresKeys == null) {
			measuresKeys = MetricKeys.getAllMetricKeys();
		}

		// Avoid "Post too large"
		if (measuresKeys.size() > DEFAULT_SPLIT_LIMIT) {
			initMeasuresSplittingRequests(measures, projectKey);
		} else {
			this.addMeasures(measures, measuresKeys, projectKey);
		}

		return measures;

	}

	/**
	 * This method does the required requests to get all measures from Sonar,
	 * but taking care to avoid too large requests (measures are taken by 20).
	 */
	private void initMeasuresSplittingRequests(final Measures measures, final String projectKey)
			throws HttpException, IOException {
		Iterator<String> it = measuresKeys.iterator();
		LOGGER.debug("Getting " + measuresKeys.size() + " metric measures from Sonar by splitting requests");
		List<String> twentyMeasures = new ArrayList<String>(20);
		int i = 0;
		while (it.hasNext()) {
			twentyMeasures.add(it.next());
			i++;
			if (i % DEFAULT_SPLIT_LIMIT == 0) {
				LOGGER.debug("Split request for: " + twentyMeasures);
				addMeasures(measures, twentyMeasures, projectKey);
				i = 0;
				twentyMeasures.clear();
			}
		}
		if (i != 0) {
			LOGGER.debug("Split request for remain metric measures: " + twentyMeasures);
			addMeasures(measures, twentyMeasures, projectKey);
		}
	}

	/**
	 * Add measures to this.
	 */
	private void addMeasures(final Measures measures, final List<String> measuresAsString, final String projectKey)
			throws HttpException, IOException {

		/*
		 * String[] measuresAsArray = measuresAsString .toArray(new
		 * String[measuresAsString.size()]); ResourceQuery query =
		 * ResourceQuery.createForMetrics(projectKey, measuresAsArray);
		 * query.setDepth(0); query.setIncludeTrends(true); Resource resource =
		 * wsClient.find(query);
		 */
		ComponentWsRequest compWsReq = new ComponentWsRequest();
		compWsReq.setComponentKey(projectKey);
		compWsReq.setMetricKeys(measuresAsString);

		ComponentWsResponse compWsRes = wsClient.measures().component(compWsReq);

		if (compWsRes.getComponent().getMeasuresCount() != 0) {
			this.addAllMeasuresFromDocument(measures, compWsRes);
		} else {
			LOGGER.debug("Empty response when looking for measures: " + measuresAsString.toString());
		}
	}

	private void addAllMeasuresFromDocument(final Measures measures, final ComponentWsResponse compWsRes) {
		List<org.sonarqube.ws.WsMeasures.Measure> allNodes = compWsRes.getComponent().getMeasuresList();
		Iterator<org.sonarqube.ws.WsMeasures.Measure> it = allNodes.iterator();
		while (it.hasNext()) {
			addMeasureFromNode(measures, it.next());
		}
	}

	private void addMeasureFromNode(final Measures measures, final org.sonarqube.ws.WsMeasures.Measure measureNode) {
		Measure measure = MeasureBuilder.initFromNode(measureNode);
		measures.addMeasure(measure.getMetric(), measure);
	}
}