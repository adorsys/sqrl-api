package de.adorsys.smartlogin.sqrl;

/**
 * Enum of possible states to inform the web client about the sqrl auth state.
 * 
 * @author mko
 */
public enum SqrlState {
    NONE, // unset
    INITIALIZED, // web client requested uri with nut
    PREPARED, // necessary preparation ready
    SUCCEEDED, // finish - yeah
    LOGIN_SUCCEEDED, // finish - yeah
    CREATE_SUCCEEDED, // finish - yeah
    FAILED // finish - oh no
}
