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
<p>Your service is getting in trouble now, please check the detail in the table below: </p>

<#assign aDateTime = .now>
<#assign aTime = aDateTime?time>

<h5>Server information, time: ${aTime}</h5>
<table>
    <tr>
        <th>Service Name</th>
        <th>Deployed server</th>
        <th>Service Status</th>
        <th>Problem type</th>
        <th>Detail</th>
        <th>View on tool</th>
    </tr>
</table>

<p>Thanks & Best regards, <br>
</body>
</html>