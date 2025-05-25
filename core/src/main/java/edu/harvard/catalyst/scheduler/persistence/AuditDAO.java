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

import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.core.BookedVisitActivityLogStatics;
import edu.harvard.catalyst.scheduler.entity.BookedVisit;
import edu.harvard.catalyst.scheduler.entity.BookedVisitActivityLog;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Repository
@Transactional
public class AuditDAO extends SiteDAO {

    public List<BookedVisitActivityLog> getActivityLogForBookedVisit(BookedVisit bookedVisit) {

        String hql = "select bval from BookedVisitActivityLog bval "
                + "where bval.bookedVisit = :bookedVisit "
                + "and bval.actionPerformed IN (:actions) "
                + "order by bval.date DESC";

        List<String> actions = Arrays.asList(
                BookedVisitActivityLogStatics.BATCH_ENTRY.getLogString(),
                BookedVisitActivityLogStatics.CANCELLED.getLogString(),
                BookedVisitActivityLogStatics.CHECKED_IN.getLogString(),
                BookedVisitActivityLogStatics.CHECKED_OUT.getLogString(),
                BookedVisitActivityLogStatics.OVERBOOKED.getLogString(),
                BookedVisitActivityLogStatics.RESCHEDULED.getLogString(),
                BookedVisitActivityLogStatics.SCHEDULED.getLogString()
        );

        Query query = session().createQuery(hql);

        query.setParameter("bookedVisit", bookedVisit);
        query.setParameterList("actions", actions);

        return query.list();

    }

}
