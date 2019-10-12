package com.pedro.rtsp.rtsp;

import android.media.MediaCodec;
import android.util.Log;

import com.google.gson.Gson;
import com.pedro.rtsp.rtcp.BaseSenderReport;
import com.pedro.rtsp.rtp.packets.AacPacket;
import com.pedro.rtsp.rtp.packets.AudioPacketCallback;
import com.pedro.rtsp.rtp.packets.BasePacket;
import com.pedro.rtsp.rtp.packets.H264Packet;
import com.pedro.rtsp.rtp.packets.H265Packet;
import com.pedro.rtsp.rtp.packets.VideoPacketCallback;
import com.pedro.rtsp.rtp.sockets.BaseRtpSocket;
import com.pedro.rtsp.rtp.sockets.ICSocket;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;
import com.pedro.rtsp.utils.IntegrityObject;
import com.pedro.rtsp.utils.RtpConstants;
import com.pedro.rtsp.utils.SymetricEncryption;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.security.*;

/**
 * Created by pedro on 7/11/18.
 */

public class RtspSender implements VideoPacketCallback, AudioPacketCallback {

  private final static String TAG = "RtspSender";
  private BasePacket videoPacket;
  private AacPacket aacPacket;
  private BasePacket icPacket;
  private BaseRtpSocket rtpSocket;
  private ICSocket icSocket;
  private BaseSenderReport baseSenderReport;
  private volatile BlockingQueue<RtpFrame> rtpFrameBlockingQueue =
      new LinkedBlockingQueue<>(getDefaultCacheSize());

  private volatile BlockingQueue<IntegrityObject> rtpSequenceQueue =
          new LinkedBlockingQueue<>(getDefaultCacheSize());
  private Thread thread;
  private ConnectCheckerRtsp connectCheckerRtsp;
  private long audioFramesSent = 0;
  private long videoFramesSent = 0;
  private long droppedAudioFrames = 0;
  private long droppedVideoFrames = 0;


  public RtspSender(ConnectCheckerRtsp connectCheckerRtsp) {
    this.connectCheckerRtsp = connectCheckerRtsp;
  }

  public void setInfo(Protocol protocol, byte[] sps, byte[] pps, byte[] vps, int sampleRate) {
    videoPacket =
        vps == null ? new H264Packet(sps, pps, this) : new H265Packet(sps, pps, vps, this);
    aacPacket = new AacPacket(sampleRate, this);
    rtpSocket = BaseRtpSocket.getInstance(protocol);
    icSocket = new ICSocket();
    baseSenderReport = BaseSenderReport.getInstance(protocol);
  }

  /**
   * @return number of packets
   */
  private int getDefaultCacheSize() {
    return 10 * 1024 * 1024 / RtpConstants.MTU;
  }

  public void setDataStream(OutputStream outputStream, String host) {
    icSocket.setDataStream(outputStream, host );//+ //":9000");
    rtpSocket.setDataStream(outputStream, host);
    baseSenderReport.setDataStream(outputStream, host);
  }

  public void setVideoPorts(int rtpPort, int rtcpPort) {
    videoPacket.setPorts(rtpPort, rtcpPort);
  }

  public void setAudioPorts(int rtpPort, int rtcpPort) {
    aacPacket.setPorts(rtpPort, rtcpPort);
  }




  public void sendVideoFrame(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
    videoPacket.createAndSendPacket(h264Buffer, info);
  }

  public void sendAudioFrame(ByteBuffer aacBuffer, MediaCodec.BufferInfo info) {
    aacPacket.createAndSendPacket(aacBuffer, info);
  }

  public void sendIntegrityFrame(ByteBuffer buffer){

  }

  @Override
  public void onVideoFrameCreated(RtpFrame rtpFrame) {
    try {
      rtpFrameBlockingQueue.add(rtpFrame);
//      IntegrityObject integrityObject = new IntegrityObject(rtpFrame);
//      rtpSequenceQueue.add(integrityObject);

    } catch (IllegalStateException e) {
      System.out.println(e);
      Log.i(TAG, "Video frame discarded");
      droppedVideoFrames++;
    }
//    } catch (NoSuchAlgorithmException e) {
//      e.printStackTrace();
//    }
  }

  @Override
  public void onAudioFrameCreated(RtpFrame rtpFrame) {
    try {
      rtpFrameBlockingQueue.add(rtpFrame);
//      IntegrityObject integrityObject = new IntegrityObject(rtpFrame);
//      rtpSequenceQueue.add(integrityObject);

    } catch   (IllegalStateException e) {
      Log.i(TAG, "Audio frame discarded");
      droppedAudioFrames++;
    }
//    } catch (NoSuchAlgorithmException e) {
//      e.printStackTrace();
//    }
  }

  public void start() {
    final Gson gson = new Gson();


    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (!Thread.interrupted()) {

          try {
            RtpFrame rtpFrame = rtpFrameBlockingQueue.poll(1, TimeUnit.SECONDS);
            if (rtpFrame == null) {
              Log.i(TAG, "Skipping iteration, frame null");
              continue;
            }

            ////////////////////////////////
            ///////Integrity Pipeline///////
            ////////////////////////////// /
            try {
              //Start sequencing each 1 in 5 frames;
              //Take a frame, and create and poll the integrity object.
              //Send the integrity object to the server. With TCP

              if(rtpFrame.getSequence() % 5 == 0) {
                IntegrityObject integrityObject = new IntegrityObject(rtpFrame);
                Socket clientSocket = new Socket("192.168.2.13", 27005);
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream());
                output.print(gson.toJson(integrityObject));
                output.flush();
                output.close();
              }

            }catch (Exception e){
                System.out.println(e);
            }
            ////////////////////////////////
            ///////End/////////////////////
            ///////////////////////////////

            rtpSocket.sendFrame(rtpFrame);


            if (rtpFrame.isVideoFrame()) {
              videoFramesSent++;
            } else {
              audioFramesSent++;
            }

            baseSenderReport.update(rtpFrame);

          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } catch (IOException e) {
            Log.e(TAG, "send error: ", e);
            connectCheckerRtsp.onConnectionFailedRtsp("Error send packet, " + e.getMessage());
          }
        }
      }
    });
    thread.start();
  }

  public void stop() {
    if (thread != null) {
      thread.interrupt();
      try {
        thread.join(100);
      } catch (InterruptedException e) {
        thread.interrupt();
      }
      thread = null;
    }
    rtpFrameBlockingQueue.clear();
    baseSenderReport.reset();
    baseSenderReport.close();
    rtpSocket.close();
    aacPacket.reset();
    videoPacket.reset();

    resetSentAudioFrames();
    resetSentVideoFrames();
    resetDroppedAudioFrames();
    resetDroppedVideoFrames();
  }

  public void resizeCache(int newSize) {
    if (newSize < rtpFrameBlockingQueue.size() - rtpFrameBlockingQueue.remainingCapacity()) {
      throw new RuntimeException("Can't fit current cache inside new cache size");
    }

    BlockingQueue<RtpFrame> tempQueue = new LinkedBlockingQueue<>(newSize);
    rtpFrameBlockingQueue.drainTo(tempQueue);
    rtpFrameBlockingQueue = tempQueue;
  }

  public int getCacheSize() {
    return rtpFrameBlockingQueue.size();
  }

  public long getSentAudioFrames() {
    return audioFramesSent;
  }

  public long getSentVideoFrames() {
    return videoFramesSent;
  }

  public long getDroppedAudioFrames() {
    return droppedAudioFrames;
  }

  public long getDroppedVideoFrames() {
    return droppedVideoFrames;
  }

  public void resetSentAudioFrames() {
    audioFramesSent = 0;
  }

  public void resetSentVideoFrames() {
    videoFramesSent = 0;
  }

  public void resetDroppedAudioFrames() {
    droppedAudioFrames = 0;
  }

  public void resetDroppedVideoFrames() {
    droppedVideoFrames = 0;
  }
}
