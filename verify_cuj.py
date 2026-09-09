from playwright.sync_api import sync_playwright
import os
import subprocess
import time

def run_cuj(page):
    filepath = "file://" + os.path.abspath("testdir/index.html")
    page.goto(filepath)
    page.wait_for_timeout(500)

    # Optional: interact with translated text by translating it

    page.screenshot(path="/home/jules/verification/screenshots/verification.png", full_page=True)
    page.wait_for_timeout(1000)

if __name__ == "__main__":
    os.makedirs("/home/jules/verification/videos", exist_ok=True)
    os.makedirs("/home/jules/verification/screenshots", exist_ok=True)

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(
            record_video_dir="/home/jules/verification/videos",
            locale="en-US"
        )
        page = context.new_page()
        try:
            run_cuj(page)
        finally:
            context.close()
            browser.close()
