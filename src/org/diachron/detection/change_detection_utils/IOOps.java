package org.diachron.detection.change_detection_utils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author rousakis
 */
public class IOOps {

    public static boolean createZipArchive(String srcFolder, OutputStream output) throws Exception {
        int BUFFER = 2048;
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(output));
        byte data[] = new byte[BUFFER];

        File subDir = new File(srcFolder);
        File subdirList[] = subDir.listFiles();
        for (File f : subdirList) {
            // get a list of files from current directory
            //File f = new File(srcFolder + File.separator + sd);
            if (f.isDirectory()) {
                File files[] = f.listFiles();

                for (int i = 0; i < files.length; i++) {
                    System.out.println("Adding: " + files[i].getName());
                    FileInputStream fi = new FileInputStream(files[i]);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(f.getName() + File.separator + files[i].getName());
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                        //out.flush();
                    }
                    out.closeEntry();
                    origin.close();

                }
            } else //it is just a file
            {
                System.out.println("Adding: " + f.getName() + " canRead:" + f.canRead());
                FileInputStream fi = new FileInputStream(f);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(f.getName());
                out.putNextEntry(entry);
                int count;
                int totalCount = 0;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                    //out.flush();
                    totalCount = totalCount + count;
                    System.out.println("Read inc. " + count + " bytes for: " + f.getName());
                }
                System.out.println("Read totally " + totalCount + " bytes for: " + f.getName());
                entry.setSize(totalCount);
                out.closeEntry();
                origin.close();
            }
        }
        //out.flush();
        out.finish();
        out.close();
        return true;
    }

    public static String readData(String filename) {
        File f = new File(filename);
        String s = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = br.readLine()) != null) {
                s += (line + "\n");
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage() + " occured .");
            return null;
        }
        return s;
    }

    public static void saveResponceToFile(InputStream entity, String filename) throws Exception {
        long start = System.currentTimeMillis();
        BufferedInputStream bis = new BufferedInputStream(entity);
        FileOutputStream out = new FileOutputStream(new File(filename));
        int size = 2048 * 1000;
        byte[] buffer = new byte[size];
        int count;
        while ((count = bis.read(buffer, 0, size)) != -1) {
            out.write(buffer, 0, count);
            out.flush();
        }
        out.close();
        bis.close();
        System.out.println("Saved XML answer: " + (System.currentTimeMillis() - start));
    }

    public static void testFileSize(int mb) throws IOException {
        File file = File.createTempFile("queryServiceResult", ".xml");
        file.deleteOnExit();
        char[] chars = new char[1024];
        Arrays.fill(chars, 'A');
        String longLine = new String(chars);
        long start1 = System.nanoTime();
        PrintWriter pw = new PrintWriter(new FileWriter(file));
        for (int i = 0; i < mb * 1024; i++) {
            pw.println(longLine);
        }
        pw.close();
        long time1 = System.nanoTime() - start1;
        System.out.printf("Took %.3f seconds to write to a %d MB, file rate: %.1f MB/s%n",
                time1 / 1e9, file.length() >> 20, file.length() * 1000.0 / time1);

        long start2 = System.nanoTime();
        BufferedReader br = new BufferedReader(new FileReader(file));
        while (br.readLine() != null) {
        }
        br.close();
        long time2 = System.nanoTime() - start2;
        System.out.printf("Took %.3f seconds to read to a %d MB file, rate: %.1f MB/s%n",
                time2 / 1e9, file.length() >> 20, file.length() * 1000.0 / time2);
        file.delete();
    }
}
