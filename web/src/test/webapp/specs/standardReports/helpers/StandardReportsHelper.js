/*
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

var StandardReportsHelper = {};
var SRH = StandardReportsHelper;

////////// daily overview ////////////

SRH.dailyOverviewResult0 =
    {
        birthdate: "10/29/1900",
        bookedVisitId: 33000,
        comment: "",
        firstName: "LOU000",
        genderName: "Unreported",
        irb: "2015P00000",
        lastName: "CRO000",
        localId: "1200",
        middleName: "",
        mrn: "894000",
        offInstitution: "NO",
        resourceEndTime: "Feb 14, 2000 2:30:00 PM",
        resourceName: "Nurse - 000",
        resourceStartTime: "Feb 14, 2000 8:30:00 AM",
        scheduledEndTime: "Feb 14, 2000 2:45:00 PM",
        scheduledStartTime: "Feb 14, 2000 8:30:00 AM",
        sublocationName: "CNY000",
        visitName: "Visit 000",
        visitStatus: "Scheduled",
        visitType: "Outpatient CRC"
    };
SRH.dailyOverviewResult1 =
    {
        birthdate: "10/29/1901",
        bookedVisitId: 33001,
        comment: "",
        firstName: "LOU001",
        genderName: "Unreported",
        irb: "2015P00001",
        lastName: "CRO001",
        localId: "1201",
        middleName: "",
        mrn: "894001",
        offInstitution: "NO",
        resourceEndTime: "Feb 14, 2000 2:30:00 PM",
        resourceName: "Nurse - 001",
        resourceStartTime: "Feb 14, 2000 8:30:00 AM",
        scheduledEndTime: "Feb 14, 2000 2:45:00 PM",
        scheduledStartTime: "Feb 14, 2000 8:30:00 AM",
        sublocationName: "CNY001",
        visitName: "Visit 001",
        visitStatus: "Scheduled",
        visitType: "Outpatient CRC"
    };

SRH.dailyOverviewStatusScheduled = "<div class='statusBlock reportSecondary'>";
SRH.dailyOverviewStatusCheckedIn = "<div class='statusBlock reportCheckedInSecondary'>";
SRH.dailyOverviewStatusHold = "<div class='statusBlock reportSecondary'>";

SRH.dailyOverviewOutput1a = "<div class='reportRow'>      <div class='secondaryNameBlock'>       " +
    "<div class='mainName'>DT - DT</div>      </div>      <div class='statusBlockContainer'> ";

SRH.dailyOverviewOutput1b = "          <div class='label boldReportField'>Subject</div>          " +
    "<div class='value boldReportField'>FN</div>          <div class='label'>MRN</div>          " +
    "<div class='value'>894000</div>          <div class='label'>Gender</div>          " +
    "<div class='value'>Unreported</div>          <div class='label'>Birth Date</div>          " +
    "<div class='value'>10/29/1900</div>          <div class='label'>Local ID</div>          " +
    "<div class='value'>1200</div>          <div class='label'>IRB #</div>          " +
    "<div class='value'>2015P00000</div>          <div class='label'>Visit Name</div>          " +
    "<div class='value'>Visit 000</div>          <div class='label'>Visit Type</div>          " +
    "<div class='value'>Outpatient CRC</div>          <div class='label'>Sublocation</div>          " +
    "<div class='value'>CNY000</div>          <div class='label'>Is part of the Visit Off Unit?</div>          " +
    "<div class='value'>NO</div>          <div class='label'>Check-In</div>          " +
    "<div class='value'></div>          <div class='label'>Check-Out</div>          " +
    "<div class='value'></div>          <div class='label'>Comment</div>          " +
    "<div class='value'></div>        </div>      </div>      " +
    "<div style='clear: both;'></div>      <div class='CSSTableGenerator'>" +
    "<table width='100%' border='0' cellspacing='0'              cellpadding='5' style='font-size: 90%;'>        " +
    "<tr style='font-weight:bold'>          <th>Resource Name</th>          " +
    "<th>Resource Start Time</th>          <th>Resource End Time</th>          " +
    "<th>Comment</th>        </tr><tr>  <td>Nurse - 000</td>          <td>DT</td>          " +
    "<td>DT</td>          <td></td>        </tr> </table>    " +
    "<!-- table generator --></div> <!-- report row --></div> " +
    "<hr class='report' />    " +
    "<div class='reportRow'>      <div class='secondaryNameBlock'>       " +
    "<div class='mainName'>DT - DT</div>      " +
    "</div>      <div class='statusBlockContainer'> ";

SRH.dailyOverviewOutput1c = " <div class='label boldReportField'>Subject</div>          <div class='value boldReportField'>FN</div>          " +
    "<div class='label'>MRN</div>          <div class='value'>894001</div>          <div class='label'>Gender</div>          " +
    "<div class='value'>Unreported</div>          <div class='label'>Birth Date</div>          " +
    "<div class='value'>10/29/1901</div>          <div class='label'>Local ID</div>          " +
    "<div class='value'>1201</div>          <div class='label'>IRB #</div>          " +
    "<div class='value'>2015P00001</div>          <div class='label'>Visit Name</div>          " +
    "<div class='value'>Visit 001</div>          <div class='label'>Visit Type</div>          " +
    "<div class='value'>Outpatient CRC</div>          <div class='label'>Sublocation</div>          " +
    "<div class='value'>CNY001</div>          <div class='label'>Is part of the Visit Off Unit?</div>          " +
    "<div class='value'>NO</div>          <div class='label'>Check-In</div>          <div class='value'></div>          " +
    "<div class='label'>Check-Out</div>          <div class='value'></div>          " +
    "<div class='label'>Comment</div>          <div class='value'></div>        </div>      </div>      " +
    "<div style='clear: both;'></div>      <div class='CSSTableGenerator'>" +
    "<table width='100%' border='0' cellspacing='0'              cellpadding='5' style='font-size: 90%;'>        " +
    "<tr style='font-weight:bold'>          <th>Resource Name</th>          <th>Resource Start Time</th>          " +
    "<th>Resource End Time</th>          <th>Comment</th>        </tr><tr>  <td>Nurse - 001</td>          " +
    "<td>DT</td>          <td>DT</td>          <td></td>        </tr>";

SRH.dailyOverviewExport1 = 'Visit Length,Subject,MRN,Gender,Birth Date,Local ID,IRB #,Visit Name,' +
    'Visit Type,Sublocation,Is part of the Visit Off Unit?,Resource Name,Start Time,End Time,Comment' +
    ' "DT - DT","FN","894000","Unreported","10/29/1900","1200","2015P00000","Visit 000","Outpatient CRC",' +
    '"CNY000","NO","undefined","DT","DT",""' +
    ' "DT - DT","FN","894001","Unreported","10/29/1901","1201","2015P00001","Visit 001","Outpatient CRC",' +
    '"CNY001","NO","undefined","DT","DT",""';

///////// off unit //////////////

SRH.offUnitResult0 = {
    birthdate: "Dec 25, 1980 12:00:00 AM",
    bookedVisitId: 34000,
    comment: null,
    firstName: "MANUAL0",
    genderName: "Female",
    irb: "2001T000",
    lastName: "TESTER000",
    localId: "TA-000",
    middleName: null,
    mrn: "999006",
    resourceEndTime: "Feb 4, 2017 8:00:00 AM",
    resourceName: "Snack - WHT 13",
    resourceStartTime: "Feb 4, 2017 7:00:00 AM",
    scheduledEndTime: "Feb 4, 2017 8:00:00 AM",
    scheduledStartTime: "Feb 4, 2017 7:00:00 AM",
    visitName: "Out TA-000 Non CRC",
    visitStatus: "Scheduled",
    visitType: "Outpatient Non CRC"
};
SRH.offUnitResult1 = {
    birthdate: "Dec 25, 1980 12:01:00 AM",
    bookedVisitId: 34001,
    comment: null,
    firstName: "MANUAL1",
    genderName: "Female",
    irb: "2001T001",
    lastName: "TESTER001",
    localId: "TA-001",
    middleName: null,
    mrn: "999006",
    resourceEndTime: "Feb 4, 2017 8:01:00 AM",
    resourceName: "Snack - WHT 13",
    resourceStartTime: "Feb 4, 2017 7:01:00 AM",
    scheduledEndTime: "Feb 4, 2017 8:01:00 AM",
    scheduledStartTime: "Feb 4, 2017 7:01:00 AM",
    visitName: "Out TA-001 Non CRC",
    visitStatus: "Checked-In",
    visitType: "Outpatient Non CRC"
};

SRH.offUnitStatusScheduled = " <div class='statusBlock reportSecondary'> ";
SRH.offUnitStatusCheckedIn = " <div class='statusBlock reportCheckedInSecondary'> ";

SRH.offUnitOut1a = "    <div class='reportRow'>      <div class='secondaryNameBlock'>       " +
    "<div class='mainName'>Out TA-000 Non CRC - Outpatient Non CRC</div>      </div>      " +
    "<div class='statusBlockContainer'> ";

SRH.offUnitOut1b = "<div class='label'>Subject Name</div>          <div class='value'>FN</div>          " +
    "<div class='label'>MRN</div>          <div class='value'>999006</div>          " +
    "<div class='label'>Gender</div>          <div class='value'>Female</div>          " +
    "<div class='label'>Birth Date</div>          <div class='value'>12/25/1980</div>          " +
    "<div class='label'>Local ID</div>          <div class='value'>TA-000</div>          " +
    "<div class='label'>IRB #</div>          <div class='value'>2001T000</div>          " +
    "<div class='label'>Visit Start Time</div>          <div class='value'>DT</div>          " +
    "<div class='label'>Visit End Time</div>          <div class='value'>DT</div>          " +
    "<div class='label'>Comment</div>          <div class='value'>null</div>        " +
    "</div>      </div>      <div style='clear: both;'></div>      " +
    "<div class='CSSTableGenerator'><table width='100%' border='0' " +
    "cellspacing='0' cellpadding='5' style='font-size: 90%;'>        " +
    "<tr style='font-weight:bold'>          <th>Resource Name</th>          " +
    "<th>Resource Start Time</th>          <th>Resource End Time</th>        " +
    "</tr><tr>  <td>Snack - WHT 13</td>          <td>DT</td>          " +
    "<td>DT</td>          <td> </td>        </tr> </table>    " +
    "<!-- table generator --></div> <!-- report row --></div> " +
    "<hr class='report' />    <div class='reportRow'>      " +
    "<div class='secondaryNameBlock'>       " +
    "<div class='mainName'>Out TA-001 Non CRC - Outpatient Non CRC</div>      " +
    "</div>      <div class='statusBlockContainer'> ";

SRH.offUnitOut1c = "<div class='label'>Subject Name</div>          <div class='value'>FN</div>          " +
    "<div class='label'>MRN</div>          <div class='value'>999006</div>          " +
    "<div class='label'>Gender</div>          <div class='value'>Female</div>          " +
    "<div class='label'>Birth Date</div>          <div class='value'>12/25/1980</div>          " +
    "<div class='label'>Local ID</div>          <div class='value'>TA-001</div>          " +
    "<div class='label'>IRB #</div>          <div class='value'>2001T001</div>          " +
    "<div class='label'>Visit Start Time</div>          <div class='value'>DT</div>          " +
    "<div class='label'>Visit End Time</div>          <div class='value'>DT</div>          " +
    "<div class='label'>Comment</div>          <div class='value'>null</div>        " +
    "</div>      </div>      <div style='clear: both;'></div>      " +
    "<div class='CSSTableGenerator'><table width='100%' border='0' " +
    "cellspacing='0' cellpadding='5' style='font-size: 90%;'>        " +
    "<tr style='font-weight:bold'>          <th>Resource Name</th>          " +
    "<th>Resource Start Time</th>          <th>Resource End Time</th>        " +
    "</tr><tr>  <td>Snack - WHT 13</td>          <td>DT</td>          " +
    "<td>DT</td>          <td> </td>        </tr>" +
    "<tr class='altRow'> <td>Snack - WHT 13</td> <td>DT</td> <td>DT</td> <td> </td> </tr>";

SRH.offUnitExport1 = 'Visit Name-Visit Type,Subject Name,MRN,Gender,Birth Date,Local ID,IRB #,' +
    'Visit Start Time,Visit End Time,Comment,Resource Name,Start Time,End Time ' +
    '"Out TA-000 Non CRC - Outpatient Non CRC","FN","999006","Female","12/25/1980","TA-000","2001T000",' +
    '"DT","DT","null","undefined","DT","DT" ' +
    'Visit Name-Visit Type,Subject Name,MRN,Gender,Birth Date,Local ID,IRB #,Visit Start Time,' +
    'Visit End Time,Comment,Resource Name,Start Time,End Time ' +
    '"Out TA-001 Non CRC - Outpatient Non CRC","FN","999006","Female","12/25/1980","TA-001",' +
    '"2001T001","DT","DT","null","undefined","DT","DT" ,,,,,,,,,,"undefined","DT","DT"';

///////// billable resources //////////////

SRH.billableResourcesResult0 = {
    bookedVisitComment: "",
    catalystId: "AS000",
    checkInTime: "Feb 4, 2017 9:20:59 AM",
    checkOutTime: "Feb 4, 2017 9:21:00 AM",
    crcFunded: false,
    genderName: null,
    industryInitiated: false,
    irb: "2003AS000",
    localId: "AS000",
    mrn: "111103",
    piFirstName: "AutoStudy",
    piLastName: "Staff100",
    piMiddleName: "",
    resourceActivities: " ",
    resourceActivitiesComment: " ",
    resourceActivitiesQuantity: " ",
    resourceId: 47,
    resourceName: "Ancillary Only - Non CRC0",
    scheduledEndTime: "Feb 1, 2017 7:30:00 AM",
    scheduledStartTime: "Feb 1, 2017 7:00:00 AM",
    studyName: "Auto Study 000",
    subjectFirstName: "AUTO",
    subjectLastName: "TESTER100",
    subjectMiddleName: null,
    sublocationName: "Non CRC",
    visitName: "Out AS001 Non CRC",
    visitTypeName: "Outpatient Non CRC"
};
SRH.billableResourcesResult1 = {
    bookedVisitComment: "",
    catalystId: "AS001",
    checkInTime: "Feb 4, 2017 9:20:59 AM",
    checkOutTime: "Feb 4, 2017 9:21:00 AM",
    crcFunded: false,
    genderName: null,
    industryInitiated: false,
    irb: "2003AS001",
    localId: "AS001",
    mrn: "111103",
    piFirstName: "AutoStudy",
    piLastName: "Staff101",
    piMiddleName: "",
    resourceActivities: " ",
    resourceActivitiesComment: " ",
    resourceActivitiesQuantity: " ",
    resourceId: 48,
    resourceName: "Ancillary Only - Non CRC1",
    scheduledEndTime: "Feb 1, 2017 7:30:00 AM",
    scheduledStartTime: "Feb 1, 2017 7:00:00 AM",
    studyName: "Auto Study 001",
    subjectFirstName: "AUTO",
    subjectLastName: "TESTER101",
    subjectMiddleName: null,
    sublocationName: "Non CRC",
    visitName: "Out AS001 Non CRC",
    visitTypeName: "Outpatient Non CRC"
};

SRH.billableResourcesOut1 = "<div class='reportRow'> <div class='secondaryNameBlock'> " +
    "<div class='mainName'>Ancillary Only - Non CRC0</div> </div> <div class='clear'></div>" +
    " <div class='CSSTableGenerator billingResourcesReport'> <table>" +
    " <tr class='stdReport_columnHeading100px'> " +
    "<th> Check-In </th> <th> " +
    "Check-Out </th> <th> Visit Duration </th> <th>" +
    " Study Subject </th> <th> MRN </th> " +
    "<th> Study Name </th> <th> " +
    "Catalyst ID </th> <th> Local ID </th> <th>" +
    " IRB # </th> <th> PI </th> <th>" +
    " Industry Initiated </th> <th> CRC Approved Study " +
    "</th> <th> Visit Name </th> <th> " +
    "Visit Type </th> <th> Sublocation </th> <th>" +
    " Resource Start Time </th> <th> " +
    " Resource End Time </th> <th> Booked Visit " +
    "Comment </th> </tr><tr> <td style='word-wrap: break-word;'>DT</td> <td style='word-wrap: " +
    "break-word;'>DT</td> <td style='word-wrap: break-word;'>undefined</td> <td style='word-wrap: " +
    "break-word;'>FN</td> <td style='word-wrap: break-word;'>111103</td> <td style='word-wrap: " +
    "break-word;'>Auto Study 000</td> <td style='word-wrap: break-word;'>AS000</td> " +
    "<td style='word-wrap: break-word;'>AS000</td> <td style='word-wrap: break-word;'>2003AS000</td> " +
    "<td style='word-wrap: break-word;'>FN</td> <td style='word-wrap: break-word;'>No</td> " +
    "<td style='word-wrap: break-word;'>No</td> <td style='word-wrap: break-word;'>Out AS001 " +
    "Non CRC</td> <td style='word-wrap: break-word;'>Outpatient Non CRC</td> <td style='word-wrap: " +
    "break-word;'>Non CRC</td> <td style='word-wrap: break-word;'>DT</td> <td style='word-wrap: " +
    "break-word;'>DT</td> <td style='word-wrap: break-word;'></td> </tr> </table> " +
    "<!-- table generator --></div> <!-- report row --></div>  " +
    "<hr class='report' /> ";

SRH.billableResourcesOut2 = "<div class='reportRow'> " +
    "<div class='secondaryNameBlock'> <div class='mainName'>Ancillary Only - Non CRC1</div> " +
    "</div> <div class='clear'></div> " +
    "<div class='CSSTableGenerator billingResourcesReport'>" +
    " <table>" +
    " <tr class='stdReport_columnHeading100px'> " +
    "<th> Check-In </th> <th> " +
    "Check-Out </th> <th> Visit Duration </th> <th> " +
    " Study Subject </th> <th> " +
    "MRN </th> <th> Study Name </th> <th>" +
    " Catalyst ID </th> <th> Local ID </th> <th> " +
    " IRB # </th> <th> PI </th> " +
    "<th> Industry Initiated </th> <th>" +
    " CRC Approved Study </th> <th> Visit Name " +
    "</th> <th> Visit Type </th> <th>" +
    " Sublocation </th> <th> Resource " +
    "Start Time </th> <th> Resource " +
    "End Time </th> <th> Booked Visit Comment </th> </tr><tr> " +
    "<td style='word-wrap: break-word;'>DT</td> <td style='word-wrap: break-word;'>DT</td> " +
    "<td style='word-wrap: break-word;'>undefined</td> <td style='word-wrap: break-word;'>FN</td> " +
    "<td style='word-wrap: break-word;'>111103</td> <td style='word-wrap: break-word;'>Auto " +
    "Study 001</td> <td style='word-wrap: break-word;'>AS001</td> <td style='word-wrap: " +
    "break-word;'>AS001</td> <td style='word-wrap: break-word;'>2003AS001</td> <td " +
    "style='word-wrap: break-word;'>FN</td> <td style='word-wrap: break-word;'>No</td> " +
    "<td style='word-wrap: break-word;'>No</td> <td style='word-wrap: break-word;'>Out AS001 " +
    "Non CRC</td> <td style='word-wrap: break-word;'>Outpatient Non CRC</td> <td style='word-wrap: " +
    "break-word;'>Non CRC</td> <td style='word-wrap: break-word;'>DT</td> <td style='word-wrap: " +
    "break-word;'>DT</td> <td style='word-wrap: break-word;'></td> </tr><tr class='altRow'> <td " +
    "style='word-wrap: break-word;'>DT</td> <td style='word-wrap: break-word;'>DT</td> <td " +
    "style='word-wrap: break-word;'>undefined</td> <td style='word-wrap: break-word;'>FN</td> " +
    "<td style='word-wrap: break-word;'>111103</td> <td style='word-wrap: break-word;'>Auto " +
    "Study 001</td> <td style='word-wrap: break-word;'>AS001</td> <td style='word-wrap: " +
    "break-word;'>AS001</td> <td style='word-wrap: break-word;'>2003AS001</td> <td " +
    "style='word-wrap: break-word;'>FN</td> <td style='word-wrap: break-word;'>No</td> " +
    "<td style='word-wrap: break-word;'>No</td> <td style='word-wrap: break-word;'>Out " +
    "AS001 Non CRC</td> <td style='word-wrap: break-word;'>Outpatient Non CRC</td> <td " +
    "style='word-wrap: break-word;'>Non CRC</td> <td style='word-wrap: break-word;'>DT</td> " +
    "<td style='word-wrap: break-word;'>DT</td> " +
    "<td style='word-wrap: break-word;'></td> </tr>";

SRH.billableResourcesExport1 = 'Resource Name,Check In,Check Out,Visit Duration,Study Subject' +
    ',MRN,Industry Initiated,CRC Approved Study,Sublocation,Study Name,Catalyst ID,Local ID,IRB #,PI,Visit ' +
    'Name,Visit Type,Resource Start Time,Resource End Time, Booked Resource ' +
    'Activities, Booked Resource Activities Comment, Booked Resource Activities Quantity, Booked Visit ' +
    'Comment ' +
    'undefined,"DT","DT","undefined","FN","111103","No","No","Non CRC","undefined","AS000","AS000","2003AS000","FN",' +
    '"Out AS001 Non CRC","Outpatient Non CRC","Feb 1, 2017 7:00:00 AM","Feb 1, 2017 7:30:00 AM","undefined",' +
    '"undefined","undefined","","" ' +
    'undefined,"DT","DT","undefined","FN","111103","No","No","Non CRC","undefined","AS001","AS001","2003AS001","FN",' +
    '"Out AS001 Non CRC","Outpatient Non CRC","Feb 1, 2017 7:00:00 AM","Feb 1, 2017 7:30:00 AM","undefined",' +
    '"undefined","undefined","","" undefined,"DT","DT","undefined","FN","111103","No","No","Non CRC",' +
    '"undefined","AS001","AS001","2003AS001","FN","Out AS001 Non CRC","Outpatient Non CRC","Feb 1, 2017 ' +
    '7:00:00 AM","Feb 1, 2017 7:30:00 AM","undefined","undefined","undefined","",""';
