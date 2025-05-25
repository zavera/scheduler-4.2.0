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

import com.google.common.collect.Sets;

import java.util.Set;

import static edu.harvard.catalyst.scheduler.entity.reporttemplate.Edge.JoinAssembly.rightField;
import static edu.harvard.catalyst.scheduler.entity.reporttemplate.Edge.JoinAssembly.wholeField;
import static edu.harvard.catalyst.scheduler.entity.reporttemplate.Edge.JoinType.LEFT;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 12/23/14
 * Time: 1:03 PM
 */
public class CommonGraph extends Graph {

    protected final Vertex s = new Vertex("study", "s");
    protected final Vertex vt = new Vertex("visit_template", "vt");
    protected final Vertex u_inv = new Vertex("user", "u_inv");
    protected final Vertex u_sch = new Vertex("user", "u_sch");
    protected final Vertex u_nrs = new Vertex("user", "u_nrs");
    protected final Vertex u_nut = new Vertex("user", "u_nut");
    protected final Vertex u_doc = new Vertex("user", "u_doc");
    protected final Vertex u_doc2 = new Vertex("user", "u_doc2");
    protected final Vertex u_sch2 = new Vertex("user", "u_sch2");
    protected final Vertex u_sch3 = new Vertex("user", "u_sch3");
    protected final Vertex u_nrs2 = new Vertex("user", "u_nrs2");
    protected final Vertex u_nut2 = new Vertex("user", "u_nut2");
    protected final Vertex sfs_f1 = new Vertex("study_funding_source_cai", "sfs_f1");
    protected final Vertex sfs_f2 = new Vertex("study_funding_source_cai", "sfs_f2");
    protected final Vertex sfs_f3 = new Vertex("study_funding_source_cai", "sfs_f3");
    protected final Vertex sfs_f4 = new Vertex("study_funding_source_cai", "sfs_f4");
    protected final Vertex tr = new Vertex("template_resource", "tr");
    protected final Vertex tra = new Vertex("template_resource_annotations", "tra");
    protected final Vertex lla_vt = new Vertex("line_level_annotations", "lla_vt");
    protected final Vertex rsrc = new Vertex("resource", "rsrc");

    protected final Set<Edge> commonEdges = Sets.newHashSet(
            new Edge(s, u_inv, LEFT, "principal_investigator"),
            new Edge(s, u_sch, LEFT, "primary_scheduling_contact"),
            new Edge(s, u_nrs, LEFT, "protocol_nurse"),
            new Edge(s, u_nut, LEFT, "protocol_nutritionist"),
            new Edge(s, u_doc, LEFT, "responsible_physician"),
            new Edge(s, u_doc2, LEFT, "responsible_physician2"),
            new Edge(s, u_sch2, LEFT, "secondary_scheduling_contact"),
            new Edge(s, u_sch3, LEFT, "scheduler3"),
            new Edge(s, u_nrs2, LEFT, "associate_nurse"),
            new Edge(s, u_nut2, LEFT, "associate_protocol_nutritionist"),
            new Edge(s, sfs_f1, LEFT, "(sfs_f1.study = s.id and sfs_f1.one_to_four = 1)", wholeField),
            new Edge(s, sfs_f2, LEFT, "(sfs_f2.study = s.id and sfs_f2.one_to_four = 2)", wholeField),
            new Edge(s, sfs_f3, LEFT, "(sfs_f3.study = s.id and sfs_f3.one_to_four = 3)", wholeField),
            new Edge(s, sfs_f4, LEFT, "(sfs_f4.study = s.id and sfs_f4.one_to_four = 4)", wholeField),
            new Edge(tr, tra, LEFT, "template_resource", rightField),
            new Edge(tra, lla_vt, LEFT, "line_level_annotations")
            );

    public enum GraphType {BookedVisit, StudyAndVisitTemplate}

    protected void myInit(final Set<Edge> specificEdges, final Vertex rootVertex, final boolean renumber) {
        final Set<Edge> edges = Sets.newTreeSet();
        edges.addAll(commonEdges);
        edges.addAll(specificEdges);

        init(edges, rootVertex, renumber);
    }

}
