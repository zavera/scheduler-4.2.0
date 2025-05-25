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
package edu.harvard.catalyst.scheduler.entity.reporttemplate;

import edu.harvard.catalyst.scheduler.entity.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 11/5/14
 * Time: 4:20 PM
 */
@Entity
@Table(name = "report_template")
public class ReportTemplate extends BaseEntity implements Serializable {
    static public enum TemplateType {
        Administrative,
        Operational
    }

    private static Map<CommonGraph.GraphType, Graph> graphMap;

    static {
        graphMap = new HashMap<CommonGraph.GraphType, Graph>();

        graphMap.put(CommonGraph.GraphType.BookedVisit, BookedVisitGraph.getInstance());
        graphMap.put(CommonGraph.GraphType.StudyAndVisitTemplate, StudyAndVisitTemplateGraph.getInstance());
    }

    private String displayName;
    private TemplateType type;
    private Boolean dateBound;
    private CommonGraph.GraphType graphType;

    public ReportTemplate() {
        super(null);
    }
    public ReportTemplate(Integer id, String displayName, TemplateType type,
                          Boolean dateBound, CommonGraph.GraphType graphType) {
        super(id);

        this.displayName = displayName;
        this.type = type;
        this.dateBound = dateBound;
        this.graphType = graphType;
    }

    @Column(name = "display_name")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public TemplateType getType() {
        return type;
    }

    public void setType(TemplateType type) {
        this.type = type;
    }

    @Column(name = "date_bound")
    public Boolean getDateBound() {
        return dateBound;
    }

    public void setDateBound(Boolean dateBound) {
        this.dateBound = dateBound;
    }

    @Column(name = "graph_type")
    @Enumerated(EnumType.STRING)
    public CommonGraph.GraphType getGraphType() {
        return graphType;
    }

    public void setGraphType(CommonGraph.GraphType graphType) {
        this.graphType = graphType;
    }

    @Transient
    public Graph getGraph() {
        return graphMap.get(graphType);
    }
}
