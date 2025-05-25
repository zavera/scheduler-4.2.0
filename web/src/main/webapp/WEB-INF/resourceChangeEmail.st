<!--

    Copyright (c) 2015-2016, President and Fellows of Harvard College

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
    WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
    MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
    EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
    SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
    TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

-->
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Scheduler</title>
</head>

<body style="font: 14px Arial, Helvetica, sans-serif; color: #000000; background: #ffffff;">
		<div style="width:640px;">
        <div style="border-bottom: 1px solid #aaaaaa;">
            <div style="padding: 5px 0 10px 10px;">
            <span style="display: inline-block; vertical-align: bottom; font-family: 'Myriad Pro'; font-size: 14pt; padding: 0px 10px 5px 0; text-transform:uppercase;">Harvard Catalyst</span>
            <span style="display: inline-block; vertical-align: bottom; width: 10px; font-family: 'Myriad Pro'; font-size: 22pt;">|</span> 
            <span style="display: inline-block; vertical-align: bottom; padding: 0px 0 7px 10px; font-family: 'Myriad Pro'; font-size: 11pt; text-transform:uppercase;">The Harvard Clinical and Translational Science Center</span>
        </div>
        <div style="clear: both;"></div>
            <div style="font-family: Arial; font-size: 12pt; font-weight: bold; padding: 0px 0px 0 7px;">at</div> 
            <div style="padding: 10px 0px 0 7px; font-family: 'Georgia'; font-size: 22pt;">
            <tr>
          		<td>$institution$</td>
          	</tr>
        </div>
    </div>
    <div style="padding: 10px 10px 20px 10px; display:block;">
      <p>The following Resource quantity and/or availability has been modified: <br />
        <br />
      </p>
      <table width="610" border="0" cellspacing="0" cellpadding="5">
        <tr>
          <td width="127"><strong>Resource Name:</strong></td>
          <td width="463">$resourceName$</td>
        </tr>
       </table>
        <p>has changed. This will impact visits already booked on <br /> </p>
        <table width="610" border="0" cellspacing="0" cellpadding="5">
        <tr>
          <td><strong>Day Of Week:</strong></td>
          <td>$editday$</td>
        </tr>
        <tr>
          <td><strong>Start Time:</strong></td>
          <td>$editstart$</td>
        </tr>
        <tr>
          <td><strong>End Time:</strong></td>
          <td>$editend$</td>
        </tr>
        <tr>
          <td><strong>Quantity:</strong></td>
          <td>$editquantity$</td>
        </tr>
      </table>
      <p>. Going forward, <br /></p>
      <table width="610" border="0" cellspacing="0" cellpadding="5">
        <tr>
          <td><strong>Day Of Week:</strong></td>
          <td>$newday$</td>
        </tr>
        <tr>
          <td><strong>Start Time:</strong></td>
          <td>$newstart$</td>
        </tr>
        <tr>
          <td><strong>End Time:</strong></td>
          <td>$newend$</td>
        </tr>
        <tr>
          <td><strong>Quantity:</strong></td>
          <td>$newquantity$</td>
        </tr>
      </table>
      <p><br />
      Log in to the system at: <a href="http://catalyst.harvard.edu/hccrc/scheduling">http://catalyst.harvard.edu/hccrc/scheduling</a></p>
</div>

</div>
    <div class="footer">You are receiving this email because you have an account on the Harvard Catalyst Scheduling System.<br /><br />
The use of this application is for business purposes only. Your use of this application is monitored and a record of access may be reviewed by the institution and may be made available to authorized persons, including the subject, upon request. By using this application, you agree to keep any information found within private. Unauthorized use or disclosure of subject PHI (Protected Health Information) is strictly prohibited.<br /><br />
To report issues, give comments, ask questions. Email us: <a href="mailto:support@harvardcatalyst.zendesk.com">support@harvardcatalyst.zendesk.com</a></div>
</body>
</html>