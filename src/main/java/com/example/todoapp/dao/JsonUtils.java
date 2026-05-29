package com.example.todoapp.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Classe utilitaire finale permettant la sérialisation et la désérialisation JSON.
 * Utilise la librairie Jackson.
 */
public final class JsonUtils {

    // Configuration globale du Mapper (ignore les champs inconnus envoyés par le client)
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Constructeur privé pour empêcher l'instanciation de cette classe utilitaire.
     */
    private JsonUtils() {}

    /**
     * Transforme un objet Java en chaîne de caractères JSON.
     * * @param o L'objet à sérialiser.
     * @return La représentation JSON de l'objet.
     * @throws JsonProcessingException Si la conversion échoue.
     */
    public static String serialize(Object o) throws JsonProcessingException {
        return MAPPER.writeValueAsString(o);
    }

    /**
     * Transforme une chaîne de caractères JSON en objet Java.
     * * @param json  La chaîne JSON reçue.
     * @param clazz La classe de l'objet cible (ex: TaskCreationDto.class).
     * @param <T>   Le type de l'objet retourné.
     * @return L'objet Java instancié et hydraté.
     * @throws IOException Si le JSON est malformé ou incompatible avec la classe.
     */
    public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
        return MAPPER.readValue(json, clazz);
    }
}