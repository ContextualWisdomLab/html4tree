## 2026-06-27 - [CLI Output Accessibility]
**Learning:** Even simple generated HTML outputs (like static index pages) from CLI tools often lack basic accessibility semantics out-of-the-box. Emoticons (📁, ▹) are insufficient indicators for screen readers identifying interactive elements like file links.
**Action:** Always inject `aria-label` attributes to explicitly describe interactive items (e.g., "Parent directory", "Directory: folderName") when generating static HTML UI, replacing or augmenting icon-only context.
