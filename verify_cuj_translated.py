from playwright.sync_api import sync_playwright
import os

def run_cuj(page):
    # Instead of running actual Chrome Translate, which is hard to mock in Playwright headless,
    # we manually mock the DOM mutation that Google Translate does to the text node.
    filepath = "file://" + os.path.abspath("testdir/index.html")
    page.goto(filepath)
    page.wait_for_timeout(500)

    # Mock translation
    page.evaluate('''() => {
        const h2 = document.getElementById('nav-heading');
        if (h2) {
            h2.innerHTML = '<font style="vertical-align: inherit;"><font style="vertical-align: inherit;">Directory listing</font></font>';
        }
    }''')
    page.wait_for_timeout(500)

    # Get the accessible name of the nav element to verify it picked up the translated text
    nav_name = page.evaluate('''() => {
        const nav = document.querySelector('nav');
        // A simple way to get computed accessible name in JS if we don't have axe
        return nav.getAttribute('aria-labelledby') ? document.getElementById(nav.getAttribute('aria-labelledby')).textContent : nav.getAttribute('aria-label');
    }''')
    print(f"Accessible name of nav after translation: {nav_name}")

    page.screenshot(path="/home/jules/verification/screenshots/verification_translated.png", full_page=True)
    page.wait_for_timeout(1000)

if __name__ == "__main__":
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(
            record_video_dir="/home/jules/verification/videos"
        )
        page = context.new_page()
        try:
            run_cuj(page)
        finally:
            context.close()
            browser.close()
