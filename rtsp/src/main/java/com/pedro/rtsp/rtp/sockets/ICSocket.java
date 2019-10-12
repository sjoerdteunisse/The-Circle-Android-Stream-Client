package com.pedro.rtsp.rtp.sockets;

//Integrity Socket

import android.util.Log;

import com.pedro.rtsp.rtsp.RtpFrame;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

public class ICSocket {


    private OutputStream outputStream;

    public ICSocket(){

    }

    public void setDataStream(OutputStream outputStream, String host) {
        this.outputStream = outputStream;
    }

    public void sendFrame(String json) throws IOException {
        sendFrameTCP(json);
    }

    public void close() {

    }

    private void sendFrameTCP(String json) throws IOException {
        synchronized (outputStream) {
            int len = json.length();

            outputStream.write(json.getBytes(), 0, len);

            outputStream.flush();


            Log.i(this.getClass().getName(), "wrote packet: "
                    + ("frame IC out")
                    + ", size: "
                    + json.length());
        }
    }


}
