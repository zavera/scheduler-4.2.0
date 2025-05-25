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
package edu.harvard.catalyst.scheduler.persistence;

import edu.harvard.catalyst.scheduler.entity.*;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author smitha
 *
 */
@Repository
@Transactional
public class SearchAlgorithmDAO extends SiteDAO {

    public List<TemplateResourceGroup> getTemplateResourceGroups(final VisitTemplate selectedVisit, final boolean flexVsFloat) {
        final String findTemplateResourceGroups =
                "SELECT trg FROM TemplateResourceGroup trg"
                        + " WHERE trg.visit = :visit AND trg.flexGroup = :flexGroup ";

        final Query query = newQuery(findTemplateResourceGroups);
        query.setParameter("visit", selectedVisit);
        query.setParameter("flexGroup", flexVsFloat);

        return query.list();
    }

    public List<TemplateResource> getSingleFloatTemplateResources(final VisitTemplate selectedVisit) {
        final String findFloatTemplateResource = "SELECT tr FROM TemplateResource tr"
                + " WHERE tr.visitTemplate = :visit AND tr.floatable = true AND tr.groupId IS NULL ";

        final Query query = newQuery(findFloatTemplateResource);
        query.setParameter("visit", selectedVisit);

        return query.list();
    }

    public boolean isSublocationClosed(final Sublocation sublocation, final Date startDate, final Date endDate) {
        final String findSublocationClosure = "select s FROM SublocationClosureInterval s where s.sublocation = :sublocation AND "
                + " ((:startTime >= s.startTime and :startTime < s.endTime) "
                + " or (:endTime > s.startTime and :endTime <= s.endTime) "
                + " or (s.startTime >= :startTime and s.endTime <= :endTime))";


        final Query query = newQuery(findSublocationClosure);
        query.setParameter("sublocation", sublocation);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);

        @SuppressWarnings("unchecked")
        final List<SublocationClosureInterval> list = query.list();

        return !list.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public List<BookedResource> findOverbookConflictResourcesByVisitStatus(final Resource resource, final Date startDate, final Date endDate) {

        final String findBookedResource = "select br FROM BookedResource br, BookedVisit bv where "
                +" br.bookedVisit = bv.id and bv.appointmentStatus IN (1,2,5) and br.resource = :resource "
                + " and ((:startTime >= br.scheduledStartTime and :startTime < br.scheduledEndTime) "
                + " or (:endTime > br.scheduledStartTime and :endTime <= br.scheduledEndTime) "
                + " or (br.scheduledStartTime >= :startTime and br.scheduledEndTime <= :endTime)) ORDER BY br.scheduledStartTime";

        final Query query = newQuery(findBookedResource);

        query.setParameter("resource", resource);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);

        return query.list();
    }

}
