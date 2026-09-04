package com.boardwise.backend.marketplace.service.webscrapers;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PreDestroy;

@Service
public class BrowserManager {

    private static final ThreadLocal<Playwright> playwrightTL = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserTL = new ThreadLocal<>();

    // track instances across threads
    private final Set<Playwright> allPlaywrights = ConcurrentHashMap.newKeySet();
    private final Set<Browser> allBrowsers = ConcurrentHashMap.newKeySet();

    public Browser getBrowser() {
        Browser browser = browserTL.get();
        if (browser == null || !browser.isConnected()) {
            Playwright pw = Playwright.create();
            browser = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            playwrightTL.set(pw);
            browserTL.set(browser);
            allPlaywrights.add(pw);
            allBrowsers.add(browser);
        }
        return browser;
    }

    @PreDestroy
    public void shutdown() {
        allBrowsers.forEach(Browser::close);
        allPlaywrights.forEach(Playwright::close);
    }
}