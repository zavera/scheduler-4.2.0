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

var sumupPage = (function () {


    var initFn = function () {
        resetUI();
        commonInit();



        $.get("common_dialogs.html", function (content) {
            $('#main_dialog_wrapper').after(content);
        });

        app_runIdleTimer();
    };
    var commonInit = function () {

        loadMetaHeaders();
        initFooter();
        eraseLicense();
    };


    // Send message on button click or Enter key
    function sendMessage() {
        $('#chatSendBtn').on('click', sendMessage);
        var userMessage = $('#chatInputBox').val().trim();
        if (!userMessage) return;

        // Append user's message to chat
        $('.chat-messages').append(
            '<div><strong>User:</strong> ' + $('<div/>').text(userMessage).html() + '</div>'
        );
        $('#chatInputBox').val(''); // Clear input

        // Scroll to bottom
        $('.chat-messages').scrollTop($('.chat-messages')[0].scrollHeight);

        $.ajax({
            url: 'http://35.238.160.181:11434/api/generate',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                model: "gemma:2b",
                prompt: "prompt: \"Patient Information: Name: Jane Doe Age: 54 Date of Birth: 1969-04-12 Gender: Female Medical Record Number: 123456 Phone: (555) 123-4567 Email: jane.doe@email.com Primary Physician: Dr. Taylor\n" +
                    "\n" +
                    "Medical History: - Hypertension - Hyperlipidemia - Family history of heart disease\n" +
                    "\n" +
                    "Current Medications: - Atorvastatin 20mg daily - Lisinopril 10mg daily - Multivitamin once daily\n" +
                    "\n" +
                    "Allergies: - Penicillin\n" +
                    "\n" +
                    "Recent Symptoms: - Occasional chest pain (since 05/10/2025) - Mild shortness of breath (since 05/12/2025)\n" +
                    "\n" +
                    "Recent Lab Results: - Cholesterol: 245 mg/dL (05/20/2025) - Blood Pressure: 138/86 mmHg (05/20/2025)\n" +
                    "\n" +
                    "Appointments: - Cardiology with Dr. Smith at 9:00 AM - Physical Therapy with Dr. Lee at 10:30 AM - Nutritionist with Dr. Patel at 12:00 PM - Occupational Therapy with Dr. Adams at 2:00 PM - Follow-up Consultation with Dr. Johnson at 4:00 PM\n" +
                    "\n" +
                    "Meals: - Breakfast: Oatmeal with berries and green tea at 7:30 AM - Snack: Greek yogurt with almonds at 10:00 AM - Lunch: Grilled chicken salad with vinaigrette and water at 12:30 PM - Snack: Apple slices with peanut butter at 3:00 PM - Dinner: Baked salmon, steamed broccoli, brown rice, and herbal tea at 6:30 PM\n" +
                    "\n" +
                    userMessage

            }),
            xhrFields: {
                onprogress: function(e) {
                    // Get the current response text
                    var responseText = e.currentTarget.responseText;
                    // Split by newlines to get each JSON object
                    var lines = responseText.split('\n');
                    // Remove empty lines and already processed lines
                    lines = lines.filter(line => line.trim() !== "");

                    // We'll keep track of the last processed line
                    if (typeof window.lastProcessedLine === 'undefined') {
                        window.lastProcessedLine = 0;
                    }

                    for (let i = window.lastProcessedLine; i < lines.length; i++) {
                        try {
                            var chunk = JSON.parse(lines[i]);
                            // Append the chunk's response to the chat window
                            if (chunk.response) {
                                // If you want to append word by word, you can split by space, but most models stream by token/chunk
                                $('.chat-messages').append(
                                    $('<span/>').text(chunk.response).html()
                                );
                                $('.chat-messages').scrollTop($('.chat-messages')[0].scrollHeight);
                            }
                            // Stop appending when done is true
                            if (chunk.done) {
                                window.lastProcessedLine = 0; // Reset for next request
                                return;
                            }
                        } catch (err) {
                            // Ignore incomplete JSON lines
                        }
                    }
                    window.lastProcessedLine = lines.length;
                }
            },
            success: function(response) {
                // Optionally handle the final response (not always needed for streaming)
            },
            error: function(xhr) {
                $('.chat-messages').append(
                    '<div><strong>Bot:</strong> <span style="color:red;">Error: Unable to reach server.</span></div>'
                );
                $('.chat-messages').scrollTop($('.chat-messages')[0].scrollHeight);
            }
        });
    }



    // Allow sending with Enter key
    $('#chatInputBox').on('keydown', function(e) {
    if (e.key === 'Enter') {
    sendMessage();
    e.preventDefault();
}});});

