package com.codeminders.ardrone.video;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;

import com.codeminders.ardrone.ARDrone;

public class VideoReader implements Runnable {
	
	private final ARDrone 			drone;
	private DatagramChannel  		channel;

	public VideoReader (ARDrone drone, InetAddress drone_addr, int video_port) throws IOException {
		this.drone = drone;
		
		channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(video_port));
        channel.socket().setSoTimeout(3000);
        channel.connect(new InetSocketAddress(drone_addr, video_port));
	}
	
	@Override
	public void run() {
		try {
			byte[] buf_snd = {0x01, 0x00, 0x00, 0x00};
            DatagramPacket packet_snd = new DatagramPacket(buf_snd, buf_snd.length);
            channel.socket().send(packet_snd);
            byte[] buf_rcv = new byte[153600];
            DatagramPacket packet_rcv = new DatagramPacket(buf_rcv, buf_rcv.length);
            BufferedImage image;
            while (true) {
            	channel.socket().receive(packet_rcv);
            	image = ImageDecoder.readUINT_RGBImage(buf_rcv);
            	drone.ImageReceived(image);
            	channel.socket().send(packet_snd);
            }
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
}
