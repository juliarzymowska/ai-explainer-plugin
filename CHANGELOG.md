# AI Console Explainer Changelog

## [Unreleased]
### Added

- ...

## [1.0.0]
### Added
- **AI Explainer Tool Window:** Introduced a dedicated tool window for analyzing stack traces and console errors.
- **Context-Aware Action:** Added the `SendToAiAction` to the editor context menu, allowing users to automatically extract selected error text and its corresponding `.java` source code file.
- **Multi-Provider Support:** Built a dynamic API Provider Factory to easily support multiple LLMs (e.g., OpenAI, Gemini).
- **Secure Credential Storage:** Integrated IntelliJ's native `PasswordSafe` to securely store API keys in the OS Keychain/Credential Manager.
- **Settings UI:** Created a fully functional Settings/Preferences page to manage API keys and model selection.
- **Testing Suite:** Implemented comprehensive Light Tests (`BasePlatformTestCase`) and pure JUnit tests to secure core data flow, state management, and UI logic.