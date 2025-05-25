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
//
// loadResourcesData / etc.
// --> createPaginationLinks
//     --> buildPagination

// the trick here will be to not inline the callbacks' names into the generated HTML.
// instead. generate the HTML, insert it into the DOM, and *then* add callbacks to the DOM elements
// referenced as methods from within this closure. This way all the callbacks for various navigations
// on the same page will have the same function names, but they will be distinct functions weach with its
// own closure

/**
 * A factory for all the html and javascript needed to implement table pagination.
 *
 * Called with a config object such as:
 *
 * {
 *   paginationElementId           --> the id of an existing empty <div> element which will be set to contain with the navigation controls
 *   initialRecordsPerPage  --> just that, how many records per page to show before the user has had a chance to set that manually
 *   paginationEventHandler --> this gets called whenever the navigation controls are interacted with. It will get
 *                              called with this signature: paginationEventHandler(currentPage, recordsPerPage)
 *   postUpdateCallback     --> this function will get called as the last statement in updatePagination(). This
 *                              is useful if the generated table needs to be augmented with additional controls,
 *                              style changes, etc.
 * }
 *
 * Returns an object consisting of 4 methods:
 *
 *  {
 *   generatePaginationElements:           --> called just once to generate the navigation elements the first time around.
 *                                      After that, the navigation will automatically
 *                                      get re-generated everytime the table gets re-generated
 *   initTableData: ,
 *   updatePagination               --> must be called by the table generator once it is done. It must be called with a partial config
 *                                      object containing config values to be updated, such as total number of records.
 *   getCurriedDataLoadingFunction  --> generates a function which calls the data loader / table generator function with
 *                                      arguments being the current state of the navigation. One can write a custom function
 *                                      to be curried this way and act as an adapter to the table-generator. The beauty
 *                                      is that because of the closure, the function has contiuous access to the navigation's
 *                                      state variable as they change
 * }
 */

var tablePaginationWidget = function(config) {

    /**
     * State, stored in a closure.
     * The 'config' argument is also in the closure; we could use it, but decided not too since the
     * total number of variables in the widget's state is small
     */

    var currentPageNumber = 1;
    var recordsPerPage = config.initialRecordsPerPage;
    var totalNumberOfPages = 'n/a';

    var recordsOnCurrentPage = 0;

    var paginationElementId = config.paginationElementId;
    var paginationElement = $('#' + config.paginationElementId);

    var recordsPerPageSelectorId = paginationElementId + "_recordsPerPageSelector";
    var firstPageLinkId = paginationElementId + "_firstPageLink";
    var previousPageLinkId = paginationElementId + "_previousPageLink";
    var nextPageLinkId = paginationElementId + "_nextPageLink";
    var lastPageLinkId = paginationElementId + "_lastPageLink";
    var gotoPageButtonId = paginationElementId + "_gotoPageButton";
    var gotoPageInputId = paginationElementId + "_gotoPageInput";

    var paginationEventHandler = config.paginationEventHandler;
    var displayRecordsEventHandler = config.displayRecordsEventHandler || config.paginationEventHandler;
    var postUpdateCallback = config.postUpdateCallback;


    /**
     * Main method.
     *
     * generates HTML for table navigation and replaces the contents of paginationElement with it
     */

    var generatePaginationElements = function () {

        if (paginationElement.length == 0) {
            console.log('ERROR: no element found by id ' + paginationElementId);
            return;
        }
        if (paginationElement.length > 1) {
            console.log('ERROR: ' + paginationElement.length + ' elements found by id ' + paginationElementId);
            return;
        }
        if (paginationElement.hasClass('tablePagination')) {
            console.log('ERROR: element with id ' + paginationElementId + ' already has table navigation applied to it');
            return;
        }
        if (!paginationElement.is('div')) {
            console.log('ERROR: table navigation must be applied to a <div> element');
        }

        paginationElement.empty();

        var dropdownValuesMap = {"5": "5", "10" : "10", "25": "25", "50": "50", "100" : "100"};

        var selectOptions = {
            callbackElementId: recordsPerPageSelectorId,
            label: 'Display Records: ',
            name: 'selectRecords',
            values: dropdownValuesMap
        };

        WidgetUtil.createSelectBox(paginationElement, selectOptions);
        addArrowsAndManualInput(paginationElement, currentPageNumber, totalNumberOfPages);

        // initialize drop down for number of records per page
        $('#' + recordsPerPageSelectorId).val(recordsPerPage);

        // now that all the controls are in the DOM, set up all the callbacks
        $('#' + recordsPerPageSelectorId).on('change', recordsPerPageChangeCallback);
        $('#' + firstPageLinkId).on('click', navigateToFirstPage);
        $('#' + nextPageLinkId).on('click', navigateToNextPage);
        $('#' + previousPageLinkId).on('click', navigateToPreviousPage);
        $('#' + lastPageLinkId).on('click', navigateToLastPage);
        $('#' + gotoPageButtonId).on('click', navigateToSpecifiedPage);
        $('#' + gotoPageInputId).on('change', validatePageNavigationInput);
        $('#' + gotoPageInputId).on('keypress', navigateToPageByEnterKey);

    };

    // initialize table data based on current state of navigation widget
    function initTableData() {
        // currently defers to recordsPerPageChangeCallback() but that could change.
        // in any case, having a different name for this function makes the intent clearer
        recordsPerPageChangeCallback();
    }

    function recordsPerPageChangeCallback() {
        // get the number of records to display per page from the drop-down
        recordsPerPage = $('#' + recordsPerPageSelectorId).val();
        recordsPerPage = parseInt(recordsPerPage);
        // reset current page to 1 because it's the only thing that makes sense when the number of records per page changes
        currentPageNumber = 1;
        // call the callback provided by the caller of generatePaginationElements(). That is responsible for loading and displaying data.
        displayRecordsEventHandler(currentPageNumber, recordsPerPage);
    }

    function navigateToNextPage() {
        currentPageNumber = parseInt(currentPageNumber); // in case it's passed as string

        if (currentPageNumber == totalNumberOfPages) {
            util_showMainMessage("You are already on the last page. There is no next page.");
            return;
        }
        if (currentPageNumber > totalNumberOfPages) {
            util_showMainMessage("Not a valid Page number!! Please enter a valid page number.");
            return;
        }
        currentPageNumber += 1;
        paginationEventHandler(currentPageNumber, recordsPerPage);
    }

    function navigateToPreviousPage() {
        if (currentPageNumber == 1) {
            util_showMainMessage("You are already on the first page..There is no previous page.");
            return;
        }
        currentPageNumber = currentPageNumber - 1;
        paginationEventHandler(currentPageNumber, recordsPerPage);
    }

    /**
     * helpers
     */
    function addArrowsAndManualInput(container, currentPageNumber, totalNumberOfPages) {

        var wrapperContainer = $('<div>')
            .addClass('pagination_block');

        var pagination = $('<div>');
        pagination.attr('id', 'pagination');

        if (currentPageNumber > 1) {
            addBeforeArrows(pagination);
        }

        pagination.append('Page ');

        var pageInput = $('<input>')
            .attr('type', 'text')
            .attr('id', gotoPageInputId)
            .addClass('input_page')
            .addClass('numbersOnly')
            .val(currentPageNumber)
            .appendTo(pagination);

        pagination.append(' of ' + totalNumberOfPages);

        addGoButton(pagination);

        if (currentPageNumber < totalNumberOfPages) {
            addAfterArrows(pagination)
        }

        pagination.appendTo(wrapperContainer);
        wrapperContainer.appendTo(container);

        WidgetUtil.addDynamicResizingToInput(pageInput);
    }

    function addBeforeArrows(container) {

        var leftArrowUrl = "css/images/gantt/back_arrow.png";
        var leftDoubleArrowUrl = "css/images/gantt/allthewayback_arrow.png";

        $("<img>")
            .attr('src', leftDoubleArrowUrl)
            .attr('id', firstPageLinkId)
            .css('vertical-align', 'middle')
            .appendTo(container);

        $("<img>")
            .attr('src', leftArrowUrl)
            .attr('id', previousPageLinkId)
            .css('vertical-align', 'middle')
            .appendTo(container);

    }

    function addAfterArrows(container) {

        var rightArrowUrl = "css/images/gantt/forward_arrow.png";
        var rightDoubleArrowUrl = "css/images/gantt/allthewayforward_arrow.png";

        $("<img>")
            .attr('src', rightArrowUrl)
            .attr('id', nextPageLinkId)
            .css('vertical-align', 'middle')
            .appendTo(container);

        $("<img>")
            .attr('src', rightDoubleArrowUrl)
            .attr('id', lastPageLinkId)
            .css('vertical-align', 'middle')
            .appendTo(container);

    }

    function addGoButton(container) {

        $('<input>')
            .attr('type', 'button')
            .attr('id', gotoPageButtonId)
            .attr('value', 'Go')
            .addClass('formButton')
            .addClass('goButton')
            .appendTo(container);

    }

    function navigateToFirstPage() {
        if (currentPageNumber == 1) {
            util_showMainMessage("You are already on the first page.");
            return;
        }
        currentPageNumber = 1;
        paginationEventHandler(currentPageNumber, recordsPerPage);
    }

    function navigateToLastPage() {
        if (currentPageNumber == totalNumberOfPages) {
            util_showMainMessage("You are already on the last page.");
            return;
        }
        currentPageNumber = totalNumberOfPages;
        paginationEventHandler(currentPageNumber, recordsPerPage);
    }

    function validatePageNavigationInput() {
        var pageInput = $('#' + gotoPageInputId);
        page = pageInput.val();
        if (isNaN(page) || page == 'undefined') {
            util_showMainMessage("Please enter a valid page number.");
            pageInput.val(1);
            return;
        }
        if (page > totalNumberOfPages || page < 1) {
            util_showMainMessage("Entered value outside the page range. Please enter a valid page.");
            pageInput.val(currentPageNumber);
            WidgetUtil.addDynamicResizingToInput(pageInput);
            return;
        }
    }

    function navigateToSpecifiedPage() {
        var pageInput = $('#' + gotoPageInputId);
        var page = pageInput.val();
        if (isNaN(page) || page == 'undefined') {
            util_showMainMessage("Please enter a valid page number.");
            WidgetUtil.addDynamicResizingToInput(pageInput);
            pageInput.val(1);
            return;
        }
        if (page > totalNumberOfPages || page < 1) {
            util_showMainMessage("Entered value outside the page range. Please enter a valid page.");
            pageInput.val(currentPageNumber);
            WidgetUtil.addDynamicResizingToInput(pageInput);
            return;
        }
        if (!page) {
            page = 1;
        }
        if (page > totalNumberOfPages) {
            util_showMainMessage("Not a valid Page number!! Please enter a valid page number.");
            return;
        }
        currentPageNumber = page;
        paginationEventHandler(currentPageNumber, recordsPerPage);
    }

    function navigateToPageByEnterKey(e) {
        var keycode;
        if (window.event){
            keycode = window.event.keyCode;
        } else if (e) {
            keycode = e.which;
        } else  {
            return true;
        }
        if (keycode === keyCodes.ENTER) {
            navigateToSpecifiedPage();
            // don't let this enter key press make it to the input element
            return false;
        }
        // let the key press pass through and actually affect the input element
        return true;
    }

    function updatePagination(newPaginationConfig) {
        // 2 steps:
        // (1) update the pagination widget configurations
        // (2) generate fresh pagination HTML
        if (newPaginationConfig.totalNumberOfPages) {
            totalNumberOfPages = newPaginationConfig.totalNumberOfPages;
        }
        generatePaginationElements();
        if (postUpdateCallback) {
            postUpdateCallback();
        }
    }

    function getCurriedDataLoadingFunction(fn) {
        return (function() {
            return fn(currentPageNumber, recordsPerPage, updatePagination);
        });
    }

    function applyCurriedDataLoader(fn) {
      getCurriedDataLoadingFunction(fn)();
    }

    function decrRecordsOnCurrentPage() {
      setRecordsOnCurrentPage(recordsOnCurrentPage - 1);
    }

    function setRecordsOnCurrentPage(value) {
      recordsOnCurrentPage = value;

      if (value == 0 && currentPageNumber > 1) {
        currentPageNumber--;
      }
    }

    function getCurrentPageNumber() {
        return currentPageNumber;
    }

    function setCurrentPageNumber(curr) {
        currentPageNumber = curr;
    }

    function setRecordsPerPage(rpg) {
        recordsPerPage = rpg;
    }

    function getRecordsPerPage() {
        return recordsPerPage;
    }

    return {
        generatePaginationElements: generatePaginationElements,
        initTableData: initTableData,
        updatePagination: updatePagination,
        getCurriedDataLoadingFunction: getCurriedDataLoadingFunction,

        applyCurriedDataLoader: applyCurriedDataLoader,
        setRecordsOnCurrentPage: setRecordsOnCurrentPage,
        decrRecordsOnCurrentPage: decrRecordsOnCurrentPage,
        getCurrentPageNumber: getCurrentPageNumber,
        setCurrentPageNumber: setCurrentPageNumber,
        getRecordsPerPage: getRecordsPerPage,
        setRecordsPerPage: setRecordsPerPage
    }
};

var PaginationHelper = function(){};

PaginationHelper.initPagination = function(
            paginationElementId,
            totalData,
            maxResults,
            paginationEventHandler,
            displayRecordsEventHandler) {

    displayRecordsEventHandler = displayRecordsEventHandler || paginationEventHandler;

    var paginationConfig = {
        paginationElementId: paginationElementId,
        paginationEventHandler: paginationEventHandler,
        displayRecordsEventHandler: displayRecordsEventHandler,
        postUpdateCallback: null,
        initialRecordsPerPage: maxResults
    };

    var paginationWidget = tablePaginationWidget(paginationConfig);
    var totalNumberOfPages = PaginationHelper.calculateNumberOfPages(totalData, maxResults);
    paginationWidget.updatePagination({totalNumberOfPages: totalNumberOfPages});
    paginationWidget.generatePaginationElements();
    return paginationWidget;
};

PaginationHelper.updatePagination = function(paginationWidget, totalData) {
    var recordsPerPage = paginationWidget.getRecordsPerPage();
    var totalNumberOfPages = PaginationHelper.calculateNumberOfPages(totalData, recordsPerPage);
    paginationWidget.updatePagination({totalNumberOfPages: totalNumberOfPages});
};

PaginationHelper.calculateNumberOfPages = function(totalData, maxResults) {
    var remainder = totalData % maxResults;

    var totalPages = Math.floor(totalData / maxResults);

    // very first page has floor of 0
    if (remainder > 0) {
        totalPages++;
    }
    // if still 0, then there were no results, but still it's page 1
    if (totalPages === 0) {
        totalPages++;
    }

    return totalPages;
};

PaginationHelper.suffix = "Pagination";
PaginationHelper.defaultPerPage = 25;

PaginationHelper.getPaginationId = function(tableId) {
    return tableId + PaginationHelper.suffix;
};


