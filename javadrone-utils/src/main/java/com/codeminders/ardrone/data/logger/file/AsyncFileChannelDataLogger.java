package com.codeminders.ardrone.data.logger.file;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.logging.Logger;

import com.codeminders.ardrone.data.logger.DataLogger;
import com.codeminders.ardrone.data.logger.ChannelDataChunk;


public class AsyncFileChannelDataLogger implements DataLogger {
    
    private static final int DAFAULT_QUEUE_CAPACITY = 128;
    private static final int DAFAULT_STREAM_QUEUE_CAPACITY = 32 * 1024; // 32 Kb;

    private Logger log = Logger.getLogger(getClass().getName());
    
    private static String LOG_FILE_SUFFIX_TIME_FORMAT = "yyMMdd_HH:mm:ss";
    private static SimpleDateFormat nameFormat = new SimpleDateFormat(LOG_FILE_SUFFIX_TIME_FORMAT);
    private static char indexSeparator = '_';
    
    SaveToFile toFile;
    SaveStreamToFile streamFile;
    
    public AsyncFileChannelDataLogger(String logDirPath, String filePrefix) throws ClosedChannelException,
            IOException {
        
        validate(logDirPath, filePrefix);
        shiftIndexOfPreviousLogFiles(logDirPath, filePrefix);
        
        toFile = new SaveToFile(new File(logDirPath + File.separatorChar + getFileName(filePrefix) + "-chunk" + getFileExt())  , DAFAULT_QUEUE_CAPACITY);
        toFile.start();
        
        streamFile = new SaveStreamToFile(new File(logDirPath + File.separatorChar + getFileName(filePrefix)+ "-stream" + getFileExt())  , DAFAULT_STREAM_QUEUE_CAPACITY);
        toFile.start();
    }
   

    @Override
    public void log(ChannelDataChunk data) {
          toFile.toFile(data);
    }
    
    @Override
    public void logStreamContent(int data) {
        streamFile.toFile(data);
    }
    
    private void validate(String logDirPath, String filePrefix) {
        if (null == logDirPath || logDirPath.trim().isEmpty()) {
            log.severe("Error. Please scpecify correct value of logging derectory");
            throw new RuntimeException("Error. Please scpecify correct value of logging derectory");
        }
        if (null == filePrefix || filePrefix.trim().isEmpty()) {
            log.severe("Error. Please scpecify correct value of logging file prefix");
            throw new RuntimeException("Error. Please scpecify correct value of logging file prefix");
        }
        
        File dir = new File(logDirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            log.severe("Error. Please scpecify correct value of logging derectory. Current: " + logDirPath);
            throw new RuntimeException("Error. Please scpecify correct value of logging derectory. Current: " + logDirPath);
        } else if (!dir.isDirectory()) {
            log.severe("Error. Value " + logDirPath + " is not directory");
            throw new RuntimeException("Error. Value " + logDirPath + " is not directory");
        } else if (!dir.canWrite()) {
            log.severe("Error. Have no wright permissions to " + logDirPath);
            throw new RuntimeException("Error. Have no wright permissions to " + logDirPath);
        }
    }
    
    private void shiftIndexOfPreviousLogFiles(String logDirPath, String filePrefix) {
        File dir = new File(logDirPath);
        File[] files = dir.listFiles();
        
        if (null != files) {
            for (File file : files) {
                String name = file.getName();
                if (name.startsWith(filePrefix)) {
                    int indexPosition;
                    if (-1 != (indexPosition = name.lastIndexOf(indexSeparator)) && (indexPosition > (LOG_FILE_SUFFIX_TIME_FORMAT.length() + filePrefix.length()))) {
                        int index = Integer.parseInt(name.substring(indexPosition + 1, name.indexOf('.')));
                        file.renameTo(new File(logDirPath + File.separatorChar + name.substring(0, indexPosition + 1) + (++index) + getFileExt()));  
                    } else if (!file.renameTo(new File(logDirPath + File.separatorChar + name.substring(0, name.indexOf('.')) + indexSeparator + '0' + getFileExt()))) {
                        log.severe("Error. Can't increase last log file index version");
                        throw new RuntimeException("Error. Can't increase last log file index version");
                    }
                    
                }
            }
         }
    }
    
    private String getFileExt() {
        return ".dat";
    }

    private String getFileName(String filePrefix) {
        return filePrefix + "_" + nameFormat.format(new Date());
    }
    
    
    public synchronized void finish() {
        toFile.finish();
        
        try {
            toFile.join(1000);
        } catch (InterruptedException e) {
            log.severe("Error. Faild to wait till file logger is topped or aborded");
        } 
        
        streamFile.finish();
        
        try {
            streamFile.join(1000);
        } catch (InterruptedException e) {
            log.severe("Error. Faild to wait till stream file logger is topped or aborded");
        } 
    }

}
