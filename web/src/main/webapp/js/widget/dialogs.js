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
/**
 * Created by jill on 10/7/16.
 */


var DialogsUtil = function () {
};


DialogsUtil.showMessageDialog = function(message, fnOnOk, isError, buttonText, headerText, modal, fnOnClose, isWarning) {

    var dialogClass = "messageDialog";

    if(isError)
    {
        dialogClass += " errorDialog";
        headerText =  headerText === undefined || headerText === null ? "Error": "";
    }
    else if (isWarning) {
        dialogClass += " errorDialog";
        headerText =  headerText === undefined || headerText === null ? "Warning": "";
    }
    else {
        headerText =  headerText === undefined || headerText === null ? "Success": "";
    }

    $("#dialog-message-header").text(headerText);

    var buttons = {};
    var func = function () {
        $(this).dialog("close");

        if(fnOnOk)
            fnOnOk();
    };

    if(!buttonText)
    {
        buttonText = "OK";
    }

    buttons[buttonText] = func;

    $("#dialog-message").show();
    $('#dialog-message .content').text(message);

    if(modal === undefined ||modal === null)
    {
        modal = true;
    }

    var dialogOptions = {
        width: 450,
        modal: modal,
        dialogClass: dialogClass,
        buttons: buttons
    };
    if (fnOnClose) {
        dialogOptions.close = fnOnClose;
    }

    $("#dialog-message").dialog(dialogOptions);
};

DialogsUtil.showConfirmationDialog = function(element, options)
{
    if (options === undefined) {
        options = {};
    }

    options.dialogClass = "confirmationDialog";
    
    if(options.modal === undefined)
    {
        options.modal = true;
    }

    if(options.height === undefined)
    {
        options.height = "auto";
    }

    if(options.width === undefined)
    {
        options.width = 600;
    }

    if(options.resizable === undefined)
    {
        options.resizable = true;
    }
        
    $(element).dialog(options);
};

DialogsUtil.showMessageDialogWithOkFn = function (message, yesCallback, yesText, noText, headerText) {
  DialogsUtil.showMessageDialogWithOkCancelFns(message, yesCallback, undefined, yesText, noText, headerText);
};

DialogsUtil.showMessageDialogWithOkCancelFns = function(message, yesCallback, noCallback, yesText, noText, headerTextParam) {

  var dialogClass = "confirmationDialog";

  var headerText = headerTextParam === undefined ? "Choice:" : headerTextParam;

  if (headerText == "") {
    $("#dialog-message-header").hide();
  }
  else {
    $("#dialog-message-header").text(headerText);
  }

  var buttons = {};
  var noFunc = function () {
    $(this).dialog("close");

    if(noCallback)
      noCallback();
  };

  var yesFunc = function () {
    $(this).dialog("close");

    if(yesCallback)
      yesCallback();
  };

  var yesButton = yesText === undefined ? "OK" : yesText;
  var noButton = noText === undefined ? "Cancel" : noText;

  buttons[yesButton] = yesFunc;
  buttons[noButton] = noFunc;

  $("#dialog-message").show();
  $('#dialog-message .content').text(message);

  modal = true;

  $("#dialog-message").dialog({
    width: 450,
    modal: modal,
    dialogClass: dialogClass,
    buttons: buttons
  });
};

DialogsUtil.showErrorMessage = function(message, fnOnOk, buttonText, headerText, modal, fnOnClose) {
    DialogsUtil.showMessageDialog(message, fnOnOk, true, buttonText, headerText, modal, fnOnClose);
};

