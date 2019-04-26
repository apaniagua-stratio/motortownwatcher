package com.stratio.microservice.motortownwatcher.service;

import com.jcraft.jsch.*;
import com.stratio.microservice.motortownwatcher.entity.CsvRow;
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
                jsch.addIdentity(sftpkey,"mypasswordforlocalkey");
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

    public List<CsvRow> unzipFileFromSftp(String user,String host, String sftpkey, String remoteZipFile, String sftpoutfolder)
    {

        int port=22;
        List<CsvRow> rowsReaded = new ArrayList<>();

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


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

                ZipEntry entry = zipInputStream.getNextEntry();

                while (entry != null) {

                    log.info("AURGI: Zip file contains this: " + entry.getName());

                    InputStream in = convertToInputStream(zipInputStream);

                    inputStreams.add(in);
                    inputFilenames.add(entry.getName());

                    //add row from csv to readed list
                    //motortown_stock_por_centro.csv
                    //motortown_productos_y_servicios.csv

                    //if (entry.getName().equalsIgnoreCase("motortown_stock_por_centro.csv")) {

                        Scanner scanner = new Scanner(in);
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            CsvRow row = new CsvRow(line, entry.getName());
                            rowsReaded.add(row);
                        }
                    //}

                    zipInputStream.closeEntry();
                    entry = zipInputStream.getNextEntry();
                }
            }


            sftpChannel.disconnect();
            log.info("AURGI: SFTP GET CHANNEL DISCONNECT");

            //another chanel for puting files
            ChannelSftp sftpPutChannel = (ChannelSftp) session.openChannel("sftp");
            sftpPutChannel.connect();

            log.info("AURGI: SFTP PUT CHANNEL CONNECT");



            for (int i = 0; i < inputFilenames.size(); i++) {
                sftpPutChannel.put (inputStreams.get(i),sftpoutfolder + inputFilenames.get(i));

            }


            sftpPutChannel.disconnect();
            log.info("AURGI: SFTP PUT CHANNEL DISCONNECT");

            session.disconnect();
            log.info("AURGI: SFTP SESSION DISCONNECT");


            return rowsReaded;
        }


        catch(JSchException | IOException | SftpException e)
        {
            log.error("AURGI: " + e);
        }

        return null;

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
