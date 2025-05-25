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
package edu.harvard.catalyst.scheduler.dto;

import edu.harvard.catalyst.scheduler.core.BookedVisitActivityLogStatics;
import edu.harvard.catalyst.scheduler.entity.BookedVisitActivityLog;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.util.DateUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.harvard.catalyst.scheduler.util.DateUtility.date24HTime;

public class ScheduledVisitHistoryDTO {

    List<Map<String,String>> bookedVisitActivity;

    public ScheduledVisitHistoryDTO() {
    }

    public ScheduledVisitHistoryDTO(List<BookedVisitActivityLog> bookedVisitActivityLogList) {
        bookedVisitActivity = new ArrayList<>();
        for (BookedVisitActivityLog bookedVisitActivityLogItem : bookedVisitActivityLogList) {
            Map<String, String> bookedVisitActivityItemMap = new HashMap<>();

            User user = bookedVisitActivityLogItem.getPerformingUser();
            if (user != null) {
                bookedVisitActivityItemMap.put("user", bookedVisitActivityLogItem.getPerformingUser().getEcommonsId());
            }
            else {
                bookedVisitActivityItemMap.put("user", null);
            }

            String actionString = bookedVisitActivityLogItem.getActionPerformed();
            BookedVisitActivityLogStatics action = BookedVisitActivityLogStatics.valueByLogString(actionString);
            bookedVisitActivityItemMap.put("action", action.getLogString());

            String formattedDate = DateUtility.format(date24HTime(), bookedVisitActivityLogItem.getDate());
            bookedVisitActivityItemMap.put("date", formattedDate);
            bookedVisitActivity.add(bookedVisitActivityItemMap);
        }

    }

    /**
     * for testability
     */
    public List<Map<String,String>> getBookedVisitActivity() {

        return bookedVisitActivity;

    }

}
