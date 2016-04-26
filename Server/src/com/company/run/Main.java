package com.company.run;

import com.company.kerberos.KerberosServer;
import com.company.kerberos.KerberosTokenUpdateType;

public class Main {

    public static void main(String[] args) throws Exception {
        new KerberosServer(9000, 3600000, KerberosTokenUpdateType.CLOSE);
    }
}
