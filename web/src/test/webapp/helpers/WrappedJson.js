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
var HelpStudies = {};

HelpStudies.sampleStaticData = '                                              \
{                                                                     \
"resourceTypes":[                                                     \
    "Nursing",                                                        \
    "Nutrition"                                                       \
],                                                                    \
    "rooms":[                                                         \
    {                                                                 \
        "name":"Room 1 - Sub-Location 1",                             \
        "resourceType":"Room",                                        \
        "sublocations":[                                              \
            {                                                         \
                "name":"Sub-Location 1",                              \
                "institution":{                                       \
                    "name":"DEMO1",                                   \
                    "longName":"Demo Institution 1",                  \
                    "id":1                                            \
                },                                                    \
                "id":1                                                \
            }                                                         \
        ],                                                            \
        "id":33                                                       \
    },                                                                \
    {                                                                 \
        "name":"Room 1 - Sub-Location 2",                             \
        "resourceType":"Room",                                        \
        "sublocations":[                                              \
            {                                                         \
                "name":"Sub-Location 2",                              \
                "institution":{                                       \
                    "name":"DEMO1",                                   \
                    "longName":"Demo Institution 1",                  \
                    "id":1                                            \
                },                                                    \
                "id":2                                                \
            }                                                         \
        ],                                                            \
        "id":36                                                       \
    }                                                                 \
],                                                                    \
    "credentials":[                                                   \
    {                                                                 \
        "name":"MD",                                                  \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"PhD",                                                 \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "nurseAnnotations":[                                              \
    {                                                                 \
        "name":"Admitting/Discharging Inpatient",                     \
        "quantity":0,                                                 \
        "resourceAnnotations":0,                                      \
        "quantifiable":false,                                         \
        "selected":false,                                             \
        "id":44                                                       \
    },                                                                \
    {                                                                 \
        "name":"Anthropometry",                                       \
        "quantity":0,                                                 \
        "resourceAnnotations":0,                                      \
        "quantifiable":true,                                          \
        "selected":false,                                             \
        "id":19                                                       \
    }                                                                 \
],                                                                    \
    "roles":[                                                         \
    {                                                                 \
        "name":"Study Coordinator - Research Asst",                   \
        "type":"ROLE_STUDY_COORDINATOR_RESEARCH_ASST",                \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"PI",                                                  \
        "type":"ROLE_PI",                                             \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "sublocations":[                                                  \
    {                                                                 \
        "name":"Sub-Location 1",                                      \
        "institution":{                                               \
            "name":"DEMO1",                                           \
            "longName":"Demo Institution 1",                          \
            "id":1                                                    \
        },                                                            \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"Sub-Location 2",                                      \
        "institution":{                                               \
            "name":"DEMO1",                                           \
            "longName":"Demo Institution 1",                          \
            "id":1                                                    \
        },                                                            \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "divisions":[                                                     \
    {                                                                 \
        "name":"Adolescent Medicine",                                 \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"Allergy u0026 Inflammation",                          \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "states":[                                                        \
    {                                                                 \
        "name":"Massachusetts",                                       \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"Alabama",                                             \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "studyStatuses":[                                                 \
    {                                                                 \
        "name":"IRB_PROCESS",                                         \
        "shortName":"PENDING",                                        \
        "isPending": true,                                            \
        "isOpen": false,                                              \
        "isClosed": false,                                            \
        "id":11                                                       \
    },                                                                \
    {                                                                 \
        "name":"OPEN",                                                \
        "shortName":"OPEN",                                           \
        "isPending": false,                                           \
        "isOpen": true,                                               \
        "isClosed": false,                                            \
        "id":22                                                       \
    }                                                                 \
],                                                                    \
    "visitCancelStatuses":[                                           \
    {                                                                 \
        "name":"No Show",                                             \
        "id":5                                                        \
    },                                                                \
    {                                                                 \
        "name":"Late Cancellation",                                   \
        "id":6                                                        \
    }                                                                 \
],                                                                    \
    "labAnnotations":[                                                \
    {                                                                 \
        "name":"Comment",                                             \
        "quantity":0                                                  \
    }                                                                 \
],                                                                    \
    "facultyRanks":[                                                  \
    {                                                                 \
        "name":"Instructor",                                          \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"Assistant Professor",                                 \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "overrideReasons":[                                               \
    {                                                                 \
        "name":"Change of resource",                                  \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"Override (resource otherwise not available)",         \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "departments":[                                                   \
    {                                                                 \
        "name":"Anesthesia",                                          \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"CRC",                                                 \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "centersAndInstitutions":[                                        \
    {                                                                 \
        "name":"Center for Information Technology (CIT)",             \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"Center for Scientific Review (CSR)",                  \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "appointmentStatuses":[                                           \
    {                                                                 \
        "name":"Scheduled",                                           \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"Checked-In",                                          \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "roomAnnotations":[                                               \
    {                                                                 \
        "name":"Comment",                                             \
        "quantity":0                                                  \
    }                                                                 \
],                                                                    \
    "checkOutReasons":[                                               \
    {                                                                 \
        "name":"Visit Completed",                                     \
        "appointmentStatus":{                                         \
            "name":"Checked-Out",                                     \
            "id":3                                                    \
        },                                                            \
        "id":15                                                       \
    },                                                                \
    {                                                                 \
        "name":"Visit Terminated Prior to Completion",                \
        "appointmentStatus":{                                         \
            "name":"Checked-Out",                                     \
            "id":3                                                    \
        },                                                            \
        "id":14                                                       \
    }                                                                 \
],                                                                    \
    "visitTypes":[                                                    \
    {                                                                 \
        "id":1,                                                       \
        "name":"Outpatient CRC"                                       \
    },                                                                \
    {                                                                 \
        "id":2,                                                       \
        "name":"Outpatient Non CRC"                                   \
    }                                                                 \
],                                                                    \
    "resources":[                                                     \
    {                                                                 \
        "name":"Assistant/Tech, Diet - Sub-Location 2",               \
        "resourceType":"Nutrition",                                   \
        "sublocations":[                                              \
            {                                                         \
                "name":"Sub-Location 2",                              \
                "institution":{                                       \
                    "name":"DEMO1",                                   \
                    "longName":"Demo Institution 1",                  \
                    "id":1                                            \
                },                                                    \
                "id":2                                                \
            }                                                         \
        ],                                                            \
        "id":17                                                       \
    },                                                                \
    {                                                                 \
        "name":"Assistant/Tech, Lab - Sub-Location 2",                \
        "resourceType":"Nursing",                                     \
        "sublocations":[                                              \
            {                                                         \
                "name":"Sub-Location 2",                              \
                "institution":{                                       \
                    "name":"DEMO1",                                   \
                    "longName":"Demo Institution 1",                  \
                    "id":1                                            \
                },                                                    \
                "id":2                                                \
            }                                                         \
        ],                                                            \
        "id":9                                                        \
    }                                                                 \
],                                                                    \
    "genders":[                                                       \
    {                                                                 \
        "name":"Male",                                                \
        "code":"M",                                                   \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"Female",                                              \
        "code":"F",                                                   \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "countries":[                                                     \
    {                                                                 \
        "name":"UNITED STATES",                                       \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"UNITED KINGDOM",                                      \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "institutionRoles":[                                              \
    {                                                                 \
        "type":"ROLE_SUPER_ADMIN",                                    \
        "description":"Super Admin has access to all ",               \
        "name":"Super Admin",                                         \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "type":"ROLE_RESOURCE_MANAGER",                               \
        "description":"ND or RD Manager / AD has limited access",     \
        "name":"Resource Manager",                                    \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "institutions":[                                                  \
    {                                                                 \
        "name":"DEMO1",                                               \
        "longName":"Demo Institution 1",                              \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"DEMO2",                                               \
        "longName":"Demo Institution 2",                              \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "cancellationReasons":[                                           \
    {                                                                 \
        "name":"Administrative Cancel: Data Entry Error",             \
        "appointmentStatus":{                                         \
            "name":"Cancellation",                                    \
            "id":4                                                    \
        },                                                            \
        "id":11                                                       \
    },                                                                \
    {                                                                 \
        "name":"Administrative Cancel: Equipment Failure",            \
        "appointmentStatus":{                                         \
            "name":"Cancellation",                                    \
            "id":4                                                    \
        },                                                            \
        "id":7                                                        \
    }                                                                 \
],                                                                    \
    "equipmentAnnotations":[                                          \
    {                                                                 \
        "name":"Body Composition Scan",                               \
        "quantity":0,                                                 \
        "resourceAnnotations":0,                                      \
        "quantifiable":true,                                          \
        "selected":false,                                             \
        "id":58                                                       \
    },                                                                \
    {                                                                 \
        "name":"Bone Scan",                                           \
        "quantity":0,                                                 \
        "resourceAnnotations":0,                                      \
        "quantifiable":true,                                          \
        "selected":false,                                             \
        "id":59                                                       \
    }                                                                 \
],                                                                    \
    "resourceNames":[                                                 \
    "Assistant/Tech, Diet - Sub-Location 1",                          \
    "Assistant/Tech, Diet - Sub-Location 2"                           \
],                                                                    \
    "ethnicities":[                                                   \
    {                                                                 \
        "name":"Not Hispanic or Latino",                              \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"Hispanic or Latino",                                  \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "irbInstitutions":[                                               \
    {                                                                 \
        "name":"IRB Demo Inst 1",                                     \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"IRB Demo Inst 2",                                     \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "races":[                                                         \
    {                                                                 \
        "name":"White",                                               \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"Black or African American",                           \
        "id":2                                                        \
    }                                                                 \
],                                                                    \
    "nutritionAnnotations":[                                          \
    {                                                                 \
        "name":"Anthropometry",                                       \
        "quantity":0,                                                 \
        "resourceAnnotations":0,                                      \
        "quantifiable":true,                                          \
        "selected":false,                                             \
        "id":19                                                       \
    },                                                                \
    {                                                                 \
        "name":"Body Composition Scan",                               \
        "quantity":0,                                                 \
        "resourceAnnotations":0,                                      \
        "quantifiable":true,                                          \
        "selected":false,                                             \
        "id":58                                                       \
    }                                                                 \
],                                                                    \
    "fundingSources":[                                                \
    {                                                                 \
        "name":"Pilot Grant",                                         \
        "id":1                                                        \
    },                                                                \
    {                                                                 \
        "name":"Federal PHS",                                         \
        "id":2                                                        \
    }                                                                 \
]                                                                     \
}                                                                     \
';

HelpStudies.sampleUser = '                                                                                     \
    {                                                                                                  \
         "id":409,                                                                                     \
         "ecommonsId":"fa123",                                                                         \
         "firstName":"Alma",                                                                           \
         "middleName":"",                                                                              \
         "lastName":"Apple",                                                                           \
         "primaryPhone":"(555) 555-5555",                                                              \
         "secondaryPhone":"",                                                                          \
         "email":"fa123@e.edu",                                                                        \
         "fax":"",                                                                                     \
         "pager":"",                                                                                   \
         "division":{                                                                                  \
             "name":"Adolescent Medicine",                                                             \
             "id":1                                                                                    \
         },                                                                                            \
         "department":{                                                                                \
             "name":"Anesthesia",                                                                      \
             "id":1                                                                                    \
         },                                                                                            \
         "institutionRole":{                                                                           \
             "type":"ROLE_SUPER_ADMIN",                                                                \
             "description":"Super Admin has access to all modules and functionality in the System",    \
             "name":"Super Admin",                                                                     \
             "id":123456                                                                              \
         },                                                                                            \
         "credential":{                                                                                \
             "name":"MD",                                                                              \
             "id":1                                                                                    \
         },                                                                                            \
         "facultyRank":{                                                                               \
             "name":"Instructor",                                                                      \
             "id":1                                                                                    \
         },                                                                                            \
         "active":true,                                                                                \
         "previousLoginTime":"Mon, Jan 30, 2017 3:52 PM",                                              \
         "sessionId":"NO_SESSION",                                                                     \
         "institution":{                                                                               \
             "name":"DEMO3",                                                                           \
             "longName":"Demo Institution 3",                                                          \
             "id":3                                                                                    \
         },                                                                                            \
         "authStatus":1                                                                                \
     }                                                                                                 \
    ';
