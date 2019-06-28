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
<p>Hi ${user},</p>
<p>Welcome to Service Monitoring tool</p>
<p>Your account info: </p>
<p>email: ${email}</p>
<p>Default password: ${password}</p>
<p>Thanks & Best regards, <br>
<i>*This is an automated email, please do not reply.*</i>
</body>
</html>