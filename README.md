<div align="center">
  <img src="project/resources/icons/icon.png" width="160" height="160" alt="AndroidPE-Lite">
</div>

<h1 align="center">AndroidPE-Lite</h1>
# Contributing to AndroidPE-Lite

Thank you for considering contributing to **AndroidPE-Lite**! 🎉  
Your help is very welcome.

---

## How to Contribute

1. **Fork** this repository
2. Create a new branch for your feature/fix  
   (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Test your changes in **Sketchware Open Source**
5. Commit your changes
6. Push to your fork
7. Open a **Pull Request** to the `main` branch

---

## Important Guidelines

- **Always add comments** on every code you **change** or **add**.
- Write clear and meaningful comments so other contributors can easily understand your code.
- Follow the existing code style.
- Keep your changes focused (one feature/fix per Pull Request is preferred).
- Make sure your code doesn't break existing functionality.

### Example of Good Commenting:

```java
// Added support for custom branch name (2026-05-12)
public void pushToBranch(String branchName) {
    // ... code here
}

// Fixed NullPointerException when token is empty
if (TextUtils.isEmpty(token)) {
    listener.onError("GitHub token cannot be empty");
    return;
}
