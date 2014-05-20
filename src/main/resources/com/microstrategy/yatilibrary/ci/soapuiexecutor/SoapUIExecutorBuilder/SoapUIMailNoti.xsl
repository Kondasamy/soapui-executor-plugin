<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<html xml:lang="en" lang="en">
			<head>
				<title>soapUI Test Results</title>
				<style type="text/css">
					body {
					font:normal 80% verdana,arial,helvetica;
					color:#000000;
					}
					table tr td, table tr th {
					font-size: 100%;
					text-align:left;
					table-layout:fixed;
					word-break:break-all;
					}
					table.summary tr th{
					font-weight: bold;
					text-align:left;
					background:#a6caf0;
					table-layout:fixed;
					word-break:break-all;
					}
					table.fsummary tr th{
					font-weight: bold;
					text-align:left;
					background:#FA5858;
					table-layout:fixed;
					word-break:break-all;
					}
					table.summary tr td{
					font-size: 100%;
					text-align:left;
					table-layout:fixed;
					word-break:break-all;
					}
					p {
					line-height:1.5em;
					margin-top:0.5em; margin-bottom:1.0em;
					}
					h1 {
					margin: 0px 0px 5px; font: 265% verdana,arial,helvetica
					}
					h2 {
					margin-top: 1em; margin-bottom: 0.5em; font: bold 225%
					verdana,arial,helvetica; text-align:left
					}
					h3 {
					margin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica
					}
					h4 {
					margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
					}
					h5 {
					margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
					}
					h6 {
					margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
					}
					.Error {
					font-weight:bold; color:red;
					}
					.Failure {
					font-weight:bold; color:red;
					background:#eeeee0;
					}
					.Success {
					font-weight:bold; color:green;
					background:#eeeee0;
					}
					.Cancel {
					font-weight:bold; color:grey;
					background:#eeeee0;
					}
					.SuccessBk {
					background:#B3D9D9;
					}
					.FailureBk {
					background:#FA5858;
					}
					.Total {
					font-weight:bold; background:#a6caf0;
					}
					.Normal {
					background:#eeeee0;
					}
					.Properties {
					text-align:right;
					}
				</style>
			</head>
			<body>
				<a name="top"></a>
				<xsl:for-each select="projects">
					<xsl:variable name="url">
						<xsl:value-of select="@url" />
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="@status = 'FAILED'">
							<h2>
								<a>
									<xsl:attribute name="href"><xsl:value-of
										select="@url" /></xsl:attribute>
									<xsl:value-of select="@name" />
								</a>
								-
								<a class="Failure">
									<xsl:value-of select="@status" />
								</a>
							</h2>
						</xsl:when>
						<xsl:otherwise>
							<h2>
								<a>
									<xsl:attribute name="href"><xsl:value-of
										select="@url" /></xsl:attribute>
									<xsl:value-of select="@name" />
								</a>
								-
								<a class="Success">PASSED</a>
							</h2>
						</xsl:otherwise>
					</xsl:choose>


					<a>
						<xsl:value-of select="@trigger" />
					</a>
					<br />
					<a>
						Start Time:
						<xsl:value-of select="@startTime" />
					</a>
					<br />
					<a>
						Duration:
						<xsl:value-of select="@timetaken" />
					</a>
					<br />
					<a>
						Jenkins Node:
						<xsl:value-of select="@nodeName" />
					</a>
					<br />
					<a>
						Test Machine:
						<xsl:value-of select="@hostName" />
					</a>
					<br />
					<br />
					<xsl:for-each select="params/@*">
						<a>
							<xsl:value-of select="name()" />
							:
							<xsl:value-of select="." />
						</a>
						<br />
					</xsl:for-each>


					<h3>SoapUI Test Summary</h3>
					<table width="95%" cellspacing="2" cellpadding="5" border="0"
						class="summary">
						<tr valign="top">
							<th>SoapUI project</th>
							<th width="80">Status</th>
							<th width="100">TestCases</th>
							<th width="80">Failures</th>
							<th width="120">Time Taken</th>
						</tr>

						<xsl:for-each select="project">
							<xsl:variable name="projectName">
								<xsl:value-of select="@name" />
							</xsl:variable>
							<xsl:variable name="totalcase">
								<xsl:value-of select="@caseCount" />
							</xsl:variable>
							<xsl:variable name="totalfail">
								<xsl:value-of select="@failedCaseCount" />
							</xsl:variable>
							<xsl:variable name="totaltime">
								<xsl:value-of select="@timetaken" />
							</xsl:variable>

							<xsl:choose>
								<xsl:when test="@status = 'FINISHED'">
									<tr valign="top" class="Normal">
										<td class="Success">
											<a>
												<xsl:attribute name="href"><xsl:copy-of
													select="$url" />#<xsl:copy-of select="$projectName" /></xsl:attribute>
												<xsl:copy-of select="$projectName" />
											</a>
										</td>
										<td class="Success">PASSED</td>
										<td>
											<xsl:value-of select="@caseCount" />
										</td>
										<td>
											<xsl:value-of select="@failedCaseCount" />
										</td>
										<td>
											<xsl:value-of select="@timetaken" />
										</td>
									</tr>
								</xsl:when>
								<xsl:otherwise>
									<tr valign="top" class="Normal">
										<td class="Failure">
											<a>
												<xsl:attribute name="href"><xsl:copy-of
													select="$url" />#<xsl:copy-of select="$projectName" /></xsl:attribute>
												<xsl:copy-of select="$projectName" />
											</a>
										</td>
										<td class="Failure">
											<xsl:value-of select="@status" />
										</td>
										<td>
											<xsl:value-of select="@caseCount" />
										</td>
										<td class="Failure">
											<xsl:value-of select="@failedCaseCount" />
										</td>
										<td>
											<xsl:value-of select="@timetaken" />
										</td>
									</tr>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</table>
					<br />
					<br />
					<br />
					<br />

					<xsl:choose>
						<xsl:when test="@hasNewFailure != 'false' or @hasOldFailure != 'false'">
							<h3>
								<a>Failure List</a>
							</h3>

							<xsl:if test="@hasNewFailure = 'true'">
								<table width="95%" cellspacing="2" cellpadding="5" border="0"
									class="fsummary">
									<tr valign="top">
										<th width="200">Comments</th>
										<th>New Failures</th>
										<th width="80">Status</th>
										<th width="120">Time Taken</th>
										<th width="60">Steps</th>
										<th width="70">Failures</th>
									</tr>
									<xsl:for-each select="project">
										<xsl:variable name="projectName">
											<xsl:value-of select="@name" />
										</xsl:variable>
										<xsl:for-each select="testsuite">
											<xsl:variable name="suiteName">
												<xsl:value-of select="@name" />
											</xsl:variable>
											<xsl:for-each select="testcase">
												<xsl:choose>
													<xsl:when test="@status = 'FAILED' and @newF = 'true'">
														<tr valign="top">
															<td class="Failure" width="200">
																<xsl:value-of select="@comm" />
															</td>
															<td class="Normal">
																<xsl:copy-of select="$suiteName" />
																-
																<a>
																	<xsl:attribute name="href"><xsl:copy-of
																		select="$url" />#<xsl:copy-of select="$projectName" />-<xsl:copy-of
																		select="$suiteName" />-<xsl:value-of
																		select="@name" /></xsl:attribute>
																	<xsl:value-of select="@name" />
																</a>
															</td>
															<td class="Failure">
																<xsl:value-of select="@status" />
															</td>
															<td class="Normal">
																<xsl:value-of select="@timetaken" />
															</td>
															<td class="Normal">
																<xsl:value-of select="@stepCount" />
															</td>
															<td class="Failure">
																<xsl:value-of select="@failedStepCount" />
															</td>
														</tr>
													</xsl:when>
												</xsl:choose>
											</xsl:for-each>
										</xsl:for-each>
									</xsl:for-each>
								</table>
							</xsl:if>

							<xsl:if test="@hasOldFailure = 'true'">
								<table width="95%" cellspacing="2" cellpadding="5" border="0"
									class="summary">
									<tr valign="top">
										<th width="200">Comments</th>
										<th>Old Failures</th>
										<th width="80">Status</th>
										<th width="120">Time Taken</th>
										<th width="60">Steps</th>
										<th width="70">Failures</th>
									</tr>
									<xsl:for-each select="project">
										<xsl:variable name="projectName">
											<xsl:value-of select="@name" />
										</xsl:variable>
										<xsl:for-each select="testsuite">
											<xsl:variable name="suiteName">
												<xsl:value-of select="@name" />
											</xsl:variable>
											<xsl:for-each select="testcase">
												<xsl:choose>
													<xsl:when test="@status = 'FAILED' and not(@newF='true')">
														<tr valign="top">
															<td class="Failure" width="200">
																<xsl:value-of select="@comm" />
															</td>
															<td class="Normal">
																<xsl:copy-of select="$suiteName" />
																-
																<a>
																	<xsl:attribute name="href"><xsl:copy-of
																		select="$url" />#<xsl:copy-of select="$projectName" />-<xsl:copy-of
																		select="$suiteName" />-<xsl:value-of
																		select="@name" /></xsl:attribute>
																	<xsl:value-of select="@name" />
																</a>
															</td>
															<td class="Failure">
																<xsl:value-of select="@status" />
															</td>
															<td class="Normal">
																<xsl:value-of select="@timetaken" />
															</td>
															<td class="Normal">
																<xsl:value-of select="@stepCount" />
															</td>
															<td class="Failure">
																<xsl:value-of select="@failedStepCount" />
															</td>
														</tr>
													</xsl:when>
												</xsl:choose>
											</xsl:for-each>
										</xsl:for-each>
									</xsl:for-each>
								</table>
							</xsl:if>
							<br />
							<br />
							<br />
							<br />
						</xsl:when>
					</xsl:choose>

					<h3>
						<a>SoapUI Test Results</a>
					</h3>
					<table width="95%" cellspacing="2" cellpadding="5" border="0"
						class="summary">
						<tr valign="top">
							<th>SoapUI TestCase</th>
							<th width="80">Status</th>
							<th width="120">Time Taken</th>
							<th width="60">Steps</th>
							<th width="70">Failures</th>
						</tr>

						<xsl:for-each select="project">
							<xsl:variable name="projectName">
								<xsl:value-of select="@name" />
							</xsl:variable>
							<xsl:for-each select="testsuite">
								<xsl:variable name="suiteName">
									<xsl:value-of select="@name" />
								</xsl:variable>
								<xsl:for-each select="testcase">
									<xsl:choose>
										<xsl:when test="@status = 'FINISHED'">
											<tr valign="top">
												<td class="Normal">
													<xsl:copy-of select="$projectName" />
													-
													<xsl:copy-of select="$suiteName" />
													-
													<a>
														<xsl:attribute name="href"><xsl:copy-of
															select="$url" />#<xsl:copy-of select="$projectName" />-<xsl:copy-of
															select="$suiteName" />-<xsl:value-of select="@name" /></xsl:attribute>
														<xsl:value-of select="@name" />
													</a>
												</td>
												<td class="Success">PASSED</td>
												<td class="Normal">
													<xsl:value-of select="@timetaken" />
												</td>
												<td class="Normal">
													<xsl:value-of select="@stepCount" />
												</td>
												<td class="Normal">
													<xsl:value-of select="@failedStepCount" />
												</td>
											</tr>
										</xsl:when>
										<xsl:when test="@status = 'CANCELED'">
											<tr valign="top">
												<td class="Normal">
													<xsl:copy-of select="$projectName" />
													-
													<xsl:copy-of select="$suiteName" />
													-
													<a>
														<xsl:attribute name="href"><xsl:copy-of
															select="$url" />#<xsl:copy-of select="$projectName" />-<xsl:copy-of
															select="$suiteName" />-<xsl:value-of select="@name" /></xsl:attribute>
														<xsl:value-of select="@name" />
													</a>
												</td>
												<td class="Cancel">
													<xsl:value-of select="@status" />
												</td>
												<td class="Normal">
													<xsl:value-of select="@timetaken" />
												</td>
												<td class="Normal">
													<xsl:value-of select="@stepCount" />
												</td>
												<td class="Normal">
													<xsl:value-of select="@failedStepCount" />
												</td>
											</tr>
										</xsl:when>
										<xsl:otherwise>
											<tr valign="top">
												<td class="Normal">
													<xsl:copy-of select="$projectName" />
													-
													<xsl:copy-of select="$suiteName" />
													-
													<a>
														<xsl:attribute name="href"><xsl:copy-of
															select="$url" />#<xsl:copy-of select="$projectName" />-<xsl:copy-of
															select="$suiteName" />-<xsl:value-of select="@name" /></xsl:attribute>
														<xsl:value-of select="@name" />
													</a>
												</td>
												<td class="Failure">
													<xsl:value-of select="@status" />
												</td>
												<td class="Normal">
													<xsl:value-of select="@timetaken" />
												</td>
												<td class="Normal">
													<xsl:value-of select="@stepCount" />
												</td>
												<td class="Failure">
													<xsl:value-of select="@failedStepCount" />
												</td>
											</tr>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>

								<p></p>
							</xsl:for-each>
						</xsl:for-each>
					</table>
					<br />
					<br />
					<br />
					<br />


					<h3>API Health Status</h3>
					<table width="95%" cellspacing="2" cellpadding="5" border="0"
						class="summary">
						<tr valign="top">
							<th>API URL</th>
							<th width="200">Status</th>
						</tr>
						<xsl:for-each select="apis">
							<xsl:for-each select="api">
								<xsl:choose>
									<xsl:when test="@status = 'FAILED'">
										<tr valign="top">
											<td class="Normal">
												<xsl:value-of select="@url" />
											</td>
											<td class="Failure">
												<xsl:value-of select="@status" />
											</td>
										</tr>
									</xsl:when>
								</xsl:choose>
							</xsl:for-each>

							<xsl:for-each select="api">
								<xsl:choose>
									<xsl:when test="@status = 'OK'">
										<tr valign="top">
											<td class="Normal">
												<xsl:value-of select="@url" />
											</td>
											<td class="Success">
												<xsl:value-of select="@status" />
											</td>
										</tr>
									</xsl:when>
									<xsl:when test="@status = 'UNKNOWN'">
										<tr valign="top">
											<td class="Normal">
												<xsl:value-of select="@url" />
											</td>
											<td class="Cancel">
												<xsl:value-of select="@status" />
											</td>
										</tr>
									</xsl:when>
								</xsl:choose>
							</xsl:for-each>

						</xsl:for-each>
					</table>

					<br />
					<br />
					<br />
					<br />

				</xsl:for-each>
				<hr />
				<h4>
					<a>Network QE Team @ MicroStrategy Beijing</a>
				</h4>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>