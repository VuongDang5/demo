# README #

This is the source code for Service Monitoring System - APIs server.

### How do I get set up? ###

- Tech docs: 
    + All documents available at: https://drive.google.com/drive/u/1/folders/1mcW_QWuzqOQ1WHlLdL2T0--mgIWapb6s
    + Project overview: https://docs.google.com/presentation/d/1JDMG9kJyQzEr5k2DlhZjIdUHyuLcP_RS2lod4-tk5RY/edit#slide=id.p
    + Project's specific document: https://docs.google.com/document/d/1aS5-lDUabnfBvpgqETTNsMdQmp7E8QRqoTcg2LT00uk/edit
- To run the source code on your local machine, the following softwares must be installed:
    + JDK (1.8)
    + Maven 3
    + MySQL    
    + Setup lombok (https://projectlombok.org/)  
      For eclipse: https://projectlombok.org/setup/eclipse  
      For intellij: https://projectlombok.org/setup/intellij      
    + Run Application.java to start our application. (Remember to start mysqld service in advance)
    + Link to explore swagger-ui: http://localhost:8082/swagger-ui.html
    + Setup a sudo user for this tool on your local:
        1. Create a user named "monitoring" with command: `sudo adduser monitoring --disabled-password`
        2. Allow this user to execute all sudo command without password: `sudo visudo`
        3. Added this line to the end of file: `monitoring      ALL=(ALL) NOPASSWD:ALL` then save and quit the editor
        4. Login to monitoring user with command: `sudo su monitoring`
        5. Create .ssh folder for monitoring user: `mkdir /home/monitoring/.ssh`
        6. Added public key of your main user to authorized_keys of monitoring user: `sudo cat /home/tuyennta/.ssh/id_rsa.pub > /home/monitoring/.ssh/authorized_keys`       
         If your main user doesn't have ssh public key, then login to your main user and run this command: `ssh-keygen -t rsa`
        7. Logging out of monitoring user then test if you can ssh to monitoring user and try a sudo command:
        - `ssh monitoring@localhost`
        - `sudo -v`
        If both commands success without asking for password then you're done. 
 - DBSchema and description about entity relationship:
    https://drive.google.com/open?id=1PzWudOa3KHBhIyCn7ZJABn3sg3rjmJpm

- Deployment docs (Ubuntu Env):
 - Install Java: https://medium.com/coderscorner/installing-oracle-java-8-in-ubuntu-16-10-845507b13343
 - Install MySQL: https://www.digitalocean.com/community/tutorials/how-to-install-mysql-on-ubuntu-18-04
 