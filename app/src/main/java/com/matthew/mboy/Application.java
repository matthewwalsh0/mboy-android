package com.matthew.mboy;

public class Application {

    private static Application s_instance;

    private Gameboy m_gameboy;

    public Gameboy getGameboy() {
        return m_gameboy;
    }

    public Gameboy createGameboy() {
        m_gameboy = new Gameboy();
        return m_gameboy;
    }

    public static Application getInstance() {
        if(s_instance == null) {
            s_instance = new Application();
        }

        return s_instance;
    }

}
