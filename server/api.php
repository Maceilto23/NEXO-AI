<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-App-Token');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

if (($_SERVER['REQUEST_METHOD'] ?? '') === 'OPTIONS') {
    http_response_code(204);
    exit;
}

function envv(string $key, string $default = ''): string {
    $v = getenv($key);
    return $v === false ? $default : trim((string)$v);
}

function json_out(int $status, array $data): never {
    http_response_code($status);
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

$apiKey = envv('OPENAI_API_KEY');
$appToken = envv('APP_TOKEN');
$fast = envv('OPENAI_MODEL_FAST', 'gpt-5.6-luna');
$balanced = envv('OPENAI_MODEL_BALANCED', 'gpt-5.6-terra');
$smart = envv('OPENAI_MODEL_SMART', 'gpt-5.6-sol');
$maxChars = max(1000, (int)envv('MAX_CHARS', '12000'));

if (($_SERVER['REQUEST_METHOD'] ?? '') === 'GET') {
    json_out(200, [
        'ok' => true,
        'service' => 'Nexo AI Backend',
        'status' => 'online',
        'php' => PHP_VERSION,
        'curl' => function_exists('curl_init'),
        'openai_configured' => $apiKey !== '',
        'app_token_configured' => $appToken !== '',
        'models' => [
            'fast' => $fast,
            'balanced' => $balanced,
            'smart' => $smart,
        ],
    ]);
}

if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') {
    json_out(405, ['ok' => false, 'error' => 'Método não permitido']);
}

if (!function_exists('curl_init')) {
    json_out(500, ['ok' => false, 'error' => 'cURL não está disponível no servidor']);
}

$raw = file_get_contents('php://input');
$j = json_decode((string)$raw, true);
if (!is_array($j)) {
    json_out(400, ['ok' => false, 'error' => 'JSON inválido']);
}

$auth = trim((string)($_SERVER['HTTP_AUTHORIZATION'] ?? ''));
$xToken = trim((string)($_SERVER['HTTP_X_APP_TOKEN'] ?? ''));
if ($auth === '' && function_exists('getallheaders')) {
    $headers = getallheaders();
    if (is_array($headers)) {
        foreach ($headers as $k => $v) {
            if (strcasecmp((string)$k, 'Authorization') === 0) $auth = trim((string)$v);
            if (strcasecmp((string)$k, 'X-App-Token') === 0) $xToken = trim((string)$v);
        }
    }
}

$bodyToken = trim((string)($j['token'] ?? ''));
$provided = '';
if (stripos($auth, 'Bearer ') === 0) $provided = trim(substr($auth, 7));
if ($provided === '' && $xToken !== '') $provided = $xToken;
if ($provided === '' && $bodyToken !== '') $provided = $bodyToken;

if ($appToken === '') {
    json_out(500, ['ok' => false, 'error' => 'APP_TOKEN não configurado no servidor']);
}
if ($provided === '' || !hash_equals($appToken, $provided)) {
    json_out(401, ['ok' => false, 'error' => 'Token do aplicativo inválido']);
}
if ($apiKey === '') {
    json_out(500, ['ok' => false, 'error' => 'OPENAI_API_KEY não configurada no servidor']);
}

$text = trim((string)($j['text'] ?? ''));
$mode = trim((string)($j['mode'] ?? 'auto'));
$context = trim((string)($j['context'] ?? ''));
$style = trim((string)($j['style'] ?? ''));

if ($text === '') json_out(422, ['ok' => false, 'error' => 'Texto vazio']);
if (mb_strlen($text) > $maxChars) json_out(413, ['ok' => false, 'error' => 'Texto muito grande']);

$modeInstructions = [
    'auto' => 'Decida automaticamente se o usuário quer responder, reescrever ou melhorar o texto. Entregue a melhor versão pronta para enviar.',
    'reply' => 'O texto recebido é uma mensagem de outra pessoa. Escreva somente a melhor resposta que o usuário pode enviar.',
    'quick' => 'Produza uma resposta muito curta, natural e eficiente, pronta para enviar.',
    'optimize' => 'Reescreva o texto mantendo a intenção, porém mais claro, elegante e natural.',
    'professional' => 'Reescreva em tom profissional, seguro, cordial e objetivo, sem excesso de formalidade.',
    'polite' => 'Reescreva de forma educada, cordial e natural, preservando firmeza quando necessário.',
    'direct' => 'Reescreva de forma direta e objetiva, sem soar ríspido.',
    'persuasive' => 'Reescreva de forma convincente, estratégica e respeitosa, sem manipulação nem exageros.',
    'natural' => 'Reescreva em português brasileiro natural, espontâneo e humano.',
    'firm' => 'Reescreva com firmeza, respeito e clareza, sem agressividade.',
    'correct' => 'Corrija português, pontuação e clareza sem mudar o sentido.',
];

$instruction = $modeInstructions[$mode] ?? $modeInstructions['auto'];
$model = in_array($mode, ['quick', 'correct'], true)
    ? $fast
    : (in_array($mode, ['auto', 'reply', 'persuasive', 'firm'], true) ? $smart : $balanced);

$system = 'Você é Nexo AI, um copiloto pessoal de mensagens. Escreva sempre em português brasileiro. Gere apenas o texto final que deve ser enviado, sem explicações, títulos ou aspas. Seja natural e não pareça texto de IA. Evite linguagem engessada, repetições e formalidade excessiva. Adapte-se ao contexto.';
if ($style !== '') $system .= ' Estilo pessoal preferido: ' . $style . '.';
$system .= ' Ação solicitada: ' . $instruction;
if ($context !== '') $system .= ' Contexto adicional fornecido pelo usuário: ' . $context;

$payload = [
    'model' => $model,
    'input' => [
        ['role' => 'system', 'content' => [['type' => 'input_text', 'text' => $system]]],
        ['role' => 'user', 'content' => [['type' => 'input_text', 'text' => $text]]],
    ],
    'max_output_tokens' => 700,
];

$ch = curl_init('https://api.openai.com/v1/responses');
curl_setopt_array($ch, [
    CURLOPT_POST => true,
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_CONNECTTIMEOUT => 15,
    CURLOPT_TIMEOUT => 60,
    CURLOPT_HTTPHEADER => [
        'Content-Type: application/json',
        'Authorization: Bearer ' . $apiKey,
    ],
    CURLOPT_POSTFIELDS => json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
]);

$body = curl_exec($ch);
$err = curl_error($ch);
$code = (int)curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

if ($body === false) {
    json_out(502, ['ok' => false, 'error' => 'Falha de conexão com OpenAI: ' . $err]);
}

$r = json_decode((string)$body, true);
if ($code < 200 || $code >= 300) {
    $msg = is_array($r) ? (string)($r['error']['message'] ?? ('OpenAI HTTP ' . $code)) : ('OpenAI HTTP ' . $code);
    json_out(502, ['ok' => false, 'error' => $msg, 'openai_status' => $code]);
}

$out = '';
if (is_array($r) && isset($r['output']) && is_array($r['output'])) {
    foreach ($r['output'] as $item) {
        if (($item['type'] ?? '') !== 'message' || !isset($item['content']) || !is_array($item['content'])) continue;
        foreach ($item['content'] as $part) {
            if (($part['type'] ?? '') === 'output_text') $out .= (string)($part['text'] ?? '');
        }
    }
}
$out = trim($out);
if ($out === '') json_out(502, ['ok' => false, 'error' => 'A IA não retornou texto']);

json_out(200, ['ok' => true, 'text' => $out, 'model' => $model]);
