package com.syncup.structures;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación personalizada de HashMap para el sistema SyncUp.
 * Utiliza hashing para acceso rápido O(1) promedio y manejo de colisiones
 * mediante encadenamiento separado (separate chaining).
 * 
 * @param <K> Tipo de la clave
 * @param <V> Tipo del valor
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class HashMap<K, V> {
    
    /** Capacidad inicial por defecto */
    private static final int DEFAULT_CAPACITY = 16;
    
    /** Factor de carga máximo antes de redimensionar */
    private static final double LOAD_FACTOR = 0.75;
    
    /** Array de buckets para almacenar las entradas */
    private Node<K, V>[] buckets;
    
    /** Número actual de elementos */
    private int size;
    
    /** Capacidad actual del HashMap */
    private int capacity;
    
    /**
     * Clase interna para representar un nodo en la cadena de colisiones.
     */
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> next;
        
        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }
    
    /**
     * Constructor por defecto que crea un HashMap con capacidad inicial.
     */
    @SuppressWarnings("unchecked")
    public HashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.size = 0;
        this.buckets = new Node[capacity];
    }
    
    /**
     * Constructor que permite especificar la capacidad inicial.
     * 
     * @param initialCapacity Capacidad inicial del HashMap
     */
    @SuppressWarnings("unchecked")
    public HashMap(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("La capacidad inicial debe ser positiva");
        }
        this.capacity = initialCapacity;
        this.size = 0;
        this.buckets = new Node[capacity];
    }
    
    /**
     * Calcula el índice del bucket para una clave dada.
     * 
     * @param key Clave para calcular el índice
     * @return Índice del bucket
     */
    private int getBucketIndex(K key) {
        if (key == null) {
            return 0; // Las claves null van al bucket 0
        }
        return Math.abs(key.hashCode()) % capacity;
    }
    
    /**
     * Inserta o actualiza un par clave-valor en el HashMap.
     * Complejidad promedio: O(1)
     * 
     * @param key Clave a insertar
     * @param value Valor asociado a la clave
     * @return Valor anterior si la clave ya existía, null en caso contrario
     */
    public V put(K key, V value) {
        // Verificar si necesitamos redimensionar
        if ((double) size / capacity >= LOAD_FACTOR) {
            resize();
        }
        
        int bucketIndex = getBucketIndex(key);
        Node<K, V> head = buckets[bucketIndex];
        
        // Si el bucket está vacío, crear el primer nodo
        if (head == null) {
            buckets[bucketIndex] = new Node<>(key, value);
            size++;
            return null;
        }
        
        // Buscar si la clave ya existe en la cadena
        Node<K, V> current = head;
        while (current != null) {
            if (keysEqual(current.key, key)) {
                // Clave encontrada, actualizar valor
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            current = current.next;
        }
        
        // Clave no encontrada, agregar al inicio de la cadena
        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = head;
        buckets[bucketIndex] = newNode;
        size++;
        
        return null;
    }
    
    /**
     * Obtiene el valor asociado a una clave.
     * Complejidad promedio: O(1)
     * 
     * @param key Clave a buscar
     * @return Valor asociado a la clave, null si no existe
     */
    public V get(K key) {
        int bucketIndex = getBucketIndex(key);
        Node<K, V> current = buckets[bucketIndex];
        
        while (current != null) {
            if (keysEqual(current.key, key)) {
                return current.value;
            }
            current = current.next;
        }
        
        return null;
    }
    
    /**
     * Elimina una clave y su valor asociado del HashMap.
     * Complejidad promedio: O(1)
     * 
     * @param key Clave a eliminar
     * @return Valor que estaba asociado a la clave, null si no existía
     */
    public V remove(K key) {
        int bucketIndex = getBucketIndex(key);
        Node<K, V> head = buckets[bucketIndex];
        
        if (head == null) {
            return null;
        }
        
        // Si el primer nodo es el que buscamos
        if (keysEqual(head.key, key)) {
            buckets[bucketIndex] = head.next;
            size--;
            return head.value;
        }
        
        // Buscar en el resto de la cadena
        Node<K, V> current = head;
        while (current.next != null) {
            if (keysEqual(current.next.key, key)) {
                V value = current.next.value;
                current.next = current.next.next;
                size--;
                return value;
            }
            current = current.next;
        }
        
        return null;
    }
    
    /**
     * Verifica si el HashMap contiene una clave específica.
     * 
     * @param key Clave a verificar
     * @return true si la clave existe, false en caso contrario
     */
    public boolean containsKey(K key) {
        return get(key) != null;
    }
    
    /**
     * Verifica si el HashMap contiene un valor específico.
     * Complejidad: O(n)
     * 
     * @param value Valor a verificar
     * @return true si el valor existe, false en caso contrario
     */
    public boolean containsValue(V value) {
        for (Node<K, V> head : buckets) {
            Node<K, V> current = head;
            while (current != null) {
                if (valuesEqual(current.value, value)) {
                    return true;
                }
                current = current.next;
            }
        }
        return false;
    }
    
    /**
     * Obtiene todas las claves del HashMap.
     * 
     * @return Lista con todas las claves
     */
    public List<K> keySet() {
        List<K> keys = new ArrayList<>();
        for (Node<K, V> head : buckets) {
            Node<K, V> current = head;
            while (current != null) {
                keys.add(current.key);
                current = current.next;
            }
        }
        return keys;
    }
    
    /**
     * Obtiene todos los valores del HashMap.
     * 
     * @return Lista con todos los valores
     */
    public List<V> values() {
        List<V> values = new ArrayList<>();
        for (Node<K, V> head : buckets) {
            Node<K, V> current = head;
            while (current != null) {
                values.add(current.value);
                current = current.next;
            }
        }
        return values;
    }
    
    /**
     * Obtiene el número de elementos en el HashMap.
     * 
     * @return Número de elementos
     */
    public int size() {
        return size;
    }
    
    /**
     * Verifica si el HashMap está vacío.
     * 
     * @return true si está vacío, false en caso contrario
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Elimina todos los elementos del HashMap.
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        buckets = new Node[capacity];
        size = 0;
    }
    
    /**
     * Redimensiona el HashMap cuando el factor de carga es excedido.
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldBuckets = buckets;
        int oldCapacity = capacity;
        
        // Duplicar la capacidad
        capacity *= 2;
        size = 0;
        buckets = new Node[capacity];
        
        // Reinsertar todos los elementos
        for (int i = 0; i < oldCapacity; i++) {
            Node<K, V> current = oldBuckets[i];
            while (current != null) {
                put(current.key, current.value);
                current = current.next;
            }
        }
    }
    
    /**
     * Compara dos claves de manera segura (maneja nulls).
     * 
     * @param key1 Primera clave
     * @param key2 Segunda clave
     * @return true si las claves son iguales
     */
    private boolean keysEqual(K key1, K key2) {
        if (key1 == null && key2 == null) {
            return true;
        }
        if (key1 == null || key2 == null) {
            return false;
        }
        return key1.equals(key2);
    }
    
    /**
     * Compara dos valores de manera segura (maneja nulls).
     * 
     * @param value1 Primer valor
     * @param value2 Segundo valor
     * @return true si los valores son iguales
     */
    private boolean valuesEqual(V value1, V value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }
        return value1.equals(value2);
    }
    
    /**
     * Obtiene información sobre la distribución de elementos en los buckets.
     * Útil para debugging y análisis de rendimiento.
     * 
     * @return String con estadísticas de distribución
     */
    public String getDistributionStats() {
        int emptyBuckets = 0;
        int maxChainLength = 0;
        int totalChainLength = 0;
        
        for (Node<K, V> head : buckets) {
            if (head == null) {
                emptyBuckets++;
            } else {
                int chainLength = 0;
                Node<K, V> current = head;
                while (current != null) {
                    chainLength++;
                    current = current.next;
                }
                maxChainLength = Math.max(maxChainLength, chainLength);
                totalChainLength += chainLength;
            }
        }
        
        double averageChainLength = capacity - emptyBuckets > 0 ? 
            (double) totalChainLength / (capacity - emptyBuckets) : 0;
        double loadFactor = (double) size / capacity;
        
        return String.format(
            "HashMap Stats: Size=%d, Capacity=%d, LoadFactor=%.3f, " +
            "EmptyBuckets=%d, MaxChainLength=%d, AvgChainLength=%.2f",
            size, capacity, loadFactor, emptyBuckets, maxChainLength, averageChainLength
        );
    }
    
    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        boolean first = true;
        for (Node<K, V> head : buckets) {
            Node<K, V> current = head;
            while (current != null) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(current.key).append("=").append(current.value);
                first = false;
                current = current.next;
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
}