package vn.vccorp.servicemonitoring.utils;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
public class AppUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppUtils.class);

    /**
     * Get total size of a folder on remote server
     * @param folder    folder to get size
     * @param serverIp  server ip address of folder
     * @param sshPort   ssh port to connect to the server
     * @param sshUsername   username to connect to server via ssh command
     * @return  size of folder (Kb)
     */
    public static Integer getFolderSize(String folder, String serverIp, String sshPort, String sshUsername){
        String command = String.format("ssh -p %s %s@%s -t 'sudo du -s %s'", sshPort, sshUsername, serverIp, folder);
        List<String> out = executeCommand(command);
        if (out.isEmpty()){
            return 0;
        } else {
            return Integer.parseInt(out.get(0).split("\\s")[0]);
        }
    }

    /**
     * Get size of the disk on remote server which is having folder
     * @param folder    absolute path to folder on the disk
     * @param serverIp  server to check disk size
     * @param sshPort   ssh port to connect to that server
     * @param sshUsername   ssh user to connect to that server
     * @return  size of disk (Kb)
     */
    public static Integer getDiskSize(String folder, String serverIp, String sshPort, String sshUsername){
        String command = String.format("ssh -p %s %s@%s -t 'sudo df -h %s --output=size'", sshPort, sshUsername, serverIp, folder);
        List<String> out = executeCommand(command);
        if (out.size() <= 1){
            return 0;
        } else {
            return Integer.parseInt(out.get(1).replace("G", "").trim()) * 1024  * 1024;
        }
    }

    /**
     * Check if a process is alive on a remote server using pid of process
     *
     * @param serverIP    server to check
     * @param PID         process id to check
     * @param sshPort
     * @param sshUsername
     * @return true if process is alive otherwise false
     */
    public static boolean isProcessAlive(String serverIP, String PID, String sshPort, String sshUsername) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP + " -t 'ps -p " + PID + " > /dev/null'; echo $?";
        List<String> out = executeCommand(command);
        //if command execute success it will return 0
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    /**
     * Check if a folder is existed on a remote server
     *
     * @param serverIP    server to check
     * @param filePath    folder to check
     * @param sshPort
     * @param sshUsername
     * @return true if folder existed otherwise false
     */
    public static boolean isFolderExist(String serverIP, String filePath, String sshPort, String sshUsername) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP + " -t 'test -d " + filePath + "'; echo $?";
        List<String> out = executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    /**
     * Check if a file is exist on remote server or not
     *
     * @param serverIP    server to check
     * @param filePath    file to check
     * @param sshPort
     * @param sshUsername
     * @return true if file is existed otherwise false
     */
    public static boolean isFileExist(String serverIP, String filePath, String sshPort, String sshUsername) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP + " -t 'test -f " + filePath + "'; echo $?";
        List<String> out = executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    /**
     * sync log from that host to current host
     *
     * @param serverIP    server to check
     * @param remoteLog   file log remote in service
     * @param localLog    file log in local
     * @param sshPort
     * @param sshUsername
     * @return true if sync success
     */
    public static boolean syncLogFromRemote(String serverIP, String remoteLog, String localLog, int limit, String sshPort, String sshUsername) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP + " -t 'tail -n " + limit + " " + remoteLog + " >> " + localLog + "'; echo $?";
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
    }

    /**
     * sync log from that host to current host
     *
     * @param serverIP    server to check
     * @param path   file log remote in service
     * @param sshPort
     * @param sshUsername
     * @return last line if true , -1 if exception
     */
    public static long getLastLine(String serverIP, String path, String sshPort, String sshUsername){
        try {
            //get last line in file log remote
            String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIP + " -t 'cat " + path + " | wc -l'; echo $?";
            List<String> outline = AppUtils.executeCommand(command);
            return Long.parseLong(outline.get(0));
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    /**
     * Change user mod of a file on specific server
     *
     * @param file        file to change
     * @param serverIp    server where the file is located
     * @param mod         mod to change
     * @param sshPort
     * @param sshUsername
     */
    public static void chmod(String file, String serverIp, int mod, String sshPort, String sshUsername) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIp + " -t '" +
                "sudo chmod " + mod + " " + file + "'; echo $?";
        executeCommand(command);
    }

    /**
     * Change mode of a file or directory to specified mod on a remote serverIp
     *
     * @param file        file or directory to change
     * @param serverIp    server where the file is located
     * @param mod         mode to change
     * @param sshPort
     * @param sshUsername
     */
    public static void chmodRecursive(String file, String serverIp, int mod, String sshPort, String sshUsername) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIp + " -t '" +
                "sudo chmod " + mod + " " + file + " -r'; echo $?";
        executeCommand(command);
    }

    /**
     * Create directory on target server
     *
     * @param dir         absolute path to directory need to create
     * @param serverIp    server where to create directory
     * @param sshPort
     * @param sshUsername
     */
    public static void mkdir(String dir, String serverIp, String sshPort, String sshUsername) {
        String commandPrefix = "ssh -p " + sshPort + " " + sshUsername + "@" + serverIp + " -t '";
        String mkdirCmd = "sudo mkdir " + " " + dir + " -p'; echo $?";
        executeCommand(commandPrefix + mkdirCmd);
        if (dir.lastIndexOf("/") == dir.length() - 1) {
            dir = dir.substring(0, dir.length() - 1);
        }
        dir = dir.substring(0, dir.lastIndexOf("/"));
        String chownCmd = "sudo chown " + sshUsername + ":" + sshUsername + " -R " + dir + "'; echo $?";
        executeCommand(commandPrefix + chownCmd);
    }

    /**
     * Execute command on terminal and return the output
     *
     * @param command command to execute
     * @return output of command
     */
    public static List<String> executeCommand(String command) {
        List<String> out = new ArrayList<>();
        String[] args = new String[]{"/bin/bash", "-c", command, "with", "args"};
        ProcessBuilder pb = new ProcessBuilder(args);
        try {
            out = execute(pb);
        } catch (IOException e) {
            LOGGER.error("Exception while executing command: " + command, e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Execute script file on terminal
     *
     * @param filePath file to execute
     * @return output of script when execute on stdout
     */
    public static List<String> executeScriptFile(String filePath) {
        List<String> out = new ArrayList<>();
        ProcessBuilder pb = new ProcessBuilder(filePath);
        try {
            out = execute(pb);
        } catch (IOException e) {
            LOGGER.error("Exception while executing file: " + filePath, e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return out;
    }

    private static List<String> execute(ProcessBuilder pb) throws IOException, InterruptedException {
        List<String> out = new ArrayList<>();
        Process proc = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            out.add(line);
        }
        proc.waitFor();
        return out;
    }

    public static Date getStartedDateOfProcess(String serverId, String sshUsername, String sshPort, String pid) {
        String command = "ssh -p " + sshPort + " " + sshUsername + "@" + serverId + " -t 'date -r /proc/" + pid + " --rfc-3339=ns'";
        List<String> out = executeCommand(command);
        if (!out.isEmpty()) {
//            return Date.from(OffsetDateTime.parse(out.get(0).replace(" ", "T")).toInstant());
            return LocalDateTime.parse(out.get(0).split("\\.")[0].replace(" ", "T")).toDate();
        }
        return LocalDateTime.now().toDate();
    }

    public static void putFile(String serverId, String sshUsername, String sshPort, String sourceFile, String destination) {
        String command = "scp -P " + sshPort + " " + sourceFile + " " + sshUsername + "@" + serverId + ":" + destination;
        executeCommand(command);
    }
}
