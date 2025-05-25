/**
 * Copyright (c) 2015-2016, President and Fellows of Harvard College
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.harvard.catalyst.scheduler.dto.response;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.catalyst.scheduler.entity.reporttemplate.Graph;

import com.google.common.base.Joiner;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 9/16/14
 * Time: 3:02 PM
 */
public class ReportTemplateResultDTO {
	private List<String> csvHeaders = new ArrayList<>();
	private List<List<String>> csvRows = new ArrayList<>();
	private final String reportName;
	private Graph.QueryScalarsTcfs cachedQsTcfs;

	public ReportTemplateResultDTO(final String reportName) {
		this.reportName = reportName;
	}

	public String toCsvString() {
		final StringBuilder builder = new StringBuilder();

		// empty after first one of multi-chunks
		if ( ! csvHeaders.isEmpty()) {
			final String headers = Joiner.on(",").join(csvHeaders);
			
			builder.append(headers).append("\n");
		}

		for (final List<String> csvRow : csvRows) {
			final String row = Joiner.on(",").join(csvRow);
			
			builder.append(row).append("\n");
		}

		return builder.toString();
	}

	public Integer getResultSize() {
		return csvRows.size();
	}

	public String getReportName() {
		return reportName;
	}

	public void setCsvHeaders(final List<String> csvHeaders) {
		this.csvHeaders = csvHeaders;
	}
	public List<String> getCsvHeaders() {
		return csvHeaders;
	}

	public List<List<String>> getCsvRows() {
		return csvRows;
	}
	public void setCsvRows(final List<List<String>> csvRows) {
		this.csvRows = csvRows;
	}

	public Graph.QueryScalarsTcfs getCachedQsTcfs() {
		return cachedQsTcfs;
	}

	public void setCachedQsTcfs(final Graph.QueryScalarsTcfs cachedQsTcfs) {
		this.cachedQsTcfs = cachedQsTcfs;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final ReportTemplateResultDTO that = (ReportTemplateResultDTO) o;

		if (csvHeaders != null ? !csvHeaders.equals(that.csvHeaders) : that.csvHeaders != null) {
			return false;
		}

		if (csvRows != null ? !csvRows.equals(that.csvRows) : that.csvRows != null) {
			return false;
		}

		if (reportName != null ? !reportName.equals(that.reportName) : that.reportName != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = csvHeaders != null ? csvHeaders.hashCode() : 0;
		result = 31 * result + (csvRows != null ? csvRows.hashCode() : 0);
		result = 31 * result + (reportName != null ? reportName.hashCode() : 0);
		return result;
	}
}
