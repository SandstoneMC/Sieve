package com.example.guest;

import com.example.host.IPlugin;

import java.util.function.BiFunction;

public class TestPlugin implements IPlugin {

    private static final String PLUGIN_NAME = "Example Guest Plugin";
    private static final BiFunction<String, Wrapper, String> wrapFunc = (str, wrap) -> wrap.prefix + str + wrap.suffix;

    public String getName() {
        return wrapFunc.apply(PLUGIN_NAME, Wrapper.CURLY);
    }

    private static String wrap(String str) {
        return "(" + str + ")";
    }

    enum Wrapper {
        SQUARE("[", "]"),
        ROUND("(", ")"),
        TRIANGLE("<", ">"),
        CURLY("{", "}"),
        SLASH("/", "\\"),
        DASH("-", "-");

        final String prefix;
        final String suffix;

        Wrapper(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }
    }
}