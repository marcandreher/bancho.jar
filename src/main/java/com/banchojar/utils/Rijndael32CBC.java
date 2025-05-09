package com.banchojar.utils;

import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CBCModeCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class Rijndael32CBC {
    public static byte[] encrypt(byte[] plaintext, byte[] key, byte[] iv) throws Exception {
       RijndaelEngine engine = new RijndaelEngine(256);
       CBCModeCipher cbc = CBCBlockCipher.newInstance(engine);
       PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbc, new ZeroBytePadding());

       cipher.init(true, new ParametersWithIV(new KeyParameter(key), iv));

       byte[] output = new byte[cipher.getOutputSize(plaintext.length)];
       int len = cipher.processBytes(plaintext, 0, plaintext.length, output, 0);
       len += cipher.doFinal(output, len);

       byte[] result = new byte[len];
       System.arraycopy(output, 0, result, 0, len);
       return result;
    }

    public static byte[] decrypt(byte[] ciphertext, byte[] key, byte[] iv) throws Exception {
       RijndaelEngine engine = new RijndaelEngine(256);
       CBCModeCipher cbc = CBCBlockCipher.newInstance(engine);
       PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbc, new ZeroBytePadding());

       cipher.init(false, new ParametersWithIV(new KeyParameter(key), iv));

       byte[] output = new byte[cipher.getOutputSize(ciphertext.length)];
       int len = cipher.processBytes(ciphertext, 0, ciphertext.length, output, 0);
       len += cipher.doFinal(output, len);

       byte[] result = new byte[len];
       System.arraycopy(output, 0, result, 0, len);
       return result;
    }
}
