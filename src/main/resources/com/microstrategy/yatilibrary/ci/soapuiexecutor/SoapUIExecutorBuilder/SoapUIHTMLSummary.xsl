<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
			<head>
				<title>soapUI Test Results</title>
				<style type="text/css">
					body {
					font:normal 68% verdana,arial,helvetica;
					color:#000000;
					}
					table tr td, table tr th {
					font-size: 68%;
					}
					table.summary tr th{
					font-weight: bold;
					text-align:left;
					background:#a6caf0;
					}
					table.summary tr td{

					}
					table.list tr th{
					font-weight: bold;
					text-align:left;
					background:#a6caf0;
					}
					table.list tr td{

					}
					table.case tr th{
					font-weight: bold;
					text-align:left;
					}
					table.case tr td{
					background:#eeeee0;
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
					}
					.Success {
					font-weight:bold; color:green;
					}
					.Cancel {
					font-weight:bold; color:grey;
					}
					.SuccessBk {
					background:#B3D9D9;
					}
					.FailureBk {
					background:#D9B3B3;
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

					<xsl:variable name="jobName">
						<xsl:value-of select="@name" />
					</xsl:variable>
					<xsl:variable name="jobStatus">
						<xsl:value-of select="@status" />
					</xsl:variable>
					<xsl:variable name="jobTime">
						<xsl:value-of select="@timetaken" />
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="@status = 'FAILED'">
							<h2>
								<xsl:copy-of select="$jobName" />
								-
								<a class="Failure">
									<xsl:value-of select="@status" />
								</a>
							</h2>
						</xsl:when>
						<xsl:otherwise>
							<h2>
								<xsl:copy-of select="$jobName" />
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
						Start time:
						<xsl:value-of select="@startTime" />
					</a>
					<br />
					<a>
						Finish time:
						<xsl:value-of select="@finishTime" />
					</a>
					<br />
					<a>
						Time taken:
						<xsl:value-of select="@timetaken" />
					</a>
					<br />
					<a>
						Build number:
						<xsl:value-of select="@buildnumber" />
					</a>
					<br />
					<a>
						Milestone:
						<xsl:value-of select="@milestone" />
					</a>
					<br />
					<a>
						Test node:
						<xsl:value-of select="@nodeName" />
					</a>
					<br />
					<a>
						Test machine:
						<xsl:value-of select="@hostName" />
					</a>
					<br />



					<h3>SoapUI Test Summary</h3>
					<table valign="top" width="100%">
						<tr>
							<td width="80%">
								<div>
									<table width="95%" cellspacing="2" cellpadding="5"
										border="0" class="summary">
										<tr valign="top">
											<th width="500">SoapUI project</th>
											<th width="80">Status</th>
											<th width="60">TestCases</th>
											<th width="60">Failures</th>
											<th width="150">Time Taken</th>
											<th>Description</th>
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
															<xsl:copy-of select="$projectName" />
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
														<td></td>
													</tr>
												</xsl:when>
												<xsl:otherwise>
													<tr valign="top" class="Normal">
														<td class="Failure">
															<xsl:copy-of select="$projectName" />
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
														<td></td>
													</tr>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:for-each>
									</table>
									<a href="#top">Back to top</a>
									<br />
									<br />
									<br />
									<br />

									<h3>API Health Status</h3>
									<table width="95%" cellspacing="2" cellpadding="5"
										border="0" class="list">
										<tr valign="top">
											<th width="60%">API URL</th>
											<th>Status</th>
										</tr>
										<xsl:for-each select="apis">
											<xsl:for-each select="api">
												<xsl:choose>
													<xsl:when test="@status = 'OK'">
														<tr valign="top">
															<td class="Normal">
																<xsl:value-of select="@url" />
															</td>
															<td class="Success Normal">
																<xsl:value-of select="@status" />
															</td>
														</tr>
													</xsl:when>
													<xsl:when test="@status = 'UNKNOWN'">
														<tr valign="top">
															<td class="Normal">
																<xsl:value-of select="@url" />
															</td>
															<td class="Cancel Normal">
																<xsl:value-of select="@status" />
															</td>
														</tr>
													</xsl:when>
													<xsl:otherwise>
														<tr valign="top">
															<tr valign="top">
																<td class="Normal">
																	<xsl:value-of select="@url" />
																</td>
																<td class="Failure Normal">
																	<xsl:value-of select="@status" />
																</td>
															</tr>
														</tr>
													</xsl:otherwise>
												</xsl:choose>
												<p></p>
											</xsl:for-each>
										</xsl:for-each>
									</table>
									<a href="#top">Back to top</a>

									<br />
									<br />
									<br />
									<br />

									<h3>
										<a>SoapUI Test Results</a>
									</h3>
									<table width="95%" cellspacing="2" cellpadding="5"
										border="0" class="list">
										<tr valign="top">
											<th width="300">SoapUI project</th>
											<th width="300">TestSuite</th>
											<th width="450">TestCase</th>
											<th width="80">Status</th>
											<th width="150">Time Taken</th>
											<th width="60">Teststeps</th>
											<th width="60">Failures</th>
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
																</td>
																<td class="Normal">
																	<xsl:copy-of select="$suiteName" />
																</td>
																<td class="Normal">
																	<a>
																		<xsl:value-of select="@name" />
																	</a>
																</td>
																<td class="Success Normal">PASSED</td>
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
																</td>
																<td class="Normal">
																	<xsl:copy-of select="$suiteName" />
																</td>
																<td class="Normal">
																	<a>
																		<xsl:value-of select="@name" />
																	</a>
																</td>
																<td class="Cancel Normal">
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
																</td>
																<td class="Normal">
																	<xsl:copy-of select="$suiteName" />
																</td>
																<td class="Normal">
																	<a>
																		<xsl:value-of select="@name" />
																	</a>
																</td>
																<td class="Failure Normal">
																	<xsl:value-of select="@status" />
																</td>
																<td class="Normal">
																	<xsl:value-of select="@timetaken" />
																</td>
																<td class="Normal">
																	<xsl:value-of select="@stepCount" />
																</td>
																<td class="Failure Normal">
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
									<a href="#top">Back to top</a>

								</div>
							</td>
							<td valign="top">
								<div>
									<div id="fb-root"></div>
									<script>(function(d, s, id) {
										var js, fjs = d.getElementsByTagName(s)[0];
										if (d.getElementById(id)) return;
										js = d.createElement(s); js.id = id;
										js.src = "//connect.facebook.net/en_GB/all.js#xfbml=1";
										fjs.parentNode.insertBefore(js, fjs);
										}(document, 'script', 'facebook-jssdk'));</script>
									<div class="fb-comments" data-width="350" data-num-posts="10">
										<xsl:attribute name="data-href"><xsl:value-of
											select="@fburl" /></xsl:attribute>
									</div>

								</div>
							</td>
						</tr>
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