{
        "Input": [<#assign List = body><#list List as listItem>
             {
                "DOB": "${listItem.dob}",
                "hasLicense?": ${listItem.haslicense?c},
                "isLicenseValid?": ${listItem.islicensevalid?c}
             }<#if listItem?is_last><#else>,</#if></#list>
        ]
    }