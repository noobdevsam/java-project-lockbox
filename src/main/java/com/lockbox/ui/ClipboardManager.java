package com.lockbox.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Timer;
import java.util.TimerTask;

public class ClipboardManager {
    private static Timer timer;

    public static void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);

        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer(true); // Daemon thread
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                clearClipboard();
            }
        }, 30000); // 30 seconds
    }

    public static void clearClipboard() {
        StringSelection emptySelection = new StringSelection("");
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(emptySelection, emptySelection);
    }
}
