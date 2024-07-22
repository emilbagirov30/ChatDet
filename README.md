# Чат с детектором лжи

Это приложение Android позволяет пользователям участвовать в сеансах чата с дополнительной функцией обнаружения лжи. Пользователи могут проходить аутентификацию, управлять своими аккаунтами и общаться различными способами, одновременно наблюдая за текущими действиями своих собеседников.

## Функции:

- **Аутентификация пользователя**: войдите в существующую учетную запись или создайте новую.
- **Регистрация**: загрузите аватар, установите уникальное имя пользователя и пароль во время регистрации.
- **Управление учетной записью**: обновление информации о пользователе в настройках учетной записи.
- **Доступ к сеансу**: введите существующий код доступа или создайте новый, чтобы начать общение с одним или несколькими участниками.

## Функциональность чата:

- Отправлять текстовые сообщения;
- Записывайте и отправляйте голосовые сообщения;
- Отправить изображения;
- Просмотр текущих действий собеседников (например, «печатает», «записывает голосовое сообщение» или «выбирает изображение»).
- **Детектор лжи**: проверка сообщений на предмет потенциальной лжи после их отправки.

## Технологический стек:

- **Kotlin и Java**: основные языки программирования, используемые в приложении.
- **Fragment**: для модульных компонентов пользовательского интерфейса.
- **SharedPreferences**: для локального хранения пользовательских настроек.
- **WebSocket**: для общения в реальном времени.
- **Coroutine**: для управления асинхронными задачами.
- **PostgreSQL**: для управления базами данных.
- **RecyclerView**: для отображения сообщений чата в виде оптимизированного списка.
- **Glide**: для загрузки и отображения изображений.

---

# Chat with Lie Detector

This Android application allows users to engage in chat sessions with the added feature of lie detection. Users can authenticate, manage their accounts, and communicate in various ways while seeing the current actions of their chat partners.

## Features:

- **User Authentication**: Sign in to an existing account or create a new one.
- **Registration**: Upload an avatar, set a unique username, and password during registration.
- **Account Management**: Update user information in account settings.
- **Session Access**: Enter an existing access code or create a new one to start chatting with one or more participants.

## Chat Functionality:

- Send text messages
- Record and send voice messages
- Send images
- View the current actions of chat partners (e.g., "typing," "recording a voice message," or "selecting an image")
- **Lie Detection**: Check messages for potential falsehoods after they are sent.

## Technology Stack:

- **Kotlin & Java**: Core programming languages for the app.
- **Fragment**: For modular UI components.
- **SharedPreferences**: For local storage of user settings.
- **WebSocket**: For real-time communication.
- **Coroutine**: For managing asynchronous tasks.
- **PostgreSQL**: For database management.
- **RecyclerView**: For displaying chat messages in a list.
- **Glide**: For loading and displaying images.
