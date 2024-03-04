package de.mhus.commons.security;

import de.mhus.commons.crypt.DummyEncoder;
import de.mhus.commons.crypt.Md5Encoder;
import de.mhus.commons.crypt.Rot13And5Encoder;
import de.mhus.commons.crypt.Rot13Encoder;

import java.util.HashMap;
import java.util.Map;

public class DefaultPasswordEncoderFactory implements IPasswordEncoderFactory {

    private Map<String, IPasswordEncoder> encodings = new HashMap<>();

    public DefaultPasswordEncoderFactory(){
        encodings.put(MPassword.ROT13, new Rot13Encoder());
        encodings.put(MPassword.ROT13AND5, new Rot13And5Encoder());
        encodings.put(MPassword.DUMMY, new DummyEncoder());
        encodings.put(MPassword.MD5, new Md5Encoder());
    }

    @Override
    public Map<String, IPasswordEncoder> get() {
        return encodings;
    }
}
