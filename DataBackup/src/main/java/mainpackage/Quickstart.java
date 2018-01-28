package mainpackage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import com.profesorfalken.jpowershell.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;
import java.io.FileInputStream;
import java.nio.file.Paths;


public class Quickstart {
    
    public static final String dir=System.getProperty("user.home")+"\\DataBackupFiles";
    public static final String zipPath=dir+"\\zipy\\";
    public static final String confPath=dir+"\\config.txt";
    static String czas;
    static boolean rdy=true;
 
    public static String zipFile(String path) throws PowerShellNotAvailableException{
        String name=currentTime();
        new java.io.File(zipPath).mkdirs();
        PowerShell powerShell = PowerShell.openSession();
        String command="Compress-Archive -Path " + path +" -DestinationPath "+zipPath+name;
        System.out.println("POWERSHELL:"+powerShell.executeCommand(command).getCommandOutput());
        powerShell.close();
        
        return (zipPath+name+".zip");
    }
    
    public static void scheduleTask() throws PowerShellNotAvailableException{
            PowerShell powerShell=PowerShell.openSession();
            String command=        
            "$T=New-ScheduledTaskTrigger -Daily -At "+czas+"\n"+
            "$A=New-ScheduledTaskAction -Execute "+ System.getProperty("user.dir")+"\\DataBackup.bat"+"\n"+
            "$D = New-ScheduledTask -Action $A -Trigger $T\n"+
            "Register-ScheduledTask DBTask -InputObject $D";
            
            System.out.println("POWERSHELL:"+powerShell.executeCommand(command).getCommandOutput());
        }

    public static String currentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
        return sdf.format(cal.getTime()); 
}
    /** Application name. */
    private static final String APPLICATION_NAME =
        "Drive API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/drive-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;
    

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    private static final List<String> SCOPES =
        Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
            Quickstart.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    

    public static void main(String[] args) throws IOException, InterruptedException {
        // Build a new authorized API client service.
        Drive service = getDriveService();

        
    File fileMetadata;
    String path;
    
    //System.out.println("currpath="+System.getProperty("/"));
    
    
        
    if(!new java.io.File(confPath).isFile()) {
        new java.io.File(dir).mkdirs();
        JFrame frame=new JFrame();
        javax.swing.JFileChooser fd=new JFileChooser();
        fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fd.setAcceptAllFileFilterUsed(false);
        fd.showOpenDialog(frame);
        
            try (PrintWriter writer = new PrintWriter(confPath, "UTF-8")) {
                writer.println(fd.getSelectedFile().getPath());
                writer.close();
            }
            CzasFrame cf=new CzasFrame();
            cf.setVisible(true);
            rdy=false;
            while(!rdy){
                Thread.sleep(1000);
            }
            scheduleTask();
    }
    StringBuilder sb;
        try (Scanner in = new Scanner(new FileReader(confPath))) {
            sb = new StringBuilder();
            while(in.hasNext()) {
                sb.append(in.next());
            }
        }
        path = sb.toString();
        


path=zipFile(path);
java.io.File filePath =new java.io.File(path);

fileMetadata = new File();
fileMetadata.setName(filePath.getName());




FileContent mediaContent = new FileContent("application/zip",filePath);
File file = service.files().create(fileMetadata,mediaContent)
    .setFields("id")
    .execute();
System.out.println("File ID: " + file.getId());

        
       System.exit(0);
    }

}