from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page()
    page.goto("file://" + __import__("os").path.abspath("test_a11y.html"))

    # Use CDP to get accessibility tree
    client = page.context.new_cdp_session(page)
    client.send("Accessibility.enable")
    ax_tree = client.send("Accessibility.getFullAXTree")

    for node in ax_tree["nodes"]:
        role = node.get("role", {}).get("value")
        name = node.get("name", {}).get("value")
        if role == "navigation":
            print(f"Role: {role}, Name: '{name}'")

    browser.close()
