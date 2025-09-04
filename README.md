# AI Assistant - IntelliJ IDEA Plugin

## Project Overview

AI Assistant is an IntelliJ IDEA plugin designed to provide developers with intelligent programming assistance tools. The plugin integrates locally deployted large language models to provide intelligent code completion, code review, and optimization suggestions to enhance development efficiency and code quality.

## Core Features

### 🤖 AI Chat Assistant
- **Multi-Session Management**: Support for creating, renaming, and deleting multiple chat sessions
- **Real-time Conversation**: Real-time dialogue with local AI models (via Ollama)
- **Context-Aware**: Support for adding file references to provide project context to AI
- **Markdown Rendering**: Support for rich text Markdown format response display

### ⚡ Intelligent Code Completion
- **Real-time Code Suggestions**: Context-based intelligent code completion
- **Keyboard Shortcut Support**: Smart handling of Tab, Enter, and Escape keys
- **Inline Display**: Code completion suggestions displayed inline without disrupting coding flow



## Technical Architecture

### Project Structure
```
src/main/java/mp25/aiassistant/
├── core/                    # Plugin initialization
│   └── AIAssistantPlugin.java
├── ai/                      # AI service integration
│   └── OllamaService.java
├── chat/                    # Chat session management
│   ├── ChatSession.java
│   └── SessionManager.java
├── completion/              # Intelligent code completion
│   ├── CompletionPlugin.java
│   ├── handlers/           # Keyboard event handlers
│   ├── managers/           # Completion managers
│   └── services/           # Completion services
├── ui/                      # User interface components
│   └── MainLayer.java
├── extensibility/           # Extensibility framework
│   ├── ExtensibleFeatureManager.java
│   ├── commands/           # Shortcut commands
│   └── menus/             # Context menus
└── utils/                   # Utility classes
    ├── markdown/           # Markdown processing
    └── ReferenceProcessor  # Reference handling
```



## Deployment Methods

### System Requirements
- **IntelliJ IDEA**: Version 2023.2 or higher (Community/Ultimate)
- **Java**: JDK 17 or higher
- **Ollama**: Local AI model service (for AI functionality)

### 1. Build and Deploy from Source

#### Step 1: Environment Preparation
```bash
# 1. Ensure JDK 17+ is installed
java -version

# 2. Install Ollama (for AI functionality)
# Visit https://ollama.ai to download and install

# 3. Pull AI models
ollama pull llama2
ollama pull codellama
```

#### Step 2: Build Plugin
```bash
# 1. Clone the project
git clone <repository-url>
cd AIassistant

# 2. Build the plugin
./gradlew buildPlugin

# 3. Plugin package will be generated at:
# build/distributions/AIassistant-1.0-SNAPSHOT.zip
```

#### Step 3: Install Plugin
1. Open IntelliJ IDEA
2. Go to `File → Settings → Plugins`
3. Click the gear icon → `Install Plugin from Disk...`
4. Select the generated plugin package file
5. Restart IntelliJ IDEA

### 2. Development Environment Deployment

#### Running Development Version
```bash
# Run plugin directly for testing
./gradlew runIde
```



### 3. Production Environment Configuration

#### Ollama Service Configuration
```bash
# Start Ollama service
ollama serve

# Verify service status
curl http://localhost:11434/api/tags
```









