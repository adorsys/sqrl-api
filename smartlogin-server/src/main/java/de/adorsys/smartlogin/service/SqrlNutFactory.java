package de.adorsys.smartlogin.service;

import java.nio.charset.Charset;

import android.util.changed.Base64;
import net.vrallev.java.sqrl.SqrlProtocol;
import net.vrallev.java.sqrl.util.SqrlCipherTool;

/**
 * A factory for creating cryptographic strong unique strings called "nuts".
 * 
 * @author mko
 */
public final class SqrlNutFactory {

    private static SqrlCipherTool mCipherToolInstance;
    public static final Charset ASCII = Charset.forName("US-ASCII");;
    
    /**
     * Creates the nut.
     *
     * @return the string
     */
    public static String createNut() {
        if(mCipherToolInstance == null){
            mCipherToolInstance = SqrlProtocol.instance().getSqrlCipherTool();
        }
        byte[] rawNut = mCipherToolInstance.createRandomHash(160);
        return Base64.encodeToString(rawNut, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }
}
