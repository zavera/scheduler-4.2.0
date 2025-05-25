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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import edu.harvard.catalyst.scheduler.entity.TemplateResource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.scheduler.util.DateUtility.*;
import static edu.harvard.catalyst.scheduler.dto.response.GanttResourcesResponseDTO.*;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/28/14
 * Time: 1:02 PM
 */
public final class GanttResourcesGroupResponseDTO extends GanttResponseDTO {
    private final List<GanttResourceGroup> ganttResourceGroupList;

    private final int totalVisitResources;
    private final int totalVisitCalendarDays;

    public GanttResourcesGroupResponseDTO(final List<TemplateResource> allTemplateResourcesFromVisit,
                                          final Predicate<? super TemplateResource> relevancePredicate) {

        final List<TemplateResource> relevantTemplateResourcesFromVisit = getRelevantTemplateResources(
                allTemplateResourcesFromVisit, relevancePredicate);

        ganttResourceGroupList = Lists.newArrayList();

        if (relevantTemplateResourcesFromVisit.size() > 0) {
            boolean flexDesired = relevantTemplateResourcesFromVisit.get(0).getFlexible();

            partitionResourcesIntoResourceGroupList(
                    relevantTemplateResourcesFromVisit,
                    allTemplateResourcesFromVisit,
                    flexDesired);
        }

        this.totalVisitResources = allTemplateResourcesFromVisit.size();
        this.totalVisitCalendarDays = TemplateResource.totalVisitCalendarDays(allTemplateResourcesFromVisit);
    }

    void partitionResourcesIntoResourceGroupList(
            final List<TemplateResource> relevantTemplateResourcesFromVisit,
            final List<TemplateResource> allTemplateResourcesFromVisit,
            final boolean flexDesired) {

        final Map<String, List<TemplateResource>> groupIdToGroupsTrList = new HashMap<>();
        final Map<String, List<TemplateResource>> groupIdToGroupsComboDisplayList = new HashMap<>();

        final Set<String> groupIds = Sets.newLinkedHashSet();

        relevantTemplateResourcesFromVisit.stream()
                .forEach(tr -> {
                    String groupId = tr.getGroupId();
                    groupIds.add(groupId);
                    groupIdToGroupsTrList.put(groupId, Lists.newArrayList());
                    groupIdToGroupsComboDisplayList.put(groupId, Lists.newArrayList());
                });

        // flex groups check *all TRs* for membership in groups (groupIds) associated with this day, since they might be
        //   hiding in another day

        // float groups only need to check *relevant TRs* for membership
        addTrToMaps(
                groupIdToGroupsTrList,
                groupIdToGroupsComboDisplayList,
                groupIds,
                flexDesired ? allTemplateResourcesFromVisit : relevantTemplateResourcesFromVisit);


        for (final String groupId : groupIds) {

            List<TemplateResource> groupsTrResourceColumnList = groupIdToGroupsTrList.get(groupId);
            List<TemplateResource> groupsTrDisplayList = groupIdToGroupsComboDisplayList.get(groupId);

            List<GanttResourceDetail> detailList = groupsTrResourceColumnList.stream()
                    .map(tr -> new GanttResourceDetail(tr))
                    .collect(Collectors.toList());

            ganttResourceGroupList.add(new GanttResourceGroup(groupId, detailList, groupsTrDisplayList));
        }
    }

    void addTrToMaps(Map<String, List<TemplateResource>> groupIdToGroupsTrList,
                     Map<String, List<TemplateResource>> groupIdToGroupsComboDisplayList,
                     Set<String> groupIds,
                     List<TemplateResource> trList) {

        for (final TemplateResource tr : trList) {

            String groupId = tr.getGroupId();

            if (groupIds.contains(groupId)) {
                addToTrGroupsTrList(tr, groupIdToGroupsTrList);
                addTrToComboDisplayList(groupId, tr, groupIdToGroupsComboDisplayList);
            }
        }
    }

    void addTrToComboDisplayList(String groupId, TemplateResource tr, Map<String, List<TemplateResource>> groupIdToGroupsComboDisplayList) {
        List<TemplateResource> comboDisplayList = groupIdToGroupsComboDisplayList.get(groupId);
        comboDisplayList.add(tr);
    }

    void addToTrGroupsTrList(TemplateResource tr,
                             Map<String, List<TemplateResource>> groupIdToGroupsTrList) {

        String groupId = tr.getGroupId();
        List<TemplateResource> groupTrList = groupIdToGroupsTrList.get(groupId);
        groupTrList.add(tr);
    }

    @Override
    public String jsonify(Gson gson) {
        return gson.toJson(this);
    }


    public static final class GanttResourceGroup {
        private final List<GanttResourceDetail> children;
        private final String groupId;
        private final String groupComboDisplay;

        GanttResourceGroup(final String groupId, final List<GanttResourceDetail> children, List<TemplateResource> trListForComboDisplay) {
            this.children = children;
            this.groupId = groupId;

            List<String> displayList = trListForComboDisplay.stream()
                    .map(tr -> trToDisplayString(tr))
                    .collect(Collectors.toList());

            groupComboDisplay = Joiner.on(";").join(displayList);
        }

        String trToDisplayString(TemplateResource tr) {
            int startDayNumber = minutesToDayNumber(tr.getStartMinutes());
            int startMinutesIntoDay = minutesIntoDay(tr.getStartMinutes());
            int startHour = startMinutesIntoDay / MINS_PER_HR;
            int startMinute = startMinutesIntoDay - startHour * MINS_PER_HR;

            int endDayNumber = minutesToDayNumber(tr.getEndMinutes());
            int endMinutesIntoDay = minutesIntoDay(tr.getEndMinutes());
            int endHour = endMinutesIntoDay / MINS_PER_HR;
            int endMinute = endMinutesIntoDay - endHour * MINS_PER_HR;

            String result = new StringBuilder()
                    .append(tr.getResource().getName())
                    .append(", ")
                    .append(tr.getResource().getResourceType().getName())
                    .append(" (Day ")
                    .append(startDayNumber)
                    .append(" ")
                    .append(padTime(startHour))
                    .append(":")
                    .append(padTime(startMinute))
                    .append(" - Day ")
                    .append(endDayNumber)
                    .append(" ")
                    .append(padTime(endHour))
                    .append(":")
                    .append(padTime(endMinute))
                    .append(")")
                    .toString();

            return result;
        }
    }
}
