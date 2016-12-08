package de.adorsys.smartlogin.service;

/**
 * Specified SQRL commands.
 * 
 * @author mko
 */
public final class SqrlCommands {
    // supported
    public static final String CREATE = "create";
    public static final String LOGIN = "login";
    public static final String SET_KEY = "setkey";
    public static final String SET_LOCK = "setlock";
    
    // not implemented
    public static final String DISABLE = "disable";
    public static final String ENABLE = "enable";
    public static final String DELETE = "delete";
    public static final String LOGME = "logme";
    public static final String LOGOFF = "logoff";
}
