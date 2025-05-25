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
// Karma configuration
// Generated on Wed Sep 21 2016 13:55:09 GMT-0400 (EDT)

// this karma configuration is intended to use by other configuration files
// which set autoWatch and singleRun, for instance
module.exports = function(karma) {
    karma.set({
        basePath: 'src',
        frameworks: ['jasmine', 'sinon'],
        preprocessors: {
            // server HTML and JSON fixtures as javascript variables
            '**/*.html': ['html2js'],
            '**/*.json': ['html2js'],
            // perform code coverage analysis on all js files in the js directory (but not in subdirectories)
            'main/webapp/js/*.js': ['coverage'],
            'main/webapp/js/common/*.js': ['coverage'],
            'main/webapp/js/Home/*.js': ['coverage'],
            'main/webapp/js/Management/*.js': ['coverage'],
            'main/webapp/js/ReportBuilder/*.js': ['coverage'],
            'main/webapp/js/Resources/*.js': ['coverage'],
            'main/webapp/js/StandardReports/*.js': ['coverage'],
            'main/webapp/js/Studies/*.js': ['coverage'],
            'main/webapp/js/Subjects/*.js': ['coverage'],
            'main/webapp/js/util/*.js': ['coverage'],
            'main/webapp/js/widget/*.js': ['coverage']
            // 'main/webapp/js/*.js': ['coverage'],
            // 'main/webapp/js/(!lib)/*.js': ['coverage']
        },
        html2JsPreprocessor: {},
        // IMPORTANT: the files need to be listed in the order in which they need to be
        // loaded so that dependencies are met when loading each of these files
        // Also: this list should contain all the javascript files used by the application _and_ the tests.
        // Note: using **/*.js to point to all the javacript at once does not work because
        // the list doesn't respect the order in which the js files need to be loaded, given
        // their dependencies. For example, jQuery needs to be loaded before most of the other
        // js files. Therefore:
        // we list all the javascript files that need to come before any other js files, one by one.
        // Also: list html files that are loaded via javascript (e.g. header.html):
        // html files MUST be included in order to work with html2js
        files: [
            // HMTL source pages and test fixtures, and JSON test fixtures
            {pattern: '**/*.html', watched: true, served: true, included: true},
            {pattern: '**/*.json', watched: true, served: true, included: true},

            // IMPORTANT: the application's javascript files are listed in a specific order, the same in which
            // they are listed in the HTML files. This order needs to be respected because of
            // dependencies between various js files.

            // 3rd-party libraries
            // NOTE: see below the ES5 and ES6 loaded, in order to support the use of Map in util/tables.js, and potentially other ES6 features in the future
            'https://cdn.polyfill.io/v2/polyfill.min.js?features=Intl.~locale.en',
            {pattern: 'main/webapp/js/lib/es5-shim.min.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/es6-shim.min.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/json2.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery.min-3.4.0.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-ui.min-1.12.1.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/maskedInputPlugin.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/date.format.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery.blockUI.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-easyui-1.8.2/jquery.menu.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-easyui-1.8.2/jquery.linkbutton.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-easyui-1.8.2/jquery.tooltip.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-easyui-1.8.2/jquery.panel.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-easyui-1.8.2/jquery.parser.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-easyui-1.8.2/jquery.validatebox.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-easyui-1.8.2/jquery.textbox.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-easyui-1.8.2/jquery.combo.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-easyui-1.8.2/jquery.combobox.js', watched: false, served: true, included: true},

            {pattern: 'main/webapp/js/lib/fullcalendar-scheduler-4.2.0/packages/core/main.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/fullcalendar-scheduler-4.2.0/packages/daygrid/main.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/fullcalendar-scheduler-4.2.0/packages/timegrid/main.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/fullcalendar-scheduler-4.2.0/packages/interaction/main.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/fullcalendar-scheduler-4.2.0/packages/timeline/main.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/fullcalendar-scheduler-4.2.0/packages/resource-common/main.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/fullcalendar-scheduler-4.2.0/packages/resource-timeline/main.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-ui-sliderAccess.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery.multiple.select.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jquery-ui-timepicker-addon.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/lib/jsrender.js', watched: false, served: true, included: true},

            // our Scheduler javascript code
            // IMPORTANT! keep this list up to date
            {pattern: 'main/webapp/js/util/breadcrumbs.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/util/dateRanges.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/util/dates.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/util/dto.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/util/global2.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/util/util.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/widget/columns.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/widget/dialogs.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/widget/table_filter.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/widget/table_pagination.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/widget/tables.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/widget/widget_util.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/visit_templates_needing_approval.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/home_util.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/home_screen.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/home_study.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/overbook_check_table.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/appt_dialogs.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/appt_search.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/appt_subjects_tables.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/appointment_screen.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/appt_calendar.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/appt_calendar_page.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/appt_tables.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/appt_template_resource_table.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/timeline.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Home/booked_resources_table.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/common/feedback_screen.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/common/landing_screen.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Management/mgmt_form.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Management/mgmt_screen.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Management/mgmt_tables.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/ReportBuilder/report_templates.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Resources/resource.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Resources/resource_form.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Resources/resource_tables.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/StandardReports/report_screen_filter.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/StandardReports/report_screen_get.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/StandardReports/report_screen_show.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/StandardReports/report_screen_sort.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/StandardReports/report_screen.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Studies/study_form.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Studies/study_module.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Studies/study_screen.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Studies/study_tables.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Studies/template_resource_timeline.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Studies/link_resources_table.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Studies/study_member_tables.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Subjects/add_edit_subject.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Subjects/subjectGrid.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Subjects/subject_search_page.js', watched: false, served: true, included: true},
            {pattern: 'main/webapp/js/Subjects/subjects.js', watched: false, served: true, included: true},

            {pattern: '../node_modules/jasmine2-custom-message/jasmine2-custom-message.js', watched: false, served: true, included: true},

            // Make sure to include our test helpers before the Specs. Here are 3 ways to include them,
            // which may overlap, but Karma doesn't mind.
            {pattern: 'test/webapp/specs/**/helpers/*.js', watched: true, served: true, included: true},
            {pattern: 'test/webapp/specs/**/*Helper.js', watched: true, served: true, included: true},
            {pattern: 'test/webapp/helpers/*.js', watched: true, served: true, included: true},
            // the rest of these should be (just) the tests . optionally replace with a more-specific list
            {pattern: 'test/webapp/specs/**/*.js', watched: true, served: true, included: true}
        ],
        exclude: [""],
        reporters: ['progress', 'coverage', 'sonarqubeUnit'],
        // temporarily disable sonarqubeUnit
        // reporters: ['progress', 'coverage'],
        sonarQubeUnitReporter: {
            sonarQubeVersion: '6.3.1',
            outputFile: '../test-output/unit-tests/unit-tests-for-sonar.xml',
            useBrowserName: false
        },
        coverageReporter: {
            type: 'lcov',
            dir: '../test-output/coverage',
            subdir: '.'
        },
        port: 9876,
        concurrency: Infinity,
        logLevel: karma.DEBUG
    });
};
