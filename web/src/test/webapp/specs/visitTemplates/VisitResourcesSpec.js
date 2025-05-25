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
describe('Build visit resource activity dialog', function() {

    it('allows filtering the activity by selection status', function() {

        FixtureHelper.loadTestHtmlFixtureIntoDom('visitTemplates/html/elements-for-add-resource-activity-filter.html');

        var options = $(".filterSelectedResourcesSelect").children();
        expect(options.length).toBe(0);

        VisitTables.buildDisplayFilterSelect(undefined, ResourceAnnotationsTable.filterOptions);

        options = $(".filterSelectedResourcesSelect").children();
        expect(options.length).toBe(3);
        expect($(options[0]).attr('value')).toBe('No');
        expect($(options[0]).text()).toBe('Display Unselected Annotations');
        expect($(options[1]).attr('value')).toBe('Yes');
        expect($(options[1]).text()).toBe('Display Selected Annotations');
        expect($(options[2]).attr('value')).toBe('All');
        expect($(options[2]).text()).toBe('Display Both');

        var select = $(".filterSelectedResourcesSelect");
        expect(select.length).toBe(1);

        var comboBoxSpan = select.next('span');
        expect(comboBoxSpan.length).toBe(1);
        expect(comboBoxSpan.hasClass('textbox'));
        expect(comboBoxSpan.hasClass('combo'));

        var comboBoxInputs = comboBoxSpan.children('input');
        expect(comboBoxInputs.length).toBe(2);
        var hiddenValueInput2 = $(comboBoxInputs[1]);
        expect(hiddenValueInput2.hasClass('textbox-value')).toBe(true);
        expect(hiddenValueInput2.val()).toBe('All');

        var onChangeSpy = spyOn(window, 'filterSelectedResources');

        // set the combobox to 'Yes'

        select.combobox('setValue', 'Yes');

        expect(onChangeSpy).toHaveBeenCalledTimes(1);
        expect(onChangeSpy).toHaveBeenCalledWith(undefined, 'Yes');

        // seems like the following elements got re-generated, so we need to get fresh values from jQuery
        // otherwise we do not see the change in value
        comboBoxInputs = comboBoxSpan.children('input');
        expect(comboBoxInputs.length).toBe(2);
        hiddenValueInput2 = $(comboBoxInputs[1]);
        expect(hiddenValueInput2.val()).toBe('Yes');

        // set the combobox to 'No'

        select.combobox('setValue', 'No');

        expect(onChangeSpy).toHaveBeenCalledTimes(2);
        expect(onChangeSpy).toHaveBeenCalledWith(undefined, 'No');

        // seems like the following elements got re-generated, so we need to get fresh values from jQuery
        // otherwise we do not see the change in value
        comboBoxInputs = comboBoxSpan.children('input');
        expect(comboBoxInputs.length).toBe(2);
        hiddenValueInput2 = $(comboBoxInputs[1]);
        expect(hiddenValueInput2.val()).toBe('No');

        // set the combobox to 'All'

        select.combobox('setValue', 'All');

        expect(onChangeSpy).toHaveBeenCalledTimes(3);
        expect(onChangeSpy).toHaveBeenCalledWith(undefined, 'All');

        // seems like the following elements got re-generated, so we need to get fresh values from jQuery
        // otherwise we do not see the change in value
        comboBoxInputs = comboBoxSpan.children('input');
        expect(comboBoxInputs.length).toBe(2);
        hiddenValueInput2 = $(comboBoxInputs[1]);
        expect(hiddenValueInput2.val()).toBe('All');

    });

    it('opens a dialog containing a filterSelectedResourcesSelect combobox when copying a resource', function() {

        FixtureHelper.loadTestHtmlFixtureIntoDom('visitTemplates/html/elements-for-filter-select-resources-combobox.html');

        var spy = spyOn(VisitTables, 'buildDisplayFilterSelect');

        crudResourceClick(undefined);
        $("#crud_template_resource").dialog('open');

        expect(spy).toHaveBeenCalledTimes(1);
        expect(spy).toHaveBeenCalledWith("resourceAnnotationsTable", ResourceAnnotationsTable.filterOptions);

    })

    it('opens a dialog containing a filterSelectedResourcesSelect combobox when adding a resource', function() {

        FixtureHelper.loadTestHtmlFixtureIntoDom('visitTemplates/html/elements-for-filter-select-resources-combobox.html');

        // spy1 we don't care about in this test. We just don't want to execute that function
        var spy1 = spyOn(window, 'createSelectableTable');
        var spy2 = spyOn(VisitTables, 'buildDisplayFilterSelect');

        TRT.loadSelectResourcesDataOpen();

        expect(spy2).toHaveBeenCalledTimes(1);
        expect(spy2).toHaveBeenCalledWith("billableResourcesTable", ResourceBillingTable.filterOptions);

    })

});
