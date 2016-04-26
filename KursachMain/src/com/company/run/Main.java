package com.company.run;

import com.company.core.Server;
import com.company.dao.DatabaseUtils;

public class Main {

    public static void main(String[] args) throws Exception {
        DatabaseUtils.prepare();
        new Server(9001);
    }
}
