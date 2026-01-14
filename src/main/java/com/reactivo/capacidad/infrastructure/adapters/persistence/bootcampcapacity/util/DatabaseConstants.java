package com.reactivo.capacidad.infrastructure.adapters.persistence.bootcampcapacity.util;

public final class DatabaseConstants {

    private DatabaseConstants() {
    }

    // Columnas
    public static final String COLUMN_ID_CAPACITY = "id_capacity";
    public static final String COLUMN_ID_TECH = "tech_id";
    public static final String COLUMN_TECH_NAME = "tech_name";
    public static final String COLUMN_ID_TECHNOLOGY = "id_technology";

    // Queries
    public static final String PAGED_CAPACITY_IDS_QUERY = """
            SELECT id_capacity
            FROM (
                SELECT id_capacity, COUNT(id_technology) AS tech_count
                FROM TECHNOLOGY.CAPACITY_TECHNOLOGY
                GROUP BY id_capacity
                ORDER BY tech_count %s
            ) AS sub
            LIMIT :limit OFFSET :offset
            """;

    public static final String FETCH_TECHNOLOGIES_QUERY = """
            SELECT ct.%s, t.id AS %s, t.name AS %s
            FROM TECHNOLOGY.CAPACITY_TECHNOLOGY ct
            JOIN TECHNOLOGY.TECHNOLOGY t ON ct.id_technology = t.id
            WHERE ct.%s IN (:capacityIds)
            ORDER BY ct.%s ASC, t.name ASC
            """;
}
