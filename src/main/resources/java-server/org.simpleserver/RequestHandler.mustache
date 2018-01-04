package org.ea;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.ea.controllers.notes.NotesCollection;
import org.ea.controllers.notes.NotesItem;
import org.ea.repositories.NotesRepository;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestHandler implements HttpHandler {

    private Map<String, Endpoint> endpointMap = new HashMap<String, Endpoint>();

    public RequestHandler() {
        endpointMap.put("/notes", new NotesCollection(NotesRepository.getInstance()));
        endpointMap.put("/notes/{id}", new NotesItem(NotesRepository.getInstance()));
    }

    private String getRequestBody(HttpExchange t) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        br.close();
        return sb.toString();
    }

    private void setResponseBody(HttpExchange t, String s, int responseCode) throws IOException {
        t.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
        t.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:8081");
        t.getResponseHeaders().add("Connection", "keep-alive");
        t.getResponseHeaders().add("Content-Type", "application/json");
        t.getResponseHeaders().add("Date", new Date().toString());
        t.getResponseHeaders().add("Vary", "Origin");
        t.sendResponseHeaders(responseCode, s.length());
        OutputStream os = t.getResponseBody();
        os.write(s.getBytes());
        os.close();
    }

    private void handleOPTION(HttpExchange t) throws IOException {
        System.out.println("OPTIONS");
        t.getResponseHeaders().add("Connection", "keep-alive");
        t.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
        t.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:8081");
        t.getResponseHeaders().add("Access-Control-Allow-Headers", "content-type");
        t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE");
        t.getResponseHeaders().add("Date", new Date().toString());
        t.getResponseHeaders().add("Vary", "Origin, Access-Control-Request-Headers");
        t.sendResponseHeaders(204, 0);
    }


    private Endpoint getEndpoint(String s) {
        Pattern keyPattern = Pattern.compile("\\{([a-zA-Z]+)\\}");
        Endpoint ep = null;
        for(Map.Entry<String, Endpoint> entity : endpointMap.entrySet()) {
            List<String> keys = new ArrayList<>();
            String testPath = entity.getKey();
            Matcher m = keyPattern.matcher(testPath);
            while(m.find()) {
                String key = m.group();
                keys.add(m.group().substring(1, key.length() - 1));
                testPath = testPath.replace(key, "([^\\/]+)");
            }

            Pattern testPattern = Pattern.compile("^" + testPath + "$");
            if(s == null) return null;
            Matcher stringMatcher = testPattern.matcher(s);
            if(!stringMatcher.matches() || stringMatcher.groupCount() != keys.size()) continue;
            ep = entity.getValue();

            for(int i=0; i<keys.size(); i++) {
                String value = stringMatcher.group(i+1);
                ep.addPathParam(keys.get(i), value);
            }
            return ep;
        }
        return ep;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if(t.getRequestMethod().equals("OPTIONS")) {
            this.handleOPTION(t);
        }
        Endpoint ep = this.getEndpoint(t.getRequestURI().getPath());
        String resp = ep.handleRequest(t.getRequestMethod(), this.getRequestBody(t));
        this.setResponseBody(t, resp, ep.getResponseCode());
    }
}
