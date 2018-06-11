package org.astonbitecode.rustkeylock.api;

import static java.util.Arrays.asList;

import java.util.List;

import com.sun.jna.Structure;

public class JavaEntry extends Structure {

    public static class ByReference extends JavaEntry implements Structure.ByReference {
    }

    public static class ByValue extends JavaEntry implements Structure.ByValue {
    }

    public String name;
    public String url;
    public String user;
    public String pass;
    public String desc;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    protected List<String> getFieldOrder() {
        return asList("name", "url", "user", "pass", "desc");
    }
}
