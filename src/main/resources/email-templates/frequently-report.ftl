<html>
<head>
    <style>
        table {
            font-family: arial, sans-serif;
            border-collapse: collapse;
            width: 100%;
        }

        td, th {
            border: 1px solid #dddddd;
            text-align: left;
            padding: 8px;
        }

        tr:nth-child(even) {
            background-color: #dddddd;
        }
    </style>
</head>
<body>
<p>Hi all,</p>
<p><strong>*This is an automated email, please do not reply. *</strong></p>
<p>Frequently report, the detail in the table below: </p>

<#assign aDateTime = .now>
<#assign aTime = aDateTime?time>

<h5>Server information, time: ${aTime}</h5>
<table>
    <tr>
        <th>Service ID</th>
        <th>Service Name</th>
        <th>Description</th>
		<th>PID</th>
        <th>Server Ip</th>
        <th>Server Port</th>
        <th>Project</th>
        <th>API endpoint</th>
        <th>Kong mapping</th>
        <th>Start time</th>
        <th>Status</th>
        <th>Note</th>
        <th>Ram used (%)</th>
        <th>Cpu used (%)</th>
        <th>Gpu used (%)</th>
        <th>Disk used (%)</th>
        <th>Owner</th>
        <th>Total error</th>
        <th>Total warning</th>
    </tr>
    
    <#list serviceInfo as item>
	    <tr>
	        <td><strong>${item.id}</strong></td>
	        <td><strong>${item.serviceName}</strong></td>
			<td><strong>${item.description}</strong></td>
			<td><strong>${item.pid}</strong></td>
        	<td><strong>${item.serverIp}</strong></td>
			<td><strong>${item.serverPort}</strong></td>        
			<td><strong>${item.project}</strong></td>
       		<td><strong>${item.apiEndpoint}</strong></td>
			<td><strong>${item.kongMapping}</strong></td>
			<td><strong>${item.startTime}</strong></td>
			<td><strong>${item.status}</strong></td>
			<td><strong>${item.note}</strong></td>
			<td><strong>${item.ramUsed}</strong></td>
			<td><strong>${item.cpuUsed}</strong></td>
			<td><strong>${item.gpuUsed}</strong></td>
			<td><strong>${item.diskUsed}</strong></td>
			<td><strong>${item.owner}</strong></td>
			<td><strong>${item.totalError}</strong></td>
			<td><strong>${item.totalWarning}</strong></td>
	    </tr>
	</#list>
</table>

<p>Thanks & Best regards, <br>
</body>
</html>