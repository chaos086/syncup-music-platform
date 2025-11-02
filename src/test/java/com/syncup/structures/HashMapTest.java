package com.syncup.structures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Clase de pruebas unitarias para la implementación personalizada de HashMap.
 * Verifica el correcto funcionamiento de todas las operaciones del HashMap.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
class HashMapTest {
    
    private HashMap<String, String> hashMap;
    private HashMap<Integer, String> integerHashMap;
    
    @BeforeEach
    void setUp() {
        hashMap = new HashMap<>();
        integerHashMap = new HashMap<>();
    }
    
    @Test
    @DisplayName("Debe crear HashMap vacío correctamente")
    void testCreacionHashMapVacio() {
        assertTrue(hashMap.isEmpty());
        assertEquals(0, hashMap.size());
        assertNull(hashMap.get("clave_inexistente"));
    }
    
    @Test
    @DisplayName("Debe insertar y recuperar elementos correctamente")
    void testInsertarYRecuperar() {
        // Insertar elementos
        assertNull(hashMap.put("clave1", "valor1"));
        assertNull(hashMap.put("clave2", "valor2"));
        assertNull(hashMap.put("clave3", "valor3"));
        
        assertEquals(3, hashMap.size());
        assertFalse(hashMap.isEmpty());
        
        // Recuperar elementos
        assertEquals("valor1", hashMap.get("clave1"));
        assertEquals("valor2", hashMap.get("clave2"));
        assertEquals("valor3", hashMap.get("clave3"));
        
        // Clave inexistente debe retornar null
        assertNull(hashMap.get("clave_inexistente"));
    }
    
    @Test
    @DisplayName("Debe actualizar valores existentes correctamente")
    void testActualizarValores() {
        // Insertar valor inicial
        hashMap.put("clave1", "valor_original");
        assertEquals("valor_original", hashMap.get("clave1"));
        assertEquals(1, hashMap.size());
        
        // Actualizar valor
        String valorAnterior = hashMap.put("clave1", "valor_actualizado");
        assertEquals("valor_original", valorAnterior);
        assertEquals("valor_actualizado", hashMap.get("clave1"));
        assertEquals(1, hashMap.size()); // El tamaño no debe cambiar
    }
    
    @Test
    @DisplayName("Debe manejar colisiones correctamente")
    void testManejoColisiones() {
        // Crear HashMap pequeño para forzar colisiones
        HashMap<Integer, String> smallMap = new HashMap<>(4);
        
        // Agregar elementos que probablemente colisionen
        smallMap.put(1, "uno");
        smallMap.put(5, "cinco");   // 5 % 4 = 1 (misma posición que 1)
        smallMap.put(9, "nueve");   // 9 % 4 = 1 (misma posición que 1 y 5)
        smallMap.put(13, "trece");  // 13 % 4 = 1 (misma posición que anteriores)
        
        assertEquals(4, smallMap.size());
        assertEquals("uno", smallMap.get(1));
        assertEquals("cinco", smallMap.get(5));
        assertEquals("nueve", smallMap.get(9));
        assertEquals("trece", smallMap.get(13));
    }
    
    @Test
    @DisplayName("Debe eliminar elementos correctamente")
    void testEliminarElementos() {
        // Agregar elementos
        hashMap.put("clave1", "valor1");
        hashMap.put("clave2", "valor2");
        hashMap.put("clave3", "valor3");
        assertEquals(3, hashMap.size());
        
        // Eliminar elemento existente
        String valorEliminado = hashMap.remove("clave2");
        assertEquals("valor2", valorEliminado);
        assertEquals(2, hashMap.size());
        assertNull(hashMap.get("clave2"));
        
        // Verificar que otros elementos siguen ahí
        assertEquals("valor1", hashMap.get("clave1"));
        assertEquals("valor3", hashMap.get("clave3"));
        
        // Intentar eliminar elemento inexistente
        assertNull(hashMap.remove("clave_inexistente"));
        assertEquals(2, hashMap.size());
    }
    
    @Test
    @DisplayName("Debe verificar existencia de claves y valores correctamente")
    void testContainsKeyYContainsValue() {
        hashMap.put("clave1", "valor1");
        hashMap.put("clave2", "valor2");
        hashMap.put("clave3", "valor1"); // Valor duplicado
        
        // Verificar claves
        assertTrue(hashMap.containsKey("clave1"));
        assertTrue(hashMap.containsKey("clave2"));
        assertTrue(hashMap.containsKey("clave3"));
        assertFalse(hashMap.containsKey("clave_inexistente"));
        
        // Verificar valores
        assertTrue(hashMap.containsValue("valor1"));
        assertTrue(hashMap.containsValue("valor2"));
        assertFalse(hashMap.containsValue("valor_inexistente"));
    }
    
    @Test
    @DisplayName("Debe obtener conjuntos de claves y valores correctamente")
    void testKeySetYValues() {
        hashMap.put("clave1", "valor1");
        hashMap.put("clave2", "valor2");
        hashMap.put("clave3", "valor3");
        
        List<String> claves = hashMap.keySet();
        List<String> valores = hashMap.values();
        
        assertEquals(3, claves.size());
        assertEquals(3, valores.size());
        
        assertTrue(claves.contains("clave1"));
        assertTrue(claves.contains("clave2"));
        assertTrue(claves.contains("clave3"));
        
        assertTrue(valores.contains("valor1"));
        assertTrue(valores.contains("valor2"));
        assertTrue(valores.contains("valor3"));
    }
    
    @Test
    @DisplayName("Debe limpiar HashMap correctamente")
    void testClear() {
        hashMap.put("clave1", "valor1");
        hashMap.put("clave2", "valor2");
        hashMap.put("clave3", "valor3");
        assertEquals(3, hashMap.size());
        
        hashMap.clear();
        
        assertEquals(0, hashMap.size());
        assertTrue(hashMap.isEmpty());
        assertNull(hashMap.get("clave1"));
        assertNull(hashMap.get("clave2"));
        assertNull(hashMap.get("clave3"));
    }
    
    @Test
    @DisplayName("Debe manejar claves null correctamente")
    void testManejoClaveNull() {
        // Insertar con clave null
        assertNull(hashMap.put(null, "valor_null"));
        assertEquals(1, hashMap.size());
        assertEquals("valor_null", hashMap.get(null));
        assertTrue(hashMap.containsKey(null));
        
        // Actualizar clave null
        String valorAnterior = hashMap.put(null, "nuevo_valor_null");
        assertEquals("valor_null", valorAnterior);
        assertEquals("nuevo_valor_null", hashMap.get(null));
        assertEquals(1, hashMap.size());
        
        // Eliminar clave null
        String valorEliminado = hashMap.remove(null);
        assertEquals("nuevo_valor_null", valorEliminado);
        assertEquals(0, hashMap.size());
        assertNull(hashMap.get(null));
    }
    
    @Test
    @DisplayName("Debe redimensionar automáticamente")
    void testRedimensionamiento() {
        // Crear HashMap pequeño
        HashMap<Integer, String> smallMap = new HashMap<>(4);
        
        // Agregar muchos elementos para forzar redimensionamiento
        for (int i = 0; i < 20; i++) {
            smallMap.put(i, "valor" + i);
        }
        
        assertEquals(20, smallMap.size());
        
        // Verificar que todos los elementos siguen accesibles
        for (int i = 0; i < 20; i++) {
            assertEquals("valor" + i, smallMap.get(i));
        }
    }
    
    @Test
    @DisplayName("Debe generar estadísticas de distribución")
    void testEstadisticasDistribucion() {
        // Agregar varios elementos
        for (int i = 0; i < 10; i++) {
            hashMap.put("clave" + i, "valor" + i);
        }
        
        String stats = hashMap.getDistributionStats();
        
        assertNotNull(stats);
        assertTrue(stats.contains("HashMap Stats"));
        assertTrue(stats.contains("Size=10"));
        assertTrue(stats.contains("Capacity="));
        assertTrue(stats.contains("LoadFactor="));
    }
    
    @Test
    @DisplayName("Debe manejar diferentes tipos de datos")
    void testTiposDatosDiferentes() {
        HashMap<Integer, Double> numericMap = new HashMap<>();
        HashMap<String, Boolean> booleanMap = new HashMap<>();
        
        // HashMap numérico
        numericMap.put(1, 3.14);
        numericMap.put(2, 2.71);
        assertEquals(Double.valueOf(3.14), numericMap.get(1));
        assertEquals(Double.valueOf(2.71), numericMap.get(2));
        
        // HashMap booleano
        booleanMap.put("verdadero", true);
        booleanMap.put("falso", false);
        assertEquals(Boolean.TRUE, booleanMap.get("verdadero"));
        assertEquals(Boolean.FALSE, booleanMap.get("falso"));
    }
    
    @Test
    @DisplayName("Debe generar toString informativo")
    void testToString() {
        // HashMap vacío
        assertEquals("{}", hashMap.toString());
        
        // HashMap con elementos
        hashMap.put("clave1", "valor1");
        String hashMapString = hashMap.toString();
        assertTrue(hashMapString.contains("clave1=valor1"));
        assertTrue(hashMapString.startsWith("{"));
        assertTrue(hashMapString.endsWith("}"));
    }
    
    @Test
    @DisplayName("Debe manejar constructores correctamente")
    void testConstructores() {
        // Constructor por defecto
        HashMap<String, String> map1 = new HashMap<>();
        assertTrue(map1.isEmpty());
        
        // Constructor con capacidad inicial
        HashMap<String, String> map2 = new HashMap<>(32);
        assertTrue(map2.isEmpty());
        
        // Constructor con capacidad inválida debe lanzar excepción
        assertThrows(IllegalArgumentException.class, () -> {
            new HashMap<String, String>(0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new HashMap<String, String>(-5);
        });
    }
}