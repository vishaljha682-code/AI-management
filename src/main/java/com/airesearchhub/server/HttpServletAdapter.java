package com.airesearchhub.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Bridges standard HttpServlet instances to lightweight embedded Java HttpServer.
 * Guarantees standard javax.servlet API compatibility.
 */
public class HttpServletAdapter implements HttpHandler {
    private final HttpServlet servlet;
    private final String contextPath;
    private static final Map<String, Map<String, Object>> sessionStore = new ConcurrentHashMap<>();

    public HttpServletAdapter(HttpServlet servlet, String contextPath) {
        this.servlet = servlet;
        this.contextPath = contextPath;
        try {
            this.servlet.init();
        } catch (ServletException e) {
            throw new RuntimeException("Failed to initialize servlet: " + e.getMessage(), e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ByteArrayOutputStream responseBodyBuffer = new ByteArrayOutputStream();
        SimpleServletResponse responseWrapper = new SimpleServletResponse(exchange, responseBodyBuffer);
        SimpleServletRequest requestWrapper = new SimpleServletRequest(exchange, contextPath);

        try {
            servlet.service(requestWrapper, responseWrapper);
        } catch (ServletException e) {
            responseWrapper.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseBodyBuffer.write(("Servlet Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }

        // Send headers and commit
        byte[] respBytes = responseBodyBuffer.toByteArray();
        int statusCode = responseWrapper.getStatus() > 0 ? responseWrapper.getStatus() : 200;

        Headers responseHeaders = exchange.getResponseHeaders();
        for (Map.Entry<String, String> entry : responseWrapper.getHeaders().entrySet()) {
            responseHeaders.set(entry.getKey(), entry.getValue());
        }

        // Ensure session cookie is set if session exists
        if (requestWrapper.hasSession()) {
            String sessionId = requestWrapper.getSession().getId();
            responseHeaders.set("Set-Cookie", "JSESSIONID=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax");
        }

        if (respBytes.length == 0 && statusCode == 200) {
            exchange.sendResponseHeaders(statusCode, -1);
        } else {
            exchange.sendResponseHeaders(statusCode, respBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(respBytes);
                os.flush();
            }
        }
    }

    // --- Inner Adapter for HttpServletRequest ---
    static class SimpleServletRequest implements HttpServletRequest {
        private final HttpExchange exchange;
        private final String contextPath;
        private final byte[] bodyBytes;
        private final Map<String, String> parameters;
        private String sessionId;
        private SimpleSession session;

        public SimpleServletRequest(HttpExchange exchange, String contextPath) throws IOException {
            this.exchange = exchange;
            this.contextPath = contextPath;
            try (InputStream is = exchange.getRequestBody()) {
                this.bodyBytes = is.readAllBytes();
            }
            this.parameters = parseQueryParams(exchange.getRequestURI());
            extractSessionId();
        }

        private void extractSessionId() {
            List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");
            if (cookieHeaders != null) {
                for (String header : cookieHeaders) {
                    String[] parts = header.split(";");
                    for (String part : parts) {
                        String[] pair = part.trim().split("=", 2);
                        if (pair.length == 2 && "JSESSIONID".equalsIgnoreCase(pair[0])) {
                            this.sessionId = pair[1];
                            return;
                        }
                    }
                }
            }
        }

        private Map<String, String> parseQueryParams(URI uri) {
            Map<String, String> map = new LinkedHashMap<>();
            String rawQuery = uri.getRawQuery();
            if (rawQuery != null && !rawQuery.isEmpty()) {
                String[] pairs = rawQuery.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=", 2);
                    String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                    map.put(k, v);
                }
            }
            return map;
        }

        public boolean hasSession() {
            return session != null;
        }

        @Override
        public String getMethod() {
            return exchange.getRequestMethod();
        }

        @Override
        public String getRequestURI() {
            return exchange.getRequestURI().getPath();
        }

        @Override
        public String getPathInfo() {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith(contextPath)) {
                String sub = path.substring(contextPath.length());
                return sub.isEmpty() ? "/" : sub;
            }
            return path;
        }

        @Override
        public String getParameter(String name) {
            return parameters.get(name);
        }

        @Override
        public String getHeader(String name) {
            List<String> vals = exchange.getRequestHeaders().get(name);
            return vals != null && !vals.isEmpty() ? vals.get(0) : null;
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bodyBytes), StandardCharsets.UTF_8));
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(bodyBytes);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() { return bais.available() == 0; }
                @Override
                public boolean isReady() { return true; }
                @Override
                public void setReadListener(javax.servlet.ReadListener readListener) {}
                @Override
                public int read() { return bais.read(); }
            };
        }

        @Override
        public HttpSession getSession(boolean create) {
            if (session != null) return session;
            if (sessionId != null && sessionStore.containsKey(sessionId)) {
                session = new SimpleSession(sessionId, sessionStore.get(sessionId));
                return session;
            }
            if (create) {
                sessionId = UUID.randomUUID().toString();
                Map<String, Object> map = new ConcurrentHashMap<>();
                sessionStore.put(sessionId, map);
                session = new SimpleSession(sessionId, map);
                return session;
            }
            return null;
        }

        @Override
        public HttpSession getSession() {
            return getSession(true);
        }

        @Override
        public String getRemoteAddr() {
            return exchange.getRemoteAddress() != null ? exchange.getRemoteAddress().getAddress().getHostAddress() : "127.0.0.1";
        }

        @Override
        public String getRemoteHost() {
            return "localhost";
        }

        // Standard Dummy implementations for unused methods
        @Override public String getAuthType() { return null; }
        @Override public Cookie[] getCookies() { return new Cookie[0]; }
        @Override public long getDateHeader(String name) { return -1; }
        @Override public Enumeration<String> getHeaders(String name) { return Collections.enumeration(exchange.getRequestHeaders().getOrDefault(name, Collections.emptyList())); }
        @Override public Enumeration<String> getHeaderNames() { return Collections.enumeration(exchange.getRequestHeaders().keySet()); }
        @Override public int getIntHeader(String name) { return -1; }
        @Override public String getPathTranslated() { return null; }
        @Override public String getContextPath() { return contextPath; }
        @Override public String getQueryString() { return exchange.getRequestURI().getRawQuery(); }
        @Override public String getRemoteUser() { return null; }
        @Override public boolean isUserInRole(String role) { return false; }
        @Override public java.security.Principal getUserPrincipal() { return null; }
        @Override public String getRequestedSessionId() { return sessionId; }
        @Override public StringBuffer getRequestURL() { return new StringBuffer("http://localhost:8080" + getRequestURI()); }
        @Override public String getServletPath() { return contextPath; }
        @Override public String changeSessionId() { return sessionId; }
        @Override public boolean isRequestedSessionIdValid() { return true; }
        @Override public boolean isRequestedSessionIdFromCookie() { return true; }
        @Override public boolean isRequestedSessionIdFromURL() { return false; }
        @Override public boolean isRequestedSessionIdFromUrl() { return false; }
        @Override public boolean authenticate(HttpServletResponse response) { return true; }
        @Override public void login(String username, String password) {}
        @Override public void logout() {}
        @Override public java.util.Collection<javax.servlet.http.Part> getParts() { return Collections.emptyList(); }
        @Override public javax.servlet.http.Part getPart(String name) { return null; }
        @Override public <T extends javax.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public Enumeration<String> getAttributeNames() { return Collections.emptyEnumeration(); }
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public void setCharacterEncoding(String env) {}
        @Override public int getContentLength() { return bodyBytes.length; }
        @Override public long getContentLengthLong() { return bodyBytes.length; }
        @Override public String getContentType() { return getHeader("Content-Type"); }
        @Override public Enumeration<String> getParameterNames() { return Collections.enumeration(parameters.keySet()); }
        @Override public String[] getParameterValues(String name) { return parameters.containsKey(name) ? new String[]{parameters.get(name)} : null; }
        @Override public Map<String, String[]> getParameterMap() {
            Map<String, String[]> res = new HashMap<>();
            parameters.forEach((k, v) -> res.put(k, new String[]{v}));
            return res;
        }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public String getScheme() { return "http"; }
        @Override public String getServerName() { return "localhost"; }
        @Override public int getServerPort() { return 8080; }
        @Override public void setAttribute(String name, Object o) {}
        @Override public void removeAttribute(String name) {}
        @Override public java.util.Locale getLocale() { return java.util.Locale.getDefault(); }
        @Override public Enumeration<java.util.Locale> getLocales() { return Collections.enumeration(List.of(java.util.Locale.getDefault())); }
        @Override public boolean isSecure() { return false; }
        @Override public javax.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
        @Override public String getRealPath(String path) { return null; }
        @Override public int getRemotePort() { return 0; }
        @Override public String getLocalName() { return "localhost"; }
        @Override public String getLocalAddr() { return "127.0.0.1"; }
        @Override public int getLocalPort() { return 8080; }
        @Override public ServletContext getServletContext() { return null; }
        @Override public javax.servlet.AsyncContext startAsync() { return null; }
        @Override public javax.servlet.AsyncContext startAsync(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) { return null; }
        @Override public boolean isAsyncStarted() { return false; }
        @Override public boolean isAsyncSupported() { return false; }
        @Override public javax.servlet.AsyncContext getAsyncContext() { return null; }
        @Override public javax.servlet.DispatcherType getDispatcherType() { return javax.servlet.DispatcherType.REQUEST; }
    }

    // --- Inner Adapter for HttpServletResponse ---
    static class SimpleServletResponse implements HttpServletResponse {
        private final HttpExchange exchange;
        private final ByteArrayOutputStream outputStream;
        private final PrintWriter writer;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private int status = 200;

        public SimpleServletResponse(HttpExchange exchange, ByteArrayOutputStream outputStream) {
            this.exchange = exchange;
            this.outputStream = outputStream;
            this.writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);
        }

        public Map<String, String> getHeaders() { return headers; }

        @Override public int getStatus() { return status; }
        @Override public void setStatus(int sc) { this.status = sc; }
        @Override public void setStatus(int sc, String sm) { this.status = sc; }
        @Override public void setHeader(String name, String value) { headers.put(name, value); }
        @Override public void addHeader(String name, String value) { headers.put(name, value); }
        @Override public void setContentType(String type) { headers.put("Content-Type", type); }
        @Override public PrintWriter getWriter() { return writer; }
        @Override public ServletOutputStream getOutputStream() {
            return new ServletOutputStream() {
                @Override public boolean isReady() { return true; }
                @Override public void setWriteListener(WriteListener writeListener) {}
                @Override public void write(int b) { outputStream.write(b); }
                @Override public void write(byte[] b, int off, int len) { outputStream.write(b, off, len); }
            };
        }
        @Override public void sendRedirect(String location) { status = 302; setHeader("Location", location); }
        @Override public void addCookie(Cookie cookie) {
            setHeader("Set-Cookie", cookie.getName() + "=" + cookie.getValue() + "; Path=" + cookie.getPath());
        }
        @Override public boolean containsHeader(String name) { return headers.containsKey(name); }
        @Override public String encodeURL(String url) { return url; }
        @Override public String encodeRedirectURL(String url) { return url; }
        @Override public String encodeUrl(String url) { return url; }
        @Override public String encodeRedirectUrl(String url) { return url; }
        @Override public void sendError(int sc, String msg) { status = sc; writer.print(msg); }
        @Override public void sendError(int sc) { status = sc; }
        @Override public void setDateHeader(String name, long date) { setHeader(name, String.valueOf(date)); }
        @Override public void addDateHeader(String name, long date) { setHeader(name, String.valueOf(date)); }
        @Override public void setIntHeader(String name, int value) { setHeader(name, String.valueOf(value)); }
        @Override public void addIntHeader(String name, int value) { setHeader(name, String.valueOf(value)); }
        @Override public String getHeader(String name) { return headers.get(name); }
        @Override public java.util.Collection<String> getHeaders(String name) { return headers.containsKey(name) ? List.of(headers.get(name)) : Collections.emptyList(); }
        @Override public java.util.Collection<String> getHeaderNames() { return headers.keySet(); }
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public String getContentType() { return headers.get("Content-Type"); }
        @Override public void setCharacterEncoding(String charset) {}
        @Override public void setContentLength(int len) { setIntHeader("Content-Length", len); }
        @Override public void setContentLengthLong(long len) { setHeader("Content-Length", String.valueOf(len)); }
        @Override public void setBufferSize(int size) {}
        @Override public int getBufferSize() { return outputStream.size(); }
        @Override public void flushBuffer() { writer.flush(); }
        @Override public void resetBuffer() { outputStream.reset(); }
        @Override public boolean isCommitted() { return false; }
        @Override public void reset() { headers.clear(); outputStream.reset(); status = 200; }
        @Override public void setLocale(java.util.Locale loc) {}
        @Override public java.util.Locale getLocale() { return java.util.Locale.getDefault(); }
    }

    // --- Inner Session Implementation ---
    static class SimpleSession implements HttpSession {
        private final String id;
        private final Map<String, Object> attributes;
        private final long creationTime;

        public SimpleSession(String id, Map<String, Object> attributes) {
            this.id = id;
            this.attributes = attributes;
            this.creationTime = System.currentTimeMillis();
        }

        @Override public long getCreationTime() { return creationTime; }
        @Override public String getId() { return id; }
        @Override public long getLastAccessedTime() { return System.currentTimeMillis(); }
        @Override public ServletContext getServletContext() { return null; }
        @Override public void setMaxInactiveInterval(int interval) {}
        @Override public int getMaxInactiveInterval() { return 1800; }
        @Override public javax.servlet.http.HttpSessionContext getSessionContext() { return null; }
        @Override public Object getAttribute(String name) { return attributes.get(name); }
        @Override public Object getValue(String name) { return getAttribute(name); }
        @Override public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }
        @Override public String[] getValueNames() { return attributes.keySet().toArray(new String[0]); }
        @Override public void setAttribute(String name, Object value) {
            if (value == null) attributes.remove(name);
            else attributes.put(name, value);
        }
        @Override public void putValue(String name, Object value) { setAttribute(name, value); }
        @Override public void removeAttribute(String name) { attributes.remove(name); }
        @Override public void removeValue(String name) { removeAttribute(name); }
        @Override public void invalidate() {
            attributes.clear();
            sessionStore.remove(id);
        }
        @Override public boolean isNew() { return false; }
    }
}
