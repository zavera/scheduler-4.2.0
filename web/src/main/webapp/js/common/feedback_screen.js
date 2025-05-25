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

var setupZendeskWidget = function() {

    window.zEmbed || function (e, t) {
        var n, o, d, i, s,
            a = [],
            r = document.createElement("iframe");

        window.zEmbed = function () {
            a.push(arguments)
        },
        window.zE = window.zE || window.zEmbed,
        r.src = "javascript:false",
        r.title = "", r.role = "presentation",
        (r.frameElement || r).style.cssText = "display: none",
        d = document.getElementsByTagName("script"),
        d = d[d.length - 1],
        d.parentNode.insertBefore(r, d),
        i = r.contentWindow, s = i.document;

        try {
            o = s
        } catch (e) {
            n = document.domain,
                r.src = 'javascript:var d=document.open();d.domain="' + n + '";void(0);',
                o = s
        }
        o.open()._l = function () {
            var o = this.createElement("script");
            n && (this.domain = n),
            o.id = "js-iframe-async",
            o.src = e,
            this.t = +new Date,
            this.zendeskHost = t,
            this.zEQueue = a,
            this.body.appendChild(o)
        };
        o.write('<body onload="document._l();">'),
        o.close()
    }("https://assets.zendesk.com/embeddable_framework/main.js",
        "harvardcatalyst.zendesk.com");

    zE(function () {
        zE.hide();
    });
}();

function openPhiWarningForZendesk() {

    var okFn = function() {
        openZendesk();
    };

    var message = "Sending PHI through the help desk is strictly prohibited.";
    DialogsUtil.showMessageDialog(message, okFn, false, "Ok", null, true, okFn, true);

}

function openZendesk() {
    zE.activate({hideOnClose: true});
}

