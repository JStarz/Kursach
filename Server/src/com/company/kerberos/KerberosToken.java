package com.company.kerberos;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

interface KerberosTokenExpired {
    void tokenExpired(KerberosToken token);
}

public class KerberosToken {

    final UUID tokenValue;

    KerberosTokenUpdateType type;
    KerberosTokenExpired delegate;

    Timer timer;
    TimerTask task;

    long ttl;

    public KerberosToken(long timeToLive, KerberosTokenUpdateType type) {
        this.tokenValue = UUID.randomUUID();
        this.type = type;
        this.ttl = timeToLive;
        this.timer = new Timer();
        startTimer(timeToLive);
    }

    private void startTimer(long timeToLive) {
        KerberosToken self = this;
        task = new TimerTask() {
            @Override
            public void run() {
                if (delegate != null)
                    delegate.tokenExpired(self);
            }
        };
        timer.schedule(task, timeToLive);
    }

    @Override
    protected void finalize() throws Throwable {
        timer.cancel();
        task.cancel();
        delegate = null;
        super.finalize();
    }

    @Override
    public String toString() {
        return tokenValue.toString();
    }
}
