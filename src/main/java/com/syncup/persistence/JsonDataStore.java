package com.syncup.persistence;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class JsonDataStore {
    private final Path dataDir;
    private final Path usersFile;

    public JsonDataStore(Path baseDir){
        this.dataDir = baseDir.resolve("data");
        this.usersFile = dataDir.resolve("users.json");
    }

    public synchronized void ensureFiles() throws IOException {
        if(!Files.exists(dataDir)) Files.createDirectories(dataDir);
        if(!Files.exists(usersFile)){
            writeAtomic(usersFile, "[]");
        }
    }

    public synchronized List<Map<String,Object>> loadUsers() throws IOException {
        ensureFiles();
        String json = Files.readString(usersFile, StandardCharsets.UTF_8).trim();
        return parseUsersArray(json);
    }

    public synchronized void saveUsers(List<Map<String,Object>> users) throws IOException {
        ensureFiles();
        String json = toJsonArray(users);
        writeAtomic(usersFile, json);
    }

    private void writeAtomic(Path file, String content) throws IOException {
        Path tmp = file.resolveSibling(file.getFileName().toString()+".tmp");
        Files.writeString(tmp, content, StandardCharsets.UTF_8);
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    // Minimal JSON writer for our simple map/array with primitives and strings
    private String toJsonArray(List<Map<String,Object>> arr){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Map<String,Object> m: arr){
            if(!first) sb.append(','); first=false;
            sb.append(toJsonObject(m));
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonObject(Map<String,Object> m){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first=true;
        for (Map.Entry<String,Object> e: m.entrySet()){
            if(!first) sb.append(','); first=false;
            sb.append('"').append(escape(e.getKey())).append('"').append(':');
            sb.append(toJsonValue(e.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    private String toJsonValue(Object v){
        if(v==null) return "null";
        if(v instanceof String) return '"'+escape((String)v)+'"';
        if(v instanceof Number || v instanceof Boolean) return String.valueOf(v);
        if(v instanceof List){
            StringBuilder sb=new StringBuilder("["); boolean first=true;
            for(Object o: (List<?>)v){ if(!first) sb.append(','); first=false; sb.append(toJsonValue(o)); }
            sb.append("]"); return sb.toString();
        }
        if(v instanceof Map){
            @SuppressWarnings("unchecked") Map<String,Object> mm=(Map<String,Object>)v;
            return toJsonObject(mm);
        }
        return '"'+escape(String.valueOf(v))+'"';
    }

    private String escape(String s){
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
    }

    // Very small JSON parser for an array of flat objects with strings/numbers/arrays
    private List<Map<String,Object>> parseUsersArray(String json) throws IOException {
        // For simplicity and robustness, if malformed return empty array
        try {
            json = json.trim();
            if(json.isEmpty() || json.equals("[]")) return new ArrayList<>();
            // Extremely simple: split objects by top-level commas. Assumes well-formed previous writes.
            if(json.charAt(0)!='[' || json.charAt(json.length()-1)!=']') return new ArrayList<>();
            String body = json.substring(1, json.length()-1).trim();
            if(body.isEmpty()) return new ArrayList<>();
            List<Map<String,Object>> out = new ArrayList<>();
            int depth=0; int start=0; for(int i=0;i<body.length();i++){
                char c=body.charAt(i);
                if(c=='{') depth++; else if(c=='}') depth--; else if(c==',' && depth==0){
                    String obj = body.substring(start,i).trim();
                    Map<String,Object> m = parseObject(obj);
                    if(m!=null) out.add(m); start=i+1;
                }
            }
            String last = body.substring(start).trim();
            Map<String,Object> lm = parseObject(last); if(lm!=null) out.add(lm);
            return out;
        } catch(Exception ex){
            // fallback safe
            return new ArrayList<>();
        }
    }

    private Map<String,Object> parseObject(String obj){
        obj=obj.trim(); if(obj.isEmpty()) return null; if(obj.charAt(0)!='{'||obj.charAt(obj.length()-1)!='}') return null;
        Map<String,Object> map = new LinkedHashMap<>();
        String body=obj.substring(1,obj.length()-1).trim(); if(body.isEmpty()) return map;
        int i=0; while(i<body.length()){
            // key
            while(i<body.length() && Character.isWhitespace(body.charAt(i))) i++;
            if(i>=body.length()||body.charAt(i)!='"') break; i++;
            int j=i; StringBuilder key=new StringBuilder();
            while(j<body.length() && body.charAt(j)!='"'){ if(body.charAt(j)=='\\' && j+1<body.length()) j++; key.append(body.charAt(j)); j++; }
            i=j+1; // skip closing quote
            while(i<body.length() && (body.charAt(i)==':'||Character.isWhitespace(body.charAt(i)))) i++;
            // value (only strings/numbers/booleans/arrays of strings expected)
            if(i>=body.length()) break; char c=body.charAt(i);
            Object val=null; if(c=='"'){
                i++; StringBuilder sb=new StringBuilder(); while(i<body.length() && body.charAt(i)!='"'){ if(body.charAt(i)=='\\' && i+1<body.length()) i++; sb.append(body.charAt(i)); i++; } i++; val=sb.toString();
            } else if(c=='['){
                int depth=1; int k=i+1; while(k<body.length() && depth>0){ char ch=body.charAt(k); if(ch=='[') depth++; else if(ch==']') depth--; k++; }
                String arr=body.substring(i+1,k-1).trim(); List<Object> list=new ArrayList<>();
                if(!arr.isEmpty()){
                    int a=0; while(a<arr.length()){
                        while(a<arr.length() && Character.isWhitespace(arr.charAt(a))) a++;
                        if(a<arr.length() && arr.charAt(a)=='"'){ a++; StringBuilder ss=new StringBuilder(); while(a<arr.length() && arr.charAt(a)!='"'){ if(arr.charAt(a)=='\\' && a+1<arr.length()) a++; ss.append(arr.charAt(a)); a++; } a++; list.add(ss.toString()); }
                        while(a<arr.length() && arr.charAt(a)!=',') a++; if(a<arr.length() && arr.charAt(a)==',') a++;
                    }
                }
                val=list; i=k;
            } else {
                int k=i; while(k<body.length() && ",}".indexOf(body.charAt(k))==-1) k++; String token=body.substring(i,k).trim();
                if("true".equals(token)||"false".equals(token)) val=Boolean.parseBoolean(token); else {
                    try{ val=Long.parseLong(token);}catch(Exception e){ val=token; }
                }
                i=k;
            }
            map.put(key.toString(), val);
            while(i<body.length() && body.charAt(i)!=',') i++; if(i<body.length()&&body.charAt(i)==',') i++;
        }
        return map;
    }
}
