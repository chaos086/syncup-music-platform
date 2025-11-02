package com.syncup.structures;

import java.util.*;

/**
 * Implementación de Trie para autocompletado eficiente.
 * RF-025: Trie para autocompletado de búsquedas
 * RF-026: Optimización de consultas con Trie
 * RF-003: Búsqueda con autocompletado
 */
public class TrieAutocompletado {
    
    private NodoTrie raiz;
    private int size;
    
    public TrieAutocompletado() {
        raiz = new NodoTrie();
        size = 0;
    }
    
    /**
     * Inserta una palabra en el Trie.
     */
    public void insert(String palabra) {
        if (palabra == null || palabra.trim().isEmpty()) return;
        
        palabra = palabra.toLowerCase().trim();
        NodoTrie actual = raiz;
        
        for (char c : palabra.toCharArray()) {
            if (!actual.hijos.containsKey(c)) {
                actual.hijos.put(c, new NodoTrie());
            }
            actual = actual.hijos.get(c);
        }
        
        if (!actual.esFinal) {
            actual.esFinal = true;
            actual.palabra = palabra;
            size++;
        }
        actual.frecuencia++;
    }
    
    /**
     * Busca palabras que empiecen con el prefijo dado.
     */
    public List<String> getSuggestions(String prefijo) {
        if (prefijo == null || prefijo.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        prefijo = prefijo.toLowerCase().trim();
        NodoTrie nodo = buscarNodoPrefijo(prefijo);
        
        if (nodo == null) {
            return new ArrayList<>();
        }
        
        List<PalabraFrecuencia> sugerencias = new ArrayList<>();
        recolectarPalabras(nodo, sugerencias);
        
        // Ordenar por frecuencia y tomar las 10 primeras
        sugerencias.sort((a, b) -> Integer.compare(b.frecuencia, a.frecuencia));
        
        List<String> resultado = new ArrayList<>();
        int limite = Math.min(10, sugerencias.size());
        for (int i = 0; i < limite; i++) {
            resultado.add(sugerencias.get(i).palabra);
        }
        
        return resultado;
    }
    
    /**
     * Busca el nodo que corresponde al prefijo.
     */
    private NodoTrie buscarNodoPrefijo(String prefijo) {
        NodoTrie actual = raiz;
        
        for (char c : prefijo.toCharArray()) {
            if (!actual.hijos.containsKey(c)) {
                return null;
            }
            actual = actual.hijos.get(c);
        }
        
        return actual;
    }
    
    /**
     * Recolecta todas las palabras desde un nodo dado.
     */
    private void recolectarPalabras(NodoTrie nodo, List<PalabraFrecuencia> resultado) {
        if (nodo.esFinal && nodo.palabra != null) {
            resultado.add(new PalabraFrecuencia(nodo.palabra, nodo.frecuencia));
        }
        
        for (NodoTrie hijo : nodo.hijos.values()) {
            recolectarPalabras(hijo, resultado);
        }
    }
    
    /**
     * Verifica si una palabra existe en el Trie.
     */
    public boolean search(String palabra) {
        if (palabra == null) return false;
        
        palabra = palabra.toLowerCase().trim();
        NodoTrie nodo = buscarNodoPrefijo(palabra);
        
        return nodo != null && nodo.esFinal;
    }
    
    /**
     * Verifica si existe algún prefijo.
     */
    public boolean startsWith(String prefijo) {
        if (prefijo == null) return false;
        return buscarNodoPrefijo(prefijo.toLowerCase().trim()) != null;
    }
    
    /**
     * Limpia todo el Trie.
     */
    public void clear() {
        raiz = new NodoTrie();
        size = 0;
    }
    
    /**
     * Retorna el número de palabras en el Trie.
     */
    public int size() {
        return size;
    }
    
    /**
     * Verifica si el Trie está vacío.
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Obtiene todas las palabras del Trie.
     */
    public List<String> getAllWords() {
        List<PalabraFrecuencia> palabras = new ArrayList<>();
        recolectarPalabras(raiz, palabras);
        
        List<String> resultado = new ArrayList<>();
        for (PalabraFrecuencia pf : palabras) {
            resultado.add(pf.palabra);
        }
        return resultado;
    }
    
    /**
     * Clase interna para representar un nodo del Trie.
     */
    private static class NodoTrie {
        Map<Character, NodoTrie> hijos;
        boolean esFinal;
        String palabra;
        int frecuencia;
        
        public NodoTrie() {
            hijos = new HashMap<>();
            esFinal = false;
            palabra = null;
            frecuencia = 0;
        }
    }
    
    /**
     * Clase auxiliar para almacenar palabra y frecuencia.
     */
    private static class PalabraFrecuencia {
        String palabra;
        int frecuencia;
        
        PalabraFrecuencia(String palabra, int frecuencia) {
            this.palabra = palabra;
            this.frecuencia = frecuencia;
        }
    }
    
    @Override
    public String toString() {
        return String.format("TrieAutocompletado{size=%d}", size);
    }
}