from playwright.sync_api import sync_playwright

html_content = """
<!DOCTYPE html>
<html>
<head>
<style>
.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  margin: -1px;
  padding: 0;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>
</head>
<body>
  <h1>Test</h1>
  <nav id="old" aria-label="디렉토리 목록">Old</nav>
  <nav id="new" aria-labelledby="nav-heading">
    <h2 id="nav-heading" class="visually-hidden"><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">Directory listing</font></font></h2>
    New
  </nav>
</body>
</html>
"""

with open("test_a11y.html", "w") as f:
    f.write(html_content)

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page()
    page.goto("file://" + __import__("os").path.abspath("test_a11y.html"))

    snapshot = page.accessibility.snapshot()

    for node in snapshot.get("children", []):
        if node.get("role") == "navigation":
            print(f"Role: {node.get('role')}, Name: '{node.get('name')}'")

    browser.close()
