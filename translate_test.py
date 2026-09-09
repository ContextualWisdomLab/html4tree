import urllib.parse
from bs4 import BeautifulSoup
import re

html_content = ""
with open("testdir/index.html", "r") as f:
    html_content = f.read()

soup = BeautifulSoup(html_content, "html.parser")
nav_heading = soup.find("h2", id="nav-heading")
print("Original nav_heading:", nav_heading)

# Let's mock a translation by modifying the text
nav_heading.string = "Directory listing"
print("Translated nav_heading:", nav_heading)

with open("testdir/index_translated.html", "w") as f:
    f.write(str(soup))
