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

var AppointmentSearch = {};

var AppointmentSearchForm = {};
AppointmentSearchForm.isSearching = false;
AppointmentSearchForm.Schedule = {};
AppointmentSearchForm.Overbook = {};
AppointmentSearchForm.selectedVisit = {};
AppointmentSearchForm.selectedSubject = {};
AppointmentSearchForm.NO_SUBJECT_ASSIGNED = "No Subject Assigned";

AppointmentSearchForm.init = function () {
    $("#apptSearchRestart").hide();
    $("#apptSearchOverbook").hide();
    $("#apptSearchDayOfWeek").hide();

    WidgetUtil.ExpandableSection.hideSection('apptSearchResourceHeading', 'apptSearchResourceExpandIcon', 'apptSearchTemplateResources');

    var comboBoxWidth = 400;
    WidgetUtil.createComboBox($("#apptSearchStudy"), {
        err: function () {
        },
        valueField: 'id',
        textField: 'truncatedLabel',
        limitToList: false,
        width: comboBoxWidth,
        mode: "remote",
        method: "get",
        loader: AppointmentSearchForm.studyLoader,
        onChange: function (newValue, oldValue) {
            $(".apptSearchVisitDuration").empty();
            $("#apptSearchInvestigatorLabel").empty();
            $("#apptSearchInvestigator").empty();

            AppointmentSearchForm.selectedStudy = lookUpComboboxDataByValue($("#apptSearchStudy"), newValue, "id");

            if (newValue.length === 0) {
                AppointmentSearchForm.restart(false);
            } else if (AppointmentSearchForm.selectedStudy === null) {
                AppointmentSearchForm.restart(true);
            } else {
                AppointmentSearchForm.resetResourcesTable();
                $("#apptSearchRestart").show();

                $("#apptSearchVisit").combobox("enable");
                $("#apptSearchVisit").combobox("setValue", "");
                $("#apptSearchVisit").combobox("reload");
                $("#apptSearchHold").prop("disabled", false);
                $("#apptSearchHold").prop("checked", false);

                $("#apptSearchSubject").combobox("enable");
                $("#apptSearchSubject").combobox("setValue", "");
                $("#apptSearchSubject").combobox("reload");
            }
        },
        unSelect: function (record) {
            $("#apptSearchVisit").combobox("setValue", "");
            $("#apptSearchVisit").combobox("disable");
        }
    });

    $("#apptSearchStudy").combobox("textbox").on("blur", function () {
        AppointmentSearchForm._limitToList($("#apptSearchStudy"));
    });

    WidgetUtil.createComboBox($("#apptSearchVisit"), {
        err: function () {
        },
        disabled: true,
        limitToList: false,
        valueField: 'id',
        textField: 'truncatedLabel',
        width: comboBoxWidth,
        mode: "remote",
        method: "get",
        loader: AppointmentSearchForm.visitLoader,
        onChange: function (newValue, oldValue) {
            AppointmentCalendar.removeAppointmentsByClass(['NotFound', 'Searching']);

            if (newValue.length != 0) {
                var record = lookUpComboboxDataByValue($("#apptSearchVisit"), newValue, "id");
                if (record !== null) {
                    var displayTime = AppointmentSearchForm.calculateVisitDuration(record.visitDuration);
                    var visitDurationText = record.visitDuration + ' minutes' + ' (' + displayTime + ')';
                    $("#apptSearchVisitDurationLabel").text("Visit Duration:");
                    $("#apptSearchVisitDuration").text(visitDurationText);

                    $("#searchApptResourceToggle").prop("disabled", false);
                    AppointmentSearchForm.populateResourcesTable();

                    var startLabel = $("#apptSearch_startDateLabel").text();

                    if (MiscUtil.isOutpatientVisit(record.visitTypeId)) {
                        if (startLabel !== "Start Date") {
                            $("#apptSearch_startDateLabel").text("Start Date");
                            $("#apptSearch_startTimeLabel").text("Start Time");
                            $("#apptSearch_endDateLabel").text("End Date");
                            $("#apptSearch_endTimeLabel").text("End Time");
                            $("#apptSearchDayOfWeek").show();
                        }
                    } else {
                        $("#apptSearch_startDateLabel").text("Earliest Start Date");
                        $("#apptSearch_startTimeLabel").text("Earliest Start Time");
                        $("#apptSearch_endDateLabel").text("Latest End Date");
                        $("#apptSearch_endTimeLabel").text("Latest End Time");
                        $("#apptSearchDayOfWeek").hide();
                    }

                    AppointmentSearchForm.selectedVisit = record;
                    AppointmentSearchForm._setDateAndTimeDefaultValues();
                    AppointmentSearchForm.checkForRoomResources(AppointmentSearchForm.selectedVisit.id);
                    AppointmentSearchForm.checkDisplaySearchOrReserveVisit();

                    var isInpatient = MiscUtil.isInpatientVisit(AppointmentSearchForm.selectedVisit.visitTypeId);

                    if (isInpatient && !AppointmentSearchForm.selectedVisit.relativeTime) {
                        $("#apptSearchOverbookStartTime").hide();
                    } else {
                        $("#apptSearchOverbookStartTime").show();
                    }

                    var isSubjectSelected = $("#apptSearchSubject").combobox("getValue").length > 0;
                    var isHoldAppt = $("#apptSearchHold").is(":checked");

                    if (isSubjectSelected || isHoldAppt) {
                        AppointmentSearchForm.enableSearchRange();
                    }
                } else {
                    $("#apptSearchVisitDurationLabel").text("");
                    $("#apptSearchVisitDuration").text("");
                    AppointmentSearchForm._clearDateAndTimeDefaultValues();
                    $("#apptSearchSearch").hide();
                    $("#apptSearchOverbookBtn").hide();
                    AppointmentSearchForm.disableSearchRange();
                }
            } else {
                $("#apptSearchVisitDurationLabel").text("");
                $("#apptSearchVisitDuration").text("");

                var startDateLabel = $("#apptSearch_startDateLabel").text();
                if (startDateLabel !== "Earliest Start Date") {
                    $("#apptSearch_startDateLabel").text("Earliest Start Date");
                    $("#apptSearch_startTimeLabel").text("Earliest Start Time");
                    $("#apptSearch_endDateLabel").text("Latest End Date");
                    $("#apptSearch_endTimeLabel").text("Latest End Time");
                    $("#apptSearchDayOfWeek").hide();
                }
                AppointmentSearchForm._clearDateAndTimeDefaultValues();
                AppointmentSearchForm.disableSearchRange();
                ApptTemplateResourceTable.currentTable.clearTableBody();
                $("#apptSearchVisit").combobox("reload");
            }

            AppointmentSearchForm.clearErrors();
            AppointmentSearchForm.clearOverbookForm();
        }
    });
    $("#apptSearchVisit").combobox("textbox").on("blur", function () {
        AppointmentSearchForm._limitToList($("#apptSearchVisit"));
    });

    WidgetUtil.createComboBox($("#apptSearchSubject"), {
        err: function () {
        },
        disabled: true,
        limitToList: false,
        width: comboBoxWidth,
        valueField: 'id',
        textField: 'text',
        mode: "remote",
        method: "get",
        loader: AppointmentSearchForm.subjectLoader,
        onChange: function (newValue, oldValue) {
            AppointmentCalendar.removeAppointmentsByClass(['NotFound', 'Searching']);

            if (newValue.length != 0) {
                var record = lookUpComboboxDataByValue($("#apptSearchSubject"), newValue, "id");

                if (AppointmentSearchForm.isStudyAndVisitAndSubjectProvided() && record != null) {
                    AppointmentSearchForm.enableSearchRange();
                } else {
                    AppointmentSearchForm.disableSearchRange();
                }

                AppointmentSearchForm.selectedSubject = record;
            } else {
                AppointmentSearchForm.disableSearchRange();
                AppointmentSearchForm.clearOverbookForm();
                $("#apptSearchSubject").combobox("reload");
            }
            AppointmentSearchForm.checkDisplaySearchOrReserveVisit();

            AppointmentSearchForm.clearErrors();
        }
    });

    $("#apptSearchSubject").combobox("textbox").on("blur", function () {
        AppointmentSearchForm._limitToList($("#apptSearchSubject"));
    });

    var options = {
        minDate: "+0M +0D",
        preventTextInput: true,
        startDateElem: "#apptSearch_startDate",
        endDateElem: "#apptSearch_endDate",
        startTimeElem: "#apptSearch_startTime",
        endTimeElem: "#apptSearch_endTime"
    };

    WidgetUtil.DateTimeAndSpecificDaySection(options);

    $("#apptSearchStudyIcon").tooltip({"content": "<div class='apptSearchInfo'> Filter studies on Local ID and Name </div>"});
    $("#apptSearchVisitIcon").tooltip({"content": "<div class='apptSearchInfo'> Filter visits on visit name </div>"});
    $("#apptSearchSubjectIcon").tooltip({"content": "<div class='apptSearchInfo'> Filter subjects on full last name or MRN </div>"});

    ApptTemplateResourceTable.create();

    if (UserRoleUtil.userIsCrcStaff()) {
        AppointmentSearchForm.Overbook.initOverbookForm();
    } else {
        $("#apptSearchTypeOverbookMain").hide();
        $("#apptSearchHoldMain").hide();
        $("#apptSearchTypeScheduleMain").hide();
        $.ajax({
            type: "GET",
            url: "rest/appointment/getSchedulingRestriction",
            data: "",
            success: function (data) {
                var dataInt = parseInt(data);
                var dayText;
                if (dataInt !== 0) {
                    dataInt === 1 ? dayText = " day " : dayText = " days ";
                    var warningText = "You are not able to schedule appointments within " + data + dayText + "of the appointment start time. CRC Administrators should be contacted for such bookings.";
                    if (!$("#schedulingRestrictionWarning").text()) {
                        $("#schedulingRestrictionWarning").append(warningText);
                    }
                }
            }
        });
    }
    AppointmentSearchForm.disable();

    $("#apptSearchStudy").combobox("textbox").parent().find(".textbox-addon").click(function () {
        $("#apptSearchStudy").combobox("setValue", "");
        $("#apptSearchStudy").combobox("reload");
    });
};

AppointmentSearchForm._limitToList = function (element) {
    var comboPanelVisible = $(element).combobox("panel").is(":visible");
    if (!validComboboxValue($(element)) && !comboPanelVisible) {
        $(element).combobox("reload");
    }
};

AppointmentSearchForm.isStudyAndVisitAndSubjectProvided = function () {
    var isStudySelected = $("#apptSearchStudy").combobox("getValue").length > 0;
    var isVisitSelected = $("#apptSearchVisit").combobox("getValue").length > 0;
    var isSubjectSelected = $("#apptSearchSubject").combobox("getValue").length > 0;
    var isHoldAppt = $("#apptSearchHold").is(":checked");

    return isStudySelected && isVisitSelected && (isSubjectSelected || isHoldAppt);
};

AppointmentSearchForm.checkDisplaySearchOrReserveVisit = function () {
    var isOverbook = $("#apptSearchTypeOverbook").is(":checked");
    var isStudySelected = $("#apptSearchStudy").combobox("getValue").length > 0;
    var isVisitSelected = $("#apptSearchVisit").combobox("getValue").length > 0;
    var isSubjectSelected = $("#apptSearchSubject").combobox("getValue").length > 0;
    var isHoldAppt = $("#apptSearchHold").is(":checked");

    if (isStudySelected && isVisitSelected && (isSubjectSelected || isHoldAppt)) {
        $("#apptSearchOverbookBtn").remove();
        $("#apptSearchSearch").remove();
        if (isOverbook) {
            var overbookBtn = $('<input type="button" id="apptSearchOverbookBtn"/>').val("Reserve Visit");
            overbookBtn.click(AppointmentSearchForm.Overbook.performOverbook).addClass("formButton");
            $("#apptSearchActions").append(overbookBtn);
        } else {
            var searchBtn = $('<input type="button" id="apptSearchSearch"/>').val("Search");
            searchBtn.click(AppointmentSearchForm.Schedule.searchApptAvailability).addClass("formButton");
            $("#apptSearchActions").append(searchBtn);
        }
    } else {
        $("#apptSearchOverbookBtn").hide();
        $("#apptSearchSearch").hide();
    }
};

AppointmentSearchForm._clearDateAndTimeDefaultValues = function () {
    $("#apptSearch_startDate").datepicker().val("");
    $("#apptSearch_endDate").datepicker().val("");
    $("#apptSearch_startTime").timepicker("setTime", "00:00").val("");
    $("#apptSearch_endTime").timepicker("setTime", "00:00").val("");
};

AppointmentSearchForm._setDateAndTimeDefaultValues = function () {
    var visitDuration = AppointmentSearchForm.selectedVisit.visitDuration;
    var durationMins = visitDuration % 60;
    var min = durationMins % 60;
    var extrahr = visitDuration - min;
    var hr = extrahr / 60;
    var hr_value = Math.floor(hr) + 7;
    var min_value = min;
    var date = new Date();
    $("#apptSearch_startDate").datepicker('setDate', date);
    $("#apptSearch_endDate").datepicker('option', 'minDate', date);
    $("#apptSearch_endDate").datepicker('setDate', date);
    $("#apptSearch_startTime").timepicker('setTime', "07:00");

    $("#apptSearchDayOfWeek .week_days").prop("checked", false);
    $("#apptSearchDayOfWeek .weekday").prop("checked", false);
    $("#apptSearchDayOfWeek .weekend").prop("checked", false);

    $("#apptSearchDayOfWeek .week_days").prop("disabled", true);
    $("#apptSearchDayOfWeek .weekday").prop("disabled", true);
    $("#apptSearchDayOfWeek .weekend").prop("disabled", true);

    var startDateDatepicker = $("#apptSearch_startDate").datepicker('getDate');
    var endDateDatepicker = $("#apptSearch_endDate").datepicker('getDate');
    adjustSpecificDayCheckboxes(startDateDatepicker, null, endDateDatepicker);

    var search_endTimeVal;

    if (visitDuration < 1005) {
        if (min_value == 0 && hr_value < 10) {
            search_endTimeVal = '0' + hr_value + ':0' + min_value;
        } else if (min_value > 0 && hr_value > 9) {
            search_endTimeVal = hr_value + ':' + min_value;
        } else if (min_value > 0 && hr_value < 10) {
            search_endTimeVal = '0' + hr_value + ':' + min_value;
        } else if (min_value == 0 && hr_value > 9) {
            search_endTimeVal = hr_value + ':0' + min_value;
        }
    } else {
        var durationHours = visitDuration / 60;
        var enddate = date.setHours(7 + durationHours);
        search_endTimeVal = '23:45';
        $('#apptSearch_endDate').datepicker('setDate', new Date(enddate));
    }
    $('#apptSearch_endTime').val(search_endTimeVal);
};

AppointmentSearchForm.studyLoader = function (param, success, error) {
    var filterString = param.q || '';
    filterString = $.trim(filterString);
    var url = 'rest/study/getOpenStudies?&page=1&maxResults=-1&orderBy=ASC&sortBy=s.localId&filterString=' + encodeURIComponent(filterString);

    $.getJSON(url, function (data) {
        var items = $.map(data.studys, function (study, index) {
            var truncatedLabel = study.localId + ' - ' + study.name.substring(0, 50);
            if (study.name.length > 50) {
                truncatedLabel += "...";
            }
            return {
                id: study.id,
                truncatedLabel: truncatedLabel,
                localId: study.localId,
                name: study.name
            };
        });

        items.unshift({id: "", truncatedLabel: ""});
        success(items);
    }).fail(function () {
        error.apply(this, arguments);
    });
};

AppointmentSearchForm.visitLoader = function (param, success, error) {
    var filterString = param.q || '';
    filterString = $.trim(filterString);
    var selectedStudy = $("#apptSearchStudy").combobox("getValue");
    var studyData = lookUpComboboxDataByValue($("#apptSearchStudy"), selectedStudy, "id");

    if (selectedStudy.length !== 0 && studyData !== null) {
        var url = "rest/appointment/getStudyVisits?page=1&maxResults=-1&orderBy=ASC&sortBy=v.name&approved=true&study="
            + selectedStudy + "&filterString=" + encodeURIComponent(filterString);

        $.getJSON(url,
            function (data) {
                var items = $.map(data.visitTemplate1s, function (visit, index) {
                    var record = visit;
                    var truncatedLabel = visit.name.substring(0, 50);
                    if (visit.name.length > 50) {
                        truncatedLabel += "...";
                    }
                    visit.truncatedLabel = truncatedLabel;
                    visit.investigatorFullName = data.investigatorFullName;
                    return record;
                });

                items.unshift({id: "", name: "", truncatedLabel: ""});

                success(items);
                if (filterString.length == 0) {
                    $("#apptSearchVisit").combobox("setValue", "");
                }

                $("#apptSearchInvestigatorLabel").text("Investigator:");
                $("#apptSearchInvestigator").text(data.investigatorFullName);

            }).fail(function () {
            error.apply(this, arguments);
        });
    }
};

AppointmentSearchForm.calculateVisitDuration = function (duration) {
    var durationMins = duration % 60;
    var min = durationMins % 60;
    var extrahr = duration - min;
    var hr = extrahr / 60;
    var days = Math.floor(duration / 24 / 60);
    var hours = Math.floor(duration / 60 % 24);
    var displayTime = days + ' d, ' + hours + ' hr, ' + min + ' min ';
    if (days < 1) {
        displayTime = hours + ' hr, ' + min + ' min ';
    } else if (days < 1 && hours < 1) {
        displayTime = min + ' min ';
    }

    return displayTime;
};

AppointmentSearchForm.subjectLoader = function (param, success, error) {
    var filterString = param.q || '';
    var selectedStudy = $("#apptSearchStudy").combobox("getValue");
    var studyData = lookUpComboboxDataByValue($("#apptSearchStudy"), selectedStudy, "id");

    $("#apptSearchSubject").combobox("setValue", filterString.toUpperCase());

    var searchClause = "";
    if (filterString.length > 0) {
        var subjectOrMrnSearch = {
            searchItems: [
                {
                    orSearchItems: [{
                        key: "mrn",
                        value: $.trim(filterString),
                        searchType: Filter.Type.EXACT
                    }, {
                        key: "lastName",
                        value: $.trim(filterString),
                        searchType: Filter.Type.EXACT
                    }]
                }]
        };
        subjectOrMrnSearch = JSON.stringify(subjectOrMrnSearch);

        searchClause = "&search=" + encodeURIComponent(subjectOrMrnSearch);
    }
    if (selectedStudy.length !== 0 && studyData !== null) {
        var url = 'rest/study/getStudySubjects?study=' + selectedStudy + '&page=1&maxResults=-1&orderBy=ASC&sortBy=' +
            'lastName' + searchClause;

        $.getJSON(url, function (data) {
            var items = $.map(data.studySubject1s, function (subject, index) {
                var text = subject.subjectLastName + ", " + subject.subjectFirstName + " - " + subject.subjectMRN
                    + " (" + MiscUtil.genderShortName(subject.subjectSchedulerGender) + ")";
                return {
                    id: subject.id,
                    subjectMrn: subject.subjectMRN,
                    subjectMrnId: subject.subjectMrnId,
                    subjectLastName: subject.subjectLastName,
                    subjectFirstName: subject.subjectFirstName,
                    subjectSchedulerGender: subject.subjectSchedulerGender,
                    text: text
                };
            });

            items.unshift({id: "", text: ""});

            success(items);

            if (filterString.length == 0) {
                $("#apptSearchSubject").combobox("setValue", "");
            }
        }).fail(function () {
            error.apply(this, arguments);
        });
    }
};

AppointmentSearchForm.disable = function () {
    $("#apptSearchVisit").combobox("disable");
    $("#apptSearchSubject").combobox("disable");

    $("#apptSearchHold").prop("disabled", true);
    $("#searchApptResourceToggle").prop("disabled", true);
    AppointmentSearchForm.disableSearchRange();
};

AppointmentSearchForm.handleTooltip = function (elem) {
    $(elem).attr("title", "");
    $(elem).tooltip('open');
};

AppointmentSearchForm.Schedule.validate = function (schedulingRestriction) {
    AppointmentSearchForm.clearErrors();

    var isValid = true;
    var errorMsg = "";

    var startTime = $("#apptSearch_startTime").val() !== "" ? $("#apptSearch_startTime").val() : null;
    var endTime = $("#apptSearch_endTime").val() !== "" ? $("#apptSearch_endTime").val() : null;

    var startDateTime = new Date($("#apptSearch_startDate").val() + " " + startTime);
    var endDateTime = new Date($("#apptSearch_endDate").val() + " " + endTime);

    if (startDateTime.toString() === "Invalid Date") {
        isValid = false;
    }

    if (endDateTime.toString() === "Invalid Date") {
        isValid = false;
    }

    if (isValid && startDateTime.getTime() >= endDateTime.getTime()) {
        errorMsg = "Start Date/Time is after End Date/Time";
        isValid = false;
    }

    var difference = endDateTime.getTime() - startDateTime.getTime();
    var totalSearchDuration = difference / 60000;
    if (isValid && AppointmentSearchForm.selectedVisit.visitDuration > totalSearchDuration) {
        isValid = false;
        errorMsg = "Selected time range is smaller than the visit duration";
    }

    if (MiscUtil.isOutpatientVisit(AppointmentSearchForm.selectedVisit.visitTypeId)) {
        var selectedDaysOfWeek = WidgetUtil.DateTimeAndSpecificDaySection.getSelectedDaysOfWeek("#apptSearchDayOfWeek");

        if (selectedDaysOfWeek.length === 0) {
            errorMsg = 'Please select the days for the search range.';
            isValid = false;
        }
    }

    if (!MiscUtil.isNotUndefinedOrNullOrEmpty(schedulingRestriction) && schedulingRestriction !== 0) {
        var startDateDiff = startDateTime.getTime() < new Date().addDays(schedulingRestriction).getTime();

        if (!UserRoleUtil.userIsCrcStaff() && startDateDiff) {
            errorMsg = 'There is a scheduling restriction. Please see above.';
            $("#schedulingRestrictionWarning").addClass("redBorder");
            isValid = false;
        }
    }

    if (!isValid) {
        AppointmentSearchForm.Schedule.showError(errorMsg);
    }
    return isValid;
};

AppointmentSearchForm.Schedule.clearError = function () {
    $("#apptSearch_scheduleValidation").empty();
};

AppointmentSearchForm.Schedule.showError = function (msg) {
    AppointmentSearchForm.Schedule.clearError();

    var errorSpan = $("<span></span>").addClass("formValidationRequired").addClass("inlineError");
    errorSpan.text(msg);
    errorSpan.css("white-space", "nowrap");
    $("#apptSearch_scheduleValidation").append(errorSpan);
    errorSpan.show();
};

AppointmentSearchForm.Schedule.searchApptAvailability = function () {
    AppointmentSearchForm.clearErrors();
    $.ajax({
        type: "GET",
        url: "rest/appointment/getSchedulingRestriction",
        data: "",
        success: function (data) {
            var schedulingRestriction = parseInt(data);

            if (AppointmentSearchForm.Schedule.validate(schedulingRestriction)) {
                AppointmentSearchForm.isSearching = true;

                no_appointments_found = true;
                erroMsgFlag = true;

                startDateVal = null;
                searching_appointments = true;
                $('#filter_loading').css({display: 'none'});

                sessionStorage.setItem("filteredString", '');
                $.blockUI();
                $('#loading').css({display: 'block'});

                var events = home_calendar.getEvents();
                events.forEach(function (event) {
                    if (event.className === 'NotFound' || event.className === 'Searching') {
                        event.remove();
                    }
                });

                if (MiscUtil.isOutpatientVisit(AppointmentSearchForm.selectedVisit.visitTypeId)) {
                    AppointmentSearchForm.doOutPatientApptSearch();
                } else {
                    AppointmentSearchForm.doInPatientApptSearch();
                }
            }
        }
    });
};

AppointmentSearchForm.Schedule.displayScheduleForm = function () {
    $("#apptSearchOverbook").hide();
    $("#apptSearchSchedule").show();

    AppointmentSearchForm.checkDisplaySearchOrReserveVisit();
};

AppointmentSearchForm.Overbook.initOverbookForm = function () {
    var comboboxWidth = 490;
    if (UserRoleUtil.userIsCrcStaff()) {
        WidgetUtil.createComboBox($("#apptSearchOverbookReason"), {
            err: function () {
            },
            valueField: "id",
            textField: "name",
            data: Overbook.overrideReasons,
            width: comboboxWidth
        });

        $("#apptSearchOverbookReason").combobox("textbox").on("blur", function () {
            AppointmentSearchForm._limitToList($("#apptSearchOverbookReason"));
        });

        var overrideRooms = [{
            id: "",
            name: "assign me a random room"
        }];

        overrideRooms = overrideRooms.concat(Overbook.overrideRooms);
        WidgetUtil.createComboBox($("#apptSearchOverbookRoom"), {
            err: function () {
            },
            valueField: "id",
            textField: "name",
            limitToList: false,
            data: overrideRooms,
            width: comboboxWidth,
            onChange: function (newValue, oldValue) {
                AppointmentSearchForm.Overbook.randomRoomId = "";
                $("#overbookRoomSelectionMain").remove();

                var startDate = $("#apptSearchOverbookStartDate").datepicker().val();
                if (newValue == "" && MiscUtil.isNotUndefinedOrNullOrEmpty(startDate)) {
                    AppointmentSearchForm.Overbook.assignRandomRoom();
                }
            }
        });

        $("#apptSearchOverbookRoom").combobox("textbox").on("blur", function () {
            AppointmentSearchForm._limitToList($("#apptSearchOverbookRoom"));
        });

        WidgetUtil.createDatepicker($("#apptSearchOverbookStartDate"), {
            onClose: function (dateText, inst) {
                var isAssignRandomRoom = $('#apptSearchOverbookRoom').combobox("getValue") == '';

                var isInpatient = MiscUtil.isInpatientVisit(AppointmentSearchForm.selectedVisit.visitTypeId);

                var timeSet = true;
                if (!(isInpatient && !AppointmentSearchForm.selectedVisit.relativeTime) &&
                    $("#apptSearchOverbookStartTime").timepicker().val() == "") {
                    timeSet = false;
                }

                if (dateText && isAssignRandomRoom && timeSet) {
                    AppointmentSearchForm.Overbook.assignRandomRoom();
                }
            }
        }, true, true);

        $("#apptSearchOverbookStartTime").attr("placeholder", "HH:MM");
        WidgetUtil.createTimepicker($("#apptSearchOverbookStartTime"), {
            onSelect: function (datetimeText, datepickerInstance) {
                var isAssignRandomRoom = $('#apptSearchOverbookRoom').combobox("getValue") == '';
                var dateSet = $("#apptSearchOverbookStartDate").datepicker().val().length > 0;
                if (datetimeText && dateSet && isAssignRandomRoom) {
                    AppointmentSearchForm.Overbook.assignRandomRoom();
                }
            }
        }, true, true);

        WidgetUtil.commentBox($("#apptSearchOverbookComment"), {
            err: function () {
            },
            width: "400px",
            rows: 4,
            cols: 50
        });
    }
};

AppointmentSearchForm.Overbook.displayOverbookForm = function () {
    if (UserRoleUtil.userIsCrcStaff()) {
        $("#apptSearchSchedule").hide();
        $("#apptSearchOverbook").show();

        AppointmentSearchForm.checkDisplaySearchOrReserveVisit();
    }
};

AppointmentSearchForm.Overbook.clearError = function () {
    $("#apptSearch_overbookReasonValidation").empty();
    $("#apptSearch_overbookDateTimeValidation").empty();
};

AppointmentSearchForm.Overbook.showError = function (element, msg) {

    var errorSpan = $("<span></span>").addClass("formValidationRequired").addClass("inlineError");
    errorSpan.text(msg);
    $(element).append(errorSpan);
    errorSpan.show();
};

AppointmentSearchForm.Overbook.validate = function () {
    AppointmentSearchForm.Overbook.clearError();

    var isValid = true;
    var errorMsg = "";
    var overbookReason = $("#apptSearchOverbookReason").combobox("getValue");

    if (MiscUtil.isUndefinedOrNullOrEmpty(overbookReason)) {
        errorMsg = "Please select an override reason.";
        isValid = false;
        AppointmentSearchForm.Overbook.showError("#apptSearch_overbookReasonValidation", errorMsg);
    }

    var isOutpatient = MiscUtil.isOutpatientVisit(AppointmentSearchForm.selectedVisit.visitTypeId);

    if (AppointmentSearchForm.selectedVisit.relativeTime || isOutpatient) {
        var startTime = $("#apptSearchOverbookStartTime").val() !== "" ? $("#apptSearchOverbookStartTime").val() : null;

        var startDateTime = new Date($("#apptSearchOverbookStartDate").val() + " " + startTime);

        if (startDateTime.toString() === "Invalid Date") {
            errorMsg = "Please select a valid Start Date/Time";
            isValid = false;
            AppointmentSearchForm.Overbook.showError("#apptSearch_overbookDateTimeValidation", errorMsg);
        }
    } else {
        var startDate = $("#apptSearchOverbookStartDate").val();
        if (startDate.length === 0) {
            errorMsg = "Please select a valid Start Date";
            isValid = false;
            AppointmentSearchForm.Overbook.showError("#apptSearch_overbookDateTimeValidation", errorMsg);
        }
    }


    return isValid;
};

AppointmentSearchForm.Overbook.performOverbook = function () {
    if (AppointmentSearchForm.Overbook.validate()) {
        var overbook_startdateVal = $("#apptSearchOverbookStartDate").val();

        var isOutpatient = MiscUtil.isOutpatientVisit(AppointmentSearchForm.selectedVisit.visitTypeId);
        if (AppointmentSearchForm.selectedVisit.relativeTime || isOutpatient) {
            var overbook_starttimeVal = $("#apptSearchOverbookStartTime").timepicker().val();

            var overbook_startdateTimeVal = overbook_startdateVal + ' ' + overbook_starttimeVal;
            overbook_startdateVal = new Date(overbook_startdateTimeVal);
        } else {
            overbook_startdateVal = new Date(overbook_startdateVal);
        }

        var overrideRoomReasons = $('#apptSearchOverbookReason').combobox("getValue");
        var overrideRoomComment = $.trim(WidgetUtil.commentBox.getValue("#apptSearchOverbookComment"));

        var roomSelectedId = $('#apptSearchOverbookRoom').combobox("getValue");
        roomSelectedId = roomSelectedId === '' ? 0 : roomSelectedId;

        if (MiscUtil.isNotUndefinedOrNullOrEmpty(AppointmentSearchForm.Overbook.randomRoomId)) {
            roomSelectedId = AppointmentSearchForm.Overbook.randomRoomId;
        }

        if (overrideRoomReasons == null || overrideRoomReasons == '') {
            var confirmationMessage = "Please select an override reason.";
            util_showMainMessage(confirmationMessage, "", "red");
            return;
        }

        var apptSelectedStudy = AppointmentSearchForm.selectedStudy.id || 0;
        var apptSelectedVisit = AppointmentSearchForm.selectedVisit.id || 0;
        var apptSelectedSubjectMrnId = AppointmentSearchForm.selectedSubject ? AppointmentSearchForm.selectedSubject.subjectMrnId : 0;

        var jsonData = JSON.stringify({
            study: apptSelectedStudy,
            subjectMrnId: apptSelectedSubjectMrnId,
            visit: apptSelectedVisit,
            startDate: overbook_startdateVal.valueOf(),
            startTime: overbook_startdateVal.valueOf(),
            overrideReason: overrideRoomReasons,
            roomSelected: roomSelectedId,
            comment: overrideRoomComment
        });

        $.post("rest/appointment/overbookRoomAppointment", {data: jsonData}, function (data) {
            var startDate = $("#apptSearchOverbookStartDate").datepicker('getDate');
            $('#loading').css({display: 'none'});
            $('#filter_loading').css({display: 'none'});
            util_showMainMessage("Visit Reserved.", resetCalendarCallBack(new Date(startDate)));

            //clear the date and time
            $("#apptSearchOverbookStartDate").datepicker().val("");
            $("#apptSearchOverbookStartTime").timepicker("setTime", "00:00").val("");
        });
    }
};

AppointmentSearchForm.Overbook.assignRandomRoom = function () {
    var apptSelectedVisit = AppointmentSearchForm.selectedVisit.id || 0;
    var apptSelectedSubjectMrnId = AppointmentSearchForm.selectedSubject ? AppointmentSearchForm.selectedSubject.subjectMrnId : 0;

    var overbook_startdateVal = $("#apptSearchOverbookStartDate").val();
    var overbook_starttimeVal = $("#apptSearchOverbookStartTime").timepicker().val();

    var overbook_startdateTimeVal = overbook_startdateVal + ' ' + overbook_starttimeVal;
    var apptOverbookStartDate = (new Date(overbook_startdateTimeVal)).valueOf();

    var jsonData = JSON.stringify({
        subjectMrnId: apptSelectedSubjectMrnId,
        visit: apptSelectedVisit,
        startDate: apptOverbookStartDate
    });

    $.post("rest/appointment/overbookRoomSelected", {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);

        if (MiscUtil.isNotUndefinedOrNull(parsedData.overbookRoomSelectedId)) {
            $("#overbookRoomSelectionMain").remove();
            var overbookRoomDiv = $("<div></div>", {"id": "overbookRoomSelectionMain"});
            var overbookRoomSelection = $("<span></span>", {"id": "overbookRoomSelection"});
            overbookRoomDiv.text("Selected Room: ");
            overbookRoomSelection.text(parsedData.overbookRoomSelected);
            overbookRoomDiv.append(overbookRoomSelection);
            $("#apptSearchOverbookRoomTd").append(overbookRoomDiv);

            AppointmentSearchForm.Overbook.randomRoomId = parsedData.overbookRoomSelectedId;
        } else {
            util_showMainMessage(parsedData.overbookRoomSelected);
        }
    });
};

AppointmentSearchForm.enableSearchRange = function () {
    $("#apptSearchTypeSchedule").prop("disabled", false);
    $("#apptSearchTypeOverbook").prop("disabled", false);
    $("#apptSearch_startDate").datepicker("option", "disabled", false);
    $("#apptSearch_endDate").datepicker("option", "disabled", false);
    $("#apptSearch_startTime").prop("disabled", false);
    $("#apptSearch_endTime").prop("disabled", false);

    if (UserRoleUtil.userIsCrcStaff()) {
        $("#apptSearchOverbookReason").combobox("enable");
        $("#apptSearchOverbookRoom").combobox("enable");
        $("#apptSearchOverbookStartDate").datepicker("option", "disabled", false);
        $("#apptSearchOverbookStartTime").prop("disabled", false);
        $("#apptSearchOverbookComment").find("textarea").prop("disabled", false);
    }

    $("#apptSearchDayOfWeek .week_days:checked").prop("disabled", false);
    $("#apptSearchDayOfWeek .weekday:checked").prop("disabled", false);
    $("#apptSearchDayOfWeek .weekend:checked").prop("disabled", false);
};

AppointmentSearchForm.disableSearchRange = function () {
    $("#apptSearchTypeSchedule").click();

    $("#apptSearchTypeSchedule").prop("disabled", true);
    $("#apptSearchTypeOverbook").prop("disabled", true);
    $("#apptSearch_startDate").datepicker("option", "disabled", true);
    $("#apptSearch_endDate").datepicker("option", "disabled", true);
    $("#apptSearch_startTime").prop("disabled", true);
    $("#apptSearch_endTime").prop("disabled", true);

    if (UserRoleUtil.userIsCrcStaff()) {
        $("#apptSearchOverbookReason").combobox("disable");
        $("#apptSearchOverbookRoom").combobox("disable");
        $("#apptSearchOverbookStartDate").datepicker("option", "disabled", true);
        $("#apptSearchOverbookStartTime").prop("disabled", true);
        $("#apptSearchOverbookComment").find("textarea").prop("disabled", true);
        AppointmentSearchForm.Overbook.clearError();
    }

    $("#apptSearchDayOfWeek .week_days").prop("disabled", true);
    $("#apptSearchDayOfWeek .weekday").prop("disabled", true);
    $("#apptSearchDayOfWeek .weekend").prop("disabled", true);
};

AppointmentSearchForm.clearComboboxValue = function (element) {
    $(element).combobox("setValue", "");
    $(element).combobox("reload");
};

AppointmentSearchForm.holdAppt = function () {
    var holdAppt = $("#apptSearchHold").is(":checked");
    if (holdAppt) {
        AppointmentSearchForm.selectedSubject = {
            subjectMrnId: 0,
            name: AppointmentSearchForm.NO_SUBJECT_ASSIGNED
        };
        AppointmentSearchForm.clearComboboxValue($("#apptSearchSubject"));
        $("#apptSearchSubject").combobox("disable");
        if (AppointmentSearchForm.isStudyAndVisitAndSubjectProvided()) {
            AppointmentSearchForm.enableSearchRange();
        }
    } else {
        $("#apptSearchSubject").combobox("enable");
        AppointmentSearchForm.disableSearchRange();
        AppointmentSearchForm.clearErrors();
        AppointmentSearchForm.clearOverbookForm();
    }
    AppointmentSearchForm.checkDisplaySearchOrReserveVisit();
};

AppointmentSearchForm.resetResourcesTable = function () {
    ApptTemplateResourceTable.currentTable.paginationWidget.setRecordsPerPage(ApptTemplateResourceTable.defaultPerPage);
    ApptTemplateResourceTable.currentTable.paginationWidget.setCurrentPageNumber(1);

    ApptTemplateResourceTable.currentTable.clearTableBody();
};

AppointmentSearchForm.populateResourcesTable = function () {
    ApptTemplateResourceTable.currentTable.paginationWidget.setRecordsPerPage(ApptTemplateResourceTable.defaultPerPage);
    ApptTemplateResourceTable.currentTable.paginationWidget.setCurrentPageNumber(1);

    ApptTemplateResourceTable.populate();
};

AppointmentSearchForm.checkForRoomResources = function () {
    $.getJSON("rest/appointment/getRoomResources?id=" + AppointmentSearchForm.selectedVisit.id, function (data) {
        AppointmentSearchForm.selectedVisit.roomResources = data.roomResources;

        if (MiscUtil.isNotUndefinedOrNull(AppointmentSearchForm.selectedVisit.roomResources)) {
            $("#apptSearchOverbookRoomRow").show();
        } else {
            $("#apptSearchOverbookRoomRow").hide();
        }
    });
};

AppointmentSearchForm.clearErrors = function () {
    AppointmentSearchForm.Schedule.clearError();
    AppointmentSearchForm.Overbook.clearError();
    $("#schedulingRestrictionWarning").removeClass("redBorder");
};

AppointmentSearchForm.restart = function (excludeStudy) {

    if (!excludeStudy) {
        $("#apptSearchStudy").combobox("clear");
        $("#apptSearchStudy").combobox("reload");
        AppointmentSearchForm.disable();
    }

    $("#apptSearchVisit").combobox("clear");
    $("#apptSearchSubject").combobox("setValue", "");

    $("#apptSearchHold").prop("checked", false);
    $("#apptSearchTypeSchedule").click();
    $("#apptSearchDayOfWeek").hide();

    $("#apptSearchSearch").hide();
    $("#apptSearchOverbookBtn").hide();
    AppointmentSearchForm._clearDateAndTimeDefaultValues();

    AppointmentSearchForm.clearErrors();

    AppointmentCalendar.removeAppointmentsByClass(['NotFound', 'Searching']);

    $("#searchApptResourceToggle").prop("disabled", false);
    AppointmentSearchForm.resetResourcesTable();

    AppointmentSearchForm.clearOverbookForm();

    if ($("#apptSearchStudy").combobox("getValue").length == 0 || !validComboboxValue($("#apptSearchStudy"))) {
        AppointmentSearchForm.disable();
        $("#apptSearchRestart").hide();
    } else {
        AppointmentSearchForm.disableSearchRange();
    }
};


AppointmentSearchForm.clearOverbookForm = function () {
    if (UserRoleUtil.userIsCrcStaff()) {
        $("#apptSearchOverbookReason").combobox("clear");
        $("#apptSearchOverbookStartDate").datepicker().val("");
        $("#apptSearchOverbookStartTime").timepicker("setTime", "00:00").val("");
        $("#apptSearchOverbookRoom").combobox("clear");
        WidgetUtil.commentBox.clearValue("#apptSearchOverbookComment");
        AppointmentSearchForm.Overbook.randomRoomId = null;
        $("#overbookRoomSelectionMain").empty();
    }
};

AppointmentSearchForm.searchAvailabilityComplete = function () {
    $('#loading').css({display: 'none'});
    $('#filter_loading').css({display: 'none'});

    if ($.active == 1) {
        $('#appt_wizard_changeAppointmentSearch').css({display: "inline-block"});
        //edit mrn here? TBD
        $.unblockUI();
        if (erroMsgFlag == true) {
            var confirmationMessage = "No Available Appointments Found. Scroll through to view unavailable appointments.";
            util_showMainMessage(confirmationMessage);
        } else if (no_appointments_found == true) {
            var confirmationMessage = "No Available Appointments were found. Please select another date range.";
            util_showMainMessage(confirmationMessage);
        } else if (erroMsgFlag == false && no_appointments_found == false) {
            var confirmationMessage = "Available Appointments Found. Scroll through to select an appointment.";
            util_showMainMessage(confirmationMessage);
        }
    }
};

AppointmentSearchForm.searchAvailabilitySuccess = function (data) {
    if (data instanceof Array && data.length > 0) {
        no_appointments_found = false;
        addCalendarEvents(home_calendar, data);
        home_calendar.gotoDate(startDateVal);
        AppointmentCalendar.scrollToCalendar();
    }
};

AppointmentSearchForm.searchAvailabilityError = function (xhr, status, error) {
    // check status && error
};

AppointmentSearchForm.doOutPatientApptSearch = function () {
    removeEventsWithGivenClassNames(home_calendar, [SEARCHING, NOT_FOUND]);
    startDateVal = $("#apptSearch_startDate").datepicker('getDate');

    search_startDateVal = $("#apptSearch_startDate").datepicker('getDate');
    search_endDateVal = $("#apptSearch_startDate").datepicker('getDate');
    var timeDiff = Math.abs($("#apptSearch_endDate").datepicker('getDate').getTime() - $("#apptSearch_startDate").datepicker('getDate').getTime());
    var totaldays = Math.ceil(timeDiff / (1000 * 3600 * 24));

    var startDate = $('#apptSearch_startDate').datepicker("getDate");
    var endDate = $('#apptSearch_endDate').datepicker("getDate");
    for (var i = 0; i <= totaldays; i++) {
        var sDay;
        if (i > 0) {
            var date1 = new Date(startDate);
            date1.setDate(date1.getDate() + 1);
            startDate = new Date(date1);
            sDay = new Date(startDate);
        } else {
            sDay = new Date(startDate);
        }
        startDate = new Date(sDay);
        endDate = new Date(sDay);

        search_startDateVal = showDate(startDate);
        search_endDateVal = showDate(startDate);

        search_startTimeVal = $("#apptSearch_startTime").timepicker().val();
        search_endTimeVal = $("#apptSearch_endTime").timepicker().val();

        var search_startdateTimeVal = search_startDateVal + ' ' + search_startTimeVal;
        search_startDateVal = new Date(search_startdateTimeVal);

        var search_enddateTimeVal = search_endDateVal + ' ' + search_endTimeVal;
        search_endDateVal = new Date(search_enddateTimeVal);

        var subjectMrnId = AppointmentSearchForm.selectedSubject ? AppointmentSearchForm.selectedSubject.subjectMrnId : 0;

        var selectedDaysOfWeek = WidgetUtil.DateTimeAndSpecificDaySection.getSelectedDaysOfWeek("#apptSearchDayOfWeek");
        if ($.inArray(sDay.getDay().toString(), selectedDaysOfWeek) != -1) {
            var searchurl = 'rest/appointment/getSearchedVisit?subjectMrnId=' + subjectMrnId
                + '&visit=' + AppointmentSearchForm.selectedVisit.id
                + '&startDate=' + search_startDateVal.valueOf()
                + '&endDate=' + search_endDateVal.valueOf()
                + '&isInpatient=false';

            requests.push($.ajax({
                type: 'POST',
                url: searchurl,
                data: "",

                success: AppointmentSearchForm.searchAvailabilitySuccess,
                error: AppointmentSearchForm.searchAvailabilityError,
                complete: AppointmentSearchForm.searchAvailabilityComplete,

                dataType: 'json',
                async: true
            }));
        }
    }
};

AppointmentSearchForm.doInPatientApptSearch = function () {
    removeEventsWithGivenClassNames(home_calendar, [SEARCHING, NOT_FOUND]);
    startDateVal = $("#apptSearch_startDate").datepicker('getDate');

    if (AppointmentSearchForm.selectedVisit.relativeTime === true) {
        search_startDateVal = $("#apptSearch_startDate").datepicker('getDate');
        search_endDateVal = $("#apptSearch_endDate").datepicker('getDate');
        var timeDiffInMillis = $("#apptSearch_endDate").datepicker('getDate') - $("#apptSearch_startDate").datepicker('getDate');
        var timeDiffInMinutes = Math.floor(timeDiffInMillis / 60000);
        var totaldays = Math.floor(timeDiffInMinutes / MINS_PER_DAY);
        var totalVisitDuration = AppointmentSearchForm.selectedVisit.visitDuration;

        // within one day
        if (totalVisitDuration < MINS_PER_DAY) {
            search_startDateVal = $("#apptSearch_startDate").val();
            search_endDateVal = $("#apptSearch_endDate").val();

            search_startTimeVal = $("#apptSearch_startTime").timepicker().val();
            search_endTimeVal = $("#apptSearch_endTime").timepicker().val();

            var search_startdateTimeVal = search_startDateVal + ' ' + search_startTimeVal;
            search_startDateVal = new Date(search_startdateTimeVal);

            var search_enddateTimeVal = search_endDateVal + ' ' + search_endTimeVal;
            search_endDateVal = new Date(search_enddateTimeVal);

            var searchurl = 'rest/appointment/getSearchedVisit?subjectMrnId=' + AppointmentSearchForm.selectedSubject.subjectMrnId
                + '&visit=' + AppointmentSearchForm.selectedVisit.id
                + '&startDate=' + search_startDateVal.valueOf()
                + '&endDate=' + search_endDateVal.valueOf()
                + '&isInpatient=true';

            requests.push($.ajax({
                type: 'POST',
                url: searchurl,
                data: "",

                success: AppointmentSearchForm.searchAvailabilitySuccess,
                error: AppointmentSearchForm.searchAvailabilityError,
                complete: AppointmentSearchForm.searchAvailabilityComplete,

                dataType: 'json',
                async: true
            }));
        } // within one day

        else { // totalVisitDuration > MINS_PER_DAY, i.e., more than 1 day in duration

            var search_absoluteEndDateVal = $("#apptSearch_endDate").val();
            var search_absoluteEndTimeVal = $("#apptSearch_endTime").timepicker().val();
            var search_absoluteEndDateTimeVal = search_absoluteEndDateVal + ' ' + search_absoluteEndTimeVal;

            var search_absoluteEndDate = new Date(search_absoluteEndDateTimeVal);

            var startDate = $('#apptSearch_startDate').datepicker('getDate');
            for (var i = 0; i <= totaldays; i++) {

                if (i > 0) { // subsequent days increment the original value of startDate
                    var date1 = new Date(startDate);
                    date1.setDate(date1.getDate() + 1);
                    startDate = new Date(date1);
                }

                var search_startDateVal = showDate(startDate);
                var search_startTimeVal = $("#apptSearch_startTime").timepicker().val();
                var search_startDateTimeVal = search_startDateVal + ' ' + search_startTimeVal;
                var search_startDate = new Date(search_startDateTimeVal);

                // last possible match starts one day after current start, minus epsilon,
                //         so ends at one day + totalVisitDuration - epsilon

                var search_endDate = new addMinutes(search_startDate, MINS_PER_DAY + totalVisitDuration - 1);

                if (search_endDate > search_absoluteEndDate) {
                    search_endDate = search_absoluteEndDate;
                }

                var searchurl = 'rest/appointment/getSearchedVisit?subjectMrnId=' + AppointmentSearchForm.selectedSubject.subjectMrnId +
                    '&visit=' + AppointmentSearchForm.selectedVisit.id +
                    '&startDate=' + search_startDate.valueOf() +
                    '&endDate=' + search_endDate.valueOf() +
                    '&isInpatient=true';

                requests.push($.ajax({
                    type: 'POST',
                    url: searchurl,
                    data: "",

                    success: AppointmentSearchForm.searchAvailabilitySuccess,
                    error: AppointmentSearchForm.searchAvailabilityError,
                    complete: AppointmentSearchForm.searchAvailabilityComplete,

                    dataType: 'json',
                    async: true
                }));

            }  // for each day
        }  // more than one day
    }  // relative
    else // non-relative 'clock' time visit
    {
        search_startDateVal = $("#apptSearch_startDate").val();
        search_endDateVal = $("#apptSearch_endDate").val();

        search_startTimeVal = $("#apptSearch_startTime").timepicker().val();
        search_endTimeVal = $("#apptSearch_endTime").timepicker().val();

        var search_startDateTimeVal = search_startDateVal + ' ' + search_startTimeVal;
        search_startDateVal = new Date(search_startDateTimeVal);

        var search_endDateTimeVal = search_endDateVal + ' ' + search_endTimeVal;
        search_endDateVal = new Date(search_endDateTimeVal);

        var searchurl = 'rest/appointment/getSearchedVisit?subjectMrnId=' + AppointmentSearchForm.selectedSubject.subjectMrnId
            + '&visit=' + AppointmentSearchForm.selectedVisit.id
            + '&startDate=' + search_startDateVal.valueOf()
            + '&endDate=' + search_endDateVal.valueOf()
            + '&isInpatient=true';

        AppointmentSearchForm.searchDataHandler = function (data) {
            var confirmationMessage;
            var parsedData = JSON.parse(data);
            $.unblockUI();
            if (parsedData.result !== undefined && !parsedData.result) {
                $('#loading').css({display: 'none'});
                $('#filter_loading').css({display: 'none'});
                confirmationMessage = parsedData.errorMsg;
                util_showMainMessage(confirmationMessage);
                sessionStorage.setItem("filteredString", '');
            } else {
                $('#loading').css({display: 'none'});
                $('#filter_loading').css({display: 'none'});
                addCalendarEvents(home_calendar, parsedData);
                home_calendar.gotoDate(search_startDateVal);
                AppointmentCalendar.scrollToCalendar();
                confirmationMessage = "Available Appointments Found. Scroll through to select an appointment.";
                if (erroMsgFlag === true) {
                    confirmationMessage = "No Available Appointments Found. Scroll through to view unavailable appointments.";
                }
                util_showMainMessage(confirmationMessage);
            }
        };
        request = $.post(searchurl, {}, AppointmentSearchForm.searchDataHandler);
    }  // clock time
};

AppointmentSearchForm.scheduleDialog = function () {

    DialogsUtil.showConfirmationDialog("#schedule-dialog-confirm", {
        buttons: {
            "Yes": function () {
                $(this).dialog("close");
                $("#appt_wizard_scheduleAppointment").css({"display": "none"});
                $('#appt_wizard_changeAppointmentSearch').css({display: "none"});
                $.blockUI();
                scheduleComment = $.trim($("#scheduleWizard_comment_txtArea").val());
                double_start = start;
                $("#appt_wizard_scheduleAppointment").css({"display": "none"});
                var jsonData = JSON.stringify({
                    study: AppointmentSearchForm.selectedStudy.id,
                    subjectMrnId: AppointmentSearchForm.selectedSubject.subjectMrnId,
                    visit: AppointmentSearchForm.selectedVisit.id,
                    comment: scheduleComment,
                    user: user.id
                });
                var isInpatient = MiscUtil.isInpatientVisit(AppointmentSearchForm.selectedVisit.visitTypeId);

                var confirmUrl = 'rest/appointment/confirmAppointment?isInpatient=' +
                    isInpatient + '&startDate=' + Date.parse(start) + '&endDate=' + Date.parse(end);

                $.post(confirmUrl, {data: jsonData}, function (data) {
                    var parsedData = JSON.parse(data);

                    handleGenderBlockForNewVisit(parsedData, resetCalendarCallBack(new Date(start)));
                });
                AppointmentSearchForm.isSearching = false;
            },
            "No": function () {
                $("#appt_wizard_scheduleAppointment").css({"display": "inline"});
                $(this).dialog("close");
            }
        }
    });
};

AppointmentSearchForm.displaySearchNotFound = function (event) {
    $("#notAvailableResources").prop("checked", true);
    $("#commentTable").css({"display": "none"});
    $("#filterSearchedResources").css({"display": "inline"});
    $("#appt_wizard_scheduleAppointment").css({"display": "none"});
    AppointmentCalendar.populateApptSearchDialogs(event);
};

AppointmentSearchForm.displaySearchAppt = function (event) {
    $("#filterSearchedResources").css({"display": "none"});
    $("#commentTable").css({"display": "inline"});
    $("#appt_wizard_scheduleAppointment").css({"display": "inline"});

    AppointmentCalendar.populateApptSearchDialogs(event);

    WidgetUtil.commentBox($("#scheduleWizard_comment"), {width: "240px"});
};

var BookedResources = {};
BookedResources.loadBookedResourcesData = function (currentPage, event) {

    BookedResourcesTable.createTable(
        "searchResourcesTable",
        event.classNames[0],
        event.id,
        AppointmentSearchForm.isSearching,
        false,
        user,
        AppointmentSearchForm.selectedVisit.id,
        AppointmentSearchForm.selectedVisit.visitTypeId,
        AppointmentSearchForm.selectedSubject.subjectMrnId,
        showDateTime(event.start),
        showDateTime(event.end),
        searchResourcesTablePostPopulateFn
    );

    BookedResourcesTable.currentTables["searchResourcesTable"].reloadFn();
    BookedResourcesTable.currentTables["searchResourcesTable"].setCurrentPage(currentPage);
};

var AppointmentCalendar = {};
AppointmentCalendar.populateApptSearchDialogs = function (event) {
    appt_search_view = true;
    eventid = event.id;
    start = showDateTime(event.start);
    end = showDateTime(event.end);
    className = event.className;

    var subjectFullName = AppointmentSearchForm.NO_SUBJECT_ASSIGNED;
    if (MiscUtil.isNotUndefinedOrNullOrEmpty(AppointmentSearchForm.selectedSubject.subjectFirstName)
        && MiscUtil.isNotUndefinedOrNullOrEmpty(AppointmentSearchForm.selectedSubject.subjectLastName)) {
        subjectFullName = AppointmentSearchForm.selectedSubject.subjectFirstName + " " + AppointmentSearchForm.selectedSubject.subjectLastName;
    }
    $('#dialog_searchstudy_name_detail').text(AppointmentSearchForm.selectedStudy.name);
    $('#dialog_searchstudy_localid').text(AppointmentSearchForm.selectedStudy.localId);
    $('#dialog_searchstudy_piname_detail').text(AppointmentSearchForm.selectedVisit.investigatorFullName);
    $('#dialog_searchsubject_name_detail').text(subjectFullName);
    $('#dialog_searchsubject_mrn_detail').text(AppointmentSearchForm.selectedSubject.subjectMrn || "N/A");
    $('#dialog_searchsubject_gender_detail').text(AppointmentSearchForm.selectedSubject.subjectSchedulerGender || "N/A");
    $('#dialog_searchvisit_name_detail').text(AppointmentSearchForm.selectedVisit.name);
    $('#dialog_searchvisit_time_detail').text(AppointmentSearchForm.selectedVisit.visitDuration);
    $('#dialog_searchvisit_start_detail').text(showDateTime(event.start));
    $('#dialog_searchvisit_end_detail').text(showDateTime(event.end));
    resetUI();
    BookedResources.loadBookedResourcesData(currentPage, event);
    $("#appt_wizard_searchedDetails").dialog({modal: true, height: 800, width: 700, dialogClass: "scheduledEventDialog"});

    $.unblockUI();
};

AppointmentCalendar.apptCalendarEventClick = function (eventClickInfo) {
    var event = eventClickInfo.event;
    var jsEvent = eventClickInfo.jsEvent;
    var view = eventClickInfo.view;
    if (!AppointmentSearchForm.isSearching) {
        apptCalendarEventClick(event, jsEvent, view);
        return;
    }

    $.blockUI();
    $('.formTextAreaSmaller').css({display: "block"});
    $("#wizard_comment_txtArea").val('');
    $("#scheduleWizard_comment_txtArea").val('');
    $("#checkoutWizard_comment").val('');
    appt_list_view = false;
    initial_load = true;

    if (event.classNames.indexOf("NotFound") > -1) {
        AppointmentSearchForm.displaySearchNotFound(event)
    } else if (event.classNames.indexOf("Searching") > -1) {
        start = showDateTime(event.start);
        end = showDateTime(event.end);

        $.unblockUI();

        AppointmentSearchForm.displaySearchAppt(event);
        showConfirmationDialogForDoubleBookingSubject();
    } else if (event.classNames.indexOf("Scheduled") > -1 || event.classNames.indexOf("Hold") > -1) {
        displayScheduledAppt(event);
    } else if (event.classNames.indexOf("Checked-In") > -1) {
        displayedCheckedInAppt(event);
    } else if (event.classNames.indexOf("Checked-Out") > -1) {
        displayedCheckedOutAppt(event);
    } else if (event.classNames.indexOf("Cancellation") > -1) {
        displayCancelledAppt(event);
    } else {
        alert(event.classNames + ' event className: Not implemented');
    }
};

AppointmentCalendar.reset = function () {
    calendar_rendered = true;
    view = null;
    var starts = new Date(new Date(start).getFullYear(), new Date(start).getMonth(), 1);
    var ends = new Date(new Date(start).getFullYear(), new Date(start).getMonth() + 2, 0, 23, 59, 59, 59);
    var isNotMonthView = AppointmentCalendar.isMonthView() === false;
    var url = "rest/appointment/getCalendarBookedVisits?starting=" + starts.getTime() + "&ending=" + ends.getTime() + "&todayVisits=" + isNotMonthView + "&filterString=&filterId=";
    $('#initial_loading').css({display: 'block'});
    searching_appointments = false;
    start = double_start;
    $.ajax({
        type: 'GET',
        url: url,
        data: "",
        success: function (data) {
            reCreateCalendar(data.visits);
            $('#initial_loading').css({display: 'none'});
            $.unblockUI();
        },
        error: function (xhr, status, error) {
            // TODO: check status && error
        },
        dataType: 'json',
        async: false
    });
    if (start != null) {
        home_calendar.gotoDate(new Date(start));
    } else {
        home_calendar.gotoDate(new Date());
    }
};

AppointmentCalendar.showApptCalendarMonthView = function () {
    home_calendar.changeView('dayGridMonth');
};

AppointmentCalendar.scrollToCalendar = function () {
    var scrollPos = sessionStorage.getItem('scrollPos');
    if (scrollPos) {
        $('.fc-scroller').scrollTop(scrollPos);
    }
    else {
        $(window).scrollTop($('#appt_calendar').offset().top);
    }
};

AppointmentCalendar.removeAppointmentsByClass = function (apptClasses) {
    var events = home_calendar.getEvents();
    events.forEach(function (event) {
        var matchFound = false;
        apptClasses.forEach(function (apptClass) {
            if (event.className === apptClass) {
                matchFound = true;
            }
        });
        if (matchFound) event.remove();
    });
};

AppointmentCalendar.isDayView = function () {
    var isDayView = true;
    if (home_calendar.view.type !== undefined) {
        isDayView = home_calendar.view.type === "dayGridDay";
    }
    return isDayView;
};

AppointmentCalendar.isMonthView = function () {
    var isMonthView = false;
    if (home_calendar.view.type !== undefined) {
        isMonthView = home_calendar.view.type === "dayGridMonth";
    }
    return isMonthView;
};

AppointmentCalendar.eventTransform = function (event) {
    var endDate;
    if (event.allDay) {
        if (MiscUtil.isNotUndefinedOrNullOrEmpty(event.end)) {
            // cut out time and replace "-" with "/" to appease the IE overlords
            endDate = new Date(event.end.split(' ')[0].replace(/-/g, '/')).addDays(1);
        }
        else {
            endDate = new Date(event.start.split(' ')[0].replace(/-/g, '/')).addDays(2);
        }
        event.end = endDate;
    }
    return event;
};