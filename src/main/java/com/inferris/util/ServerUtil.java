package com.inferris.util;

import com.inferris.Inferris;
import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;

import java.util.logging.Level;

public class ServerUtil {

    public static void log(String message, Level level){
        Inferris.getInstance().getLogger().log(level, message);
    }

    public static void log(String message, Level level, ServerState serverState) {
        if(ServerStateManager.getCurrentState() == serverState){
            log(message, level);
        }
    }

    public static void log(String message, Level level, ServerState serverState, ServerState serverState2) {
        if(ServerStateManager.getCurrentState() == serverState || ServerStateManager.getCurrentState() == serverState2){
            log(message, level);
        }
    }
}
