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

import static edu.harvard.catalyst.scheduler.entity.reporttemplate.Edge.JoinAssembly.wholeField;
import static edu.harvard.catalyst.scheduler.entity.reporttemplate.Edge.JoinType.INNER;
import static edu.harvard.catalyst.scheduler.entity.reporttemplate.Edge.JoinType.LEFT;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 12/23/14
 * Time: 1:03 PM
 */
public final class BookedVisitGraph extends CommonGraph {

    private final Vertex rootVertex = new Vertex("booked_visit", "bv");
    private final Vertex bv = rootVertex;

    private final Vertex j = new Vertex("subject", "j");
    private final Vertex br = new Vertex("booked_resource", "br");
    private final Vertex bra = new Vertex("override_booked_resource_annotations", "bra");
    private final Vertex lla_br = new Vertex("line_level_annotations", "lla_br");
    private final Vertex sm = new Vertex("subject_mrn", "sm");
    protected final Vertex u = new Vertex("user", "u");

    //NB: Holder idiom for thread-safe lazy init of fields
    private static final class Holder {
        private static final BookedVisitGraph instance = new BookedVisitGraph();
    }

    public static BookedVisitGraph getInstance() {
        return Holder.instance;
    }

    private final Set<Edge> specificEdges =
            Sets.newHashSet(
                    new Edge(bv, vt, INNER, "visit_template"),
                    new Edge(bv, s, INNER, "study"),
                    new Edge(bv, u, INNER, "scheduling_user"),
                    new Edge(bv, br, INNER, "booked_visit", Edge.JoinAssembly.rightField),
                    new Edge(bv, sm, LEFT, "subject_mrn"),
                    new Edge(sm, j, LEFT, "subject"),
                    new Edge(br, tr, LEFT, "template_resource"),
                    new Edge(br, rsrc, INNER, "resource"),
                    new Edge(br, bra, LEFT, "booked_resource", Edge.JoinAssembly.rightField),
                    new Edge(bra, lla_br, LEFT, "line_level_annotations"));

    private BookedVisitGraph() {
        myInit(specificEdges, rootVertex, true);
    }
}
