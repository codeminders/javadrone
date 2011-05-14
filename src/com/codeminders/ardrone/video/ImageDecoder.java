package com.codeminders.ardrone.video;

//Copyright � 2007-2011, PARROT SA, all rights reserved. 

//DISCLAIMER 
//The APIs is provided by PARROT and contributors "AS IS" and any express or implied warranties, including, but not limited to, the implied warranties of merchantability 
//and fitness for a particular purpose are disclaimed. In no event shall PARROT and contributors be liable for any direct, indirect, incidental, special, exemplary, or 
//consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however 
//caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this 
//software, even if advised of the possibility of such damage. 

//Author            : Daniel Schmidt
//Publishing date   : 2010-01-06 
//based on work by  : Wilke Jansoone

//Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions
//are met:
//- Redistributions of source code must retain the above copyright notice, this list of conditions, the disclaimer and the original author of the source code.
//- Neither the name of the PixVillage Team, nor the names of its contributors may be used to endorse or promote products derived from this software without 
//  specific prior written permission.

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class ImageDecoder {

	private static byte[] process(byte[] rawData) {
		VideoImage image = new VideoImage();
		image.AddImageStream(rawData);

		
		
		uint[] outData = image.getPixelData();

		byte[] outDataB = new byte[outData.length * 3];
		for (int i = 0; i < outData.length; i++) {
			int i2 = i * 3;

			// Console.WriteLine("i {0} i2 {1}", i, i2);

			uint dataI = outData[i];

			// Console.WriteLine("  dataI {0}", dataI);

			byte[] elt = dataI.getBytes();
			outDataB[i2] = elt[2];
			outDataB[i2 + 1] = elt[1];
			outDataB[i2 + 2] = elt[0];

			// Console.WriteLine("  dataI {0} = {1} {2} {3} {4}", i, elt[0],
			// elt[1], elt[2], elt[3]);

		}
		return outDataB;
	}

	public static BufferedImage readUINT_RGBImage(byte[] rawData)
			throws Exception {
		byte[] processedData = process(rawData);

		// System.out.println("file size:" + processedData.length);

		int[] pixelData = new int[processedData.length / 3];
		int raw, pixel = 0, j = 0;
		for (int i = 0; i < pixelData.length; i++) {
			pixel = 0;
			raw = processedData[j++] & 0xFF;
			// System.out.println("raw[0]:" + Integer.toHexString(raw) + " " +
			// " pixel " + Integer.toHexString(pixel));
			pixel |= (raw << 16);
			raw = processedData[j++] & 0xFF;
			// System.out.println("raw[1]:" + Integer.toHexString(raw) + " " +
			// " pixel " + Integer.toHexString(pixel));
			pixel |= (raw << 8);
			raw = processedData[j++] & 0xFF;
			// System.out.println("raw[2]:" + Integer.toHexString(raw) + " " +
			// " pixel " + Integer.toHexString(pixel));
			pixel |= (raw << 0);

			// System.out.println("pixel:" + Integer.toHexString(pixel));
			pixelData[i] = pixel;
		}
		// System.out.println("image size:" + pixelData.length);

		BufferedImage image = new BufferedImage(320, 240,
				BufferedImage.TYPE_INT_RGB);

		image.setRGB(0, 0, 320, 240, pixelData, 0, 320);
		return image;

	}

	public static BufferedImage readUSHORT_565RGBImage(String inputFileName)
			throws Exception {
		byte[] stream = readFile(inputFileName);

		// System.out.println("file size:" + stream.length);

		return wrapIn565Image(stream, 320, 240);
	}

	private static byte[] readFile(String inputFileName)
			throws FileNotFoundException, IOException {
		byte[] stream;
		File inputFile = new File(inputFileName);
		stream = new byte[(int) inputFile.length()];
		FileInputStream fin = new FileInputStream(inputFile);
		fin.read(stream);
		fin.close();
		return stream;
	}

	public static BufferedImage wrapIn565Image(byte[] rawData, int w, int h) {
		// System.out.println("db size:" + (w * h * 2));

		int[] pixelData = new int[rawData.length / 2];
		int raw, pixel, j = 0;
		for (int i = 0; i < pixelData.length; i++) {
			raw = rawData[j++];
			pixel = ((raw & 0xf8) << 24) | ((raw & 0x07) << 13);
			raw = rawData[j++];
			pixel |= ((raw & 0xe0) << 5) | ((raw & 0x1f) << 3);

			pixelData[i] = pixel;
		}
		BufferedImage image = new BufferedImage(320, 240,
				BufferedImage.TYPE_INT_RGB);

		image.setRGB(0, 0, w, h, pixelData, 0, w);
		return image;
	}
}
