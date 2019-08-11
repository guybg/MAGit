package com.magit.ui;

import com.magit.logic.system.MagitEngine;

import java.io.IOException;

public class UIMain {
    public static void main(String[] args) {
        MagitEngine magitEngine = new MagitEngine();
        try {
            UserInterface.run(magitEngine);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
