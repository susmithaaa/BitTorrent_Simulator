package com.logger;

import com.Constants;
import com.Peer;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Logger {

    int peer_ID;

    protected final String logFile;

    public Logger(int peer_ID){
        this.peer_ID = peer_ID;
        this.logFile = Constants.root + "/"+"log_peer_"+Peer.getPeerInstance().get_peerID()+".log";
    }

    public void log(String s) throws IOException {
        log(logFile, s);
    }

    public void log(String f, String s) throws IOException {
        Date date = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd 'at' hh:mm:ss a zzz");
        String currentTime = ft.format(date);
        FileWriter aWriter = new FileWriter(f, true);
        aWriter.write(currentTime + ": Peer " + peer_ID + s + "\n");
        aWriter.flush();
        aWriter.close();
    }
}
