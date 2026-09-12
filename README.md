# Nexo AI 2.0

Aplicativo Android + backend PHP para InfinityFree usando navegação WebView em vez de REST direto, evitando depender de um backend no GitHub/Render.

## Estrutura
- `android/` — aplicativo Android nativo.
- `htdocs/` — conteúdo a enviar ao InfinityFree.
- `.github/workflows/build-apk.yml` — compilação do APK.

## Segurança
Nunca envie `htdocs/config.local.php` para o GitHub. A chave OpenAI deve existir somente no servidor.
