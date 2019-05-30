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
        List<String> out = AppUtils.executeCommand(command);
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
        List<String> out = AppUtils.executeCommand(command);
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
        List<String> out = AppUtils.executeCommand(command);
        if (!out.isEmpty() && out.get(0).equals("0")) {
            return true;
        }
        return false;
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
        AppUtils.executeCommand(command);
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
        AppUtils.executeCommand(command);
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
        AppUtils.executeCommand(commandPrefix + mkdirCmd);
        if (dir.lastIndexOf("/") == dir.length() - 1) {
            dir = dir.substring(0, dir.length() - 1);
        }
        dir = dir.substring(0, dir.lastIndexOf("/"));
        String chownCmd = "sudo chown " + sshUsername + ":" + sshUsername + " -R " + dir + "'; echo $?";
        AppUtils.executeCommand(commandPrefix + chownCmd);
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
