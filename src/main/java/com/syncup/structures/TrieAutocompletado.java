package com.syncup.structures;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de Trie (Árbol de Prefijos) para funcionalidad de autocompletado
 * en el sistema SyncUp. Permite búsquedas eficientes de prefijos y sugerencias.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class TrieAutocompletado {
    
    /** Nodo raíz del Trie */
    private TrieNode root;
    
    /** Número total de palabras en el Trie */
    private int size;
    
    /**
     * Clase interna que representa un nodo del Trie.
     */
    private static class TrieNode {
        /** Array de hijos para cada caracter (a-z, A-Z, 0-9, espacio y algunos especiales) */
        private TrieNode[] children;
        
        /** Indica si este nodo representa el final de una palabra */
        private boolean isEndOfWord;
        
        /** Palabra completa almacenada en este nodo (solo si isEndOfWord = true) */
        private String word;
        
        /** Frecuencia de uso de esta palabra (para ranking) */
        private int frequency;
        
        /** Tamaño del alfabeto soportado */
        private static final int ALPHABET_SIZE = 128; // ASCII extendido
        
        /**
         * Constructor del nodo Trie.
         */
        public TrieNode() {
            children = new TrieNode[ALPHABET_SIZE];
            isEndOfWord = false;
            word = null;
            frequency = 0;
        }
        
        /**
         * Convierte un caracter a índice del array.
         * 
         * @param ch Caracter a convertir
         * @return Índice del array
         */
        private int charToIndex(char ch) {
            return (int) ch;
        }
        
        /**
         * Convierte un índice del array a caracter.
         * 
         * @param index Índice del array
         * @return Caracter correspondiente
         */
        private char indexToChar(int index) {
            return (char) index;
        }
    }
    
    /**
     * Constructor que inicializa el Trie vacío.
     */
    public TrieAutocompletado() {
        root = new TrieNode();
        size = 0;
    }
    
    /**
     * Inserta una palabra en el Trie.
     * Complejidad: O(m) donde m es la longitud de la palabra
     * 
     * @param word Palabra a insertar
     */
    public void insert(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        
        // Normalizar la palabra (convertir a minúsculas y limpiar)
        String normalizedWord = normalizeWord(word);
        
        TrieNode current = root;
        
        // Recorrer cada caracter de la palabra
        for (char ch : normalizedWord.toCharArray()) {
            int index = current.charToIndex(ch);
            
            // Crear nuevo nodo si no existe
            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }
            
            current = current.children[index];
        }
        
        // Marcar el final de la palabra
        if (!current.isEndOfWord) {
            current.isEndOfWord = true;
            current.word = word; // Guardar la palabra original
            size++;
        }
        
        // Incrementar frecuencia
        current.frequency++;
    }
    
    /**
     * Busca si una palabra existe en el Trie.
     * Complejidad: O(m) donde m es la longitud de la palabra
     * 
     * @param word Palabra a buscar
     * @return true si existe, false en caso contrario
     */
    public boolean search(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        
        TrieNode node = searchNode(normalizeWord(word));
        return node != null && node.isEndOfWord;
    }
    
    /**
     * Verifica si existe algún palabra que comience con el prefijo dado.
     * Complejidad: O(p) donde p es la longitud del prefijo
     * 
     * @param prefix Prefijo a verificar
     * @return true si existe algún palabra con ese prefijo
     */
    public boolean startsWith(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return true;
        }
        
        return searchNode(normalizeWord(prefix)) != null;
    }
    
    /**
     * Obtiene todas las palabras que comienzan con el prefijo dado.
     * Ideal para funcionalidad de autocompletado.
     * 
     * @param prefix Prefijo para buscar sugerencias
     * @param maxSuggestions Número máximo de sugerencias
     * @return Lista de sugerencias ordenada por frecuencia
     */
    public List<String> getSuggestions(String prefix, int maxSuggestions) {
        List<String> suggestions = new ArrayList<>();
        
        if (prefix == null || prefix.trim().isEmpty()) {
            return suggestions;
        }
        
        String normalizedPrefix = normalizeWord(prefix);
        TrieNode prefixNode = searchNode(normalizedPrefix);
        
        if (prefixNode == null) {
            return suggestions;
        }
        
        // Realizar DFS para encontrar todas las palabras con el prefijo
        List<WordFrequency> allSuggestions = new ArrayList<>();
        dfsCollectWords(prefixNode, allSuggestions);
        
        // Ordenar por frecuencia (descendente)
        allSuggestions.sort((a, b) -> Integer.compare(b.frequency, a.frequency));
        
        // Tomar solo las primeras maxSuggestions
        int limit = Math.min(maxSuggestions, allSuggestions.size());
        for (int i = 0; i < limit; i++) {
            suggestions.add(allSuggestions.get(i).word);
        }
        
        return suggestions;
    }
    
    /**
     * Obtiene sugerencias con un límite por defecto de 10.
     * 
     * @param prefix Prefijo para buscar sugerencias
     * @return Lista de hasta 10 sugerencias
     */
    public List<String> getSuggestions(String prefix) {
        return getSuggestions(prefix, 10);
    }
    
    /**
     * Elimina una palabra del Trie.
     * Complejidad: O(m) donde m es la longitud de la palabra
     * 
     * @param word Palabra a eliminar
     * @return true si se eliminó exitosamente, false si no existía
     */
    public boolean delete(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        
        return deleteHelper(root, normalizeWord(word), 0);
    }
    
    /**
     * Método auxiliar recursivo para eliminar una palabra.
     * 
     * @param current Nodo actual
     * @param word Palabra a eliminar
     * @param index Índice actual en la palabra
     * @return true si el nodo puede ser eliminado
     */
    private boolean deleteHelper(TrieNode current, String word, int index) {
        if (index == word.length()) {
            // Llegamos al final de la palabra
            if (!current.isEndOfWord) {
                return false; // La palabra no existe
            }
            
            current.isEndOfWord = false;
            current.word = null;
            current.frequency = 0;
            size--;
            
            // Retornar true si el nodo no tiene hijos (puede ser eliminado)
            return !hasChildren(current);
        }
        
        char ch = word.charAt(index);
        int charIndex = current.charToIndex(ch);
        TrieNode node = current.children[charIndex];
        
        if (node == null) {
            return false; // La palabra no existe
        }
        
        boolean shouldDeleteChild = deleteHelper(node, word, index + 1);
        
        if (shouldDeleteChild) {
            current.children[charIndex] = null;
            
            // Retornar true si el nodo actual puede ser eliminado
            return !current.isEndOfWord && !hasChildren(current);
        }
        
        return false;
    }
    
    /**
     * Verifica si un nodo tiene hijos.
     * 
     * @param node Nodo a verificar
     * @return true si tiene hijos, false en caso contrario
     */
    private boolean hasChildren(TrieNode node) {
        for (TrieNode child : node.children) {
            if (child != null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Busca un nodo que corresponde a una palabra o prefijo.
     * 
     * @param word Palabra o prefijo a buscar
     * @return Nodo correspondiente o null si no existe
     */
    private TrieNode searchNode(String word) {
        TrieNode current = root;
        
        for (char ch : word.toCharArray()) {
            int index = current.charToIndex(ch);
            if (current.children[index] == null) {
                return null;
            }
            current = current.children[index];
        }
        
        return current;
    }
    
    /**
     * Normaliza una palabra para la búsqueda (minúsculas, sin espacios extra).
     * 
     * @param word Palabra a normalizar
     * @return Palabra normalizada
     */
    private String normalizeWord(String word) {
        return word.toLowerCase().trim();
    }
    
    /**
     * Realiza DFS para recopilar todas las palabras desde un nodo.
     * 
     * @param node Nodo actual
     * @param result Lista para almacenar resultados
     */
    private void dfsCollectWords(TrieNode node, List<WordFrequency> result) {
        if (node.isEndOfWord) {
            result.add(new WordFrequency(node.word, node.frequency));
        }
        
        for (TrieNode child : node.children) {
            if (child != null) {
                dfsCollectWords(child, result);
            }
        }
    }
    
    /**
     * Clase auxiliar para almacenar palabra y frecuencia.
     */
    private static class WordFrequency {
        String word;
        int frequency;
        
        WordFrequency(String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
        }
    }
    
    /**
     * Obtiene el número total de palabras en el Trie.
     * 
     * @return Número de palabras
     */
    public int size() {
        return size;
    }
    
    /**
     * Verifica si el Trie está vacío.
     * 
     * @return true si está vacío, false en caso contrario
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Elimina todas las palabras del Trie.
     */
    public void clear() {
        root = new TrieNode();
        size = 0;
    }
    
    /**
     * Obtiene todas las palabras almacenadas en el Trie.
     * 
     * @return Lista con todas las palabras
     */
    public List<String> getAllWords() {
        List<WordFrequency> allWords = new ArrayList<>();
        dfsCollectWords(root, allWords);
        
        List<String> result = new ArrayList<>();
        for (WordFrequency wf : allWords) {
            result.add(wf.word);
        }
        
        return result;
    }
    
    /**
     * Obtiene la frecuencia de una palabra específica.
     * 
     * @param word Palabra a consultar
     * @return Frecuencia de la palabra, 0 si no existe
     */
    public int getWordFrequency(String word) {
        if (word == null || word.trim().isEmpty()) {
            return 0;
        }
        
        TrieNode node = searchNode(normalizeWord(word));
        return (node != null && node.isEndOfWord) ? node.frequency : 0;
    }
    
    @Override
    public String toString() {
        return "TrieAutocompletado{size=" + size + ", words=" + getAllWords() + "}";
    }
}