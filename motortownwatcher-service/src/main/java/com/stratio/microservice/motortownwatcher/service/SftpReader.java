package com.stratio.microservice.motortownwatcher.service;

import com.jcraft.jsch.*;
import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@Slf4j
public class SftpReader {


    public SftpReader(){};

    public List<String> readCsvFileFromSftp(String user,String host, String sftpkey, String remoteFile)
    {

        //2019_04_04_03_30.zip
        int port=22;

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            //change before deploy (no passphrase)
            //String privateKey = "/home/apaniagua/.ssh/id_rsa";

            System.out.println("AURGI SFTP READING KEY" + sftpkey);

            if (sftpkey.equalsIgnoreCase("/home/apaniagua/.ssh/id_rsa")) {
                jsch.addIdentity(sftpkey,"MailSagApm17");
                System.out.println("k we local");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");
            System.out.println("AURGI: Establishing Connection..." + user + "@" + host + " with " + sftpkey);

            session.connect();
            System.out.println("AURGI: Connection established.");

            System.out.println("AURGI: Creating SFTP Channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            InputStream out= null;
            System.out.println("AURGI: SFTP Getting file " + remoteFile);
            out= sftpChannel.get(remoteFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(out));

            List<String> strings = new ArrayList<String>();

            String line;
            while ((line = br.readLine()) != null)
            {
                //System.out.println(line);
                strings.add(line);
            }
            br.close();
            sftpChannel.disconnect();
            session.disconnect();

            return strings;
        }
        catch(JSchException | SftpException | IOException e)
        {
            System.out.println("AURGI: " + e);
        }

        return null;

    }

    public List<Csvfile> listZipFileFromSftp(String user, String host, String sftpkey, String remotePath)
    {

        int port=22;

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            log.info("AURGI SFTP READING KEY" + sftpkey);

            if (sftpkey.equalsIgnoreCase("/home/apaniagua/.ssh/id_rsa")) {
                jsch.addIdentity(sftpkey,"MailSagApm17");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");
            log.info("AURGI: Establishing Connection..." + user + "@" + host + " with " + sftpkey);

            session.connect();
            log.info("AURGI: Connection established.");

            log.info("AURGI: Creating SFTP Channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            sftpChannel.cd(remotePath);
            List<Csvfile> files=new ArrayList<>();

            Vector<ChannelSftp.LsEntry> list = sftpChannel.ls("*.zip");
            for(ChannelSftp.LsEntry entry : list) {

                DateFormat dateFormat = new SimpleDateFormat(
                        "EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                try {
                    Date fecha = dateFormat.parse( entry.getAttrs().getMtimeString());
                    files.add(new Csvfile(entry.getFilename(),fecha.toLocaleString()));

                } catch (ParseException e) {
                    log.info("AURGI: " + e);
                }

            }

            sftpChannel.disconnect();
            session.disconnect();

            return files;
        }
        catch(JSchException | SftpException e)
        {
            log.error("AURGI: " + e);
        }

        return null;

    }

    public String execCommandSftp(String user,String host, String sftpkey) {

        int port=22;

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            System.out.println("AURGI SFTP READING KEY" + sftpkey);

            if (sftpkey.equalsIgnoreCase("/home/apaniagua/.ssh/id_rsa")) {
                jsch.addIdentity(sftpkey,"MailSagApm17");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");
            System.out.println("AURGI: Establishing Connection..." + user + "@" + host + " with " + sftpkey);

            session.connect();
            System.out.println("AURGI: Connection established.");

            // por ejecucion de comandos
            String command = "/bin/unzip";

            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);

            try{
                InputStream input = channel.getInputStream();
                System.out.println("AURGI: Createing exec channel.");

                channel.connect();

                System.out.println("Channel Connected to machine " + host + " server with command: " + command );

                InputStreamReader inputReader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(inputReader);
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    System.out.println(line);
                }
                bufferedReader.close();
                inputReader.close();
            }catch(IOException | JSchException ex){
                ex.printStackTrace();
            }

            channel.disconnect();
            session.disconnect();

            System.out.println("AURGI: SFTP DISCONNECT");


            return null;
        }


        catch(JSchException  e)
        {
            System.out.println("AURGI: " + e);
        }


        return null;

    }

    public List<String> unzipFileFromSftp(String user,String host, String sftpkey, String remoteZipFile, String sftpoutfolder)
    {

        //2019_04_04_03_30.zip
        int port=22;

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            log.info("AURGI SFTP READING KEY" + sftpkey);

            if (sftpkey.equalsIgnoreCase("/home/apaniagua/.ssh/id_rsa")) {
                jsch.addIdentity(sftpkey,"MailSagApm17");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");
            log.info("AURGI: Establishing Connection..." + user + "@" + host + " with " + sftpkey);

            session.connect();
            log.info("AURGI: Connection established.");


            log.info("AURGI: Creating SFTP Channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            log.info("AURGI: SFTP Reading zip file " + remoteZipFile);

            List<InputStream> inputStreams = new ArrayList<>();
            List<String> inputFilenames = new ArrayList<>();

            try (ZipInputStream zipInputStream = new ZipInputStream(sftpChannel.get(remoteZipFile))) {

                InputStream stream = zipInputStream;
                sftpChannel.put (stream, "/anjana/test/" + "zipInputStream");

                ZipEntry entry = zipInputStream.getNextEntry();

                while (entry != null) {

                    log.info("AURGI Zip file contains: " + entry.getName());

                    inputStreams.add(convertToInputStream(zipInputStream));
                    inputFilenames.add(entry.getName());

                    zipInputStream.closeEntry();
                    entry = zipInputStream.getNextEntry();
                }
            }


            sftpChannel.disconnect();
            log.info("AURGI: SFTP GET CHANNEL DISCONNECT");

            //another chanel for puting files
            ChannelSftp sftpPutChannel = (ChannelSftp) session.openChannel("sftp");
            sftpPutChannel.connect();

            //sftpPutChannel.put (inputStreams.get(0),"anjana/test/" + inputFilenames.get(0));
            //sftpPutChannel.put (inputStreams.get(1),"anjana/test/" + inputFilenames.get(1));

            for (int i = 0; i < inputFilenames.size(); i++) {
                sftpPutChannel.put (inputStreams.get(i),sftpoutfolder + inputFilenames.get(i));
            }


            sftpPutChannel.disconnect();
            log.info("AURGI: SFTP PUT CHANNEL DISCONNECT");

            session.disconnect();
            log.info("AURGI: SFTP SESSION DISCONNECT");


            return null;
        }


        catch(JSchException | IOException | SftpException e)
        {
            log.error("AURGI: " + e);
        }

        return null;

    }




    public boolean getSessionFromSftp(String user,String host, String sftpkey)
    {
        //String password = ""; // = "Q_7C<p2wGnh4_e{D";
        //host = "10.20.1.112";
        int port=22;

        //String remoteFile="sample.txt";

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            //change before deploy (no passphrase)
            //String privateKey = "/home/apaniagua/.ssh/id_rsa";


            log.info("AURGI SFTP READING KEY" + sftpkey);

            if (sftpkey.equalsIgnoreCase("/home/apaniagua/.ssh/id_rsa")) {
                jsch.addIdentity(sftpkey,"MailSagApm17");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();
            log.info("AURGI TEST: Connection established.");

            log.info("AURGI TEST: Creating SFTP Channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            sftpChannel.disconnect();
            session.disconnect();

            log.info("AURGI TEST: Sftp session and channel terminated ok.");

            return true;

        }
        catch(JSchException e)
        {
            log.error("AURGI: " + e);
        }

        return false;

    }


    void unzip(final String zipFilePath, final String unzipLocation) throws IOException {



        if (!(zipIsValid(new File(zipFilePath)))) {
            System.out.format(zipFilePath + " is a bad zip file");
            return;
        }

        if (!(Files.exists(Paths.get(unzipLocation)))) {
            Files.createDirectories(Paths.get(unzipLocation));
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {


            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                Path filePath = Paths.get(unzipLocation, entry.getName());
                if (!entry.isDirectory()) {
                    unzipFiles(zipInputStream, filePath);
                } else {
                    Files.createDirectories(filePath);
                }

                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        }
    }

    private static InputStream convertToInputStream(final ZipInputStream inputStreamIn) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(inputStreamIn, out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    void unzipFiles(final ZipInputStream zipInputStream, final Path unzipFilePath) throws IOException {

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(unzipFilePath.toAbsolutePath().toString()))) {
            byte[] bytesIn = new byte[1024];
            int read = 0;
            while ((read = zipInputStream.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }

    }

    boolean zipIsValid(final File file)  {


        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(file);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                    zipfile = null;
                }
            } catch (IOException e) {
            }
        }

    }

}
