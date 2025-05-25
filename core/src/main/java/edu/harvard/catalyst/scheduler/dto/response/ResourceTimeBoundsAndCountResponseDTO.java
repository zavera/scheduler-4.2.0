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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.DateUtility.dateMonthYear;
import static edu.harvard.catalyst.scheduler.util.DateUtility.format;
import static edu.harvard.catalyst.scheduler.util.DateUtility.parse;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/28/14
 * Time: 1:02 PM
 */
public class ResourceTimeBoundsAndCountResponseDTO {
    private Long totalVisitResources;

    private List<String> daysList;

    // for unit-tests
    public List<String> getDaysList() {
        return daysList;
    }

    ResourceTimeBoundsAndCountResponseDTO(Long totalVisitResources, List<String> daysList) {
        this.totalVisitResources = totalVisitResources;
        this.daysList = daysList;
    }

    public static ResourceTimeBoundsAndCountResponseDTO fromTriple(Long totalVisitResources, Date earlistStartDate, Date latestEndDate) {
        List<String> daysList = new ArrayList<String>();

        Date startDate = earlistStartDate;
        Date endDate = latestEndDate;

        // edge case, useful for unit-tests
        if (startDate == null) {
            startDate = new Date();
        }
        if (endDate == null) {
            endDate = new Date();
        }

        startDate = parse(dateMonthYear(), format(dateMonthYear(), startDate));
        endDate = parse(dateMonthYear(), format(dateMonthYear(), endDate));

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(startDate);
        int startDay = calStart.get(Calendar.DAY_OF_YEAR);

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(endDate);
        int endDay = calEnd.get(Calendar.DAY_OF_YEAR);

        int days = (endDay - startDay);
        for(int i = 1; i <= days + 1; i++) {
            daysList.add("DAY " + i);
        }
        return new ResourceTimeBoundsAndCountResponseDTO(totalVisitResources, daysList);
    }
}
