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


var minuteMillis = (60 * 1000);
var resourceNames;

var resourcePage = (function () {
    var resourceTable;
    var bulkActivateMode = false;

    var initFn = function () {
        resetUI();
        commonInit();
        getResourceStaticLists();
        commonResourceScreenData();

        $.get("common_dialogs.html", function (content) {
            $('#main_dialog_wrapper').after(content);
        });

        app_runIdleTimer();
    };

    var initResourceTable = function(){
        var tableId = "resourceTable";

        var columns = [];

        if (bulkActivateMode) {
            columns.push(new Column({
                width: 5,
                extractDataFunction: function (val) {return val.id;},
                columnType: Column.Checkbox
            }));
        }

        columns.push(new Column({
            dbColumn: 'r.name',
            columnName: "Resource Name",
            width: 36,
            extractDataFunction: function (val) {return val.resource;},
            defaultSortOrder: Column.ASC,
            filter: {
                name: "resourceName",
                onFilter: function (value) {
                    reloadResourcesData(1);
                }
            }
        }));

        columns.push(new Column({
            dbColumn: 'r.resourceType',
            columnName: "Resource Type",
            width: 20,
            extractDataFunction: function (val) {return val.resourceType;},
            filter: {
                name: "resourceType",
                onFilter: function (value) {
                    reloadResourcesData(1);
                }
            }
        }));

        columns.push(new Column({
            dbColumn: 'rs.sublocation.name',
            columnName: "Sub-Location",
            width: 20,
            extractDataFunction: function (val) {return val.sublocation;},
            filter: {
                name: "resourceSublocation",
                onFilter: function (value) {
                    reloadResourcesData(1);
                }
            }
        }));

        columns.push(new Column({
            // The following works because the rs.active column in the database
            // can only be 0 or 1, so ordering by the negative of the
            // column's value returns 1's before 0's, where 1 means active
            // and 0 means inactive
            // This way, this column sorts in alphabetical order
            dbColumn: '-rs.active',
            columnName: "Status",
            width: 12,
            extractDataFunction: function (val) {return val.active ? "Active" : "Inactive";}
        }));

        if (!bulkActivateMode) {
            columns.push(new Column({
                width: 12,
                extractDataFunction: function () {return "More Actions";},
                columnType: Column.Hyperlink,
                rowElementHyperLink: function (val) {return "resource_handleResourceSelection(" + val.id + ")";}}));
        }

        resourceTable = new SchedulerTables({
            tableId: tableId,
            columns: columns,
            reloadFn: reloadResourcesData
        });
    };

    var setBulkActivateMode = function(bAMode)
    {
        bulkActivateMode = bAMode;
    };

    var isBulkActivateMode = function()
    {
        return bulkActivateMode;
    };

    var commonInit = function () {

        loadMetaHeaders();
        initFooter();
        eraseLicense();
    };

    var initDetailsFn = function () {
        resetUI();
        commonInit();
        commonData();
        resourceModuleRoles();
        app_selectedResource = JSON.parse(sessionStorage.getItem("resourceData"));
    };

    function commonResourceScreenData() {
        commonData();
        resourceModuleRoles();
        renderBreadcrumbs('resource_screen');
        initial_load = true;
        loadResourcesData({currentPage: 1, maxResults:100});
    }

    function initSubLocationAddition() {

        commonInit();
        getResourceStaticLists(createSublocationClosureData);

        app_runIdleTimer();
    }

    // TODO-XH: Is this for a standalone page or for resource_form.html?
    // if for resource_form.html then remove call to getResourceStaticLists()
    function initSublocationClosure() {
        getResourceStaticLists();
        commonInit();
        app_runIdleTimer();
        sublocationClosureScreenData();
    }

    function getResourceTable()
    {
        return resourceTable;
    }

    return {
        init: initFn,
        initDetails: initDetailsFn,
        initSublocationClosure: initSublocationClosure,
        initSublocationAddition: initSubLocationAddition,
        initResourceTable: initResourceTable,
        getResourceTable: getResourceTable,
        setBulkActivateMode: setBulkActivateMode,
        isBulkActivateMode: isBulkActivateMode
    };

}());


function resourceModuleRoles() {
    user = JSON.parse(sessionStorage.getItem("userData"));
    var visibility = {};
    if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
        visibility = {
            "resource_bulkActivate": "visible",
            "resource_addNewResource": "visible",
            "resource_sublocationClosure": "visible",
            "resource_editResource": "visible",
            "resource_addDefault": "visible",
            "resource_addAlternate": "visible",
            "resource_addTemporary": "visible"
        };
    } else if (UserRoleUtil.userIsScheduler()) {
        visibility = {
            "resource_bulkActivate": "hidden",
            "resource_addNewResource": "hidden",
            "resource_sublocationClosure": "visible",
            "resource_editResource": "hidden",
            "resource_addDefault": "hidden",
            "resource_addAlternate": "hidden",
            "resource_addTemporary": "hidden"
        };
    } else {
        // i.e. STUDY_STAFF, FRONT_DESK, GENERAL_VIEW, or anything else
        visibility = {
            "resource_bulkActivate": "hidden",
            "resource_addNewResource": "hidden",
            "resource_sublocationClosure": "hidden",
            "resource_editResource": "hidden",
            "resource_addDefault": "hidden",
            "resource_addAlternate": "hidden",
            "resource_addTemporary": "hidden"
        };
    }

    // TODO: stop using 'visibility' completely for the following 3 classes:
    // resource_bulkActivate
    // resource_addNewResource
    // resource_sublocationClosure
    $('.resource_bulkActivate')
        .css({
            visibility: visibility.resource_bulkActivate,
            display: visibility.resource_bulkActivate == 'visible' ? 'inline' : 'none'
        });
    $('.resource_addNewResource')
        .css({
            visibility: visibility.resource_addNewResource,
            display: visibility.resource_addNewResource == 'visible' ? 'inline' : 'none'
        });
    $('.resource_sublocationClosure')
        .css({
            visibility: visibility.resource_sublocationClosure,
            display: visibility.resource_sublocationClosure == 'visible' ? 'inline' : 'none'
        });
    // TODO: get rid of this -- .resource_editResource no long used, except in test
    $('.resource_editResource')
        .css({
            visibility: visibility.resource_editResource
        });
    // TODO: get rid of this -- .resource_editResource no long used, except in test
    $('.resource_addDefault')
        .css({
            visibility: visibility.resource_addDefault
        });
    // TODO: get rid of this -- .resource_editResource no long used, except in test
    $('.resource_addAlternate')
        .css({
            visibility: visibility.resource_addAlternate
        });
    // TODO: get rid of this -- .resource_editResource no long used, except in test
    $('.resource_addTemporary')
        .css({
            visibility: visibility.resource_addTemporary
        });

}

function sublocationClosureScreenData() {
    commonData();
    renderBreadcrumbs('resource_close_sub_list');
    setParentLocationHash("SublocationClosureInterval");
    resetUI();
    initial_load = true;
    loadSublocationClosureData(currentPage);
}

function createSublocationClosureData() {
    commonData();
    renderBreadcrumbs('resource_close_sub_form');
    sublocationClosureInterval_createWidgets();
    sublocation_clearForm();
}

function editResource() {
    sessionStorage.setItem("mode", JSON.stringify('edit'));
    setLocationHref("resource_form.html");
}

function cancelResourceClick() {
    clearSelectedResource();
    resourceModule();
}

function addNewResourceClick() {
    sessionStorage.setItem("mode", JSON.stringify('new'));
    clearSelectedResource();
    setLocationHref("resource_form.html");
}

function addSublocationClosureClick() {
    setLocationHref("add_sublocation_closure.html");
}


function sublocationClosureClick() {
    setLocationHref("sublocation_closure_screen.html");
}

function getResourceStaticLists(callback) {
    $.get("rest/app/getStaticLists", {}, function (data) {
        var parsedData = JSON.parse(data);

        sublocations = parsedData.sublocations;
        sublocations.sort(function (a, b) {
            if (a.name > b.name) {
                return 1;
            }
            else if (b.name > a.name) {
                return -1;
            }
            return 0;
        });
        sublocationSelectOptions = buildSelectOptions(sublocations, 'name');

        $("#sublocation_sublocation").html(sublocationSelectOptions);

        WidgetUtil.createComboBox("#sublocation_sublocation");

        $('#resourceSublocationSelect').html(buildSubLocCheckBoxes());
        createResourceCombobox("#resourceSublocationSelect", {onSelect: updateResourceNameSuffix});

        var resourceTypes = parsedData.resourceTypes;

        resourceNames = parsedData.resourceNames;

        if (!app_selectedResource) {
            resourceTypes.sort(function (a, b) {
                if (a > b) {
                    return 1;
                }
                else if (b > a) {
                    return -1;
                }
                return 0;
            });
            resourceTypeSelectOptions = buildSelectOptionsFromStringList(resourceTypes);
            $("#resourceTypeSelect").html(resourceTypeSelectOptions);
            createResourceCombobox("#resourceTypeSelect");
        }

        resources = parsedData.resources;

        Annotations.populateAnnotations(parsedData);

        if (callback) {
            callback();
        }
    });
}

var sublocationClosureToDelete;
var sublocationId = [];
var resource_taToRemove;
var resource_bulkActivate;
var resourceTableFilter;
var selected;


function resource_handleResourceSelection(id) {
    $.getJSON("rest/resource/getResourceDetail?resourceId=" + id, function (data) {
        sessionStorage.setItem("resourceData", JSON.stringify(data));
        app_selectedResource = JSON.parse(sessionStorage.getItem("resourceData"));
        sessionStorage.setItem("mode", JSON.stringify('view'));
        $.blockUI();
        setLocationHref("resource_form.html");
    });
}

function deleteSublocationDialog(id) {
    DialogsUtil.showConfirmationDialog("#delete-sublocation-dialog-confirm", {
        buttons: {
            "Yes": function () {
                sublocationClosureToDelete = id;
                jsonData = JSON.stringify({
                    sublocationClosureIntervalId: sublocationClosureToDelete,
                    userId: user.id
                });

                $.post("rest/resource/deleteSublocationClosureInterval", {data: jsonData}, function (data) {
                    $('#sublocationLoading').css({visibility: "hidden"});
                    var confirmationMessage = 'Sublocation Closure Deleted.';
                    util_showMainMessage(confirmationMessage);
                    currentPage = 1;
                    loadSublocationClosureData(currentPage);
                });
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
                return;
            }
        }
    });
}

//////////////////////Resource directory/////////////////////////////////////////////////////

var sortByResourceName = "r.name";
var sortByResourceType = "r.resourceType";
var sortByResourceSublocation = "rs.sublocation.name";
var sortByResourceSublocationActive = "rs.active";
var columns;

function loadResourcesData(args) {
    args.callback = displayResourcesData;
    getResourcesData(args);
}

function displayResourcesData(data, maxResults) {

    resourcePage.initResourceTable();

    //TODO: remove resourceTableControls as an argument
    resourcePage.getResourceTable().generateTable(data, maxResults, "resourceTableControls",
        function(currentPage, recordsPerPage)
        {
            reloadResourcesData(currentPage);
        });
}

function reloadResourcesData(currentPage) {

    var table = resourcePage.getResourceTable();

    if(currentPage) {
        table.setCurrentPage(currentPage);
    }

    var resourceStatus = $("#stateSelector").val();
    currentPage = currentPage || table.getCurrentPage();

    var args = {
        currentPage: currentPage,
        maxResults: table.getRecordsPerPage(),
        searchFilter: table.getAllFilterKeyValuePairs(),
        isBulkActivate: false,
        status: resourceStatus,
        orderBy: table.getSortOrder(),
        sortColumn: table.getSortColumn(),
        callback: function(data) {
            table.refreshTableBody(data);
        }
    };

    getResourcesData(args);
}

/*
 params: {
 currentPage
 maxResults,
 searchFilter,
 status,
 orderBy,
 sortColumn,
 callback
 }*/
function getResourcesData(args)
{
    if(!args) {
        args = {};
    }
    var searchQuery = {};

    args.searchFilter = args.searchFilter || [];
    args.maxResults = args.maxResults || 100;
    args.status = args.status || "active";
    args.orderBy = args.orderBy || "ASC";
    args.sortColumn = args.sortColumn || sortByResourceName;

    initial_load = false;

    searchQuery.searchItems = args.searchFilter;

    searchQuery = JSON.stringify(searchQuery);

    var getResourcesDataUrl = "rest/resource/getResourcesData?page=" + (args.currentPage != undefined ? args.currentPage : 1)
        + "&maxResults=" + args.maxResults
        + "&orderBy=" + args.orderBy
        + "&sortBy=" + args.sortColumn
        + "&status=" + args.status
        + "&search=" + encodeURIComponent(searchQuery);

    $.getJSON(getResourcesDataUrl, function (data) {
        resourcePage.attuneBulkActivateButtons(data);
        if(args.callback) {
            args.callback(data, args.maxResults, args.sortColumn, args.orderBy);
        }
    });
}

resourcePage.dataHasSomeInactive = function(data) {
    var hasSomeInactive = false;

    if (data !== undefined && data.length > 0) {
        for (var i=0, len=data.length; i<len; i++) {
            if ( ! data[i].active) {
                hasSomeInactive = true;
                break;
            }
        }
    }
    return hasSomeInactive;

};
resourcePage.attuneBulkActivateButtons = function(data) {

    if (resourcePage.isBulkActivateMode()) {
        var disabled = !resourcePage.dataHasSomeInactive(data);
        var activateButton = $('#resourceSaveBulkActivate');

        if (disabled) {
            activateButton.off('click');
            activateButton.addClass('disabled');
        }
        else {
            activateButton.on('click', saveBulkActivateResources);
            activateButton.removeClass('disabled');
        }
    }
};

function bulkActivateResourcesCall() {
    var anArray = resourcePage.getResourceTable().getSelectedRows();
    if (anArray.length !== 0) {
        DialogsUtil.showConfirmationDialog("#bulkActivateDialog", {
            buttons: {
                "Yes": function () {
                    jsonData = JSON.stringify({resourcesIds: anArray});
                    $.post("rest/resource/activateResources", {data: jsonData}, function (data) {
                        var parsedData = JSON.parse(data);
                        var confirmationMessage = "";
                        if (parsedData.result == true) {
                            confirmationMessage = "Success. Your resources have been activated.";
                            util_showMainMessage(confirmationMessage);
                            resourcePage.getResourceTable().clearAllSelectedRows();
                            updateResourceView("inactive", true);
                        }
                        else {
                            confirmationMessage = parsedData.errorMsg;
                            util_showMainMessage(confirmationMessage);
                        }
                    });
                    $(this).dialog("close");
                    clearBulkActivateErrors();
                },
                "No": function () {
                    clearBulkActivateErrors();
                    $(this).dialog("close");
                }
            }
        });
    }
    else {
        $('#bulkActivateResourceValidation').text('Please select at least one resource.');
        $('#bulkActivateResourceValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
    }
}

function clearResourceFilters() {
    resourcePage.getResourceTable().clearAllFilterKeyValuePairs();
}

function updateResourceView(activeFilter, isBulkActivate) {
    if (isBulkActivate === true) {
        clearResourceFilters();

        $("#resourceState").css({visibility: "hidden"});
        $("#resourceBulkActivate").css({display: "none"});
        $("#resourceCancelBulkActivate").css({display: "inline-block"});
        $("#resourceSaveBulkActivate").css({display: "inline-block"});
        SchedulerTables.clearSelectedCheckboxes("resourceTable");
    }
    else {
        $("#resourceState").css({visibility: "visible"});
        $("#resourceBulkActivate").attr("onclick", "javascript:bulkActivateResources();");
        $("#resourceBulkActivate span").html("Bulk Activate");
        $("#resourceBulkActivate").css({display: "inline-block"});
        $("#resourceCancelBulkActivate").css({display: "none"});
        $("#resourceSaveBulkActivate").css({display: "none"});
        clearBulkActivateErrors();
        SchedulerTables.clearSelectedCheckboxes("resourceTable");
    }
    $("#stateSelector").val(activeFilter);
    var currentPage = 1;
    reloadResourcesData(currentPage);
}

function bulkActivateResources() {
    clearResourceFilters();

    $("#resourceState").css({visibility: "hidden"});
    $("#resourceBulkActivate").css({display: "none"});
    $("#resourceCancelBulkActivate").css({display: "inline-block"});
    $("#resourceSaveBulkActivate").css({display: "inline-block"});
    SchedulerTables.clearSelectedCheckboxes("resourceTable");

    $("#stateSelector").val("inactive");
    var status = $("#stateSelector").val();

    resourcePage.setBulkActivateMode(true);

    loadResourcesData({
        currentPage:1,
        maxResults: 100,
        status: status
    });
}

function clearBulkActivateErrors() {
    $('#bulkActivateResourceValidation').text('');
    $('#bulkActivateResourceValidation').css({visibility: "hidden"});
}

function cancelBulkActivateMode() {
    $("#resourceState").css({visibility: "visible"});
    $("#resourceBulkActivate span").html("Bulk Activate");
    $("#resourceBulkActivate").css({display: "inline-block"});
    $("#resourceCancelBulkActivate").css({display: "none"});
    $("#resourceSaveBulkActivate").css({display: "none"});
    clearBulkActivateErrors();
    SchedulerTables.clearSelectedCheckboxes("resourceTable");

    $("#stateSelector").val("active");
    var resourceStatus = $("#stateSelector").val();

    resourcePage.setBulkActivateMode(false);

    loadResourcesData({
        currentPage:1,
        maxResults: 100,
        status: resourceStatus
    });
}

function saveBulkActivateResources() {
    bulkActivateResourcesCall();
}

function changeResourceStatus(resourceData) {
    var resourceId = resourceData.id;
    var sublocationId = resourceData.sublocationId;
    var active = resourceData.active;
    $.getJSON("rest/resource/changeResourceStatus?resourceId=" + resourceId + "&sublocationId=" + sublocationId + "&active=" + active, function (parsedData) {
        var state = active ? "Active" : "Inactive";
        if (parsedData.result) {
            util_showMainMessage("Changed status of resource " + resourceData.resource + " located at " + resourceData.sublocation + " to " + state);
        }
        else {
            util_showMainMessage("Error: Failed to change status of resource " + resourceData.resource + " located at " + resourceData.sublocation);
        }
    });
}

///////////////////////////Sublocation Closure/////////////////////////////////////////////////////////////////////

var sortBySublocationClosure = "sci.sublocation.name";
var sortBySublocationStartTime = "sci.startTime";
var sortBySublocationEndTime = "sci.endTime";
var sortBySublocationReason = "sci.reason";

function loadSublocationClosureData() {

    SublocationClosureTable.create();
    SublocationClosureTable.populate();

}

function sublocationClosureInterval_createWidgets() {
    var preventTextInput = true;
    WidgetUtil.createDatetimepicker("#sublocation_startDate", {
        onSelect: function (selectedDate) {
            var endDate = new Date(selectedDate);
            endDate.setTime(endDate.getTime() + (15 * minuteMillis));

            $("#sublocation_endDate").datetimepicker('setDate', endDate);
        }
    }, preventTextInput);
    WidgetUtil.createDatetimepicker("#sublocation_endDate", {}, preventTextInput);

    sublocation_clearForm();
}

function createResourceCombobox(element, config) {
    if (!config) {
        config = {}
    }
    config.width = 350;
    WidgetUtil.createComboBox(element, config);
}

function clearSelectedResource() {
    sessionStorage.setItem("resourceData", null);
    app_selectedResource = null;
}

function modifyResource(jsonData, activate, callBack) {
    var parsedJsonData = $.parseJSON(jsonData);
    var resourceName = parsedJsonData.resourceName;
    var sublocationId = parsedJsonData.sublocationId;

    $.post("rest/resource/modifyResource", {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);
        var confirmationMessage = "";
        if (parsedData.successful === true) {
            if (app_selectedResource.active == false && activate === true) {
                confirmationMessage = 'Success! This resource has been saved and activated.';
                $("#theActiveResource").css({display: "none"});
            }
            else {
                confirmationMessage = 'Success! This resource has been saved.';
            }
            resource_clearErrors();

            var sublocationData = lookUpComboboxDataByValue("#resourceSublocationSelect", sublocationId);
            var updatedResourceName = resourceName;
            updatedResourceName += " - " + sublocationData.text;

            var index = $.inArray(app_selectedResource.resource, resourceNames);
            resourceNames.splice(index, 1);
            app_selectedResource.resource = updatedResourceName;
            resourceNames.push(app_selectedResource.resource);

            sessionStorage.setItem("resourceData", JSON.stringify(app_selectedResource));
            $("#resource_selected_resource_name").text(updatedResourceName);

            util_showMainMessage(confirmationMessage);
        }
        else {
            confirmationMessage = parsedData.message;
            util_showMainMessage(confirmationMessage);
        }

        if (callBack !== undefined) {
            callBack();
        }
    });
}

/**
 *
 * @param params is expected to be of the form {active: true|false} when calling to create
 * a new subject. Otherwise left unspecified.
 */
function saveResourceClick(params) {
    resource_clearErrors();
    var isValid = true;
    var subLocationName = $("#resourceSublocationSelect").combobox("getText");
    var resourceName = $("#resourceNameInput").val();
    var fullResourceName = resourceName + " - " + subLocationName;
    if (resourceName.length < 1) {
        $('#resourceNameInputValidation').text('Please enter a Resource Name.');
        $('#resourceNameInputValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }
    else if ($.inArray(fullResourceName, resourceNames) != -1) {
        if (app_selectedResource) {
            if (!(fullResourceName === app_selectedResource.resource)) {
                $('#resourceNameInputValidation').text('This resource name already exists.');
                $('#resourceNameInputValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
                isValid = false;
            }
        }
        else {
            $('#resourceNameInputValidation').text('This resource name already exists.');
            $('#resourceNameInputValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
            isValid = false;
        }
    }
    else if (/^[a-zA-Z0-9()/ ,&-]*$/.test(resourceName) == false) {
        $('#resourceNameInputValidation').text('This resource name contains illegal characters.');
        $('#resourceNameInputValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }

    var resourceType;
    if (!app_selectedResource) {
        resourceType = $("#resourceTypeSelect").combobox("getValue");
        if (!resourceType) {
            $('#resourceTypeSelectValidation').text('Please select a Resource Type');
            $('#resourceTypeSelectValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
            isValid = false;
        }
    }

    var sublocationId = $("#resourceSublocationSelect").combobox("getValue");
    if (!sublocationId) {
        $('#resourceSublocationSelectValidation').text('Please select a Sub-Location');
        $('#resourceSublocationSelectValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }

    if (isValid == false) {
        return;
    }

    var activate = $("#activateResourceCheckbox").prop('checked');

    // FIXME: using string resourceName for new resource, and resourceId for existing resource
    var resourceId = '';

    // if app_selectedResource is populated then we are in edit mode
    if (app_selectedResource) {
        resourceId = app_selectedResource.resourceId;
        $('#resourceLoading').css({visibility: "visible"});
        jsonData = JSON.stringify({
            resourceId: resourceId,
            sublocationId: parseInt(sublocationId),
            resourceName: resourceName,
            activate: activate
        });

        if (activate === true) {
            DialogsUtil.showConfirmationDialog("#activateDialog", {
                buttons: {
                    "Yes": function () {
                        modifyResource(jsonData, activate);
                        $(this).dialog("close");
                    },
                    "No": function () {
                        $("#activateResourceCheckbox").prop('checked', false);
                        $(this).dialog("close");
                    }
                }
            });
        }
        else {
            modifyResource(jsonData, activate);
        }
    }
    // we are saving a new resource
    else {
        $('#resourceLoading').css({visibility: "visible"});
        jsonData = JSON.stringify({
            resourceName: resourceName,
            resourceType: resourceType,
            sublocationId: sublocationId,
            active: params.active
        });

        if (params.active === true) {
            DialogsUtil.showConfirmationDialog("#activateDialog", {
                buttons: {
                    "Yes": function () {
                        $.post("rest/resource/createResource", {data: jsonData}, function (data) {
                            var parsedData = JSON.parse(data);
                            var confirmationMessage = "";
                            if (parsedData.result == true) {
                                var newResourceName = parsedData.name;
                                var newResourceId = parsedData.resourceId;
                                confirmationMessage = "Success! This resource has been created.";
                                // after creating a resource we go back to the form, this time in edit mode
                                // This makes it possible for the user to start adding default availabilities,
                                // alternate resources, and temp adjustments right away
                                // NOTE: both functions called below block the UI and do NOT provide
                                // means to un-block the UI. However resource_handleResourceSelection()
                                // redirects to a new page once the REST call to get the resource details has returned.
                                resource_handleResourceSelection(newResourceId);
                                util_showMainMessage(confirmationMessage);
                            }
                            else {
                                confirmationMessage = parsedData.errorMsg;
                                util_showMainMessage(confirmationMessage);
                            }
                        });
                        $(this).dialog("close");
                    },
                    "No": function () {
                        $(this).dialog("close");
                    }
                }
            });
        }
        else {
            $.post("rest/resource/createResource", {data: jsonData}, function (data) {
                var parsedData = JSON.parse(data);
                var confirmationMessage = "";
                if (parsedData.result == true) {
                    var newResourceName = parsedData.name;
                    var newResourceId = parsedData.resourceId;
                    confirmationMessage = "Success! This resource has been created.";
                    // after creating a resource we go back to the form, this time in edit mode
                    // This makes it possible for the user to start adding default availabilities,
                    // alternate resources, and temp adjustments right away
                    resource_handleResourceSelection(newResourceId);
                    util_showMainMessage(confirmationMessage);
                }
                else {
                    confirmationMessage = parsedData.errorMsg;
                    util_showMainMessage(confirmationMessage);
                }
            });
        }
    }

}

function cancelSublocationClick() {
    resourceModule();
}

function crudSublocationClick() {
    sublocation_clearErrors();
    var isValid = true;

    var sublocation_startDateVal = new Date($("#sublocation_startDate").val());
    var sublocation_endDateVal = new Date($("#sublocation_endDate").val());

    if ($("#sublocation_startDate").val() == '') {
        $('#sublocation_startDateValidation').css({display: 'inline'});
        $('#sublocation_startDateValidation').text('Please enter valid start date');
        $('#sublocation_startDateValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }

    if ($("#sublocation_endDate").val() == '') {
        $('#sublocation_endDateValidation').css({display: 'inline'});
        $('#sublocation_endDateValidation').text('Please enter valid end date');
        $('#sublocation_endDateValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }

    if (sublocation_startDateVal >= sublocation_endDateVal) {
        $('#sublocation_startDateValidation').css({display: 'inline'});
        $('#sublocation_startDateValidation').text('Please enter valid start date range');
        $('#sublocation_startDateValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        $('#sublocation_endDateValidation').css({display: 'inline'});
        $('#sublocation_endDateValidation').text('Please enter valid end date range');
        $('#sublocation_endDateValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }

    if ($("#sublocation_sublocation").combobox("getValue").length < 1) {
        $('#sublocation_sublocationValidation').css({display: 'inline'});
        $('#sublocation_sublocationValidation').text('Required Field');
        $('#sublocation_sublocationValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }

    if (isValid == false) {
        return;
    }


    $('#sublocationLoading').css({visibility: "visible"});

    jsonData = JSON.stringify({
        sublocationId: $('#sublocation_sublocation').combobox("getValue"),
        startTime: sublocation_startDateVal.valueOf(),
        endTime: sublocation_endDateVal.valueOf(),
        reason: $('#sublocation_reason').val(),
        userId: user.id
    });

    $.post("rest/resource/createSublocationClosureInterval", {data: jsonData}, function (data) {
        $('#sublocationLoading').css({visibility: "hidden"});
        var confirmationMessage = "Sublocation Closure Scheduled";
        util_showMainMessage(confirmationMessage);
        alert(confirmationMessage);
        window.location.href = "sublocation_closure_screen.html";
    });
}

function resource_clearErrors() {
    $('#resourceNameInputValidation').css({visibility: "hidden"});
    $('#resourceTypeSelectValidation').css({visibility: "hidden"});
    $('#resourceSublocationSelectValidation').css({visibility: "hidden"});
}

function resource_clearForm() {
    $('#resourceNameInput').text("");
    $('#resource_resourceType').combobox("clear");
    $('#resourceSublocationSelect').combobox("clear");
    sublocationId = [];
    resource_clearErrors();
}

function sublocation_clearErrors() {
    $('.formElementRequired').css({visibility: "hidden"});
}

function sublocation_clearForm() {
    $('#sublocation_sublocation').combobox("clear");
    var date = new Date();
    date.setHours(0, 0, 0, 0);
    var later15 = new Date(date.getTime() + (15 * minuteMillis));

    $("#sublocation_startDate").val(showDateTime(date));
    $("#sublocation_endDate").val(showDateTime(later15));
    $('#sublocation_reason').val('');
    sublocation_clearErrors();
}

function buildSubLocCheckBoxes() {
    var html = [];
    for (var i = 0, len = sublocations.length; i < len; i++) {
        html[html.length] = "<option value='";
        html[html.length] = sublocations[i].id;
        html[html.length] = "'>";
        html[html.length] = sublocations[i].name;
        html[html.length] = "</option>";
    }
    return html.join('');
}

function buildResourcesMenu() {
    var html = [];
    for (var i = 0, len = resources.length; i < len; i++) {
        html[html.length] = "<option value='";
        html[html.length] = resources[i].id;
        html[html.length] = "'>";
        html[html.length] = resources[i].name;
        html[html.length] = "</option>";
    }
    return html.join('');
}

function buildIdAndNameMenu(items) {
    var html = [];
    for (var i = 0, len = items.length; i < len; i++) {
        html[html.length] = "<option value='";
        html[html.length] = items[i].id;
        html[html.length] = "'>";
        html[html.length] = items[i].name;
        html[html.length] = "</option>";
    }
    return html.join('');
}

/*
 * an onchange callback triggered when the sub-location selection changes in the resource form.
 * It modifies the #resourceNameSuffix span to display the currently selected value of the sublocation combo box
 */
function updateResourceNameSuffix(record) {

    var resourceNameSuffix = " - " + record.text;
    $('#resourceNameSuffix').text(resourceNameSuffix);

}

/**
 * an onchange callback triggered when the existing resource selection changes in the resource form.
 * It modifies the resource name text input field
 */
function updateResourceNameFromSelect(record) {

    var existingResourceName = "" + record.text;
    for (var i = 0; i < sublocations.length; i++) {
        if (strEndsWith(existingResourceName, ' - ' + sublocations[i].name) === true) {
            existingResourceName = existingResourceName.substring(0, existingResourceName.lastIndexOf(' - ' + sublocations[i].name));
            break;
        }
    }
    if (existingResourceName) {
        $('#resourceNameInput').val(existingResourceName);
    }

}

/**
 * This function does one thing only: populate the #mainResourceForm div with the top-part
 * of the resource form (type, sublocation, name)
 */
function createMainResourceForm(existingResourceNames) {
    var tableTemplate = $.templates("#mainFormTemplate");
    var templateData = {
        new: app_selectedResource == null,
        inactive: app_selectedResource && !app_selectedResource.active,
        existingResourceNames: existingResourceNames,
        editable: (UserRoleUtil.userIsSuperAdminOrResourceManager())
    };
    var tableContent = tableTemplate.render(templateData);

    $('#mainResourceForm').html(tableContent);
}


/**
 * Expand/Collapse function custom-made for the resource form. Inspired by
 * expandOrCollapseSubject() in subject.js
 */

function expandOrCollapseResourceFormSection(headingId, imageId, sectionId) {
    if ($('#' + headingId).attr('title') == 'Expand') {
        displayResourceFormSection(headingId, imageId, sectionId);
    } else {
        hideResourceFormSection(headingId, imageId, sectionId);
    }

    if (checkIfAllBlocksCollapsed() === true) {
        collapseAllHelper();
    }
    else {
        expandAllHelper();
    }
}

function displayResourceFormSection(headingId, imageId, sectionId) {
    $('#' + headingId).attr('title', 'Collapse').css({cursor: 'pointer'});
    $('#' + imageId).attr('src', 'css/images/sm_circle_minus.png');
    $('#' + sectionId).css({display: 'block', cursor: 'pointer'});
}

function hideResourceFormSection(headingId, imageId, sectionId) {
    $('#' + headingId).attr('title', 'Expand').css({cursor: 'pointer'});
    $('#' + imageId).attr('src', 'css/images/sm_circle_plus.png');
    $('#' + sectionId).css({display: 'none', cursor: 'default'});
}

function collapseAllHelper() {
    document.getElementById('toggleAllResourceDetails').title = "Expand";
    document.getElementById("toggleText").innerHTML = "Expand All";
    $('#toggleResourceDetailsImg').attr('src', 'css/images/arrows_expand.png');
}

function expandAllHelper() {
    document.getElementById('toggleAllResourceDetails').title = "Collapse";
    document.getElementById("toggleText").innerHTML = "Collapse All";
    $('#toggleResourceDetailsImg').attr('src', 'css/images/arrows_collapse.png');
}

function toggleAllResourceDetails(event, obj) {
    preventDefaultAction(event);

    if (obj.title == "Collapse") {
        collapseAllHelper();
        hideAllResourceForms();
    }
    else {
        expandAllHelper();
        displayAllResourceForms();
    }
}

function hideAllResourceForms() {
    for (var i = 0; i <= 5; i++) {

        var headerId = 'resourceTabHeading' + i;
        var imageId = 'resourceTabExpandIcon' + i;
        var sectionId = ((i !== 3) ? 'resourceTab' + i : 'tempAdjustmentContent');

        hideResourceFormSection(headerId, imageId, sectionId);
    }
}

function displayAllResourceForms() {
    for (var i = 0; i <= 5; i++) {

        var headerId = 'resourceTabHeading' + i;
        var imageId = 'resourceTabExpandIcon' + i;
        var sectionId = ((i !== 3) ? 'resourceTab' + i : 'tempAdjustmentContent');

        displayResourceFormSection(headerId, imageId, sectionId);
    }
}

function checkIfAllBlocksCollapsed() {
    var isAllCollapsed = true;
    for (var i = 1; i <= 5; i++) {
        var headerId = 'resourceTabHeading' + i;
        if ($('#' + headerId).attr('title') == 'Collapse') {
            isAllCollapsed = false;
        }
    }
    return isAllCollapsed;
}



