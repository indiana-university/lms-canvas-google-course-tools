Status summary for Google Course Tools Roster Syncing (${env}) as of ${runTime?datetime}

Successfully processed rosters for the following ${successes?size} courses:
<#list successes as message>
    -${message}
</#list>

Successfully inactivated rosters for the following ${inactivated?size} courses due to course/term end date:
<#list inactivated as message>
    -${message}
</#list>

There were ${errors?size} courses that had errors and were unable to be fully processed:
<#list errors as message>
    -${message}
</#list>
