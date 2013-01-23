package com.codeminders.ardrone.decoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import com.codeminders.ardrone.data.DataDecoder;
import com.codeminders.ardrone.data.reader.FileDataReader;

public class FileDataReaderAndDecoder extends FileDataReader {

    DataDecoder dataDecoder;
    
    public FileDataReaderAndDecoder(File dataFile, DataDecoder dataDecoder) throws FileNotFoundException {
        super(dataFile);
        this.dataDecoder = dataDecoder;
    }

    @Override
    public void handleData(ByteBuffer inbuf, int len) throws Exception {
//        dataDecoder.decodeData(inbuf, len);
    }

}
