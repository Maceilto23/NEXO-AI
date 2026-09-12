package com.nexoai.ultra;

import android.content.Context;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public final class ApiClient {
    public interface Callback { void ok(String text); void fail(String error); }

    public static void generate(Context c, String mode, String input, String context, Callback cb) {
        new Thread(() -> {
            HttpURLConnection h = null;
            try {
                String endpoint = Prefs.backend(c).trim();
                String appToken = Prefs.token(c).trim();
                if (endpoint.isEmpty()) { cb.fail("URL do servidor não configurada."); return; }
                if (appToken.isEmpty()) { cb.fail("Configure e salve o token privado do aplicativo."); return; }

                URL u = new URL(endpoint);
                h = (HttpURLConnection) u.openConnection();
                h.setRequestMethod("POST");
                h.setConnectTimeout(20000);
                h.setReadTimeout(65000);
                h.setUseCaches(false);
                h.setDoInput(true);
                h.setDoOutput(true);
                h.setRequestProperty("Accept", "application/json");
                h.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                // InfinityFree/OpenResty pode rejeitar cabeçalhos de autenticação personalizados
                // antes de a requisição chegar ao PHP. O token segue protegido no corpo JSON
                // e é validado pelo backend com hash_equals().
                h.setRequestProperty("User-Agent", "NexoAI-Ultra/1.3 Android");

                JSONObject j = new JSONObject();
                j.put("mode", mode);
                j.put("text", input);
                j.put("context", context == null ? "" : context);
                j.put("style", Prefs.style(c));
                j.put("token", appToken);

                byte[] payload = j.toString().getBytes(StandardCharsets.UTF_8);
                h.setFixedLengthStreamingMode(payload.length);
                try (OutputStream os = h.getOutputStream()) {
                    os.write(payload);
                    os.flush();
                }

                int code = h.getResponseCode();
                InputStream is = code >= 200 && code < 300 ? h.getInputStream() : h.getErrorStream();
                String body = read(is);
                if (body == null || body.trim().isEmpty()) {
                    cb.fail("Servidor retornou resposta vazia (HTTP " + code + ").");
                    return;
                }

                JSONObject r;
                try { r = new JSONObject(body); }
                catch (Exception parse) {
                    cb.fail("Resposta inválida do servidor (HTTP " + code + "): " + shorten(body));
                    return;
                }

                if (code >= 200 && code < 300 && r.optBoolean("ok")) {
                    String text = r.optString("text", "").trim();
                    if (text.isEmpty()) cb.fail("A IA respondeu sem texto."); else cb.ok(text);
                } else {
                    String err = r.optString("error", "Erro HTTP " + code);
                    cb.fail(err + (code > 0 ? " (HTTP " + code + ")" : ""));
                }
            } catch (SocketTimeoutException e) {
                cb.fail("Tempo de resposta esgotado. Verifique a internet e tente novamente.");
            } catch (UnknownHostException e) {
                cb.fail("Não foi possível localizar o servidor. Verifique a internet e a URL configurada.");
            } catch (Exception e) {
                cb.fail(e.getMessage() == null ? e.toString() : e.getMessage());
            } finally {
                if (h != null) h.disconnect();
            }
        }).start();
    }

    public static void health(Context c, Callback cb) {
        new Thread(() -> {
            HttpURLConnection h = null;
            try {
                String endpoint = Prefs.backend(c).trim();
                if (endpoint.isEmpty()) { cb.fail("URL do servidor não configurada."); return; }
                URL u = new URL(endpoint);
                h = (HttpURLConnection) u.openConnection();
                h.setRequestMethod("GET");
                h.setConnectTimeout(20000);
                h.setReadTimeout(30000);
                h.setUseCaches(false);
                h.setRequestProperty("Accept", "application/json");
                h.setRequestProperty("User-Agent", "NexoAI-Ultra/1.3 Android");
                int code = h.getResponseCode();
                String body = read(code >= 200 && code < 300 ? h.getInputStream() : h.getErrorStream());
                if (body == null || body.trim().isEmpty()) {
                    cb.fail("Servidor retornou resposta vazia (HTTP " + code + ").");
                    return;
                }
                JSONObject r;
                try { r = new JSONObject(body); }
                catch (Exception parse) {
                    cb.fail("Resposta não-JSON do servidor (HTTP " + code + "): " + shorten(body));
                    return;
                }
                if (code >= 200 && code < 300 && r.optBoolean("ok") && "online".equalsIgnoreCase(r.optString("status"))) {
                    boolean openai = r.optBoolean("openai_configured", false);
                    boolean token = r.optBoolean("app_token_configured", false);
                    cb.ok("Servidor online • OpenAI: " + (openai ? "OK" : "FALTA") + " • Token: " + (token ? "OK" : "FALTA"));
                } else {
                    cb.fail(r.optString("error", "Falha no servidor") + " (HTTP " + code + ")");
                }
            } catch (SocketTimeoutException e) {
                cb.fail("Tempo de resposta esgotado. O Render pode estar iniciando; tente novamente em alguns segundos.");
            } catch (UnknownHostException e) {
                cb.fail("Não foi possível localizar o servidor. Verifique a URL e a internet.");
            } catch (Exception e) {
                cb.fail(e.getMessage() == null ? e.toString() : e.getMessage());
            } finally {
                if (h != null) h.disconnect();
            }
        }).start();
    }

    private static String read(InputStream in) throws Exception {
        if (in == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder s = new StringBuilder(); String l;
            while ((l = br.readLine()) != null) s.append(l);
            return s.toString();
        }
    }
    private static String shorten(String s) { s=s.replace('\n',' ').replace('\r',' ').trim(); return s.length()>220?s.substring(0,220)+"…":s; }
}
